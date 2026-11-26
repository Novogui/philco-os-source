package com.philco.ultra777.home.media;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.util.LruCache;
import android.os.Looper;
import android.view.WindowManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.philco.ultra777.home.R;
import java.text.Normalizer;
import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/** Lightweight full-screen local music player for the TV remote. */
public final class MusicActivity extends Activity {
private static final String ACTION_TV_TUNING_INSPECT = "com.philco.ultra777.home.media.action.TUNING_INSPECT";
private static final String ACTION_TV_TUNING_APPLY = "com.philco.ultra777.home.media.action.TUNING_APPLY";
private static final String ACTION_TV_TUNING_RESTORE = "com.philco.ultra777.home.media.action.TUNING_RESTORE";
private static final int GOLD = 0xffd9ad55;
private static final int GOLD_FOCUS = 0xffffd985;
private static final int WHITE = 0xfff7f3eb;
private static final int MUTED = 0xffb9b5ad;
private static final String PREF_LAST_AUDIBLE_VOLUME = "music_last_audible_volume";
private static final int SEEK_STEP_MS = 10000;
private static final int MAX_SHORTCUTS = 7;
private static final long RELOAD_DEBOUNCE_MS = 1500L;
 private static final long MUSIC_WALLPAPER_ROTATE_MS = 10L * 60L * 1000L;
 private static final int[] MUSIC_WALLPAPERS = {
         R.drawable.wallpaper_01, R.drawable.wallpaper_02, R.drawable.wallpaper_03,
         R.drawable.wallpaper_04, R.drawable.wallpaper_05, R.drawable.wallpaper_06,
         R.drawable.wallpaper_07, R.drawable.wallpaper_08, R.drawable.wallpaper_09,
         R.drawable.wallpaper_10, R.drawable.wallpaper_11, R.drawable.wallpaper_12,
         R.drawable.wallpaper_13, R.drawable.wallpaper_14, R.drawable.wallpaper_15
 };
private final List<Track> tracks = new ArrayList<Track>();
 private final List<String> artists = new ArrayList<String>();
 private final List<Long> durations = new ArrayList<Long>();
 private final Handler handler = new Handler(Looper.getMainLooper());
 private final HashMap<String, Integer> indexByUri = new HashMap<String, Integer>();
 private final List<String> searchKeys = new ArrayList<String>();
 private final Set<String> favorites = new HashSet<String>();
 private PlayerController controller;
 private TextView currentTitle, currentArtist, playerTitle, playerArtist, status, elapsed, totalTime;
 private TextView previousIcon, toggleIcon, toggleLabel, nextIcon;
 private TextView clock, libraryCount, volumeValue;
 private TextView favoriteButton, shuffleButton, repeatButton;
 private View searchButton, volumeControl, nextAction;
 private EditText searchInput;
 private LinearLayout tabBar, recommendationRow;
 private FrameLayout navigationSlot;
 private HorizontalScrollView recommendations;
 private ListView list;
 private TextView libraryEmptyState;
 private SeekBar seekBar;
 private SeekBar volumeSlider;
 private ActionButton playbackToggle;
 private VolumeGlyph volumeGlyph;
 private ImageView heroArtwork, miniArtwork;
 private View heroArtworkFallback, miniArtworkFallback;
  private View headerScrim;
 private LinearLayout volumePopover;
 private AlertDialog transferDialog;
 private LocalMediaTransferServer transferServer;
 private TextView volumeButton;
 private LruCache<String, Bitmap> artworkCache;
 private ExecutorService artworkExecutor;
 private ImageView wallpaperView;
 private Bitmap wallpaperBitmap;
 private int wallpaperIndex;
 private final Runnable wallpaperRotation = new Runnable() {
     @Override public void run() {
         if (isFinishing() || artworkExecutor == null || artworkExecutor.isShutdown()) return;
         wallpaperIndex = (wallpaperIndex + 1) % MUSIC_WALLPAPERS.length;
         final int nextIndex = wallpaperIndex;
         try {
             artworkExecutor.execute(() -> {
                 final Bitmap next = decodeWallpaper(MUSIC_WALLPAPERS[nextIndex]);
                 handler.post(() -> {
                     if (isFinishing() || wallpaperView == null) {
                         if (next != null && !next.isRecycled()) next.recycle();
                         return;
                     }
                     Bitmap previous = wallpaperBitmap;
                     wallpaperBitmap = next;
                     wallpaperView.setImageBitmap(next);
                     if (previous != null && previous != next && !previous.isRecycled()) previous.recycle();
                     playerPreferences.edit().putInt("music_wallpaper_index", nextIndex).apply();
                 });
             });
         } catch (java.util.concurrent.RejectedExecutionException ignored) { }
         handler.postDelayed(this, MUSIC_WALLPAPER_ROTATE_MS);
     }
 };
 private TrackAdapter adapter;
 private Track renderedTrack;
 private AudioManager audioManager;
 private final List<Integer> visibleTracks = new ArrayList<Integer>();
 private String searchQuery = "";
 private int lastAudibleVolume = -1;
 private boolean bound, userSeeking, searchOpen, seekAdjustMode, clockReceiverRegistered, libraryLoadFailed;
 private boolean shuffleEnabled, completionHandled;
 private int repeatMode, restoredIndex = -1, seekReturnFocusId = View.NO_ID;
  private int activeTabDestination = 0;
 private SharedPreferences playerPreferences;
 private boolean loading, reloadPending, firstLoad = true, keepAwake, observerRegistered;
 private boolean playbackErrorShown, libraryErrorShown;
 private boolean serviceReady, bindRequested, showingState;
 private Track pendingTrack;
 private boolean pendingRememberCurrent;
 private final Runnable reloadLibrary = new Runnable() {
     @Override public void run() { loadTracks(); }
 };
 private final ContentObserver mediaObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
     @Override public void onChange(boolean selfChange) {
         handler.removeCallbacks(reloadLibrary);
         handler.postDelayed(reloadLibrary, RELOAD_DEBOUNCE_MS);
     }
 };
 private final BroadcastReceiver clockReceiver = new BroadcastReceiver() {
     @Override public void onReceive(Context context, Intent intent) { updateClock(); }
 };
 private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
     @Override public void onReceive(Context context, Intent intent) {
         if (intent == null || intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                 != AudioManager.STREAM_MUSIC) return;
         updateVolumeUi();
     }
 };
 private final Runnable refresh = new Runnable() {
     @Override public void run() {
         showState();
         updateVolumeUi();
         handler.postDelayed(this, 1000L);
     }
 };
 private final ServiceConnection connection = new ServiceConnection() {
     @Override public void onServiceConnected(ComponentName name, IBinder binder) {
         controller.attach(binder);
         bound = true;
         serviceReady = true;
         // A COMPLETED state that already existed before we attached was not witnessed by this
         // screen, so it must not trigger an automatic jump to the next track.
         completionHandled = controller.getState().status == PlaybackState.Status.COMPLETED;
          syncPlaybackQueue();
          if (pendingTrack != null) {
             Track queued = pendingTrack;
              boolean remember = pendingRememberCurrent;
             pendingTrack = null;
              pendingRememberCurrent = false;
              int index = indexOfUri(queued.uri.toString());
              if (index >= 0) controller.playQueueIndex(index, remember);
              else controller.play(queued);
         }
         showState();
     }
     @Override public void onServiceDisconnected(ComponentName name) {
         bound = false;
         serviceReady = false;
     }
 };
 @Override protected void onCreate(Bundle state) {
     super.onCreate(state);
     String tuningAction = getIntent() == null ? null : getIntent().getAction();
     if (ACTION_TV_TUNING_INSPECT.equals(tuningAction)
             || ACTION_TV_TUNING_APPLY.equals(tuningAction)
             || ACTION_TV_TUNING_RESTORE.equals(tuningAction)) {
         if (ACTION_TV_TUNING_INSPECT.equals(tuningAction)) TvTuningProfile.inspect(this);
         else if (ACTION_TV_TUNING_APPLY.equals(tuningAction)) TvTuningProfile.apply(this);
         else TvTuningProfile.restore(this);
         finish();
         return;
     }
     controller = new PlayerController(this);
     playerPreferences = getSharedPreferences("philco_music_player", MODE_PRIVATE);
     wallpaperIndex = Math.max(0, Math.min(MUSIC_WALLPAPERS.length - 1,
             playerPreferences.getInt("music_wallpaper_index", 1)));
     shuffleEnabled = playerPreferences.getBoolean("shuffle", false);
     repeatMode = Math.max(0, Math.min(2, playerPreferences.getInt("repeat_mode", 0)));
     Set<String> savedFavorites = playerPreferences.getStringSet("favorite_uris", null);
     if (savedFavorites != null) favorites.addAll(savedFavorites);
     audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
     artworkCache = new LruCache<String, Bitmap>(3 * 1024 * 1024) {
         @Override protected int sizeOf(String key, Bitmap value) { return value.getByteCount(); }
     };
     artworkExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
             new ArrayBlockingQueue<Runnable>(16), new ThreadPoolExecutor.DiscardOldestPolicy());
     if (audioManager != null) {
         int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
         if (currentVolume > 0) rememberAudibleVolume(currentVolume);
         else lastAudibleVolume = getSharedPreferences("philco_home", MODE_PRIVATE)
                 .getInt(PREF_LAST_AUDIBLE_VOLUME, -1);
     }
     setContentView(buildUi());
     updateClock();
     updateVolumeUi();
     loadTracks();
 }
 @Override protected void onDestroy() {
     handler.removeCallbacksAndMessages(null);
     if (artworkExecutor != null) artworkExecutor.shutdownNow();
     if (wallpaperView != null) wallpaperView.setImageDrawable(null);
     if (wallpaperBitmap != null && !wallpaperBitmap.isRecycled()) wallpaperBitmap.recycle();
     wallpaperBitmap = null;
     super.onDestroy();
 }
 private Bitmap decodeWallpaper(int resourceId) {
     try {
         BitmapFactory.Options bounds = new BitmapFactory.Options();
         bounds.inJustDecodeBounds = true;
         bounds.inScaled = false;
         BitmapFactory.decodeResource(getResources(), resourceId, bounds);
         int target = Math.max(1280, getResources().getDisplayMetrics().widthPixels);
         int sample = 1;
         while (bounds.outWidth > 0 && bounds.outWidth / (sample * 2) >= target) sample *= 2;
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inPreferredConfig = Bitmap.Config.RGB_565;
         options.inScaled = false;
         options.inSampleSize = sample;
         return BitmapFactory.decodeResource(getResources(), resourceId, options);
     } catch (OutOfMemoryError tooBig) {
         // Low-RAM TV: fall back to the plain dark background instead of crashing on launch.
         return null;
     }
 }
 private View buildUi() {
     FrameLayout root = new FrameLayout(this);
     wallpaperView = new ImageView(this);
     wallpaperBitmap = decodeWallpaper(MUSIC_WALLPAPERS[wallpaperIndex]);
     if (wallpaperBitmap != null) wallpaperView.setImageBitmap(wallpaperBitmap);
     wallpaperView.setScaleType(ImageView.ScaleType.CENTER_CROP);
     root.addView(wallpaperView, new FrameLayout.LayoutParams(-1, -1));
      headerScrim = new View(this);
      headerScrim.setBackground(headerGradient(0xaa2a2118));
      root.addView(headerScrim, new FrameLayout.LayoutParams(-1, -1));
     LinearLayout page = new LinearLayout(this);
     page.setOrientation(LinearLayout.VERTICAL);
     page.setPadding(dp(48), dp(16), dp(48), dp(16));
     root.addView(page, new FrameLayout.LayoutParams(-1, -1));
     page.addView(buildTopBar(), new LinearLayout.LayoutParams(-1, dp(56)));
     LinearLayout body = new LinearLayout(this);
     body.setOrientation(LinearLayout.VERTICAL);
     LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, 0, 1);
     bodyParams.topMargin = dp(14);
     page.addView(body, bodyParams);
     LinearLayout sectionLabels = new LinearLayout(this);
     sectionLabels.setGravity(Gravity.CENTER_VERTICAL);
     sectionLabels.addView(label("TOCANDO AGORA", 13, 0xffe4c77f, true),
             new LinearLayout.LayoutParams(0, dp(25), 0.43f));
     LinearLayout.LayoutParams libraryLabelParams = new LinearLayout.LayoutParams(0, dp(25), 0.57f);
     libraryLabelParams.leftMargin = dp(12);
     sectionLabels.addView(label("SUA BIBLIOTECA", 13, 0xffe4c77f, true), libraryLabelParams);
     body.addView(sectionLabels, new LinearLayout.LayoutParams(-1, dp(25)));
     LinearLayout upper = new LinearLayout(this);
     upper.setOrientation(LinearLayout.HORIZONTAL);
      LinearLayout.LayoutParams upperParams = new LinearLayout.LayoutParams(-1, dp(342));
     upperParams.topMargin = dp(2);
     body.addView(upper, upperParams);
     LinearLayout nowCard = new LinearLayout(this);
     nowCard.setOrientation(LinearLayout.VERTICAL);
     nowCard.setGravity(Gravity.CENTER_VERTICAL);
     nowCard.setPadding(dp(22), dp(14), dp(22), dp(14));
     nowCard.setBackground(glassCard());
     LinearLayout.LayoutParams nowParams = new LinearLayout.LayoutParams(0, -1, 0.43f);
     nowParams.rightMargin = dp(12);
     upper.addView(nowCard, nowParams);
     LinearLayout nowHeading = new LinearLayout(this);
     nowHeading.setGravity(Gravity.CENTER_VERTICAL);
     nowHeading.addView(label("SUA FAIXA", 12, 0xffffd987, true),
             new LinearLayout.LayoutParams(0, dp(24), 1));
     status = label("PRONTO", 11, 0xffffe7b2, true);
     status.setGravity(Gravity.CENTER);
     status.setPadding(dp(10), 0, dp(10), 0);
     status.setBackground(roundRect(0x55352b1e, 0x66e2bd67, dp(14), dp(1)));
     nowHeading.addView(status, new LinearLayout.LayoutParams(-2, dp(25)));
     nowCard.addView(nowHeading, new LinearLayout.LayoutParams(-1, dp(26)));
     LinearLayout heroContent = new LinearLayout(this);
     heroContent.setOrientation(LinearLayout.HORIZONTAL);
     heroContent.setGravity(Gravity.CENTER_VERTICAL);
     LinearLayout.LayoutParams heroContentParams = new LinearLayout.LayoutParams(-1, 0, 1f);
     heroContentParams.topMargin = dp(14);
     nowCard.addView(heroContent, heroContentParams);
     FrameLayout artwork = new FrameLayout(this);
     GradientDrawable art = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
             new int[]{0xffc89c48, 0xff54402b, 0xff202633});
     art.setCornerRadius(dp(18));
     art.setStroke(dp(1), 0x66f0d389);
     artwork.setBackground(art);
     heroArtwork = new ImageView(this);
     heroArtwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
     artwork.addView(heroArtwork, new FrameLayout.LayoutParams(-1, -1));
     TextView musicNote = label("♫", 62, 0xffffe5a0, true);
     musicNote.setGravity(Gravity.CENTER);
     musicNote.setShadowLayer(dp(5), 0, dp(2), 0xaa000000);
     artwork.addView(musicNote, new FrameLayout.LayoutParams(-1, -1));
     heroArtworkFallback = musicNote;
     LinearLayout.LayoutParams artParams = new LinearLayout.LayoutParams(dp(142), dp(142));
     heroContent.addView(artwork, artParams);
     LinearLayout nowText = new LinearLayout(this);
     nowText.setGravity(Gravity.CENTER_VERTICAL);
     nowText.setOrientation(LinearLayout.VERTICAL);
     LinearLayout.LayoutParams nowTextParams = new LinearLayout.LayoutParams(0, -1, 1f);
     nowTextParams.leftMargin = dp(18);
     heroContent.addView(nowText, nowTextParams);
     currentTitle = label("Escolha uma música", 26, WHITE, true);
     currentTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
     currentTitle.setMaxLines(2);
     currentTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
      nowText.addView(currentTitle, new LinearLayout.LayoutParams(-1, dp(76)));
     currentArtist = label("Sua biblioteca local", 17, 0xffe3ddd2, false);
     currentArtist.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
     currentArtist.setSingleLine(true);
     currentArtist.setEllipsize(android.text.TextUtils.TruncateAt.END);
     nowText.addView(currentArtist, new LinearLayout.LayoutParams(-1, dp(28)));
     favoriteButton = label("♡  Favoritar", 14, 0xffffdf9a, true);
     favoriteButton.setGravity(Gravity.CENTER);
     favoriteButton.setFocusable(true);
     favoriteButton.setClickable(true);
     favoriteButton.setContentDescription("Adicionar faixa aos favoritos");
     favoriteButton.setBackground(focusPill());
     favoriteButton.setOnClickListener(v -> toggleFavorite());
     LinearLayout.LayoutParams favoriteParams = new LinearLayout.LayoutParams(dp(150), dp(38));
     favoriteParams.topMargin = dp(12);
     nowText.addView(favoriteButton, favoriteParams);
     LinearLayout libraryCard = new LinearLayout(this);
     libraryCard.setOrientation(LinearLayout.VERTICAL);
     libraryCard.setPadding(dp(22), dp(17), dp(22), dp(12));
     libraryCard.setBackground(glassCard());
     upper.addView(libraryCard, new LinearLayout.LayoutParams(0, -1, 0.57f));
     LinearLayout libraryHeading = new LinearLayout(this);
     libraryHeading.setGravity(Gravity.CENTER_VERTICAL);
     TextView libraryTitle = label("Sua biblioteca", 23, WHITE, true);
     libraryHeading.addView(libraryTitle, new LinearLayout.LayoutParams(0, dp(32), 1));
     libraryCount = label("0 faixas", 14, 0xffffd987, true);
     libraryHeading.addView(libraryCount, new LinearLayout.LayoutParams(-2, dp(28)));
     libraryCard.addView(libraryHeading, new LinearLayout.LayoutParams(-1, dp(36)));
     libraryCard.addView(label("Músicas neste dispositivo", 15, MUTED, false),
             new LinearLayout.LayoutParams(-1, dp(29)));
     list = new ListView(this);
     list.setId(View.generateViewId());
     list.setFocusable(true);
     list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
     list.setSelector(trackSelector());
     list.setDivider(new android.graphics.drawable.ColorDrawable(0x22ffffff));
     list.setDividerHeight(dp(1));
     list.setCacheColorHint(Color.TRANSPARENT);
     list.setBackgroundColor(Color.TRANSPARENT);
     list.setOverScrollMode(View.OVER_SCROLL_NEVER);
     list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
             int first = list.getFirstVisiblePosition();
             for (int i = 0; i < list.getChildCount(); i++) {
                 View child = list.getChildAt(i);
                 if (child instanceof TrackRow) {
                     TrackRow row = (TrackRow)child;
                     boolean selected = first + i == position;
                     if (row.isHighlighted() != selected) row.setHighlighted(selected);
                 }
             }
         }
         @Override public void onNothingSelected(AdapterView<?> parent) {
             for (int i = 0; i < list.getChildCount(); i++) {
                 View child = list.getChildAt(i);
                 if (child instanceof TrackRow) ((TrackRow)child).setHighlighted(false);
             }
         }
     });
     libraryEmptyState = label("Lendo biblioteca de músicas…", 15, MUTED, false);
     libraryEmptyState.setGravity(Gravity.CENTER);
     libraryEmptyState.setMaxLines(2);
     libraryCard.addView(libraryEmptyState, new LinearLayout.LayoutParams(-1, 0, 1));
     libraryCard.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
     list.setEmptyView(libraryEmptyState);
     list.setOnItemClickListener((parent, view, position, id) -> playVisibleAt(position));
     LinearLayout collectionHeading = new LinearLayout(this);
     collectionHeading.setGravity(Gravity.CENTER_VERTICAL);
     collectionHeading.addView(label("CONTINUE OUVINDO", 13, 0xffffd987, true),
             new LinearLayout.LayoutParams(0, dp(22), 1));
     collectionHeading.addView(label("Faixas locais · favoritas primeiro", 14, 0xffded8cd, false),
             new LinearLayout.LayoutParams(-2, dp(22)));
     LinearLayout.LayoutParams collectionHeadingParams = new LinearLayout.LayoutParams(-1, dp(30));
     collectionHeadingParams.topMargin = dp(7);
     body.addView(collectionHeading, collectionHeadingParams);
     recommendations = new HorizontalScrollView(this);
     recommendations.setHorizontalScrollBarEnabled(false);
     recommendations.setFocusable(false);
     recommendationRow = new LinearLayout(this);
     recommendationRow.setGravity(Gravity.CENTER_VERTICAL);
     recommendations.addView(recommendationRow, new HorizontalScrollView.LayoutParams(-2, -1));
      body.addView(recommendations, new LinearLayout.LayoutParams(-1, dp(82)));
     LinearLayout player = new LinearLayout(this);
     player.setOrientation(LinearLayout.HORIZONTAL);
     player.setGravity(Gravity.CENTER_VERTICAL);
     player.setPadding(dp(18), dp(10), dp(18), dp(10));
     player.setBackground(roundRect(0xf50d1117, 0x66e3c477, dp(22), dp(1)));
     LinearLayout trackInfo = new LinearLayout(this);
     trackInfo.setGravity(Gravity.CENTER_VERTICAL);
     trackInfo.setOrientation(LinearLayout.HORIZONTAL);
     LinearLayout.LayoutParams trackInfoParams = new LinearLayout.LayoutParams(0, -1, 2.55f);
     trackInfoParams.rightMargin = dp(8);
     player.addView(trackInfo, trackInfoParams);
     FrameLayout trackArtwork = new FrameLayout(this);
     trackArtwork.setBackground(roundRect(0xff35291c, 0x77e2bd67, dp(11), dp(1)));
     miniArtwork = new ImageView(this);
     miniArtwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
     trackArtwork.addView(miniArtwork, new FrameLayout.LayoutParams(-1, -1));
     TextView trackGlyph = label("♪", 24, 0xffffe0a0, true);
     trackGlyph.setGravity(Gravity.CENTER);
     trackArtwork.addView(trackGlyph, new FrameLayout.LayoutParams(-1, -1));
     miniArtworkFallback = trackGlyph;
     trackInfo.addView(trackArtwork, new LinearLayout.LayoutParams(dp(54), dp(54)));
     LinearLayout trackLabels = new LinearLayout(this);
     trackLabels.setOrientation(LinearLayout.VERTICAL);
     trackLabels.setGravity(Gravity.CENTER_VERTICAL);
     LinearLayout.LayoutParams trackLabelsParams = new LinearLayout.LayoutParams(0, -1, 1f);
     trackLabelsParams.leftMargin = dp(10);
     trackInfo.addView(trackLabels, trackLabelsParams);
     playerTitle = label("Escolha uma música", 18, WHITE, true);
     playerTitle.setSingleLine(true);
     playerTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
     trackLabels.addView(playerTitle, new LinearLayout.LayoutParams(-1, dp(29)));
     playerArtist = label("Biblioteca local", 15, 0xffc8c2b8, false);
     playerArtist.setSingleLine(true);
     playerArtist.setEllipsize(android.text.TextUtils.TruncateAt.END);
     trackLabels.addView(playerArtist, new LinearLayout.LayoutParams(-1, dp(20)));
     LinearLayout actions = new LinearLayout(this);
     actions.setGravity(Gravity.CENTER_VERTICAL);
     ActionButton previous = action("|‹", "Faixa anterior", false);
     ActionButton toggle = action("▶", "Reproduzir", true);
     ActionButton next = action("›|", "Próxima faixa", false);
     playbackToggle = toggle;
     nextAction = next;
     previousIcon = previous.icon;
     toggleIcon = toggle.icon;
     toggleLabel = toggle.caption;
     nextIcon = next.icon;
     toggle.caption.setVisibility(View.GONE);
     previous.caption.setVisibility(View.GONE);
     next.caption.setVisibility(View.GONE);
     actions.addView(previous, actionParams(dp(52), dp(54), dp(3)));
     actions.addView(toggle, actionParams(dp(62), dp(60), dp(3)));
     actions.addView(next, actionParams(dp(52), dp(54), dp(3)));
     LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(0, -1, 1.9f);
     actionsParams.rightMargin = dp(8);
     player.addView(actions, actionsParams);
     LinearLayout seekRow = new LinearLayout(this);
     seekRow.setGravity(Gravity.CENTER_VERTICAL);
     elapsed = label("0:00", 13, WHITE, true);
     totalTime = label("0:00", 13, WHITE, true);
     elapsed.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
     totalTime.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
     seekBar = new SeekBar(this);
     seekBar.setId(View.generateViewId());
     seekBar.setMax(1000);
     seekBar.setFocusable(true);
     seekBar.setFocusableInTouchMode(true);
     seekBar.setProgressDrawable(goldProgressDrawable());
     seekBar.setThumb(goldThumb());
     seekBar.setThumbOffset(dp(9));
     seekBar.setThumb(null);
     seekBar.setThumbOffset(0);
      seekBar.setContentDescription("Progresso da faixa. OK ativa o ajuste; esquerda e direita mudam o tempo");
     seekRow.addView(elapsed, new LinearLayout.LayoutParams(dp(48), dp(36)));
     seekRow.addView(seekBar, new LinearLayout.LayoutParams(0, dp(42), 1));
     seekRow.addView(totalTime, new LinearLayout.LayoutParams(dp(52), dp(36)));
     LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(0, -1, 4.4f);
     seekParams.leftMargin = dp(8);
     seekParams.rightMargin = dp(12);
     player.addView(seekRow, seekParams);
     LinearLayout rightTools = new LinearLayout(this);
     rightTools.setGravity(Gravity.CENTER_VERTICAL);
     rightTools.setOrientation(LinearLayout.HORIZONTAL);
     player.addView(rightTools, new LinearLayout.LayoutParams(0, -1, 1.55f));
     volumeButton = label("", 14, WHITE, true);
     volumeButton.setGravity(Gravity.CENTER);
     volumeButton.setFocusable(true);
     volumeButton.setClickable(true);
     volumeButton.setBackground(focusPill());
     volumeButton.setContentDescription("Abrir controle de volume");
     rightTools.addView(volumeButton, new LinearLayout.LayoutParams(dp(108), dp(52)));
     volumeButton.setOnClickListener(v -> showVolumePopover());
     TextView moreButton = label("•••", 20, 0xffffe1a0, true);
     moreButton.setGravity(Gravity.CENTER);
     moreButton.setFocusable(true);
     moreButton.setClickable(true);
     moreButton.setBackground(focusPill());
     moreButton.setContentDescription("Opções de música");
     LinearLayout.LayoutParams moreButtonParams = new LinearLayout.LayoutParams(dp(46), dp(50));
     moreButtonParams.leftMargin = dp(4);
     rightTools.addView(moreButton, moreButtonParams);
     moreButton.setOnClickListener(v -> showMoreMenu());
     volumePopover = new LinearLayout(this);
     volumePopover.setGravity(Gravity.CENTER_VERTICAL);
     volumePopover.setOrientation(LinearLayout.HORIZONTAL);
     volumePopover.setPadding(dp(14), dp(8), dp(14), dp(8));
     volumePopover.setBackground(roundRect(0xf0202227, 0xffd8b766, dp(18), dp(2)));
     LinearLayout muteButton = new LinearLayout(this);
     muteButton.setId(View.generateViewId());
     muteButton.setGravity(Gravity.CENTER);
     muteButton.setFocusable(true);
     muteButton.setFocusableInTouchMode(true);
     muteButton.setClickable(true);
     muteButton.setBackground(focusPill());
     muteButton.setContentDescription("Silenciar ou restaurar volume");
     muteButton.setOnKeyListener((view, keyCode, event) -> {
         if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
             if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
                 view.performClick();
             return true;
         }
         return false;
     });
     volumeGlyph = new VolumeGlyph(this, WHITE);
     muteButton.addView(volumeGlyph, new LinearLayout.LayoutParams(dp(26), dp(26)));
     volumePopover.addView(muteButton, new LinearLayout.LayoutParams(dp(42), dp(48)));
     muteButton.setOnClickListener(v -> toggleMute());
     volumeValue = label("--", 16, WHITE, true);
     volumeSlider = new SeekBar(this);
     volumeSlider.setId(View.generateViewId());
     volumeSlider.setMax(Math.max(1, audioManager == null ? 100
             : audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
     volumeSlider.setProgressDrawable(goldProgressDrawable());
     volumeSlider.setThumb(goldThumb());
     volumeSlider.setThumbOffset(dp(7));
     volumeSlider.setFocusable(true);
     volumeSlider.setFocusableInTouchMode(true);
     volumeSlider.setContentDescription("Volume. Esquerda diminui, direita aumenta");
     volumeControl = volumeSlider;
     LinearLayout.LayoutParams volumeSliderParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
     volumeSliderParams.leftMargin = dp(8);
     volumeSliderParams.rightMargin = dp(8);
     volumePopover.addView(volumeSlider, volumeSliderParams);
     volumeValue.setGravity(Gravity.CENTER);
     volumePopover.addView(volumeValue, new LinearLayout.LayoutParams(dp(48), dp(42)));
     volumePopover.setVisibility(View.GONE);
     FrameLayout.LayoutParams volumePopoverParams = new FrameLayout.LayoutParams(dp(340), dp(72),
             Gravity.BOTTOM | Gravity.RIGHT);
     volumePopoverParams.rightMargin = dp(48);
     volumePopoverParams.bottomMargin = dp(112);
     root.addView(volumePopover, volumePopoverParams);
     volumeButton.setNextFocusUpId(volumeSlider.getId());
     volumeSlider.setNextFocusDownId(volumeButton.getId());
     volumeSlider.setNextFocusLeftId(muteButton.getId());
     muteButton.setNextFocusRightId(volumeSlider.getId());
      volumeSlider.setOnKeyListener((view, keyCode, event) -> {
         if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
              if (event.getAction() == KeyEvent.ACTION_DOWN) {
                  int direction = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1;
                  setVolumeIndex(volumeSlider.getProgress()
                          + direction * volumeStepsForRepeat(event.getRepeatCount()));
             }
             return true;
         }
         return false;
     });
     shuffleButton = null;
     repeatButton = null;
     volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
             if (fromUser) setVolumeIndex(progress);
         }
         @Override public void onStartTrackingTouch(SeekBar bar) { }
         @Override public void onStopTrackingTouch(SeekBar bar) { }
     });
     updateVolumeUi();
     list.setNextFocusDownId(seekBar.getId());
     seekBar.setNextFocusUpId(list.getId());
     seekBar.setNextFocusDownId(previous.getId());
     previous.setNextFocusUpId(seekBar.getId());
     toggle.setNextFocusUpId(seekBar.getId());
     next.setNextFocusUpId(seekBar.getId());
     previous.setNextFocusRightId(toggle.getId());
     toggle.setNextFocusLeftId(previous.getId());
     toggle.setNextFocusRightId(next.getId());
     next.setNextFocusLeftId(toggle.getId());
     next.setNextFocusRightId(volumeButton.getId());
     volumeButton.setNextFocusLeftId(next.getId());
     volumeButton.setNextFocusRightId(moreButton.getId());
     moreButton.setNextFocusLeftId(volumeButton.getId());
     seekBar.setOnKeyListener((v, keyCode, event) -> {
          if (event.getAction() != KeyEvent.ACTION_DOWN) return keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                  || keyCode == KeyEvent.KEYCODE_ENTER;
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
              if (seekBar.isEnabled()) setSeekAdjustMode(true);
             return true;
         }
          if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
              if (seekAdjustMode) {
                  if (seekBar.isEnabled()) {
                      int direction = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1;
                      seekRelative(direction * seekStepForRepeat(event.getRepeatCount()));
                  }
                  return true;
              }
              View target = keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? nextAction : volumeButton;
              if (target != null) target.requestFocus();
              return true;
          }
         return false;
     });
     seekBar.setOnFocusChangeListener((v, focused) -> {
         if (!focused && seekAdjustMode) setSeekAdjustMode(false);
         updateSeekThumbVisibility();
     });
     seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
             if (fromUser && !userSeeking) seekToProgress(progress, bar.getMax());
         }
         @Override public void onStartTrackingTouch(SeekBar bar) { userSeeking = true; }
         @Override public void onStopTrackingTouch(SeekBar bar) {
             seekToProgress(bar.getProgress(), bar.getMax());
             userSeeking = false;
             showState();
         }
     });
     previous.setOnClickListener(v -> playPrevious());
     next.setOnClickListener(v -> playNext());
     toggle.setOnClickListener(v -> togglePlayback());
     adapter = new TrackAdapter();
     list.setAdapter(adapter);
     if (searchButton != null) list.setNextFocusUpId(searchButton.getId());
     refreshPlayerTools();
     wireNavigation();
     FrameLayout.LayoutParams playerParams = new FrameLayout.LayoutParams(-1, dp(88), Gravity.BOTTOM);
     playerParams.setMargins(dp(48), 0, dp(48), dp(18));
     root.addView(player, playerParams);
      root.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
          if (newFocus == seekBar && oldFocus != null && oldFocus != seekBar)
              seekReturnFocusId = oldFocus.getId();
      });
     return root;
 }
  private void setSeekAdjustMode(boolean active) {
      seekAdjustMode = active;
      if (seekBar == null) return;
       updateSeekThumbVisibility();
      seekBar.setContentDescription(active
              ? "Ajuste de tempo ativo. Direita avança, esquerda retrocede; Voltar encerra e retorna ao controle anterior"
              : "Progresso da faixa. OK ativa o ajuste; esquerda e direita mudam o tempo");
      seekBar.setBackground(active ? roundRect(0x332e2412, GOLD_FOCUS, dp(10), dp(2)) : null);
  }
   private void updateSeekThumbVisibility() {
       if (seekBar == null) return;
       boolean show = seekAdjustMode || seekBar.hasFocus();
       seekBar.setThumb(show ? goldThumb() : null);
       seekBar.setThumbOffset(show ? dp(9) : 0);
   }
   private int volumeStepsForRepeat(int repeatCount) {
       if (repeatCount >= 10) return 5;
       if (repeatCount >= 6) return 3;
       if (repeatCount >= 3) return 2;
       return 1;
   }
   private int seekStepForRepeat(int repeatCount) {
       if (repeatCount >= 10) return 30000;
       if (repeatCount >= 5) return 20000;
       return SEEK_STEP_MS;
   }
 private TextView playerTool(String glyph, String description) {
     TextView tool = label(glyph, 21, 0xffffe2a0, true);
     tool.setGravity(Gravity.CENTER);
     tool.setSingleLine(true);
     tool.setFocusable(true);
     tool.setFocusableInTouchMode(true);
     tool.setClickable(true);
     tool.setContentDescription(description);
     tool.setBackground(focusPill());
     tool.setOnKeyListener((view, keyCode, event) -> {
         if (event.getAction() == KeyEvent.ACTION_DOWN
                 && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
             view.performClick();
             return true;
         }
         return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER;
     });
     return tool;
 }
 private void toggleFavorite() {
     PlaybackState state = controller == null ? null : controller.getState();
     if (state == null || state.track == null || favoriteButton == null) return;
     String uri = state.track.uri.toString();
     if (!favorites.remove(uri)) favorites.add(uri);
     playerPreferences.edit().putStringSet("favorite_uris", new HashSet<String>(favorites)).apply();
     updateFavoriteUi(state.track);
     if (adapter != null) adapter.notifyDataSetChanged();
     rebuildRecommendations();
 }
 private boolean isFavorite(Track track) {
     return track != null && track.uri != null && favorites.contains(track.uri.toString());
 }
 private void updateFavoriteUi(Track track) {
     if (favoriteButton == null || track == null || playerPreferences == null) return;
     boolean favorite = isFavorite(track);
     setTextIfChanged(favoriteButton, favorite ? "♥  Favoritada" : "♡  Favoritar");
     favoriteButton.setTextColor(favorite ? 0xffff9b83 : 0xffffe2a0);
     favoriteButton.setContentDescription(favorite ? "Remover dos favoritos" : "Marcar como favorita");
 }
 private void toggleShuffle() {
     shuffleEnabled = !shuffleEnabled;
     playerPreferences.edit().putBoolean("shuffle", shuffleEnabled).apply();
      syncPlaybackQueue();
     refreshPlayerTools();
 }
 private void cycleRepeat() {
     repeatMode = (repeatMode + 1) % 3;
     playerPreferences.edit().putInt("repeat_mode", repeatMode).apply();
     completionHandled = false;
      syncPlaybackQueue();
     refreshPlayerTools();
 }
 private void refreshPlayerTools() {
     if (shuffleButton != null) {
         shuffleButton.setText(shuffleEnabled ? "⇄•" : "⇄");
         shuffleButton.setTextColor(shuffleEnabled ? 0xffffd36e : 0xffffe2a0);
         shuffleButton.setContentDescription(shuffleEnabled ? "Aleatório ligado" : "Aleatório desligado");
     }
     if (repeatButton != null) {
         repeatButton.setText(repeatMode == 2 ? "↻¹" : "↻");
         repeatButton.setTextColor(repeatMode == 0 ? 0xffffe2a0 : 0xffffd36e);
         repeatButton.setContentDescription(repeatMode == 0 ? "Repetição desligada"
                 : repeatMode == 1 ? "Repetir fila" : "Repetir faixa atual");
     }
 }
 private void playNext() {
     if (tracks.isEmpty()) return;
      if (!controller.skipNext())
         Toast.makeText(this, "Esta é a última faixa da biblioteca", Toast.LENGTH_SHORT).show();
      showState();
 }
 private void playPrevious() {
     if (tracks.isEmpty()) return;
      controller.skipPrevious();
      showState();
 }
 private void seekRelative(int deltaMs) {
     PlaybackState state = controller.getState();
     if (state.track == null || state.durationMs <= 0) return;
     int target = Math.max(0, Math.min(state.durationMs, state.positionMs + deltaMs));
     controller.seekTo(target);
     seekBar.setProgress((int)((long)target * seekBar.getMax() / state.durationMs));
     elapsed.setText(formatTime(target));
 }
 private void showQueue() {
     if (tracks.isEmpty()) {
         Toast.makeText(this, "A biblioteca não contém faixas", Toast.LENGTH_SHORT).show();
         return;
     }
     String[] titles = new String[tracks.size()];
     for (int i = 0; i < tracks.size(); i++) {
         String artist = i < artists.size() ? artists.get(i) : "Artista desconhecido";
         titles[i] = (i + 1) + ".  " + tracks.get(i).title + " — " + artist;
     }
     int checked = currentIndex();
     AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Fila da biblioteca")
             .setItems(titles, (d, which) -> playAt(which))
             .setNegativeButton("FECHAR", null).create();
     dialog.show();
     if (checked >= 0) dialog.getListView().setSelection(checked);
 }
 private void showTrackDetails() {
     PlaybackState state = controller == null ? null : controller.getState();
     if (state == null || state.track == null) {
         Toast.makeText(this, "Escolha uma faixa primeiro", Toast.LENGTH_SHORT).show();
         return;
     }
     int index = indexOf(state.track);
     String artist = index >= 0 && index < artists.size() ? artists.get(index) : "Artista desconhecido";
     String length = index >= 0 && index < durations.size() ? formatTime(durations.get(index)) : formatTime(state.durationMs);
     new AlertDialog.Builder(this).setTitle(state.track.title)
             .setMessage(artist + "\nDuração: " + length + "\nBiblioteca local")
             .setPositiveButton("OK", null).show();
 }
 private void showMoreMenu() {
     PlaybackState state = controller == null ? null : controller.getState();
     String repeat = repeatMode == 0 ? "Desligado" : repeatMode == 1 ? "Repetir fila" : "Repetir faixa";
      boolean hasTrack = state != null && state.track != null;
      LinearLayout content = new LinearLayout(this);
      content.setOrientation(LinearLayout.VERTICAL);
      content.setPadding(dp(18), dp(18), dp(18), dp(12));
      content.setBackground(roundRect(0xff17191d, 0xffa88c4e, dp(22), dp(1)));
      content.addView(label("Opções de música", 21, WHITE, true),
              new LinearLayout.LayoutParams(-1, dp(34)));
      content.addView(label("Sua reprodução, do seu jeito", 13, MUTED, false),
              new LinearLayout.LayoutParams(-1, dp(27)));
      AlertDialog dialog = new AlertDialog.Builder(this)
              .setView(content)
              .create();
      addMoreMenuRow(content, dialog, "≡", "Fila de reprodução", "Ver e escolher a próxima faixa",
              "ABRIR", true, () -> showQueue());
      addMoreMenuRow(content, dialog, "⇩", "Receber do celular", "Enviar músicas e fotos pela rede local",
              "LAN", true, () -> showLocalMediaTransfer());
      addMoreMenuRow(content, dialog, "⇄", "Reprodução aleatória", "Misturar a ordem da fila",
              shuffleEnabled ? "ATIVADO" : "DESATIVADO", true, () -> toggleShuffle());
      addMoreMenuRow(content, dialog, "↻", "Repetição", "Escolher o ciclo da reprodução",
              repeat, true, () -> cycleRepeat());
      addMoreMenuRow(content, dialog, "i", "Detalhes da faixa", "Artista e duração",
              hasTrack ? "VER" : "—", hasTrack, () -> showTrackDetails());
      addMoreMenuRow(content, dialog, "♡", isFavorite(state == null ? null : state.track)
                      ? "Remover dos favoritos" : "Adicionar aos favoritos",
              "Guardar esta faixa na biblioteca", "FAVORITO", hasTrack, () -> toggleFavorite());
      TextView close = label("Fechar", 14, GOLD_FOCUS, true);
      close.setGravity(Gravity.CENTER);
      close.setFocusable(true);
      close.setClickable(true);
      close.setBackground(roundRect(0x3323262b, 0x66d8b766, dp(14), dp(1)));
      LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(-1, dp(42));
      closeParams.topMargin = dp(9);
      content.addView(close, closeParams);
      close.setOnFocusChangeListener((v, focused) -> {
          close.setBackground(roundRect(focused ? 0xffe7cb83 : 0x3323262b,
                  focused ? 0xffffe6a3 : 0x66d8b766, dp(14), dp(1)));
          close.setTextColor(focused ? 0xff211b12 : GOLD_FOCUS);
      });
      close.setOnClickListener(v -> dialog.dismiss());
      dialog.show();
      if (dialog.getWindow() != null) {
          dialog.getWindow().setBackgroundDrawable(roundRect(0xff17191d, 0xffa88c4e, dp(22), dp(1)));
          dialog.getWindow().setLayout(dp(470), WindowManager.LayoutParams.WRAP_CONTENT);
      }
      if (content.getChildCount() > 2) content.getChildAt(2).requestFocus();
  }
  private void addMoreMenuRow(LinearLayout parent, AlertDialog dialog, String glyph,
          String title, String subtitle, String value, boolean enabled, Runnable action) {
      LinearLayout row = new LinearLayout(this);
      row.setGravity(Gravity.CENTER_VERTICAL);
      row.setPadding(dp(10), 0, dp(12), 0);
      row.setFocusable(enabled);
      row.setClickable(enabled);
      row.setBackground(roundRect(0x331f2228, 0x33ffffff, dp(14), dp(1)));
      TextView icon = label(glyph, 20, enabled ? GOLD_FOCUS : MUTED, true);
      icon.setGravity(Gravity.CENTER);
      row.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(42)));
      LinearLayout text = new LinearLayout(this);
      text.setOrientation(LinearLayout.VERTICAL);
      text.setGravity(Gravity.CENTER_VERTICAL);
      LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -1, 1f);
      textParams.leftMargin = dp(9);
      row.addView(text, textParams);
      TextView heading = label(title, 15, enabled ? WHITE : MUTED, true);
      heading.setSingleLine(true);
      heading.setEllipsize(android.text.TextUtils.TruncateAt.END);
      text.addView(heading, new LinearLayout.LayoutParams(-1, dp(23)));
      TextView description = label(subtitle, 11, MUTED, false);
      description.setSingleLine(true);
      description.setEllipsize(android.text.TextUtils.TruncateAt.END);
      text.addView(description, new LinearLayout.LayoutParams(-1, dp(18)));
      TextView valueView = label(value, 10, enabled ? GOLD_FOCUS : MUTED, true);
      valueView.setGravity(Gravity.CENTER);
      valueView.setSingleLine(true);
      row.addView(valueView, new LinearLayout.LayoutParams(dp(92), dp(32)));
      LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(58));
      rowParams.topMargin = dp(5);
      parent.addView(row, rowParams);
      if (enabled) {
          row.setOnFocusChangeListener((v, focused) -> {
              row.setBackground(roundRect(focused ? 0xffe7cb83 : 0x3323262b,
                      focused ? 0xffffe6a3 : 0x44ffffff, dp(14), dp(1)));
              int primaryText = focused ? 0xff211b12 : WHITE;
              int secondaryText = focused ? 0xff55441f : MUTED;
              icon.setTextColor(focused ? 0xff55441f : GOLD_FOCUS);
              heading.setTextColor(primaryText);
              description.setTextColor(secondaryText);
              valueView.setTextColor(focused ? 0xff55441f : GOLD_FOCUS);
          });
          row.setOnClickListener(v -> {
              dialog.dismiss();
              action.run();
          });
      } else {
          row.setAlpha(0.58f);
      }
 }
 private void showLocalMediaTransfer() {
     if (transferServer != null) transferServer.close();
     transferServer = new LocalMediaTransferServer(this, new LocalMediaTransferServer.Listener() {
         @Override public void onFileReceived(File file) {
             runOnUiThread(() -> {
                 if (isFinishing()) return;
                 Toast.makeText(MusicActivity.this, "Recebido: " + file.getName(), Toast.LENGTH_LONG).show();
                 handler.postDelayed(() -> loadTracks(), 1200L);
             });
         }
         @Override public void onError(String message) {
             runOnUiThread(() -> Toast.makeText(MusicActivity.this, message, Toast.LENGTH_LONG).show());
         }
     });
     try {
         String address = transferServer.start();
         LinearLayout content = new LinearLayout(this);
         content.setOrientation(LinearLayout.VERTICAL);
         content.setPadding(dp(22), dp(20), dp(22), dp(20));
         content.setBackground(roundRect(0xff17191d, 0xffd4b365, dp(20), dp(1)));
         content.addView(label("Receber do celular", 22, WHITE, true),
                 new LinearLayout.LayoutParams(-1, dp(38)));
         content.addView(label("Celular e TV precisam estar na mesma rede Wi-Fi.", 14, MUTED, false),
                 new LinearLayout.LayoutParams(-1, dp(28)));
         TextView url = label("http://" + address, 19, 0xffffe3a0, true);
         url.setTextIsSelectable(true);
         content.addView(url, new LinearLayout.LayoutParams(-1, dp(40)));
         TextView code = label(transferServer.getPairingCode(), 36, GOLD_FOCUS, true);
         code.setGravity(Gravity.CENTER);
         code.setBackground(roundRect(0xff28231a, 0x99d4b365, dp(14), dp(1)));
         LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(-1, dp(62));
         codeParams.topMargin = dp(7);
         content.addView(code, codeParams);
         content.addView(label("Digite esse código no navegador do celular. Aceita músicas e fotos até 250 MB.",
                 13, MUTED, false), new LinearLayout.LayoutParams(-1, dp(42)));
         transferDialog = new AlertDialog.Builder(this).setView(content).create();
         transferDialog.setOnDismissListener(d -> {
             if (transferServer != null) { transferServer.close(); transferServer = null; }
             transferDialog = null;
         });
         transferDialog.show();
         if (transferDialog.getWindow() != null) {
             transferDialog.getWindow().setBackgroundDrawable(roundRect(0xff17191d, 0xffd4b365, dp(20), dp(1)));
             transferDialog.getWindow().setLayout(dp(540), WindowManager.LayoutParams.WRAP_CONTENT);
         }
     } catch (Exception error) {
         if (transferServer != null) { transferServer.close(); transferServer = null; }
         Toast.makeText(this, "Não foi possível abrir o envio local: " + error.getMessage(), Toast.LENGTH_LONG).show();
     }
 }
 private void showVolumePopover() {
     if (volumePopover == null) return;
     if (volumePopover.getVisibility() == View.VISIBLE) {
         hideVolumePopover(true);
         return;
     }
     volumePopover.setVisibility(View.VISIBLE);
     updateVolumeUi();
     volumeSlider.requestFocus();
 }
 private void hideVolumePopover(boolean returnFocus) {
     if (volumePopover == null || volumePopover.getVisibility() != View.VISIBLE) return;
     volumePopover.setVisibility(View.GONE);
     if (returnFocus && volumeButton != null) volumeButton.requestFocus();
 }
 private void setVolumeIndex(int index) {
     if (audioManager == null) return;
     try {
         setMediaVolumeIndex(index);
         if (index > 0) rememberAudibleVolume(index);
         updateVolumeUi();
     } catch (Exception error) {
         Toast.makeText(this, "O volume da TV não está disponível", Toast.LENGTH_SHORT).show();
     }
 }
 private LinearLayout buildTopBar() {
     LinearLayout top = new LinearLayout(this);
     top.setGravity(Gravity.CENTER_VERTICAL);
     LinearLayout brand = new LinearLayout(this);
     brand.setGravity(Gravity.CENTER_VERTICAL);
     TextView note = label("♪", 34, GOLD_FOCUS, true);
     note.setTypeface(Typeface.DEFAULT);
     brand.addView(note, new LinearLayout.LayoutParams(dp(36), dp(44)));
     LinearLayout brandText = new LinearLayout(this);
     brandText.setOrientation(LinearLayout.VERTICAL);
     brandText.setGravity(Gravity.CENTER_VERTICAL);
     brandText.addView(label("PHILCOOS", 18, WHITE, true), new LinearLayout.LayoutParams(-1, dp(22)));
     brandText.addView(label("MÚSICA", 12, GOLD_FOCUS, true), new LinearLayout.LayoutParams(-1, dp(18)));
     LinearLayout.LayoutParams brandTextParams = new LinearLayout.LayoutParams(dp(136), -1);
     brandTextParams.leftMargin = dp(8);
     brand.addView(brandText, brandTextParams);
     top.addView(brand, new LinearLayout.LayoutParams(dp(220), -1));
     navigationSlot = new FrameLayout(this);
     LinearLayout.LayoutParams navigationParams = new LinearLayout.LayoutParams(0, dp(46), 1);
     navigationParams.leftMargin = dp(12);
     navigationParams.rightMargin = dp(14);
     top.addView(navigationSlot, navigationParams);
     tabBar = new LinearLayout(this);
     tabBar.setGravity(Gravity.CENTER);
      tabBar.setPadding(dp(9), dp(5), dp(9), dp(5));
     tabBar.setBackground(roundRect(0xa60d1117, 0x44fff0d2, dp(24), dp(1)));
     navigationSlot.addView(tabBar, new FrameLayout.LayoutParams(-1, -1));
     addTab("Ouvir agora", 0, 1.08f);
     addTab("Biblioteca", 1, 1.0f);
     addTab("Tocando", 2, 0.9f);
     searchInput = new EditText(this);
     searchInput.setId(View.generateViewId());
     searchInput.setSingleLine(true);
     searchInput.setTextSize(20);
     searchInput.setTextColor(WHITE);
     searchInput.setHintTextColor(0xffe5d8bd);
     searchInput.setHint("Buscar músicas locais");
     searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
     searchInput.setPadding(dp(20), 0, dp(18), 0);
     searchInput.setBackground(roundRect(0xf0141820, 0x99e4c577, dp(22), dp(1)));
     searchInput.setVisibility(View.GONE);
     navigationSlot.addView(searchInput, new FrameLayout.LayoutParams(-1, -1));
     searchInput.addTextChangedListener(new TextWatcher() {
         @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
         @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
             searchQuery = s.toString();
             applyFilter();
         }
         @Override public void afterTextChanged(Editable s) { }
     });
     searchInput.setOnEditorActionListener((v, actionId, event) -> {
         if (actionId == EditorInfo.IME_ACTION_SEARCH) {
             hideSearchKeyboard();
             if (list != null && list.getCount() > 0) list.requestFocus();
             return true;
         }
         return false;
     });
      LinearLayout right = new LinearLayout(this);
     right.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
      top.addView(right, new LinearLayout.LayoutParams(dp(198), -1));
     LinearLayout searchControl = new LinearLayout(this);
     searchControl.setOrientation(LinearLayout.HORIZONTAL);
     searchControl.setGravity(Gravity.CENTER);
      searchControl.setPadding(dp(12), 0, dp(13), 0);
      searchControl.setBackground(roundRect(0xcc3a3020, 0xffd4b365, dp(22), dp(1)));
     searchControl.setFocusable(true);
     searchControl.setClickable(true);
      SearchIconView searchIcon = new SearchIconView(this);
     searchIcon.setFocusable(false);
      searchControl.addView(searchIcon, new LinearLayout.LayoutParams(dp(22), dp(22)));
      TextView searchLabel = label("Buscar", 14, 0xffffe3a0, true);
      searchLabel.setSingleLine(true);
      LinearLayout.LayoutParams searchLabelParams = new LinearLayout.LayoutParams(-2, -1);
      searchLabelParams.leftMargin = dp(7);
      searchControl.addView(searchLabel, searchLabelParams);
     searchButton = searchControl;
     searchButton.setId(View.generateViewId());
     searchButton.setContentDescription("Buscar música");
      right.addView(searchButton, new LinearLayout.LayoutParams(dp(104), dp(46)));
     searchButton.setOnClickListener(v -> openSearch());
     View lastTab = tabBar.getChildAt(tabBar.getChildCount() - 1);
     if (lastTab != null) {
         lastTab.setNextFocusRightId(searchButton.getId());
         searchButton.setNextFocusLeftId(lastTab.getId());
     }
     searchButton.setOnFocusChangeListener((v, focused) -> {
         searchButton.setBackground(roundRect(focused ? 0xffffd572 : 0xcc3a3020,
                 focused ? 0xffffefb5 : 0xffd4b365, dp(22), dp(focused ? 3 : 1)));
         searchLabel.setTextColor(focused ? 0xff211b12 : 0xffffe3a0);
         searchIcon.setIconColor(focused ? 0xff4b3610 : GOLD_FOCUS);
         searchIcon.invalidate();
     });
     LinearLayout timePill = new LinearLayout(this);
     timePill.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
     timePill.setPadding(dp(10), 0, dp(10), 0);
     timePill.setBackground(roundRect(0xaa11161d, 0x44fff0d2, dp(22), dp(1)));
      LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(dp(78), dp(46));
     timeParams.leftMargin = dp(8);
     right.addView(timePill, timeParams);
     clock = label("00:00", 16, WHITE, true);
     clock.setSingleLine(true);
     clock.setGravity(Gravity.CENTER);
     timePill.addView(clock, new LinearLayout.LayoutParams(-1, -1));
     return top;
 }
 private void addTab(String title, int destination, float weight) {
     TextView tab = label(title, 13, WHITE, true);
     tab.setGravity(Gravity.CENTER);
     tab.setSingleLine(true);
     tab.setId(View.generateViewId());
      tab.setTag(Integer.valueOf(destination));
     tab.setFocusable(true);
     tab.setBackground(roundRect(Color.TRANSPARENT, Color.TRANSPARENT, dp(18), 0));
     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, weight);
      params.leftMargin = dp(3);
      params.rightMargin = dp(3);
     tabBar.addView(tab, params);
     if (tabBar.getChildCount() > 1) {
         View previous = tabBar.getChildAt(tabBar.getChildCount() - 2);
         previous.setNextFocusRightId(tab.getId());
         tab.setNextFocusLeftId(previous.getId());
     }
      tab.setContentDescription("Ir para " + title);
      tab.setOnClickListener(v -> {
          activeTabDestination = destination;
          refreshTabStyles();
          navigateTo(destination);
      });
     tab.setOnFocusChangeListener((v, focused) -> {
          refreshTabStyles();
     });
      refreshTabStyles();
  }
  private void refreshTabStyles() {
      if (tabBar == null) return;
      for (int i = 0; i < tabBar.getChildCount(); i++) {
          View child = tabBar.getChildAt(i);
          if (!(child instanceof TextView) || !(child.getTag() instanceof Integer)) continue;
          boolean active = ((Integer)child.getTag()).intValue() == activeTabDestination;
          boolean focused = child.hasFocus();
          if (focused) child.setBackground(roundRect(0xffead18b, 0xffffe6aa, dp(18), dp(1)));
          else if (active) child.setBackground(roundRect(0xffd9b96b, 0x00ffffff, dp(18), 0));
          else child.setBackground(roundRect(Color.TRANSPARENT, Color.TRANSPARENT, dp(18), 0));
          ((TextView)child).setTextColor(active || focused ? 0xff211b12 : 0xffd0cbc2);
      }
 }
 private void navigateTo(int destination) {
     switch (destination) {
          case 0:
              if (recommendationRow != null && recommendationRow.getChildCount() > 0)
                  recommendationRow.getChildAt(0).requestFocus();
              else if (playbackToggle != null) playbackToggle.requestFocus();
              break;
         case 1:
              if (list != null && list.getCount() > 0) list.requestFocus();
              else if (searchButton != null) searchButton.requestFocus();
             break;
          case 2: if (playbackToggle != null) playbackToggle.requestFocus(); break;
         case 3: if (seekBar != null) seekBar.requestFocus(); break;
         case 4: openSearch(); break;
     }
 }
 private void wireNavigation() {
     if (tabBar == null || tabBar.getChildCount() < 3) return;
     View listen = tabBar.getChildAt(0);
     View library = tabBar.getChildAt(1);
     View nowPlaying = tabBar.getChildAt(2);
     listen.setNextFocusDownId(playbackToggle.getId());
     library.setNextFocusDownId(list.getId());
     nowPlaying.setNextFocusDownId(seekBar.getId());
     list.setNextFocusUpId(library.getId());
     seekBar.setNextFocusUpId(nowPlaying.getId());
     playbackToggle.setNextFocusUpId(seekBar.getId());
     nowPlaying.setNextFocusRightId(searchButton.getId());
     searchButton.setNextFocusLeftId(nowPlaying.getId());
 }
 private void openSearch() {
     if (searchInput == null) return;
     searchOpen = true;
     if (tabBar != null) tabBar.setVisibility(View.GONE);
     searchInput.setVisibility(View.VISIBLE);
     searchInput.requestFocus();
     searchInput.setSelection(searchInput.length());
     searchInput.post(new Runnable() {
         @Override public void run() {
             InputMethodManager input = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
             if (input != null) input.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
         }
     });
 }
 private void closeSearch() {
     if (searchInput == null) return;
     hideSearchKeyboard();
     searchOpen = false;
     if (tabBar != null) tabBar.setVisibility(View.VISIBLE);
     searchInput.setVisibility(View.GONE);
     if (searchInput.length() > 0) searchInput.setText("");
     else {
         searchQuery = "";
         applyFilter();
     }
     if (searchButton != null) searchButton.requestFocus();
 }
 private void hideSearchKeyboard() {
     if (searchInput == null) return;
     InputMethodManager input = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
     if (input != null) input.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
 }
 private Drawable focusPill() {
     StateListDrawable states = new StateListDrawable();
     states.addState(new int[]{android.R.attr.state_focused},
             roundRect(0xffe6c878, 0xffffe8ac, dp(18), dp(2)));
     states.addState(new int[]{}, roundRect(0xaa171b20, 0x44fff0d2, dp(18), dp(1)));
     return states;
 }
 private Drawable glassCard() {
     GradientDrawable glass = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
             new int[]{0xf0222428, 0xf0181c21, 0xf014181e});
     glass.setCornerRadius(dp(22));
     glass.setStroke(dp(1), 0x66dec27f);
     return glass;
 }
 private void rebuildRecommendations() {
     if (recommendationRow == null) return;
     recommendationRow.removeAllViews();
     List<Integer> order = new ArrayList<Integer>();
     for (int source : visibleTracks) if (isFavorite(tracks.get(source))) order.add(source);
     for (int source : visibleTracks) if (!isFavorite(tracks.get(source))) order.add(source);
     int shown = Math.min(MAX_SHORTCUTS, order.size());
     if (shown == 0) {
         TextView empty = label("Nenhuma faixa local encontrada", 14, MUTED, false);
         recommendationRow.addView(empty, new LinearLayout.LayoutParams(dp(300), -1));
         // No cards: restore list → seek wiring so the D-pad never points at removed view IDs.
         list.setNextFocusDownId(seekBar.getId());
         seekBar.setNextFocusUpId(list.getId());
         if (tabBar != null && tabBar.getChildCount() > 1)
             tabBar.getChildAt(1).setNextFocusDownId(list.getId());
         return;
     }
     for (int i = 0; i < shown; i++) {
         final int sourceIndex = order.get(i);
         Track track = tracks.get(sourceIndex);
         LinearLayout card = new LinearLayout(this);
         card.setOrientation(LinearLayout.HORIZONTAL);
         card.setGravity(Gravity.CENTER_VERTICAL);
         card.setPadding(dp(11), dp(7), dp(11), dp(7));
         card.setBackground(roundRect(0xe8171b20, 0x55fff0d2, dp(16), dp(1)));
         card.setFocusable(true);
         card.setId(View.generateViewId());
          LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(218), dp(74));
         cardParams.rightMargin = dp(10);
         recommendationRow.addView(card, cardParams);
         FrameLayout miniArt = new FrameLayout(this);
         GradientDrawable miniBackground = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                 coverColors(i));
         miniBackground.setCornerRadius(dp(12));
         miniBackground.setStroke(dp(1), 0x88fff0cc);
         miniArt.setBackground(miniBackground);
         ImageView cover = new ImageView(this);
         cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
         miniArt.addView(cover, new FrameLayout.LayoutParams(-1, -1));
         TextView glyph = label(isFavorite(track) ? "♥" : "♪", 26, 0xffffe7ab, true);
         glyph.setTypeface(Typeface.DEFAULT);
         glyph.setGravity(Gravity.CENTER);
         miniArt.addView(glyph, new FrameLayout.LayoutParams(-1, -1));
         card.addView(miniArt, new LinearLayout.LayoutParams(dp(54), dp(54)));
         bindArtwork(cover, glyph, track, 160);
         LinearLayout text = new LinearLayout(this);
         text.setOrientation(LinearLayout.VERTICAL);
         text.setGravity(Gravity.CENTER_VERTICAL);
         LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -1, 1);
         textParams.leftMargin = dp(11);
         card.addView(text, textParams);
          TextView title = label(track.title.length() == 0 ? "Faixa sem título" : track.title,
                  15, WHITE, true);
          title.setMaxLines(2);
         title.setEllipsize(android.text.TextUtils.TruncateAt.END);
          text.addView(title, new LinearLayout.LayoutParams(-1, dp(36)));
         TextView artist = label(sourceIndex < artists.size() ? artists.get(sourceIndex) : "Biblioteca local",
                 13, 0xffc9c3b8, false);
         artist.setMaxLines(1);
         artist.setEllipsize(android.text.TextUtils.TruncateAt.END);
          text.addView(artist, new LinearLayout.LayoutParams(-1, dp(18)));
         card.setOnClickListener(v -> playAt(sourceIndex));
         card.setOnFocusChangeListener((v, focused) -> {
             if (focused) card.setBackground(roundRect(0xffe6c878, 0xffffe8ac, dp(16), dp(2)));
             else card.setBackground(roundRect(0xe8171b20, 0x55fff0d2, dp(16), dp(1)));
             title.setTextColor(focused ? 0xff211b12 : WHITE);
             artist.setTextColor(focused ? 0xff55441f : 0xffc9c3b8);
             applyFocusScale(card, focused);
         });
         if (i == 0) card.setNextFocusUpId(list.getId());
         card.setNextFocusDownId(seekBar == null ? View.NO_ID : seekBar.getId());
     }
     if (recommendationRow.getChildCount() > 0) {
         View first = recommendationRow.getChildAt(0);
         list.setNextFocusDownId(first.getId());
         seekBar.setNextFocusUpId(first.getId());
         if (tabBar != null && tabBar.getChildCount() > 1)
             tabBar.getChildAt(1).setNextFocusDownId(first.getId());
     }
 }
 private void applyFocusScale(View view, boolean focused) {
     view.animate().cancel();
     view.animate().scaleX(focused ? 1.035f : 1f).scaleY(focused ? 1.035f : 1f)
             .setDuration(120L).start();
 }
  private Drawable headerGradient(int topColor) {
      return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
              new int[]{topColor, 0xa30a0d12, 0xb30a0d12});
  }
  /** Samples the loaded hero cover only; keeps the tint subtle and PhilcoOS gold. */
  private int dominantWarmColor(Bitmap bitmap) {
      final int fallback = 0xaa2a2118;
      if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0)
          return fallback;
      try {
          int r = 0, g = 0, b = 0, count = 0;
          int stepX = Math.max(1, bitmap.getWidth() / 8);
          int stepY = Math.max(1, bitmap.getHeight() / 8);
          for (int y = 0; y < bitmap.getHeight(); y += stepY) {
              for (int x = 0; x < bitmap.getWidth(); x += stepX) {
                  int pixel = bitmap.getPixel(x, y);
                  r += Color.red(pixel);
                  g += Color.green(pixel);
                  b += Color.blue(pixel);
                  count++;
              }
          }
          if (count == 0) return fallback;
          r /= count; g /= count; b /= count;
          r = (int)(r * 0.30f); g = (int)(g * 0.30f); b = (int)(b * 0.30f);
          r = (int)(r * 0.82f + 0xd9 * 0.18f);
          g = (int)(g * 0.82f + 0xad * 0.18f);
          b = (int)(b * 0.82f + 0x55 * 0.18f);
          return Color.argb(0xaa, Math.min(72, r), Math.min(60, g), Math.min(46, b));
      } catch (RuntimeException ignored) {
          return fallback;
      }
  }
  private void updateHeaderTint(Bitmap bitmap) {
      if (headerScrim != null) {
          headerScrim.setBackground(headerGradient(dominantWarmColor(bitmap)));
      }
  }
 private int[] coverColors(int seed) {
     switch (seed % 5) {
         case 0: return new int[]{0xffe2b450, 0xff8b4d29, 0xff25263d};
         case 1: return new int[]{0xffd67f56, 0xff68455a, 0xff25283f};
         case 2: return new int[]{0xffd8c28a, 0xff7d6945, 0xff293344};
         case 3: return new int[]{0xffbd795b, 0xff5b4b4f, 0xff202c3d};
         default: return new int[]{0xffd5ad6b, 0xff755239, 0xff303349};
     }
 }
 private String artworkKey(Track track, int targetPixels) {
     if (track == null) return "";
     String identity = track.albumId >= 0 ? "album:" + track.albumId : track.uri.toString();
     return identity + ":" + targetPixels;
 }
 private void bindArtwork(ImageView target, View fallback, Track track, int targetPixels) {
     bindArtwork(target, fallback, track, targetPixels, true);
 }
 private void bindArtwork(ImageView target, View fallback, Track track, int targetPixels,
         boolean allowEmbeddedMetadata) {
     if (target == null || fallback == null) return;
     if (track == null) {
         target.setImageDrawable(null);
         target.setTag(null);
         fallback.setVisibility(View.VISIBLE);
          if (target == heroArtwork) updateHeaderTint(null);
         return;
     }
     final String key = artworkKey(track, targetPixels);
     target.setTag(key);
     Bitmap cached = artworkCache == null ? null : artworkCache.get(key);
     if (cached != null && !cached.isRecycled()) {
         target.setImageBitmap(cached);
         fallback.setVisibility(View.GONE);
          if (target == heroArtwork) updateHeaderTint(cached);
         return;
     }
     target.setImageDrawable(null);
     fallback.setVisibility(View.VISIBLE);
      if (target == heroArtwork) updateHeaderTint(null);
     if (artworkExecutor == null || artworkExecutor.isShutdown()) return;
     final Track requested = track;
     artworkExecutor.execute(() -> {
         Bitmap bitmap = loadArtwork(requested, targetPixels, allowEmbeddedMetadata);
         if (bitmap == null) return;
         if (artworkCache != null) artworkCache.put(key, bitmap);
         handler.post(() -> {
             if (isFinishing() || isDestroyed() || target.getTag() == null
                     || !key.equals(target.getTag())) return;
             target.setImageBitmap(bitmap);
             fallback.setVisibility(View.GONE);
              if (target == heroArtwork) updateHeaderTint(bitmap);
         });
     });
 }
 private Bitmap loadArtwork(Track track, int targetPixels, boolean allowEmbeddedMetadata) {
     if (track.albumId >= 0) {
         android.net.Uri albumArt = android.content.ContentUris.withAppendedId(
                 android.net.Uri.parse("content://media/external/audio/albumart"), track.albumId);
         Bitmap bitmap = decodeArtwork(albumArt, targetPixels);
         if (bitmap != null) return bitmap;
     }
     if (!allowEmbeddedMetadata) return null;
     byte[] embedded = null;
     MediaMetadataRetriever retriever = new MediaMetadataRetriever();
     try {
         retriever.setDataSource(this, track.uri);
         embedded = retriever.getEmbeddedPicture();
     } catch (Exception ignored) {
     } finally {
         try { retriever.release(); } catch (Exception ignored) { }
     }
     if (embedded == null || embedded.length == 0) return null;
     BitmapFactory.Options bounds = new BitmapFactory.Options();
     bounds.inJustDecodeBounds = true;
     BitmapFactory.decodeByteArray(embedded, 0, embedded.length, bounds);
     if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
     BitmapFactory.Options options = new BitmapFactory.Options();
     options.inSampleSize = sampleFor(bounds.outWidth, bounds.outHeight, targetPixels);
     options.inPreferredConfig = Bitmap.Config.RGB_565;
     try { return BitmapFactory.decodeByteArray(embedded, 0, embedded.length, options); }
     catch (OutOfMemoryError ignored) { return null; }
 }
 private Bitmap decodeArtwork(android.net.Uri uri, int targetPixels) {
     try {
         BitmapFactory.Options bounds = new BitmapFactory.Options();
         bounds.inJustDecodeBounds = true;
         java.io.InputStream first = getContentResolver().openInputStream(uri);
         if (first == null) return null;
         try { BitmapFactory.decodeStream(first, null, bounds); }
         finally { first.close(); }
         if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inSampleSize = sampleFor(bounds.outWidth, bounds.outHeight, targetPixels);
         options.inPreferredConfig = Bitmap.Config.RGB_565;
         java.io.InputStream second = getContentResolver().openInputStream(uri);
         if (second == null) return null;
         try { return BitmapFactory.decodeStream(second, null, options); }
         finally { second.close(); }
     } catch (Exception ignored) { return null; }
     catch (OutOfMemoryError ignored) { return null; }
 }
 private int sampleFor(int width, int height, int targetPixels) {
     int sample = 1;
     while (width / (sample * 2) >= targetPixels && height / (sample * 2) >= targetPixels)
         sample *= 2;
     return sample;
 }
 private void applyFilter() {
     visibleTracks.clear();
     String query = normalize(searchQuery);
     for (int i = 0; i < tracks.size(); i++)
         if (query.length() == 0 || (i < searchKeys.size() && searchKeys.get(i).contains(query)))
             visibleTracks.add(i);
     if (adapter != null) adapter.notifyDataSetChanged();
     if (libraryCount != null) libraryCount.setText(visibleTracks.size()
             + (visibleTracks.size() == 1 ? " FAIXA" : " FAIXAS"));
     if (libraryEmptyState != null && visibleTracks.isEmpty()) {
         if (libraryLoadFailed) {
             libraryEmptyState.setText("Não foi possível ler a biblioteca.\nConfira o armazenamento USB.");
         } else if (tracks.isEmpty()) {
             libraryEmptyState.setText("Biblioteca vazia.\nAdicione músicas ao armazenamento USB.");
         } else {
             libraryEmptyState.setText("Nenhuma música encontrada para a busca.");
         }
     }
     rebuildRecommendations();
     if (tabBar != null && tabBar.getChildCount() > 1 && recommendationRow != null
             && recommendationRow.getChildCount() == 0)
         tabBar.getChildAt(1).setNextFocusDownId(list.getId());
 }
 private static String normalize(String value) {
     if (value == null) return "";
     return Normalizer.normalize(value, Normalizer.Form.NFD)
             .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
 }
 private void playVisibleAt(int visibleIndex) {
     if (visibleIndex >= 0 && visibleIndex < visibleTracks.size()) playAt(visibleTracks.get(visibleIndex));
 }
 private void togglePlayback() {
     PlaybackState state = controller.getState();
     switch (state.status) {
         case PLAYING:
             controller.pause();
             break;
         case PAUSED:
             controller.resume();
             break;
         case PREPARING:
             break; // already starting; a second command would only restart the preparation
         default: // IDLE, STOPPED, COMPLETED, ERROR: the service has no prepared player to resume
             if (state.track != null) {
                 int current = indexOf(state.track);
                 if (current >= 0) startTrack(current);
                 else if (serviceReady) controller.play(state.track);
             } else {
                 int start = startIndex();
                 if (start >= 0) playAt(start);
             }
             break;
     }
     showState();
 }
 private int startIndex() {
     int selected = list == null ? -1 : list.getSelectedItemPosition();
     if (selected >= 0 && selected < visibleTracks.size()) return visibleTracks.get(selected);
     if (restoredIndex >= 0 && restoredIndex < tracks.size()) return restoredIndex;
     return visibleTracks.isEmpty() ? -1 : visibleTracks.get(0);
 }
 private void updateClock() {
     if (clock == null) return;
     java.util.Calendar now = java.util.Calendar.getInstance(
             java.util.TimeZone.getTimeZone("Etc/GMT+3"));
     clock.setText(String.format(Locale.getDefault(), "%02d:%02d",
             now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE)));
 }
 private void updateVolumeUi() {
     if (audioManager == null) return;
     int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
     int maximum = Math.max(1, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
     int percent = Math.max(0, Math.min(100, Math.round(current * 100f / maximum)));
     boolean muted = current == 0;
     if (volumeGlyph != null) {
         volumeGlyph.setMuted(muted);
         volumeGlyph.setLevel(muted ? 0 : (percent < 50 ? 2 : 4));
     }
     if (volumeValue != null) volumeValue.setText(percent + "%");
     if (volumeButton != null) {
         volumeButton.setText("Volume " + percent + "%");
         volumeButton.setContentDescription("Volume " + percent + " por cento. OK abre o ajuste de volume");
     }
     if (volumeSlider != null) {
         if (volumeSlider.getMax() != maximum) volumeSlider.setMax(maximum);
         if (!volumeSlider.isPressed()) volumeSlider.setProgress(current);
     }
 }
 private void changeVolume(int direction, int steps) {
     if (audioManager == null) return;
     try {
         int maximum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
         int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
         SharedPreferences homePrefs = getSharedPreferences("philco_home", MODE_PRIVATE);
         int remembered = homePrefs.getInt("media_mute_previous_volume",
                 homePrefs.getInt(PREF_LAST_AUDIBLE_VOLUME, lastAudibleVolume));
          if (direction > 0 && current == 0
                 && (homePrefs.getBoolean("media_mute_active", false) || remembered > 0)) {
             int restore = remembered;
             if (restore <= 0) restore = Math.max(1, maximum / 2);
             setMediaVolumeIndex(restore);
             rememberAudibleVolume(restore);
             updateVolumeUi();
             return;
         }
          int delta = Math.max(1, steps);
          int target = Math.max(0, Math.min(maximum, current + (direction < 0 ? -delta : delta)));
         setMediaVolumeIndex(target);
         if (target > 0) rememberAudibleVolume(target);
         updateVolumeUi();
     } catch (Exception error) {
         Toast.makeText(this, "O volume da TV não está disponível", Toast.LENGTH_SHORT).show();
     }
 }
 private void toggleMute() {
     if (audioManager == null) return;
     try {
         int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
         if (current > 0) {
             rememberAudibleVolume(current);
             getSharedPreferences("philco_home", MODE_PRIVATE).edit()
                     .putInt("media_mute_previous_volume", current)
                     .putBoolean("media_mute_active", true).commit();
             setMediaVolumeIndex(0);
         } else {
             int restore = getSharedPreferences("philco_home", MODE_PRIVATE)
                     .getInt("media_mute_previous_volume",
                             getSharedPreferences("philco_home", MODE_PRIVATE)
                                     .getInt(PREF_LAST_AUDIBLE_VOLUME, lastAudibleVolume));
             if (restore <= 0) restore = Math.max(1, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2);
             setMediaVolumeIndex(restore);
             rememberAudibleVolume(restore);
             getSharedPreferences("philco_home", MODE_PRIVATE).edit()
                     .putBoolean("media_mute_active", false).remove("media_mute_previous_volume").commit();
         }
         updateVolumeUi();
     } catch (Exception error) {
         Toast.makeText(this, "O volume da TV não está disponível", Toast.LENGTH_SHORT).show();
     }
 }
 private void rememberAudibleVolume(int index) {
     if (index <= 0) return;
     lastAudibleVolume = index;
     getSharedPreferences("philco_home", MODE_PRIVATE).edit()
             .putInt(PREF_LAST_AUDIBLE_VOLUME, index).apply();
 }
 private void setMediaVolumeIndex(int index) throws Exception {
     int maximum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
     int bounded = Math.max(0, Math.min(maximum, index));
     try {
         audioManager.getClass().getMethod("setStreamVolumeForL4300", Integer.TYPE, Integer.TYPE,
                 Integer.TYPE, Boolean.TYPE).invoke(audioManager, Integer.valueOf(AudioManager.STREAM_MUSIC),
                 Integer.valueOf(bounded), Integer.valueOf(0), Boolean.TRUE);
     } catch (NoSuchMethodException unavailable) {
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, bounded, 0);
     }
     if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != bounded)
         throw new IllegalStateException("Volume readback mismatch");
     if (bounded > 0) clearPersistedMute();
 }
 /** Raising the volume by any path ends a persisted "mute" so Home never restores a stale one. */
 private void clearPersistedMute() {
     SharedPreferences home = getSharedPreferences("philco_home", MODE_PRIVATE);
     if (home.getBoolean("media_mute_active", false))
         home.edit().putBoolean("media_mute_active", false)
                 .remove("media_mute_previous_volume").apply();
 }
 private LinearLayout.LayoutParams actionParams(int width, int height, int margin) {
     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
     params.leftMargin = margin;
     params.rightMargin = margin;
     return params;
 }
 private ActionButton action(String glyph, String caption, boolean primary) {
     ActionButton button = new ActionButton(this, glyph, caption, primary);
     button.setId(View.generateViewId());
     button.setFocusable(true);
     button.setFocusableInTouchMode(true);
     button.setOnKeyListener((view, keyCode, event) -> {
         if (event.getAction() == KeyEvent.ACTION_DOWN
                 && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
             view.performClick();
             return true;
         }
         return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER;
     });
     return button;
 }
 private TextView label(String value, int size, int color, boolean bold) {
     TextView view = new TextView(this);
     view.setText(value);
     view.setTextSize(size);
     view.setTextColor(color);
     if (bold) view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
     view.setGravity(Gravity.CENTER_VERTICAL);
     return view;
 }
 private Drawable roundRect(int color, int strokeColor, int radius, int strokeWidth) {
     GradientDrawable shape = new GradientDrawable();
     shape.setColor(color);
     shape.setCornerRadius(radius);
     if (strokeWidth > 0) shape.setStroke(strokeWidth, strokeColor);
     return shape;
 }
 private Drawable trackSelector() {
     StateListDrawable states = new StateListDrawable();
     states.addState(new int[]{android.R.attr.state_focused},
             roundRect(0x44d4af37, GOLD, dp(11), dp(2)));
     states.addState(new int[]{android.R.attr.state_selected},
             roundRect(0x22d4af37, 0x88d4af37, dp(11), dp(1)));
     states.addState(new int[]{}, roundRect(0x12000000, 0x00000000, dp(11), 0));
     return states;
 }
 private Drawable goldProgressDrawable() {
     GradientDrawable background = new GradientDrawable();
     background.setColor(0x66ffffff);
     background.setCornerRadius(dp(3));
     GradientDrawable progress = new GradientDrawable();
     progress.setColor(GOLD);
     progress.setCornerRadius(dp(3));
     ClipDrawable clipped = new ClipDrawable(progress, Gravity.LEFT, ClipDrawable.HORIZONTAL);
     LayerDrawable layers = new LayerDrawable(new Drawable[]{background, clipped});
     layers.setId(0, android.R.id.background);
     layers.setId(1, android.R.id.progress);
     layers.setLayerInset(0, 0, dp(8), 0, dp(8));
     layers.setLayerInset(1, 0, dp(8), 0, dp(8));
     return layers;
 }
 private Drawable goldThumb() {
     GradientDrawable thumb = new GradientDrawable();
     thumb.setShape(GradientDrawable.OVAL);
     thumb.setColor(GOLD);
     thumb.setStroke(dp(2), 0xfff5e4a7);
     thumb.setSize(dp(18), dp(18));
     return thumb;
 }
 private void loadTracks() {
     if (loading) { reloadPending = true; return; }
     loading = true;
     new Thread(() -> {
         final List<Track> newTracks = new ArrayList<Track>();
         final List<String> newArtists = new ArrayList<String>();
         final List<Long> newDurations = new ArrayList<Long>();
         final List<String> newSearchKeys = new ArrayList<String>();
         boolean failed = false;
         HashMap<Long, String> genresByAudioId = loadGenresByAudioId();
         String[] columns = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                 MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
                 MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
                 MediaStore.Audio.Media.DATA};
         Cursor cursor = null;
         try {
             cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                     columns, MediaStore.Audio.Media.IS_MUSIC + "!=0", null,
                     MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
             if (cursor == null) throw new IllegalStateException("MediaStore returned no cursor");
             while (cursor.moveToNext()) {
                 long id = cursor.getLong(0);
                 String title = cursor.getString(1), artist = cursor.getString(2);
                 long length = cursor.getLong(3);
                 String album = cursor.isNull(4) ? "" : cursor.getString(4);
                 long albumId = cursor.isNull(5) ? -1L : cursor.getLong(5);
                 String filePath = cursor.isNull(6) ? "" : cursor.getString(6);
                 String[] filenameParts = parseFilename(filePath);
                 if (isUnknown(title) && filenameParts[0].length() > 0) title = filenameParts[0];
                 if (isUnknown(artist) && filenameParts[1].length() > 0) artist = filenameParts[1];
                 String genre = genresByAudioId.get(Long.valueOf(id));
                 if (genre == null) genre = "";
                 android.net.Uri trackUri = android.content.ContentUris.withAppendedId(
                         MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                 // Opening a retriever for every song is expensive on the TV. Use it only
                 // when MediaStore and the filename could not provide display metadata.
                 if (isUnknown(title) || isUnknown(artist) || isUnknown(album)) {
                     String[] embedded = readAudioMetadata(trackUri);
                     if (isUnknown(title) && !isUnknown(embedded[0])) title = embedded[0];
                     if (isUnknown(artist) && !isUnknown(embedded[1])) artist = embedded[1];
                     if (isUnknown(album) && !isUnknown(embedded[2])) album = embedded[2];
                     if (isUnknown(genre)) genre = embedded[3];
                 }
                 title = stripArtistPrefix(title, artist);
                 String displayTitle = isUnknown(title) ? "Faixa sem título" : title.trim();
                 String displayArtist = isUnknown(artist) ? "Artista desconhecido" : artist.trim();
                 Track track = new Track(trackUri, displayTitle, displayArtist, album, genre, albumId);
                 newTracks.add(track);
                 newArtists.add(displayArtist);
                 newDurations.add(length);
                 StringBuilder key = new StringBuilder(track.title).append(' ').append(track.artist)
                         .append(' ').append(track.album).append(' ').append(track.genre);
                 for (String tag : track.tags) key.append(' ').append(tag);
                 newSearchKeys.add(normalize(key.toString()));
             }
         } catch (Exception error) {
             failed = true;
             newTracks.clear();
             newArtists.clear();
             newDurations.clear();
             newSearchKeys.clear();
         } finally {
             if (cursor != null) {
                 try { cursor.close(); } catch (Exception ignored) { }
             }
         }
         final boolean loadFailed = failed;
         runOnUiThread(() -> {
             loading = false;
             if (isFinishing() || isDestroyed()) return;
             // An identical re-read (media scanner chatter) must not disturb focus or scroll position.
             boolean unchanged = !loadFailed && !firstLoad && !libraryLoadFailed
                     && sameLibrary(newTracks, newArtists, newDurations);
             if (!loadFailed) {
                 if (!unchanged) {
                     tracks.clear(); tracks.addAll(newTracks);
                     artists.clear(); artists.addAll(newArtists);
                     durations.clear(); durations.addAll(newDurations);
                     searchKeys.clear(); searchKeys.addAll(newSearchKeys);
                     indexByUri.clear();
                     for (int i = 0; i < tracks.size(); i++)
                         indexByUri.put(tracks.get(i).uri.toString(), Integer.valueOf(i));
                 }
                 libraryLoadFailed = false;
                 libraryErrorShown = false;
             } else {
                 libraryLoadFailed = true;
                 if (tracks.isEmpty() && !libraryErrorShown) {
                     libraryErrorShown = true;
                     Toast.makeText(this, "Não foi possível ler a biblioteca local", Toast.LENGTH_SHORT).show();
                 }
             }
             if (!unchanged) {
                 applyFilter();
                 if (tracks.isEmpty()) {
                     currentTitle.setText(libraryLoadFailed ? "Falha ao abrir biblioteca" : "Biblioteca vazia");
                     currentArtist.setText(libraryLoadFailed ? "Confira o armazenamento USB" : "Adicione músicas ao armazenamento USB");
                 } else {
                     restoreLastSelection(firstLoad);
                     if (firstLoad && list.getCount() > 0) list.requestFocus();
                 }
                 showState();
             }
              if (serviceReady) syncPlaybackQueue();
             firstLoad = false;
             if (reloadPending) {
                 reloadPending = false;
                 loadTracks();
             }
         });
     }, "PhilcoMusicLibrary").start();
 }
 private static boolean isUnknown(String value) {
     return value == null || value.trim().length() == 0
             || "<unknown>".equalsIgnoreCase(value.trim())
             || "unknown".equalsIgnoreCase(value.trim())
             || "Artista desconhecido".equalsIgnoreCase(value.trim())
             || "Faixa sem título".equalsIgnoreCase(value.trim());
 }
 private String[] readAudioMetadata(android.net.Uri uri) {
     String[] values = new String[] {"", "", "", ""};
     MediaMetadataRetriever retriever = new MediaMetadataRetriever();
     try {
         retriever.setDataSource(this, uri);
         values[0] = safeMetadata(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
         values[1] = safeMetadata(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
         values[2] = safeMetadata(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
         try { values[3] = safeMetadata(retriever.extractMetadata(20)); }
         catch (Exception ignored) { }
     } catch (Exception ignored) {
         // Providers/codecs without embedded tags keep the MediaStore and filename values.
     } finally {
         try { retriever.release(); } catch (Exception ignored) { }
     }
     return values;
 }
 private static String safeMetadata(String value) {
     return isUnknown(value) ? "" : value.trim();
 }
 private static String stripArtistPrefix(String title, String artist) {
     if (isUnknown(title) || isUnknown(artist)) return title;
     String prefix = artist.trim() + " - ";
     return title.regionMatches(true, 0, prefix, 0, prefix.length())
             ? title.substring(prefix.length()).trim() : title;
 }
 private static String[] parseFilename(String path) {
     if (path == null || path.length() == 0) return new String[] {"", ""};
     String name = path.substring(path.lastIndexOf('/') + 1)
             .replaceFirst("(?i)\\.(mp3|m4a|aac|flac|ogg|opus|wav|wma|3gp)$", "").trim();
     int separator = name.indexOf(" - ");
     if (separator > 0 && separator < name.length() - 3)
         return new String[] {name.substring(separator + 3).trim(), name.substring(0, separator).trim()};
     return new String[] {name, ""};
 }
 private HashMap<Long, String> loadGenresByAudioId() {
     HashMap<Long, String> result = new HashMap<Long, String>();
     Cursor genres = null;
     try {
         genres = getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                 new String[]{MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME}, null, null, null);
         if (genres == null) return result;
         while (genres.moveToNext()) {
             long genreId = genres.getLong(0);
             String name = safeMetadata(genres.getString(1));
             if (name.length() == 0) continue;
             Cursor members = null;
             try {
                 android.net.Uri membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
                 members = getContentResolver().query(membersUri,
                         new String[]{MediaStore.Audio.Genres.Members.AUDIO_ID}, null, null, null);
                 if (members == null) continue;
                 while (members.moveToNext()) {
                     Long audioId = Long.valueOf(members.getLong(0));
                     String previous = result.get(audioId);
                     result.put(audioId, previous == null ? name : previous + ", " + name);
                 }
             } catch (Exception ignored) {
                 // A vendor MediaStore may expose genre rows but not member queries.
             } finally {
                 if (members != null) try { members.close(); } catch (Exception ignored) { }
             }
         }
     } catch (Exception ignored) {
         // Genre tables are optional in vendor MediaStore implementations.
     } finally {
         if (genres != null) try { genres.close(); } catch (Exception ignored) { }
     }
     return result;
 }
 private void restoreLastSelection(boolean moveSelection) {
     restoredIndex = -1;
     PlaybackState state = controller.getState();
     if (state.track != null) return;
     String lastUri = playerPreferences.getString("last_uri", null);
     if (lastUri == null) return;
     for (int i = 0; i < tracks.size(); i++) {
         if (tracks.get(i).uri.toString().equals(lastUri)) { restoredIndex = i; break; }
     }
     if (restoredIndex >= 0) {
         currentTitle.setText(tracks.get(restoredIndex).title.length() == 0
                 ? "Faixa sem título" : tracks.get(restoredIndex).title);
         currentArtist.setText(artists.get(restoredIndex));
         int visibleIndex = visibleTracks.indexOf(Integer.valueOf(restoredIndex));
         if (moveSelection && visibleIndex >= 0) list.setSelection(visibleIndex);
     }
 }
 private int currentIndex() {
     return indexOf(controller.getState().track);
 }
  private void syncPlaybackQueue() {
      if (controller != null) controller.setQueue(tracks, shuffleEnabled, repeatMode);
  }
 private void playAt(int index) {
     if (index < 0 || index >= tracks.size()) return;
      startTrack(index, true);
 }
 private void startTrack(int index) {
      startTrack(index, true);
  }
  private void startTrack(int index, boolean rememberCurrent) {
     if (index < 0 || index >= tracks.size()) return;
     completionHandled = false;
     playbackErrorShown = false;
     restoredIndex = -1;
     Track track = tracks.get(index);
      if (serviceReady) {
          syncPlaybackQueue();
          controller.playQueueIndex(index, rememberCurrent);
      } else {
          pendingTrack = track;
          pendingRememberCurrent = rememberCurrent; // played as soon as the service connection is ready
      }
     playerPreferences.edit().putString("last_uri", track.uri.toString()).apply();
     int visibleIndex = visibleTracks.indexOf(Integer.valueOf(index));
     if (visibleIndex >= 0 && !list.hasFocus()) list.setSelection(visibleIndex);
     showState();
 }
 private void seekToProgress(int progress, int max) {
     PlaybackState state = controller.getState();
     if (state.durationMs > 0 && max > 0)
         controller.seekTo((int)((long)progress * state.durationMs / max));
 }
 private void showState() {
     if (currentTitle == null || controller == null || showingState) return;
     showingState = true; // startTrack() calls showState(); never re-enter while rendering
     try {
         renderState();
     } finally {
         showingState = false;
     }
 }
 private void renderState() {
     PlaybackState state = controller.getState();
     if (serviceReady && state.status == PlaybackState.Status.COMPLETED && !completionHandled) {
         completionHandled = true;
         int index = currentIndex();
         if (repeatMode == 2) {
             if (index >= 0) startTrack(index);
         } else if ((shuffleEnabled && tracks.size() > 1) || repeatMode == 1
                 || (index >= 0 && index < tracks.size() - 1)) {
             playNext();
         }
         state = controller.getState();
     } else if (state.status == PlaybackState.Status.PLAYING
             || state.status == PlaybackState.Status.PREPARING) {
         completionHandled = false;
     }
     String stateText;
     switch (state.status) {
         case PREPARING: stateText = "PREPARANDO"; break;
         case PLAYING: stateText = "TOCANDO"; break;
         case PAUSED: stateText = "PAUSADO"; break;
         case STOPPED: case COMPLETED: stateText = "FINALIZADA"; break;
         case ERROR: stateText = "ERRO AO REPRODUZIR"; break;
         default: stateText = "PRONTO"; break;
     }
     setTextIfChanged(status, stateText);
     boolean failed = state.status == PlaybackState.Status.ERROR;
     int statusColor = failed ? 0xffff8a80 : GOLD;
     if (status.getCurrentTextColor() != statusColor) status.setTextColor(statusColor);
     if (failed) {
         if (!playbackErrorShown) {
             playbackErrorShown = true;
             Toast.makeText(this, "Não foi possível reproduzir esta faixa", Toast.LENGTH_SHORT).show();
         }
     } else {
         playbackErrorShown = false;
     }
     setKeepAwake(state.status == PlaybackState.Status.PLAYING
             || state.status == PlaybackState.Status.PREPARING);
     if (state.track != null) {
         int index = indexOf(state.track);
         String title = state.track.title == null || state.track.title.length() == 0
                 ? "Faixa sem título" : state.track.title;
         String artist = state.track.artist == null || state.track.artist.length() == 0
                 ? "Biblioteca local" : state.track.artist;
         setTextIfChanged(playerTitle, title);
         setTextIfChanged(playerArtist, artist);
         updateFavoriteUi(state.track);
         favoriteButton.setEnabled(true);
         favoriteButton.setAlpha(1f);
         if (renderedTrack == null || !renderedTrack.uri.equals(state.track.uri)) {
             playerPreferences.edit().putString("last_uri", state.track.uri.toString()).apply();
             setTextIfChanged(currentTitle, title);
             setTextIfChanged(currentArtist, artist);
             bindArtwork(heroArtwork, heroArtworkFallback, state.track, 480);
             bindArtwork(miniArtwork, miniArtworkFallback, state.track, 160);
             int visibleIndex = visibleTracks.indexOf(Integer.valueOf(index));
             // Do not yank the list selection away while the person is browsing it.
             if (visibleIndex >= 0 && !list.hasFocus()) list.setSelection(visibleIndex);
             renderedTrack = state.track;
             if (adapter != null) adapter.notifyDataSetChanged();
         }
     } else {
         bindArtwork(heroArtwork, heroArtworkFallback, null, 480);
         bindArtwork(miniArtwork, miniArtworkFallback, null, 160);
         if (favoriteButton != null) {
             favoriteButton.setEnabled(false);
             favoriteButton.setAlpha(0.45f);
         }
         if (renderedTrack != null) { // service cleared the track: drop the stale row highlight
             renderedTrack = null;
             if (adapter != null) adapter.notifyDataSetChanged();
         }
     }
     boolean isPlaying = state.status == PlaybackState.Status.PLAYING;
     setTextIfChanged(toggleIcon, isPlaying ? "Ⅱ" : "▶");
     setTextIfChanged(toggleLabel, isPlaying ? "PAUSAR" : "REPRODUZIR");
     setTextIfChanged(elapsed, formatTime(state.positionMs));
     setTextIfChanged(totalTime, formatTime(state.durationMs));
     seekBar.setEnabled(state.durationMs > 0);
     if (!userSeeking) seekBar.setProgress(state.durationMs > 0
             ? (int)((long)state.positionMs * seekBar.getMax() / state.durationMs) : 0);
 }
 private void setKeepAwake(boolean awake) {
     if (keepAwake == awake) return;
     keepAwake = awake;
     if (awake) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 }
 /** Returns the library index of the track, or -1 when it is not in the library. */
 private int indexOf(Track track) {
     return track == null ? -1 : indexOfUri(track.uri.toString());
 }
 private int indexOfUri(String uri) {
     Integer index = uri == null ? null : indexByUri.get(uri);
     return index == null ? -1 : index.intValue();
 }
 private boolean sameLibrary(List<Track> newTracks, List<String> newArtists, List<Long> newDurations) {
     if (newTracks.size() != tracks.size()) return false;
     for (int i = 0; i < newTracks.size(); i++) {
         Track fresh = newTracks.get(i), old = tracks.get(i);
         if (!fresh.uri.equals(old.uri) || !fresh.title.equals(old.title)
                 || !fresh.artist.equals(old.artist) || !fresh.album.equals(old.album)
                 || !fresh.genre.equals(old.genre) || fresh.albumId != old.albumId
                 || !fresh.tags.equals(old.tags)
                 || !newArtists.get(i).equals(artists.get(i))
                 || !newDurations.get(i).equals(durations.get(i))) return false;
     }
     return true;
 }
 private static void setTextIfChanged(TextView view, String text) {
     if (view != null && text != null && !text.contentEquals(view.getText())) view.setText(text);
 }
 private String formatTime(long milliseconds) {
     long seconds = Math.max(0, milliseconds) / 1000;
     return String.format(java.util.Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60);
 }
 private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
 private final class SearchIconView extends View {
     private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
     private final float scale;
     private int iconColor = GOLD_FOCUS;
     SearchIconView(Context context) {
         super(context);
         scale = getResources().getDisplayMetrics().density;
         setFocusable(true);
     }
     void setIconColor(int color) { iconColor = color; invalidate(); }
     @Override protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         float cx = getWidth() * 0.5f - 1.5f * scale;
         float cy = getHeight() * 0.5f - 1.5f * scale;
          paint.setColor(iconColor);
         paint.setStyle(Paint.Style.STROKE);
         paint.setStrokeWidth(2f * scale);
         paint.setStrokeCap(Paint.Cap.ROUND);
         canvas.drawCircle(cx, cy, 6.5f * scale, paint);
         canvas.drawLine(cx + 4.8f * scale, cy + 4.8f * scale,
                 cx + 10f * scale, cy + 10f * scale, paint);
     }
 }
 @Override protected void onStart() {
     super.onStart();
     handler.removeCallbacks(wallpaperRotation);
     handler.postDelayed(wallpaperRotation, MUSIC_WALLPAPER_ROTATE_MS);
     IntentFilter clockFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
     clockFilter.addAction(Intent.ACTION_TIME_CHANGED);
     clockFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
     registerReceiver(clockReceiver, clockFilter);
     clockReceiverRegistered = true;
     registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
     try {
         getContentResolver().registerContentObserver(
                 MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaObserver);
         observerRegistered = true;
     } catch (Exception ignored) { observerRegistered = false; }
     Intent intent = new Intent(this, MusicPlaybackService.class);
     startService(intent);
     bindRequested = bindService(intent, connection, Context.BIND_AUTO_CREATE);
     bound = bindRequested;
     handler.removeCallbacks(refresh);
     handler.post(refresh);
     if (!firstLoad) loadTracks(); // USB may have been plugged/unplugged while we were stopped
 }
 @Override protected void onStop() {
     handler.removeCallbacks(wallpaperRotation);
     if (transferDialog != null) transferDialog.dismiss();
     else if (transferServer != null) { transferServer.close(); transferServer = null; }
     hideVolumePopover(false);
     setSeekAdjustMode(false);
     handler.removeCallbacks(refresh);
     handler.removeCallbacks(reloadLibrary);
     if (observerRegistered) {
         try { getContentResolver().unregisterContentObserver(mediaObserver); } catch (Exception ignored) { }
         observerRegistered = false;
     }
     if (clockReceiverRegistered) {
         unregisterReceiver(clockReceiver);
         clockReceiverRegistered = false;
     }
     try { unregisterReceiver(volumeReceiver); } catch (IllegalArgumentException ignored) { }
     if (bindRequested) {
         try { unbindService(connection); } catch (IllegalArgumentException ignored) { }
         bindRequested = false;
     }
     bound = false;
     serviceReady = false;
     setKeepAwake(false);
     super.onStop();
 }
 @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
     if (keyCode == KeyEvent.KEYCODE_BACK) {
         if (searchOpen) {
             closeSearch();
         } else if (volumePopover != null && volumePopover.getVisibility() == View.VISIBLE) {
             hideVolumePopover(true);
          } else if (seekBar != null && seekBar.hasFocus() && seekAdjustMode) {
              setSeekAdjustMode(false);
              View returnFocus = seekReturnFocusId == View.NO_ID ? null
                      : getWindow().getDecorView().findViewById(seekReturnFocusId);
              if (returnFocus != null && returnFocus.getVisibility() == View.VISIBLE)
                  returnFocus.requestFocus();
              else if (playbackToggle != null) playbackToggle.requestFocus();
         } else if (volumeControl != null && volumeControl.hasFocus()) {
             if (volumeButton != null) volumeButton.requestFocus();
         } else if (seekBar != null && seekBar.hasFocus()) {
             if (playbackToggle != null) playbackToggle.requestFocus();
         } else {
             finish();
         }
         return true;
     }
     return super.onKeyDown(keyCode, event);
 }
 @Override public boolean dispatchKeyEvent(KeyEvent event) {
     int keyCode = event.getKeyCode();
     boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
     boolean firstPress = down && event.getRepeatCount() == 0;
     if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
         if (down) {
              int direction = keyCode == KeyEvent.KEYCODE_VOLUME_UP ? 1 : -1;
              changeVolume(direction, volumeStepsForRepeat(event.getRepeatCount()));
         }
         return true;
     }
     if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
         if (firstPress) toggleMute();
         return true;
     }
     if (!searchOpen) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                 if (firstPress) togglePlayback();
                 return true;
             case KeyEvent.KEYCODE_MEDIA_PLAY:
                 if (firstPress && controller.getState().status != PlaybackState.Status.PLAYING)
                     togglePlayback();
                 return true;
             case KeyEvent.KEYCODE_MEDIA_PAUSE:
                 if (firstPress && controller.getState().status == PlaybackState.Status.PLAYING)
                     togglePlayback();
                 return true;
             case KeyEvent.KEYCODE_MEDIA_NEXT:
                 if (firstPress) playNext();
                 return true;
             case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                 if (firstPress) playPrevious();
                 return true;
             case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                 if (down) seekRelative(SEEK_STEP_MS);
                 return true;
             case KeyEvent.KEYCODE_MEDIA_REWIND:
                 if (down) seekRelative(-SEEK_STEP_MS);
                 return true;
             default:
                 break;
         }
     }
     return super.dispatchKeyEvent(event);
 }
 private final class TrackAdapter extends BaseAdapter {
     @Override public int getCount() { return visibleTracks.size(); }
     @Override public Object getItem(int position) { return tracks.get(visibleTracks.get(position)); }
     @Override public long getItemId(int position) { return visibleTracks.get(position); }
     @Override public View getView(int position, View convertView, ViewGroup parent) {
         TrackRow row = convertView instanceof TrackRow ? (TrackRow)convertView : new TrackRow();
         int sourceIndex = visibleTracks.get(position);
         Track track = tracks.get(sourceIndex);
         boolean current = renderedTrack != null && renderedTrack.uri.equals(track.uri);
         row.number.setText(current ? "♪" : String.format(Locale.getDefault(), "%02d", sourceIndex + 1));
         row.title.setText(track.title.length() == 0 ? "Faixa sem título" : track.title);
         row.currentTrack = current;
         row.artist.setText(sourceIndex < artists.size() ? artists.get(sourceIndex) : "Artista desconhecido");
         row.favorite.setText(isFavorite(track) ? "♥" : "");
         row.length.setText(sourceIndex < durations.size() ? formatTime(durations.get(sourceIndex)) : "--:--");
         row.setHighlighted(row.hasFocus());
         bindArtwork(row.artwork, row.artFallback, track, 120, false);
         return row;
     }
 }
 private final class TrackRow extends LinearLayout {
     final TextView number, title, artist, favorite, length, artFallback;
     final ImageView artwork;
     boolean currentTrack;
     private boolean highlighted;
     TrackRow() {
         super(MusicActivity.this);
         setOrientation(HORIZONTAL);
         setGravity(Gravity.CENTER_VERTICAL);
         setPadding(dp(12), dp(5), dp(12), dp(5));
         number = label("01", 13, GOLD, true);
         number.setGravity(Gravity.CENTER);
         addView(number, new LinearLayout.LayoutParams(dp(30), -1));
         FrameLayout cover = new FrameLayout(MusicActivity.this);
         GradientDrawable coverBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                 new int[]{0xffa77a39, 0xff403541, 0xff1d2633});
         coverBg.setCornerRadius(dp(8));
         coverBg.setStroke(dp(1), 0x66e2bd67);
         cover.setBackground(coverBg);
         artwork = new ImageView(MusicActivity.this);
         artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
         cover.addView(artwork, new FrameLayout.LayoutParams(-1, -1));
         artFallback = MusicActivity.this.label("♪", 22, 0xffffe0a0, true);
         artFallback.setGravity(Gravity.CENTER);
         cover.addView(artFallback, new FrameLayout.LayoutParams(-1, -1));
         LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(dp(52), dp(52));
         coverParams.rightMargin = dp(10);
         addView(cover, coverParams);
         LinearLayout text = new LinearLayout(MusicActivity.this);
         text.setOrientation(VERTICAL);
         text.setGravity(Gravity.CENTER_VERTICAL);
         title = label("", 20, WHITE, true);
         title.setSingleLine(true);
         title.setEllipsize(android.text.TextUtils.TruncateAt.END);
         artist = label("", 15, MUTED, false);
         artist.setSingleLine(true);
         artist.setEllipsize(android.text.TextUtils.TruncateAt.END);
         text.addView(title, new LinearLayout.LayoutParams(-1, 0, 1));
         text.addView(artist, new LinearLayout.LayoutParams(-1, 0, 1));
         addView(text, new LinearLayout.LayoutParams(0, -1, 1));
         favorite = label("", 16, 0xffff9b83, true);
         favorite.setGravity(Gravity.CENTER);
         addView(favorite, new LinearLayout.LayoutParams(dp(30), -1));
         length = label("0:00", 13, MUTED, false);
         length.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
         addView(length, new LinearLayout.LayoutParams(dp(56), -1));
         setMinimumHeight(dp(68));
     }
     void setHighlighted(boolean highlighted) {
         this.highlighted = highlighted;
         setBackground(highlighted
                 ? roundRect(0xff373126, 0xffe1c36f, dp(10), dp(2))
                 : roundRect(Color.TRANSPARENT, Color.TRANSPARENT, dp(10), 0));
         title.setTextColor(highlighted ? 0xffffe3a0 : (currentTrack ? GOLD_FOCUS : WHITE));
         artist.setTextColor(highlighted ? 0xfff1e7d3 : MUTED);
         number.setTextColor(highlighted ? 0xffffd875 : GOLD);
         length.setTextColor(highlighted ? 0xfff1e7d3 : MUTED);
     }
     boolean isHighlighted() { return highlighted; }
 }
 private final class VolumeGlyph extends View {
     private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
     private final Path speaker = new Path();
     private final RectF firstWave = new RectF();
     private final RectF secondWave = new RectF();
     private int color;
     private int level = 2;
     private boolean muted;
     VolumeGlyph(Context context, int tint) {
         super(context);
         color = tint;
         setFocusable(false);
     }
     void setLevel(int value) {
         int bounded = Math.max(0, Math.min(4, value));
         if (level != bounded) { level = bounded; invalidate(); }
     }
     void setMuted(boolean value) {
         if (muted != value) { muted = value; invalidate(); }
     }
     @Override protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         float w = getWidth(), h = getHeight();
         float unit = Math.min(w, h);
         float cy = h * 0.5f;
         paint.setColor(color);
         paint.setStyle(Paint.Style.FILL);
         speaker.reset();
         speaker.moveTo(w * 0.12f, h * 0.39f);
         speaker.lineTo(w * 0.32f, h * 0.39f);
         speaker.lineTo(w * 0.53f, h * 0.22f);
         speaker.lineTo(w * 0.53f, h * 0.78f);
         speaker.lineTo(w * 0.32f, h * 0.61f);
         speaker.lineTo(w * 0.12f, h * 0.61f);
         speaker.close();
         canvas.drawPath(speaker, paint);
         paint.setStyle(Paint.Style.STROKE);
         paint.setStrokeWidth(Math.max(1.5f, unit * 0.075f));
         paint.setStrokeCap(Paint.Cap.ROUND);
         firstWave.set(w * 0.46f, cy - h * 0.20f, w * 0.86f, cy + h * 0.20f);
         secondWave.set(w * 0.39f, cy - h * 0.37f, w, cy + h * 0.37f);
         if (!muted && level >= 2) canvas.drawArc(firstWave, -58, 116, false, paint);
         if (!muted && level >= 4) canvas.drawArc(secondWave, -55, 110, false, paint);
         if (muted) {
             paint.setStrokeWidth(Math.max(2f, unit * 0.09f));
             canvas.drawLine(w * 0.67f, h * 0.33f, w * 0.91f, h * 0.67f, paint);
             canvas.drawLine(w * 0.91f, h * 0.33f, w * 0.67f, h * 0.67f, paint);
         }
     }
 }
 private final class ActionButton extends LinearLayout {
     final TextView icon, caption;
     ActionButton(Context context, String glyph, String label, boolean primary) {
         super(context);
         setOrientation(HORIZONTAL);
         setGravity(Gravity.CENTER);
          setPadding(dp(6), dp(4), dp(6), dp(4));
         setBackground(actionBackground(primary));
          icon = MusicActivity.this.label(glyph, primary ? 28 : 24,
                  primary ? 0xff161616 : GOLD_FOCUS, true);
         icon.setGravity(Gravity.CENTER);
          caption = MusicActivity.this.label(label, 13, primary ? 0xff211b10 : WHITE, true);
         caption.setGravity(Gravity.CENTER);
          addView(icon, new LinearLayout.LayoutParams(dp(24), -1));
         LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(-2, -1);
          captionParams.leftMargin = dp(4);
         addView(caption, captionParams);
         setContentDescription(label);
         setOnFocusChangeListener((view, focused) -> {
             int foreground = focused ? 0xff211b10 : (primary ? 0xff211b10 : WHITE);
             icon.setTextColor(primary || focused ? 0xff211b10 : GOLD_FOCUS);
             caption.setTextColor(foreground);
             applyFocusScale(this, focused);
         });
     }
 }
 private Drawable actionBackground(boolean primary) {
     StateListDrawable states = new StateListDrawable();
     int radius = dp(primary ? 30 : 16);
     states.addState(new int[]{android.R.attr.state_focused},
             roundRect(0xfff0d36b, 0xffffe7a0, radius, dp(2)));
     states.addState(new int[]{android.R.attr.state_pressed},
             roundRect(0xffb89425, GOLD, radius, dp(1)));
     states.addState(new int[]{}, primary
             ? roundRect(GOLD, 0xfff0d36b, radius, dp(1))
             : roundRect(0x99101820, 0x55ffffff, radius, dp(1)));
     return states;
 }
}
