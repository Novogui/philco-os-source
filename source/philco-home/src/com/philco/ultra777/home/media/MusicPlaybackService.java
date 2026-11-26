package com.philco.ultra777.home.media;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MusicPlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {
    public final class LocalBinder extends Binder { public MusicPlaybackService getService() { return MusicPlaybackService.this; } }
    private final IBinder binder = new LocalBinder();
    private final PlayerStateRepository repo = PlayerStateRepository.get();
    private AudioManager audio;
    private MediaPlayer player;
    private Track track;
    private PlaybackState.Status status = PlaybackState.Status.IDLE;
    private boolean prepared, resumeOnGain, previousRestartPending;
    private final ArrayList<Track> queue = new ArrayList<Track>();
    private final PlaybackQueue playbackQueue = new PlaybackQueue(new Random());

    @Override public void onCreate() { super.onCreate(); audio = (AudioManager)getSystemService(AUDIO_SERVICE); }
    @Override public IBinder onBind(Intent intent) { return binder; }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_NOT_STICKY; }

    public void play(Track item) {
        if (item == null) { fail(); return; }
        previousRestartPending = false;
        release(); track = item; status = PlaybackState.Status.PREPARING; publish();
        player = new MediaPlayer(); final MediaPlayer target = player;
        try {
            target.setAudioStreamType(AudioManager.STREAM_MUSIC);
            target.setOnPreparedListener(mp -> { if (mp != player) return; prepared = true;
                if (audio.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                        == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) start(); else fail(); });
            target.setOnCompletionListener(mp -> {
                if (mp != player) return;
                if (playbackQueue.repeatMode() == PlaybackQueue.REPEAT_TRACK
                        && playbackQueue.current() >= 0) {
                    playQueueIndex(playbackQueue.current(), false);
                    return;
                }
                if (skipNext()) return;
                status = PlaybackState.Status.COMPLETED;
                publish();
                audio.abandonAudioFocus(this);
            });
            target.setOnErrorListener((mp, what, extra) -> {
                if (mp != player) return true;
                fail();
                return true;
            });
            target.setDataSource(this, item.uri); target.prepareAsync();
        } catch (Exception e) { fail(); }
    }
    public void pause() { if (player != null && prepared && player.isPlaying()) { player.pause(); status=PlaybackState.Status.PAUSED; publish(); } }
    public void resume() {
        if (!prepared || player == null) return;
        if (audio.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            resumeOnGain = false;
            player.setVolume(1f, 1f); // clear any transient duck before an explicit resume
            if (!player.isPlaying()) start();
        }
    }
    public void stop() { release(); status=PlaybackState.Status.STOPPED; publish(); audio.abandonAudioFocus(this); stopSelf(); }
    public void seekTo(int ms) { if (prepared) { previousRestartPending=false; player.seekTo(Math.max(0, Math.min(ms, player.getDuration()))); publish(); } }
    public boolean isPlaying() { return player != null && prepared && player.isPlaying(); }
    public PlaybackState getState() { publish(); return repo.getState(); }
    private void start() { try { if (prepared && player != null) { player.setVolume(1f,1f); player.start(); status=PlaybackState.Status.PLAYING; publish(); } } catch (IllegalStateException e) { fail(); } }
    private void fail() { release(); status=PlaybackState.Status.ERROR; publish(); if (audio != null) audio.abandonAudioFocus(this); }
    private void publish() { int pos=0,dur=0; try { if(prepared&&player!=null){pos=player.getCurrentPosition();dur=player.getDuration();} } catch(Exception ignored){} repo.publish(new PlaybackState(status,track,pos,dur)); }
    private void release() { prepared=false; if(player!=null){player.release();player=null;} }

    public void setQueue(List<Track> items, boolean shuffle, int repeat) {
        queue.clear();
        if (items != null) queue.addAll(items);
        ArrayList<String> uris = new ArrayList<String>(queue.size());
        for (Track item : queue) uris.add(item.uri.toString());
        playbackQueue.set(uris, shuffle, repeat, track == null ? null : track.uri.toString());
    }

    public void playQueueIndex(int index, boolean rememberCurrent) {
        if (index < 0 || index >= queue.size()) return;
        Track next = queue.get(index);
        playbackQueue.moveTo(index,
                rememberCurrent && track != null ? track.uri.toString() : null);
        previousRestartPending = false;
        play(next);
    }

    public boolean skipNext() {
        int next = playbackQueue.nextIndex();
        if (next < 0) return false;
        playQueueIndex(next, true);
        return true;
    }

    public boolean skipPrevious() {
        if (playbackQueue.isEmpty()) return false;
        PlaybackState state = getState();
        if (state.track == null) return skipNext();
        if (state.positionMs >= 45000 && !previousRestartPending) {
            seekTo(0);
            previousRestartPending = true;
            return true;
        }
        previousRestartPending = false;
        int previous = playbackQueue.previousIndex();
        if (previous < 0) return false;
        playQueueIndex(previous, false);
        return true;
    }
    @Override public void onAudioFocusChange(int change) {
        if(change==AudioManager.AUDIOFOCUS_GAIN){ if(resumeOnGain)start(); resumeOnGain=false; if(player!=null)player.setVolume(1f,1f); }
        else if(change==AudioManager.AUDIOFOCUS_LOSS){resumeOnGain=false;pause();}
        else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){resumeOnGain=isPlaying();pause();}
        else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK&&player!=null)player.setVolume(.2f,.2f);
    }
    @Override public void onDestroy(){release();if(audio!=null)audio.abandonAudioFocus(this);super.onDestroy();}
}
