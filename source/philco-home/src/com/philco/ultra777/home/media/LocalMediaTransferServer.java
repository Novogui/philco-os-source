package com.philco.ultra777.home.media;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Locale;

/** Short-lived, LAN-only receiver for music and photo files; it runs only while its dialog is open. */
final class LocalMediaTransferServer implements Runnable {
    interface Listener { void onFileReceived(File file); void onError(String message); }

    private static final String TAG = "PhilcoFileTransfer";
    private static final int PORT = 8765;
    private static final long MAX_BYTES = 250L * 1024L * 1024L;
    private static final int MAX_HEADER_BYTES = 16384;
    private final Context context;
    private final Listener listener;
    private final String pairingCode;
    private volatile boolean closed;
    private ServerSocket server;

    LocalMediaTransferServer(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        pairingCode = String.format(Locale.US, "%06d", Integer.valueOf(new SecureRandom().nextInt(900000) + 100000));
    }

    String start() throws Exception {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            throw new IllegalStateException("O armazenamento da TV não está disponível");
        server = new ServerSocket(PORT, 4, InetAddress.getByName("0.0.0.0"));
        server.setSoTimeout(1000);
        Thread thread = new Thread(this, "PhilcoMediaTransfer");
        thread.setDaemon(true);
        thread.start();
        return localAddress() + ":" + PORT;
    }

    String getPairingCode() { return pairingCode; }

    @Override public void run() {
        while (!closed && server != null && !server.isClosed()) {
            Socket socket = null;
            try {
                socket = server.accept();
                socket.setSoTimeout(30000);
                handle(socket);
            } catch (java.net.SocketTimeoutException ignored) {
            } catch (Exception error) {
                if (!closed) Log.w(TAG, "LAN transfer request failed", error);
            } finally {
                if (socket != null) try { socket.close(); } catch (Exception ignored) { }
            }
        }
    }

