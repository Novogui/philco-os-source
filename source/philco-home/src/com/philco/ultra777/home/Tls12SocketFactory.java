package com.philco.ultra777.home;

import android.content.Context;

import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.conscrypt.Conscrypt;

/** Enables the platform's TLS 1.2 implementation on KitKat without bypassing certificate checks. */
final class Tls12SocketFactory extends SSLSocketFactory {
    private static final Provider PROVIDER = Conscrypt.newProvider();
    private final SSLSocketFactory delegate;

    Tls12SocketFactory(Context appContext) throws Exception {
        SSLContext context = SSLContext.getInstance("TLSv1.2", PROVIDER);
        context.init(null, new TrustManager[]{weatherTrustManager(appContext)}, null);
        delegate = context.getSocketFactory();
    }

    private X509TrustManager weatherTrustManager(Context context) throws Exception {
        TrustManagerFactory systemFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        systemFactory.init((KeyStore)null);
        X509TrustManager system = findX509TrustManager(systemFactory.getTrustManagers());

        KeyStore bundledRoots = KeyStore.getInstance(KeyStore.getDefaultType());
        bundledRoots.load(null, null);
        InputStream input = null;
        try {
            input = context.getResources().openRawResource(R.raw.isrg_root_x1);
            Certificate root = CertificateFactory.getInstance("X.509").generateCertificate(input);
            bundledRoots.setCertificateEntry("lets-encrypt-isrg-root-x1", root);
        } finally { if (input != null) try { input.close(); } catch (IOException ignored) { } }
        TrustManagerFactory pinnedFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        pinnedFactory.init(bundledRoots);
        final X509TrustManager pinned = findX509TrustManager(pinnedFactory.getTrustManagers());
        final X509TrustManager platform = system;
        return new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                platform.checkClientTrusted(chain, authType);
            }
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try { platform.checkServerTrusted(chain, authType); }
                catch (CertificateException oldStoreFailure) {
                    try { pinned.checkServerTrusted(chain, authType); }
                    catch (CertificateException untrusted) {
                        throw new CertificateException("Server chain is not trusted by system roots or pinned ISRG Root X1", untrusted);
                    }
                }
            }
            @Override public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] systemIssuers = platform.getAcceptedIssuers();
                X509Certificate[] pinnedIssuers = pinned.getAcceptedIssuers();
                List<X509Certificate> combined = new ArrayList<X509Certificate>(systemIssuers.length + pinnedIssuers.length);
                for (X509Certificate certificate : systemIssuers) combined.add(certificate);
                for (X509Certificate certificate : pinnedIssuers) combined.add(certificate);
                return combined.toArray(new X509Certificate[combined.size()]);
            }
        };
    }

    private X509TrustManager findX509TrustManager(TrustManager[] managers) throws CertificateException {
        for (TrustManager manager : managers) if (manager instanceof X509TrustManager) return (X509TrustManager)manager;
        throw new CertificateException("No X.509 trust manager is available");
    }

    private Socket tls(Socket socket) throws IOException {
        if (!(socket instanceof SSLSocket)) throw new IOException("TLS socket unavailable");
        SSLSocket ssl = (SSLSocket) socket;
        boolean supportsTls12 = false;
        for (String protocol : ssl.getSupportedProtocols()) if ("TLSv1.2".equals(protocol)) supportsTls12 = true;
        if (!supportsTls12) {
            try { socket.close(); } catch (IOException ignored) { }
            throw new IOException("This Android 4.4 build does not provide TLS 1.2");
        }
        ssl.setEnabledProtocols(new String[]{"TLSv1.2"});
        return ssl;
    }

    @Override public String[] getDefaultCipherSuites() { return delegate.getDefaultCipherSuites(); }
    @Override public String[] getSupportedCipherSuites() { return delegate.getSupportedCipherSuites(); }
    @Override public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException { return tls(delegate.createSocket(s, host, port, autoClose)); }
    @Override public Socket createSocket(String host, int port) throws IOException, UnknownHostException { return tls(delegate.createSocket(host, port)); }
    @Override public Socket createSocket(String host, int port, InetAddress local, int localPort) throws IOException, UnknownHostException { return tls(delegate.createSocket(host, port, local, localPort)); }
    @Override public Socket createSocket(InetAddress host, int port) throws IOException { return tls(delegate.createSocket(host, port)); }
    @Override public Socket createSocket(InetAddress host, int port, InetAddress local, int localPort) throws IOException { return tls(delegate.createSocket(host, port, local, localPort)); }
}
