package com.philco.ultra777.home.media;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/** Queue navigation independent of Android, so its edge cases can be checked on the host. */
final class PlaybackQueue {
    static final int REPEAT_OFF = 0;
    static final int REPEAT_QUEUE = 1;
    static final int REPEAT_TRACK = 2;
    private static final int HISTORY_LIMIT = 50;

    private final ArrayList<String> uris = new ArrayList<String>();
    private final HashMap<String, Integer> indexByUri = new HashMap<String, Integer>();
    private final ArrayDeque<String> history = new ArrayDeque<String>();
    private final Random random;
    private boolean shuffle;
    private int repeatMode;
    private int current = -1;

    PlaybackQueue(Random random) { this.random = random; }

    void set(List<String> items, boolean shuffleEnabled, int repeat, String currentUri) {
        uris.clear();
        indexByUri.clear();
        if (items != null) {
            for (String uri : items) {
                if (uri == null) continue;
                int index = uris.size();
                uris.add(uri);
                if (!indexByUri.containsKey(uri)) indexByUri.put(uri, index);
            }
        }
        shuffle = shuffleEnabled;
        repeatMode = Math.max(REPEAT_OFF, Math.min(REPEAT_TRACK, repeat));
        current = indexOf(currentUri);
    }

    int size() { return uris.size(); }
    boolean isEmpty() { return uris.isEmpty(); }
    int current() { return current; }
    int repeatMode() { return repeatMode; }

    int indexOf(String uri) {
        Integer index = uri == null ? null : indexByUri.get(uri);
        return index == null ? -1 : index.intValue();
    }

    int nextIndex() {
        int size = uris.size();
        if (size == 0) return -1;
        if (shuffle && size > 1 && current >= 0) {
            int next;
            do { next = random.nextInt(size); } while (next == current);
            return next;
        }
        if (shuffle && size > 1 && current < 0) return random.nextInt(size);
        if (current < 0) return 0;
        if (current + 1 < size) return current + 1;
        return repeatMode == REPEAT_QUEUE ? 0 : -1;
    }

    int previousIndex() {
        if (uris.isEmpty()) return -1;
        String uri;
        while ((uri = history.pollFirst()) != null) {
            int index = indexOf(uri);
            if (index >= 0) return index;
        }
        if (current > 0) return current - 1;
        if (repeatMode == REPEAT_QUEUE && uris.size() > 1) return uris.size() - 1;
        return current;
    }

    void moveTo(int index, String rememberUri) {
        if (index < 0 || index >= uris.size()) return;
        if (rememberUri != null && !rememberUri.equals(uris.get(index))) {
            history.addFirst(rememberUri);
            while (history.size() > HISTORY_LIMIT) history.removeLast();
        }
        current = index;
    }
}
