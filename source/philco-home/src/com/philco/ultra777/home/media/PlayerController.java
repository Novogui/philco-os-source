package com.philco.ultra777.home.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.os.IBinder;
import java.util.List;

public final class PlayerController {
    private final Context context;
    private MusicPlaybackService service;
    private boolean bound;
    public PlayerController(Context context) { this.context=context.getApplicationContext(); }
    public boolean bind(ServiceConnection connection) { Intent i=new Intent(context,MusicPlaybackService.class); context.startService(i); bound=context.bindService(i,connection,Context.BIND_AUTO_CREATE); return bound; }
    public void attach(IBinder binder) { if(binder instanceof MusicPlaybackService.LocalBinder) service=((MusicPlaybackService.LocalBinder)binder).getService(); }
    public void play(Track t){if(service!=null)service.play(t);} public void pause(){if(service!=null)service.pause();}
    public void resume(){if(service!=null)service.resume();} public void stop(){if(service!=null)service.stop();}
    public void setQueue(List<Track> tracks, boolean shuffle, int repeatMode) {
        if (service != null) service.setQueue(tracks, shuffle, repeatMode);
    }
    public void playQueueIndex(int index, boolean rememberCurrent) {
        if (service != null) service.playQueueIndex(index, rememberCurrent);
    }
    public boolean skipNext() { return service != null && service.skipNext(); }
    public boolean skipPrevious() { return service != null && service.skipPrevious(); }
    public void seekTo(int ms){if(service!=null)service.seekTo(ms);} public PlaybackState getState(){return service==null?PlayerStateRepository.get().getState():service.getState();}
    public void unbind(ServiceConnection c){if(bound){context.unbindService(c);bound=false;}service=null;}
}
