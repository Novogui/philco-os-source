package com.philco.ultra777.home.media;

import java.util.concurrent.CopyOnWriteArrayList;

public final class PlayerStateRepository {
    public interface Observer { void onStateChanged(PlaybackState state); }
    private static final PlayerStateRepository INSTANCE = new PlayerStateRepository();
    private final CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<Observer>();
    private volatile PlaybackState state = PlaybackState.idle();
    private PlayerStateRepository() { }
    public static PlayerStateRepository get() { return INSTANCE; }
    public PlaybackState getState() { return state; }
    public void publish(PlaybackState value) { state = value; for (Observer o : observers) o.onStateChanged(value); }
    public void addObserver(Observer o) { if (o != null) observers.addIfAbsent(o); }
    public void removeObserver(Observer o) { observers.remove(o); }
}