    private void handle(Socket socket) throws Exception {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        String headers = readHeaders(input);
        String[] lines = headers.split("\\r?\\n");
        if (lines.length == 0) { respond(output, 400, "Bad request", "Solicitação inválida"); return; }
        String[] request = lines[0].split(" ");
        if (request.length < 2) { respond(output, 400, "Bad request", "Solicitação inválida"); return; }
        String method = request[0];
        String target = request[1];
        if ("GET".equals(method) && "/".equals(target)) {
            respond(output, 200, "OK", htmlPage(), "text/html; charset=utf-8");
            return;
        }
        if ("OPTIONS".equals(method)) { respond(output, 204, "No Content", ""); return; }
        if (!"POST".equals(method) || !target.startsWith("/upload?")) {
            respond(output, 404, "Not Found", "Rota não encontrada"); return;
        }
        String token = headerValue(lines, "x-philco-key");
        if (!pairingCode.equals(token)) { respond(output, 403, "Forbidden", "Código incorreto"); return; }
        long length = contentLength(lines);
        if (length <= 0 || length > MAX_BYTES) {
            respond(output, 413, "Payload Too Large", "Arquivo vazio ou maior que 250 MB"); return;
        }
        String name;
        try { name = URLDecoder.decode(target.substring(target.indexOf("name=") + 5), "UTF-8"); }
        catch (Exception invalidName) { respond(output, 400, "Bad Request", "Nome inválido"); return; }
        File destination = destinationFor(name);
        if (destination == null) { respond(output, 415, "Unsupported Media Type", "Envie somente áudio ou imagem"); return; }
        File parent = destination.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) { respond(output, 507, "Insufficient Storage", "Não foi possível criar a pasta"); return; }
        StatFs storage = new StatFs(parent.getAbsolutePath());
        long freeBytes = storage.getAvailableBlocksLong() * storage.getBlockSizeLong();
        if (freeBytes < length + 8L * 1024L * 1024L) {
            respond(output, 507, "Insufficient Storage", "Há pouco espaço livre na TV"); return;
        }
        File partial = new File(parent, destination.getName() + ".part");
        try {
            copyExactly(input, partial, length);
            if (destination.exists()) destination = uniqueFile(parent, destination.getName());
            if (!partial.renameTo(destination)) throw new java.io.IOException("Não foi possível finalizar o arquivo");
        } catch (Exception error) {
            if (partial.exists()) partial.delete();
            respond(output, 500, "Internal Server Error", "A transferência falhou");
            throw error;
        }
        respond(output, 201, "Created", "Recebido: " + destination.getName());
        final File received = destination;
        MediaScannerConnection.scanFile(context, new String[]{received.getAbsolutePath()}, null,
                (path, uri) -> { if (listener != null) listener.onFileReceived(received); });
    }

    private static String readHeaders(InputStream input) throws Exception {
        byte[] bytes = new byte[MAX_HEADER_BYTES];
        int count = 0;
        int value;
        while (count < bytes.length && (value = input.read()) >= 0) {
            bytes[count++] = (byte)value;
            if (count >= 4 && bytes[count - 4] == '\r' && bytes[count - 3] == '\n'
                    && bytes[count - 2] == '\r' && bytes[count - 1] == '\n')
                return new String(bytes, 0, count, "ISO-8859-1");
        }
        throw new java.io.IOException("HTTP headers are incomplete or too large");
    }

    private static String headerValue(String[] lines, String wanted) {
        for (String line : lines) {
            int colon = line.indexOf(':');
            if (colon > 0 && wanted.equalsIgnoreCase(line.substring(0, colon).trim()))
                return line.substring(colon + 1).trim();
        }
        return "";
    }

    private static long contentLength(String[] lines) {
        try { return Long.parseLong(headerValue(lines, "content-length")); }
        catch (NumberFormatException ignored) { return -1L; }
    }

    private static void copyExactly(InputStream input, File file, long count) throws Exception {
        FileOutputStream output = new FileOutputStream(file);
        try {
            byte[] bytes = new byte[8192];
            long remaining = count;
            while (remaining > 0) {
                int read = input.read(bytes, 0, (int)Math.min(bytes.length, remaining));
                if (read < 0) throw new java.io.IOException("Transfer ended early");
                output.write(bytes, 0, read);
                remaining -= read;
            }
            output.getFD().sync();
        } finally { output.close(); }
    }

    private static File destinationFor(String rawName) {
        if (rawName == null) return null;
        String name = rawName.substring(Math.max(rawName.lastIndexOf('/'), rawName.lastIndexOf('\\')) + 1)
                .replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (name.length() == 0 || name.startsWith(".")) return null;
        String lower = name.toLowerCase(Locale.US);
        String directory;
        if (lower.matches(".*\\.(mp3|m4a|aac|ogg|wav|flac|opus)$")) directory = Environment.DIRECTORY_MUSIC;
        else if (lower.matches(".*\\.(jpg|jpeg|png|webp)$")) directory = Environment.DIRECTORY_PICTURES;
        else return null;
        return new File(new File(Environment.getExternalStoragePublicDirectory(directory), "PhilcoOS"), name);
    }

    private static File uniqueFile(File parent, String name) {
        String stem = name, extension = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0) { stem = name.substring(0, dot); extension = name.substring(dot); }
        int suffix = 2;
        File candidate;
        do { candidate = new File(parent, stem + " (" + suffix++ + ")" + extension); }
        while (candidate.exists());
        return candidate;
    }

    private static String htmlPage() {
        return "<!doctype html><html><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<meta charset='utf-8'><title>PhilcoOS</title><style>body{font:18px sans-serif;background:#17191d;color:#f5efe3;margin:24px}"
                + "main{max-width:560px;margin:auto;background:#24262a;border:1px solid #d8b867;border-radius:20px;padding:22px}"
                + "input,button{font:inherit;margin:10px 0;width:100%;padding:14px;box-sizing:border-box;border-radius:12px}"
                + "button{background:#e2c16f;border:0;color:#211b12;font-weight:bold}small{color:#c9c3b8}</style>"
                + "<main><h2>Enviar para PhilcoOS</h2><p>Escolha uma música ou foto e digite o código mostrado na TV.</p>"
                + "<input id='code' inputmode='numeric' maxlength='6' placeholder='Código de 6 dígitos'>"
                + "<input id='file' type='file' accept='audio/*,image/jpeg,image/png,image/webp'>"
                + "<button onclick='sendFile()'>Enviar arquivo</button><small id='status'>Conectado somente à rede local</small></main>"
                + "<script>function sendFile(){var f=document.getElementById('file').files[0],c=document.getElementById('code').value;"
                + "if(!f||c.length!==6){alert('Escolha um arquivo e digite o código da TV.');return;}"
                + "var x=new XMLHttpRequest();x.open('POST','/upload?name='+encodeURIComponent(f.name));"
                + "x.setRequestHeader('X-Philco-Key',c);x.setRequestHeader('Content-Type','application/octet-stream');"
                + "x.onload=function(){document.getElementById('status').textContent=x.responseText;};"
                + "x.onerror=function(){document.getElementById('status').textContent='Falha de conexão. Verifique se celular e TV estão na mesma rede.';};"
                + "document.getElementById('status').textContent='Enviando '+f.name+'…';x.send(f);}</script></html>";
    }

    private static void respond(OutputStream output, int code, String reason, String body) throws Exception {
        respond(output, code, reason, body, "text/plain; charset=utf-8");
    }

    private static void respond(OutputStream output, int code, String reason, String body, String type) throws Exception {
        byte[] bytes = body.getBytes("UTF-8");
        String headers = "HTTP/1.1 " + code + " " + reason + "\r\nContent-Type: " + type
                + "\r\nContent-Length: " + bytes.length + "\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n";
        output.write(headers.getBytes("ISO-8859-1"));
        output.write(bytes);
        output.flush();
    }

    private static String localAddress() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces != null && interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            Enumeration<InetAddress> addresses = network.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address && !address.isLoopbackAddress()) return address.getHostAddress();
            }
        }
        throw new IllegalStateException("A TV não tem endereço IPv4 na rede local");
    }

    void close() {
        closed = true;
        if (server != null) try { server.close(); } catch (Exception ignored) { }
    }
}
