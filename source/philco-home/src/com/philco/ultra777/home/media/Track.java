package com.philco.ultra777.home.media;

import android.net.Uri;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Track {
    public final Uri uri;
    public final String title;
    public final String artist;
    public final String album;
    public final String genre;
    public final long albumId;
    public final List<String> tags;

    public Track(Uri uri, String title) {
        this(uri, title, "Artista desconhecido", "", "");
    }

    public Track(Uri uri, String title, String artist) {
        this(uri, title, artist, "", "");
    }

    public Track(Uri uri, String title, String artist, String album, String genre) {
        this(uri, title, artist, album, genre, -1L);
    }

    public Track(Uri uri, String title, String artist, String album, String genre, long albumId) {
        if (uri == null) throw new IllegalArgumentException("uri is required");
        this.uri = uri;
        this.title = clean(title, "Faixa sem título");
        this.artist = clean(artist, "Artista desconhecido");
        this.album = clean(album, "");
        this.genre = clean(genre, "");
        this.albumId = albumId;
        this.tags = Collections.unmodifiableList(buildTags(this.genre));
    }

    private static String clean(String value, String fallback) {
        if (value == null) return fallback;
        String normalized = value.trim();
        if (normalized.length() == 0 || "<unknown>".equalsIgnoreCase(normalized)
                || "unknown".equalsIgnoreCase(normalized)) return fallback;
        return normalized;
    }

    private static List<String> buildTags(String genre) {
        List<String> result = new ArrayList<String>();
        if (genre.length() == 0) return result;
        for (String part : genre.split("[,;/|]")) {
            String value = part.trim();
            if (value.length() == 0) continue;
            StringBuilder tag = new StringBuilder("#");
            boolean capitalize = true;
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    tag.append(capitalize ? Character.toUpperCase(c) : c);
                    capitalize = false;
                } else {
                    capitalize = true;
                }
            }
            if (tag.length() > 1 && !result.contains(tag.toString())) result.add(tag.toString());
        }
        return result;
    }
}
