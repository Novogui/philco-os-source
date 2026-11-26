package com.philco.ultra777.home.media;

public final class PlaybackState {
    public enum Status { IDLE, PREPARING, PLAYING, PAUSED, STOPPED, COMPLETED, ERROR }
    public final Status status;
    public final Track track;
    public final int positionMs, durationMs;
    public PlaybackState(Status status, Track track, int positionMs, int durationMs) {
        this.status = status; this.track = track;
        this.positionMs = Math.max(0, positionMs); this.durationMs = Math.max(0, durationMs);
    }
    public static PlaybackState idle() { return new PlaybackState(Status.IDLE, null, 0, 0); }
}
