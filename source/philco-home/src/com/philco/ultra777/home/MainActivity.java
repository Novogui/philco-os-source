package com.philco.ultra777.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.philco.ultra777.home.media.PlaybackState;
import com.philco.ultra777.home.media.PlayerStateRepository;
import com.philco.ultra777.home.media.MusicPlaybackService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

/** Lightweight, foreground-only TV home screen for the Realtek RTD298x platform. */
public class MainActivity extends Activity {
    private static final String TAG = "PhilcoUltra777";
    private static final String EXTRA_JOYSTICK_CONTROL_MENU =
            "com.philco.ultra777.home.extra.JOYSTICK_CONTROL_MENU";
    private static final String PREFS = "philco_home";
    private static final String PREF_CITY = "weather_city";
    private static final String PREF_WEATHER = "weather_text";
    private static final String PREF_TIME = "weather_time";
    private static final String PREF_LAT = "weather_lat";
    private static final String PREF_LON = "weather_lon";
    private static final String PREF_COORD_CITY = "weather_coord_city";
    private static final String PREF_SCENE = "wallpaper_scene";
    private static final String PREF_SAVER_SCENE = "idle_saver_scene";
    private static final String PREF_SAVER_BACKLIGHT_ORIGINAL = "idle_saver_original_backlight";
    private static final String PREF_TEMP = "weather_temperature";
    private static final String PREF_TEMP_MAX = "weather_temperature_max";
    private static final String PREF_TEMP_MIN = "weather_temperature_min";
    private static final String PREF_DAILY_FETCHED = "weather_daily_fetched";
    private static final String PREF_DESC = "weather_description";
    private static final String PREF_CODE = "weather_code";
    private static final String PREF_WEATHER_ATTEMPT = "weather_attempt_time";
    private static final String PREF_MEDIA_MUTE_ACTIVE = "media_mute_active";
    private static final String PREF_MEDIA_MUTE_VOLUME = "media_mute_previous_volume";
    private static final String PREF_LAST_INPUT = "last_confirmed_input_source";
    private static final String PREF_RESTORE_LAST_INPUT = "restore_last_input_on_boot";
    private static final String DEFAULT_CITY = "Joinville, SC";
    private static final long WEATHER_TTL_MS = 30L * 60L * 1000L;
    private static final long WEATHER_CONNECTIVITY_RETRY_MS = 2L * 60L * 1000L;
    private static final long WEATHER_CONNECTIVITY_DEBOUNCE_MS = 1200L;
    private static final long WEATHER_TIMER_INTERVAL_MS = 30L * 60L * 1000L;
    private static final long IDLE_SAVER_AFTER_MS = 5L * 60L * 1000L;
    private static final long IDLE_DIM_AFTER_MS = 15L * 60L * 1000L;
    private static final long IDLE_BACKLIGHT_MIN_AFTER_MS = 30L * 60L * 1000L;
    private static final long IDLE_STANDBY_AFTER_MS = 60L * 60L * 1000L;
    private static final long IDLE_SAVER_ROTATE_MS = 7L * 60L * 1000L;
    private static final long IDLE_STANDBY_COUNTDOWN_MS = 30L * 1000L;
    private static final String SOURCE_CHANGE_ACTION = "com.realtek.TVSignalAlarm.SourceChange";
    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    private AuroraBackdropView backdrop;
    private AlertDialog joystickControlDialog;
    private Dialog quickControlsDialog;
    private MuteOverlayView muteOverlay;
    private boolean volumeUpUnmuteKeyHeld;
    private FrameLayout idleScreensaver;
    private LinearLayout homeContent;
    private HorizontalScrollView dockScroll;
    private TextView clockView, dateView, cityView, temperatureView, highLowMaxLabelView, highLowMinLabelView,
            highLowMaxView, highLowMinView,
            weatherDescriptionView, weatherUpdatedView, brandView,
             nowPlayingTitleView, nowPlayingStatusView, nowPlayingPlayPause,
             nowPlayingPrevious, nowPlayingNext;
    private ProgressBar nowPlayingProgress;
    private LinearLayout weatherCard;
    private LinearLayout nowPlayingBar;
    private MusicPlaybackService homeMusicService;
    private boolean homeMusicBound;
    private final Runnable homePlaybackProgressTick = new Runnable() {
        @Override public void run() {
            if (!homeActive || !homeMusicBound || homeMusicService == null) return;
            try {
                PlaybackState state = homeMusicService.getState();
                updateNowPlaying(state);
                if (state.status == PlaybackState.Status.PLAYING)
                    foregroundHandler.postDelayed(this, 1000L);
            } catch (Throwable t) { Log.w(TAG, "Home mini-player progress update failed", t); }
        }
    };
    private final ServiceConnection homeMusicConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder instanceof MusicPlaybackService.LocalBinder)
                homeMusicService = ((MusicPlaybackService.LocalBinder)binder).getService();
            PlaybackState state = PlayerStateRepository.get().getState();
            updateNowPlaying(state);
            scheduleHomePlaybackProgress(state);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            homeMusicService = null;
            foregroundHandler.removeCallbacks(homePlaybackProgressTick);
        }
    };
    private int currentWeatherCode = -1;
    private TvGlyphView volumeGlyph;
    private ImageView weatherGlyph, wifiGlyph, saverWeatherGlyph, saverWifiGlyph;
    private TextView wifiStatusView, volumeStatusView;
    private TextView saverClockView, saverDateView, saverTemperatureView, saverWeatherView,
            saverWifiStatusView, saverVolumeStatusView, saverCountdownView;
    private LinearLayout saverNowPlayingBar;
    private TextView saverNowPlayingTitleView, saverNowPlayingArtistView;
    private View saverNowPlayingProgressTrack, saverNowPlayingProgressFill;
    private TvGlyphView saverVolumeGlyph;
    private AsyncTask<Void, Void, WeatherResult> weatherTask;
    private BroadcastReceiver systemReceiver, timeReceiver;
    private final Handler foregroundHandler = new Handler();
    private final Handler sourceHandler = new Handler();
    private final PlayerStateRepository.Observer homePlaybackObserver = new PlayerStateRepository.Observer() {
        @Override public void onStateChanged(final PlaybackState state) {
            foregroundHandler.post(new Runnable() {
                @Override public void run() {
                    if (homeActive && !isFinishing()) {
                        updateNowPlaying(state);
                        scheduleHomePlaybackProgress(state);
                    }
                }
            });
        }
    };
    private HandlerThread quickControlThread;
    private Handler quickControlWorker;
    private boolean homeActive;
    private boolean refreshWeatherWhenHomeResumes = true;
    private long lastHomeInteraction = SystemClock.elapsedRealtime();
    private long saverStartElapsed;
    private long lastSaverRotation;
    private int homeSceneBeforeSaver = -1;
    private volatile int saverOriginalBacklight = -1;
    private volatile boolean saverBacklightDimmed;
    private volatile int saverBacklightStage;
    private boolean saverStandbyRequested;
    private boolean saverStandbyUnavailable;
    private boolean saverStandbyDeferredByMusic;
    private boolean saverTransitioning;
    private boolean returnFocusToMusicEntry;
    private int focusBeforeSaver = R.id.tile_smarttube;
    private WifiSignal wifiSignal = WifiSignal.disconnected();
    private Typeface displayTypeface;
    private Runnable pendingInputVerification;
    private final Runnable homeIdleMonitor = new Runnable() {
        @Override public void run() {
            if (!homeActive || isFinishing()) return;
            long idle = 0L;
            try {
                long now = SystemClock.elapsedRealtime();
                idle = Math.max(0L, now - lastHomeInteraction);
                if (idle >= IDLE_SAVER_AFTER_MS && !isIdleScreensaverVisible()) showIdleScreensaver(now);
                if (isIdleScreensaverVisible()) {
                    if (!saverStandbyUnavailable && idle >= IDLE_DIM_AFTER_MS && saverBacklightStage < 1) setBacklightForIdle(40, 1);
                    if (!saverStandbyUnavailable && idle >= IDLE_BACKLIGHT_MIN_AFTER_MS && saverBacklightStage < 2) setBacklightForIdle(20, 2);
                    if (now - lastSaverRotation >= IDLE_SAVER_ROTATE_MS) rotateIdleWallpaper(now);
                    PlaybackState playback = PlayerStateRepository.get().getState();
                    boolean musicPlaying = playback != null && playback.status == PlaybackState.Status.PLAYING;
                    long countdownAt = IDLE_STANDBY_AFTER_MS - IDLE_STANDBY_COUNTDOWN_MS;
                    if (idle >= countdownAt && !saverStandbyRequested && !saverStandbyUnavailable) {
                        if (musicPlaying) {
                            saverStandbyDeferredByMusic = true;
                            saverCountdownView.setVisibility(View.GONE);
                        } else {
                            long remaining = saverStandbyDeferredByMusic
                                    ? IDLE_STANDBY_COUNTDOWN_MS : IDLE_STANDBY_AFTER_MS - idle;
                            if (remaining > 0) {
                                saverCountdownView.setText("Standby em " + Math.max(1, (remaining + 999) / 1000) + " s");
                                saverCountdownView.setVisibility(View.VISIBLE);
                                if (saverStandbyDeferredByMusic) {
                                    // Music ended after the one-hour deadline; give the user
                                    // the full warning interval before powering down.
                                    saverStandbyDeferredByMusic = false;
                                    lastHomeInteraction = now - (IDLE_STANDBY_AFTER_MS - IDLE_STANDBY_COUNTDOWN_MS);
                                }
                            } else requestRealtekStandby();
                        }
                    }
                }
            } catch (Throwable t) { Log.w(TAG, "Idle screensaver update failed", t); }
            foregroundHandler.postDelayed(this, idle >= IDLE_STANDBY_AFTER_MS - IDLE_STANDBY_COUNTDOWN_MS ? 1000L : 15000L);
        }
    };
    private final Runnable periodicWeatherRefresh = new Runnable() {
        @Override public void run() {
            if (!homeActive || isFinishing()) return;
            refreshWeather(false, false);
            schedulePeriodicWeatherRefresh();
        }
    };
    private final Runnable connectivityWeatherRefresh = new Runnable() {
        @Override public void run() {
            if (homeActive && isNetworkConnected()) refreshWeather(false, false, true);
        }
    };

    private void schedulePeriodicWeatherRefresh() {
        try {
            if (!homeActive || isFinishing()) return;
            foregroundHandler.removeCallbacks(periodicWeatherRefresh);
            SharedPreferences prefs = preferences();
            long now = System.currentTimeMillis();
            long lastSuccess = prefs.getLong(PREF_TIME, 0);
            long lastAttempt = prefs.getLong(PREF_WEATHER_ATTEMPT, 0);
            long basis = Math.max(lastSuccess, lastAttempt);
            long delay = WEATHER_TIMER_INTERVAL_MS;
            if (isNetworkConnected() && basis > 0 && now >= basis)
                delay = WEATHER_TIMER_INTERVAL_MS - (now - basis);
            foregroundHandler.postDelayed(periodicWeatherRefresh, Math.max(1000L, delay));
        } catch (Throwable t) { Log.w(TAG, "Could not schedule automatic weather refresh", t); }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(createHome());
            SharedPreferences prefs = preferences();
            backdrop.setScene(prefs.getInt(PREF_SCENE, AuroraBackdropView.AURORA));
            updateClock();
            updateWeatherDisplay(null);
            updateSystemIndicators();
            final View first = findViewById(R.id.tile_smarttube);
            if (first != null) {
                first.post(new Runnable() {
                    @Override public void run() {
                        if (!isIdleScreensaverVisible()) first.requestFocus();
                    }
                });
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not create the premium home screen; showing safe fallback", t);
            showSafeFallback();
        }
        showJoystickMenuIfRequested(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showJoystickMenuIfRequested(intent);
    }

    private void showJoystickMenuIfRequested(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_JOYSTICK_CONTROL_MENU, false)) return;
        intent.removeExtra(EXTRA_JOYSTICK_CONTROL_MENU);
        if (isIdleScreensaverVisible()) dismissIdleScreensaver();
        View decor = getWindow() == null ? null : getWindow().getDecorView();
        if (decor == null) return;
        decor.post(new Runnable() {
            @Override public void run() { showJoystickControlMenu(); }
        });
    }

    private void showJoystickControlMenu() {
        if (isFinishing()) return;
        if (joystickControlDialog != null && joystickControlDialog.isShowing()) return;
        final LinearLayout items = new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        items.setPadding(dp(20), dp(8), dp(20), dp(8));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("PhilcoOS • Controle da TV")
                .setView(items)
                .setNegativeButton("Fechar", null)
                .create();
        joystickControlDialog = dialog;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface ignored) {
                if (joystickControlDialog == dialog) joystickControlDialog = null;
            }
        });

        addJoystickMenuItem(items, "Ir para Home", true, new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        });
        addJoystickMenuItem(items, "Desligar TV — indisponível", false, null);
        addJoystickMenuItem(items, "Reiniciar TV — indisponível", false, null);

        TextView note = text("O firmware não concedeu ao PhilcoOS uma API de energia validada.",
                13, 0xFFB8C1CE, Typeface.create("sans-serif", Typeface.NORMAL));
        note.setGravity(Gravity.CENTER_VERTICAL);
        note.setPadding(dp(8), dp(10), dp(8), dp(4));
        items.addView(note, new LinearLayout.LayoutParams(-1, -2));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface ignored) {
                if (items.getChildCount() > 0) {
                    View first = items.getChildAt(0);
                    if (first != null) first.requestFocus();
                }
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.58f;
            window.setAttributes(attributes);
            window.setLayout(dp(480), -2);
        }
    }

    private void addJoystickMenuItem(LinearLayout parent, String label, boolean enabled,
                                     final Runnable action) {
        final TextView item = text(label, 19, enabled ? Color.WHITE : 0xFF777F8B,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(18), 0, dp(18), 0);
        item.setFocusable(enabled);
        item.setEnabled(enabled);
        item.setBackground(joystickMenuRowBackground(false, enabled));
        if (enabled && action != null) {
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) { action.run(); }
            });
            item.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override public void onFocusChange(View view, boolean focused) {
                    item.setBackground(joystickMenuRowBackground(focused, true));
                }
            });
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(58));
        params.topMargin = dp(4);
        params.bottomMargin = dp(4);
        parent.addView(item, params);
    }

    private GradientDrawable joystickMenuRowBackground(boolean focused, boolean enabled) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(12));
        shape.setColor(focused ? 0xFF263B50 : (enabled ? 0xFF252A32 : 0xFF1C2026));
        shape.setStroke(dp(focused ? 2 : 1), focused ? 0xFF50C8E8 : 0x55FFFFFF);
        return shape;
    }

    private View createHome() {
        FrameLayout root = new FrameLayout(this);
        root.setFocusableInTouchMode(true);
        root.setClipChildren(false);
        backdrop = new AuroraBackdropView(this);
        root.addView(backdrop, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout content = new LinearLayout(this);
        homeContent = content;
        content.setOrientation(LinearLayout.VERTICAL);
        // Keep every edge element inside the TV's overscan-safe area.
        content.setPadding(dp(64), dp(28), dp(64), dp(24));
        content.setClipChildren(false);
        root.addView(content, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setClipChildren(false);
        content.addView(header, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout timeBlock = new LinearLayout(this);
        timeBlock.setOrientation(LinearLayout.VERTICAL);
        timeBlock.setGravity(Gravity.CENTER_VERTICAL);
        timeBlock.setFocusable(false);
        clockView = text("00:00", 94, Color.WHITE, displayTypeface());
        clockView.setShadowLayer(dp(2), 0, dp(2), 0x70000000);
        dateView = text("", 22, 0xFFF3F6FC, displayTypeface());
        dateView.setShadowLayer(dp(2), 0, dp(1), 0x88000000);
        timeBlock.addView(clockView, wrap());
        LinearLayout.LayoutParams dateParams = wrap();
        dateParams.topMargin = dp(2);
        timeBlock.addView(dateView, dateParams);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, -2, 1f);
        timeParams.rightMargin = dp(24);
        header.addView(timeBlock, timeParams);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.RIGHT);
        right.setClipChildren(false);
        header.addView(right, new LinearLayout.LayoutParams(dp(472), -2));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        addActionButton(controls, "Paisagens", TvGlyphView.SCENE, 0xFF70E6CF,
                R.id.shortcut_scenes, new View.OnClickListener() {
                    @Override public void onClick(View v) { showScenePicker(); }
                });
        addActionButton(controls, "Ajustes", TvGlyphView.SETTINGS, 0xFFF0D693,
                R.id.shortcut_settings, new View.OnClickListener() {
                    @Override public void onClick(View v) { showQuickControls(); }
                });
        addActionButton(controls, "Standby", TvGlyphView.STANDBY, 0xFF9CCBFF,
                R.id.shortcut_standby, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        lastHomeInteraction = SystemClock.elapsedRealtime();
                        showIdleScreensaver(lastHomeInteraction);
                    }
                });
        right.addView(controls, wrap());

        LinearLayout status = new LinearLayout(this);
        status.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        status.setOrientation(LinearLayout.HORIZONTAL);
        wifiGlyph = createWifiImage();
        volumeGlyph = new TvGlyphView(this, TvGlyphView.VOLUME, Color.WHITE);
        wifiStatusView = text("Wi-Fi", 13, 0xFFE5EAF5, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        volumeStatusView = text("Áudio", 13, 0xFFE5EAF5, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams wifiIconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        wifiIconParams.rightMargin = dp(7);
        status.addView(wifiGlyph, wifiIconParams);
        status.addView(wifiStatusView, wrap());
        LinearLayout.LayoutParams volIconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        volIconParams.leftMargin = dp(27);
        volIconParams.rightMargin = dp(7);
        status.addView(volumeGlyph, volIconParams);
        status.addView(volumeStatusView, wrap());
        LinearLayout.LayoutParams statusParams = wrap();
        statusParams.topMargin = dp(12);
        right.addView(status, statusParams);

        LinearLayout weather = createWeatherCard();
        LinearLayout.LayoutParams weatherParams = new LinearLayout.LayoutParams(dp(360), dp(112));
        weatherParams.topMargin = dp(12);
        right.addView(weather, weatherParams);

        nowPlayingBar = createNowPlayingBar();
        LinearLayout.LayoutParams nowPlayingParams = new LinearLayout.LayoutParams(dp(360), dp(58));
        nowPlayingParams.topMargin = dp(9);
        right.addView(nowPlayingBar, nowPlayingParams);

        brandView = text("PHILCO  /  ULTRA 777", 13, 0xDDEEF2FF,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        brandView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        brandRow.addView(brandView, wrap());
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(-1, -2);
        brandParams.bottomMargin = dp(10);
        content.addView(brandRow, brandParams);
        LinearLayout.LayoutParams dockParams = new LinearLayout.LayoutParams(-1, dp(158));
        dockParams.leftMargin = -dp(64);
        dockParams.rightMargin = -dp(64);
        content.addView(createDock(), dockParams);

        muteOverlay = new MuteOverlayView(this);
        muteOverlay.setVisibility(View.GONE);
        muteOverlay.setContentDescription("Áudio silenciado");
        FrameLayout.LayoutParams muteParams = new FrameLayout.LayoutParams(dp(68), dp(68),
                Gravity.LEFT | Gravity.BOTTOM);
        muteParams.leftMargin = dp(64);
        muteParams.bottomMargin = dp(196);
        root.addView(muteOverlay, muteParams);
        idleScreensaver = createIdleScreensaver();
        idleScreensaver.setVisibility(View.GONE);
        root.addView(idleScreensaver, new FrameLayout.LayoutParams(-1, -1));
        return root;
    }

    private Typeface displayTypeface() {
        if (displayTypeface == null) {
            try { displayTypeface = Typeface.createFromAsset(getAssets(), "fonts/Lato-Medium.ttf"); }
            catch (Throwable t) {
                Log.w(TAG, "Lato display font unavailable; using Android sans-serif-medium", t);
                displayTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            }
        }
        return displayTypeface;
    }

    private ImageView createWifiImage() {
        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setImageResource(R.drawable.philco_wifi_off);
        return icon;
    }

    private FrameLayout createIdleScreensaver() {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setFocusable(true);
        overlay.setFocusableInTouchMode(true);
        overlay.setClickable(true);
        overlay.setBackgroundColor(0x99000000);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        center.setClipChildren(false);
        center.setClipToPadding(false);
        saverClockView = text("00:00", 116, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        saverClockView.setGravity(Gravity.LEFT);
        saverClockView.setShadowLayer(dp(3), 0, dp(2), 0xAA000000);
        saverDateView = text("", 22, 0xFFF4F6FA,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        saverDateView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        saverDateView.setPadding(0, 0, 0, 0);
        saverDateView.setShadowLayer(dp(2), 0, dp(1), 0x99000000);
        center.addView(saverClockView, wrap());
        LinearLayout.LayoutParams date = wrap();
        date.topMargin = dp(2);
        center.addView(saverDateView, date);

        LinearLayout weather = new LinearLayout(this);
        weather.setOrientation(LinearLayout.HORIZONTAL);
        weather.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        saverWeatherGlyph = new ImageView(this);
        saverWeatherGlyph.setScaleType(ImageView.ScaleType.FIT_CENTER);
        saverWeatherGlyph.setImageResource(R.drawable.weather_cloud);
        weather.addView(saverWeatherGlyph, new LinearLayout.LayoutParams(dp(40), dp(40)));
        saverTemperatureView = text("--°C", 27, Color.WHITE, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams temperature = wrap();
        temperature.leftMargin = dp(8);
        weather.addView(saverTemperatureView, temperature);
        saverWeatherView = text("", 18, 0xE8FFFFFF, Typeface.create("sans-serif", Typeface.NORMAL));
        LinearLayout.LayoutParams condition = wrap();
        condition.leftMargin = dp(10);
        weather.addView(saverWeatherView, condition);
        LinearLayout.LayoutParams weatherParams = wrap();
        weatherParams.topMargin = dp(15);
        center.addView(weather, weatherParams);
        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(-2, -2,
                Gravity.LEFT | Gravity.CENTER_VERTICAL);
        centerParams.leftMargin = dp(64);
        centerParams.rightMargin = 0;
        overlay.addView(center, centerParams);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.HORIZONTAL);
        status.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        status.setPadding(dp(13), dp(8), dp(13), dp(8));
        status.setBackgroundDrawable(weatherGlassBackground(false));
        saverWifiGlyph = createWifiImage();
        status.addView(saverWifiGlyph, new LinearLayout.LayoutParams(dp(24), dp(24)));
        saverWifiStatusView = text("Wi-Fi", 12, 0xFFF1F5FC, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams wifiText = wrap();
        wifiText.leftMargin = dp(5);
        status.addView(saverWifiStatusView, wifiText);
        saverVolumeGlyph = new TvGlyphView(this, TvGlyphView.VOLUME, Color.WHITE);
        LinearLayout.LayoutParams volumeIcon = new LinearLayout.LayoutParams(dp(24), dp(24));
        volumeIcon.leftMargin = dp(15);
        status.addView(saverVolumeGlyph, volumeIcon);
        saverVolumeStatusView = text("0 / 100", 12, 0xFFF1F5FC, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams volumeText = wrap();
        volumeText.leftMargin = dp(5);
        status.addView(saverVolumeStatusView, volumeText);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(-2, -2,
                Gravity.TOP | Gravity.RIGHT);
        statusParams.topMargin = dp(36);
        statusParams.rightMargin = dp(64);
        overlay.addView(status, statusParams);

        saverCountdownView = text("", 16, 0xFFF4F6FA, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        saverCountdownView.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams countdownParams = new FrameLayout.LayoutParams(-1, -2,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        countdownParams.bottomMargin = dp(48);
        overlay.addView(saverCountdownView, countdownParams);
        saverNowPlayingBar = new LinearLayout(this);
        saverNowPlayingBar.setOrientation(LinearLayout.HORIZONTAL);
        saverNowPlayingBar.setGravity(Gravity.CENTER_VERTICAL);
        saverNowPlayingBar.setPadding(dp(14), dp(8), dp(16), dp(8));
        saverNowPlayingBar.setBackground(weatherGlassBackground(false));
        TextView musicGlyph = text("♫", 24, 0xFFFFD678, Typeface.create("sans-serif-medium", Typeface.NORMAL));
        musicGlyph.setGravity(Gravity.CENTER);
        saverNowPlayingBar.addView(musicGlyph, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout nowPlayingText = new LinearLayout(this);
        nowPlayingText.setOrientation(LinearLayout.VERTICAL);
        nowPlayingText.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams nowPlayingTextParams = new LinearLayout.LayoutParams(dp(390), dp(48));
        nowPlayingTextParams.leftMargin = dp(12);
        saverNowPlayingBar.addView(nowPlayingText, nowPlayingTextParams);
        saverNowPlayingTitleView = text("", 17, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        saverNowPlayingTitleView.setSingleLine(true);
        saverNowPlayingTitleView.setEllipsize(TextUtils.TruncateAt.END);
        nowPlayingText.addView(saverNowPlayingTitleView, new LinearLayout.LayoutParams(-1, dp(25)));
        saverNowPlayingArtistView = text("", 13, 0xFFE7D7B5, Typeface.create("sans-serif", Typeface.NORMAL));
        saverNowPlayingArtistView.setSingleLine(true);
        saverNowPlayingArtistView.setEllipsize(TextUtils.TruncateAt.END);
        nowPlayingText.addView(saverNowPlayingArtistView, new LinearLayout.LayoutParams(-1, dp(18)));
        saverNowPlayingProgressTrack = new View(this);
        saverNowPlayingProgressTrack.setBackgroundColor(0x66505050);
        FrameLayout progress = new FrameLayout(this);
        progress.addView(saverNowPlayingProgressTrack,
                new FrameLayout.LayoutParams(-1, dp(3), Gravity.CENTER_VERTICAL));
        saverNowPlayingProgressFill = new View(this);
        saverNowPlayingProgressFill.setBackgroundColor(0xFFFFD678);
        FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(0, dp(3), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        progress.addView(saverNowPlayingProgressFill, fillParams);
        saverNowPlayingBar.addView(progress, new LinearLayout.LayoutParams(dp(132), dp(20)));
        saverNowPlayingBar.setVisibility(View.GONE);
        FrameLayout.LayoutParams nowPlayingBarParams = new FrameLayout.LayoutParams(-2, dp(66),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        nowPlayingBarParams.bottomMargin = dp(34);
        overlay.addView(saverNowPlayingBar, nowPlayingBarParams);
        updateIdleScreensaverContent();
        return overlay;
    }

    private boolean isIdleScreensaverVisible() {
        return idleScreensaver != null && idleScreensaver.getVisibility() == View.VISIBLE;
    }

    private void showIdleScreensaver(long now) {
        if (idleScreensaver == null || backdrop == null || isIdleScreensaverVisible()) return;
        homeSceneBeforeSaver = backdrop.getScene();
        View currentFocus = getCurrentFocus();
        if (currentFocus != null && currentFocus.getId() != View.NO_ID) focusBeforeSaver = currentFocus.getId();
        preferences().edit().putInt(PREF_SAVER_SCENE, homeSceneBeforeSaver).apply();
        lastSaverRotation = now;
        saverStartElapsed = now;
        saverStandbyRequested = false;
        saverStandbyUnavailable = false;
        saverStandbyDeferredByMusic = false;
        saverTransitioning = false;
        saverCountdownView.setVisibility(View.GONE);
        Typeface saverFace = displayTypeface();
        saverClockView.setTypeface(saverFace);
        saverDateView.setTypeface(saverFace);
        if (homeContent != null) homeContent.setVisibility(View.GONE);
        updateMuteOverlay(false);
        idleScreensaver.animate().cancel();
        idleScreensaver.setAlpha(0f);
        idleScreensaver.setVisibility(View.VISIBLE);
        idleScreensaver.bringToFront();
        updateIdleScreensaverContent();
        idleScreensaver.requestFocus();
        idleScreensaver.animate().alpha(1f).setDuration(420L).start();
        Log.i(TAG, "Idle screensaver shown after five minutes on Home");
    }

    private void rotateIdleWallpaper(long now) {
        if (backdrop == null) return;
        int last = preferences().getInt(PREF_SAVER_SCENE, -1);
        int next = (last + 1) % (AuroraBackdropView.FOREST + 1);
        if (next == backdrop.getScene()) next = (next + 1) % (AuroraBackdropView.FOREST + 1);
        preferences().edit().putInt(PREF_SAVER_SCENE, next).apply();
        backdrop.setScene(next);
        lastSaverRotation = now;
        if (isIdleScreensaverVisible()) Log.i(TAG, "Idle screensaver landscape changed to scene " + next);
    }

    private void dismissIdleScreensaver() {
        if (idleScreensaver == null || idleScreensaver.getVisibility() != View.VISIBLE || saverTransitioning) return;
        saverTransitioning = true;
        idleScreensaver.animate().cancel();
        idleScreensaver.animate().alpha(0f).setDuration(360L).start();
        foregroundHandler.postDelayed(new Runnable() {
            @Override public void run() { finishDismissIdleScreensaver(); }
        }, 380L);
    }

    private void finishDismissIdleScreensaver() {
        if (idleScreensaver == null) return;
        idleScreensaver.animate().cancel();
        idleScreensaver.setAlpha(1f);
        idleScreensaver.setVisibility(View.GONE);
        if (homeContent != null) homeContent.setVisibility(View.VISIBLE);
        if (backdrop != null && homeSceneBeforeSaver >= 0) backdrop.setScene(homeSceneBeforeSaver);
        homeSceneBeforeSaver = -1;
        saverTransitioning = false;
        saverStandbyRequested = false;
        saverStandbyUnavailable = false;
        saverCountdownView.setVisibility(View.GONE);
        // D-pad input is consumed by dispatchKeyEvent while the saver is visible,
        // so Activity.onUserInteraction() is not reliable on this Android TV build.
        // Reset idle here to prevent the monitor from immediately dimming again.
        lastHomeInteraction = SystemClock.elapsedRealtime();
        scheduleHomeIdleMonitor();
        restoreBacklightAfterIdle();
        updateMuteOverlay(volumeGlyph != null && volumeGlyph.isMuted());
        View focus = findViewById(focusBeforeSaver);
        if (focus == null) focus = findViewById(R.id.tile_smarttube);
        if (focus != null) focus.requestFocus();
        Log.i(TAG, "Idle screensaver dismissed by user activity");
    }

    private void updateIdleScreensaverContent() {
        if (saverClockView == null) return;
        if (clockView != null) saverClockView.setText(clockView.getText());
        if (temperatureView != null) saverTemperatureView.setText(temperatureView.getText());
        if (cityView != null && weatherDescriptionView != null) {
            String city = cityView.getText().toString();
            String condition = weatherDescriptionView.getText().toString();
            saverWeatherView.setText(city + (TextUtils.isEmpty(condition) ? "" : " · " + condition));
        }
        if (saverWeatherGlyph != null)
            saverWeatherGlyph.setImageResource(screensaverWeatherDrawableForCode(getCurrentWeatherCode()));
        applyWifiSignalToViews();
        if (volumeGlyph != null) {
            saverVolumeGlyph.setLevel(getCurrentVolumeLevel());
            saverVolumeGlyph.setMuted(isCurrentVolumeMuted());
            saverVolumeStatusView.setText(volumeStatusView == null ? "Áudio" : volumeStatusView.getText());
        }
        updateSaverNowPlaying(PlayerStateRepository.get().getState());
    }

    private void updateSaverNowPlaying(PlaybackState state) {
        if (saverNowPlayingBar == null) return;
        boolean active = state != null && state.track != null
                && (state.status == PlaybackState.Status.PLAYING || state.status == PlaybackState.Status.PAUSED);
        saverNowPlayingBar.setVisibility(active && isIdleScreensaverVisible() ? View.VISIBLE : View.GONE);
        if (!active) return;
        saverNowPlayingTitleView.setText(state.track.title);
        saverNowPlayingArtistView.setText(state.track.artist + (state.status == PlaybackState.Status.PLAYING
                ? "  ·  Tocando" : "  ·  Pausada"));
        final int duration = state.durationMs;
        final int position = state.positionMs;
        saverNowPlayingProgressTrack.post(new Runnable() {
            @Override public void run() {
                int width = saverNowPlayingProgressTrack.getWidth();
                int fill = duration <= 0 ? 0 : (int)((long)width * Math.min(position, duration) / duration);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)saverNowPlayingProgressFill.getLayoutParams();
                params.width = fill;
                saverNowPlayingProgressFill.setLayoutParams(params);
            }
        });
    }

    private int getCurrentWeatherCode() { return preferences().getInt(PREF_CODE, -1); }
    private int getCurrentWifiLevel() { return wifiSignal == null ? -1 : wifiSignal.bars; }
    private int getCurrentVolumeLevel() { return volumeGlyph == null ? 0 : volumeGlyph.getLevel(); }
    private boolean isCurrentVolumeMuted() { return volumeGlyph != null && volumeGlyph.isMuted(); }

    private void setBacklightForIdle(final int target, final int stage) {
        if (stage <= saverBacklightStage) return;
        saverBacklightStage = stage;
        ensureQuickControlWorker();
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    Object tvManager = getSystemService("tv");
                    SharedPreferences prefs = preferences();
                    int original = saverOriginalBacklight;
                    if (original < 0) original = prefs.getInt(PREF_SAVER_BACKLIGHT_ORIGINAL, -1);
                    if (original < 0) original = readRealtekValue(tvManager, "getBacklight");
                    if (original < 0 || original > 100) throw new IllegalStateException("Invalid original backlight " + original);
                    if (!prefs.contains(PREF_SAVER_BACKLIGHT_ORIGINAL)
                            && !prefs.edit().putInt(PREF_SAVER_BACKLIGHT_ORIGINAL, original).commit())
                        throw new IllegalStateException("Could not persist original backlight before dimming");
                    saverOriginalBacklight = original;
                    writeRealtekValue(tvManager, "setBacklight", target);
                    int actual = readRealtekValue(tvManager, "getBacklight");
                    if (actual != target) throw new IllegalStateException("Backlight readback " + actual + " != " + target);
                    saverBacklightDimmed = true;
                    Log.i(TAG, "Idle screensaver backlight stage " + stage + ": " + original + " -> " + actual);
                } catch (Throwable t) {
                    if (saverBacklightStage == stage) saverBacklightStage = stage - 1;
                    Log.w(TAG, "Could not reduce backlight for idle screensaver", t);
                }
            }
        });
    }

    private void restoreBacklightAfterIdle() {
        if (!saverBacklightDimmed && saverOriginalBacklight < 0
                && preferences().getInt(PREF_SAVER_BACKLIGHT_ORIGINAL, -1) < 0) return;
        saverBacklightDimmed = false;
        saverBacklightStage = 0;
        ensureQuickControlWorker();
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    int original = saverOriginalBacklight;
                    SharedPreferences prefs = preferences();
                    if (original < 0) original = prefs.getInt(PREF_SAVER_BACKLIGHT_ORIGINAL, -1);
                    if (original < 0) return;
                    Object tvManager = getSystemService("tv");
                    writeRealtekValue(tvManager, "setBacklight", original);
                    int actual = readRealtekValue(tvManager, "getBacklight");
                    if (actual != original) throw new IllegalStateException("Backlight restore readback " + actual + " != " + original);
                    if (!prefs.edit().remove(PREF_SAVER_BACKLIGHT_ORIGINAL).commit())
                        throw new IllegalStateException("Could not clear restored backlight recovery value");
                    saverOriginalBacklight = -1;
                    Log.i(TAG, "Idle screensaver restored backlight to " + actual);
                } catch (Throwable t) { Log.w(TAG, "Could not restore original backlight", t); }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void requestRealtekStandby() {
        saverStandbyRequested = true;
        dismissQuickControlsForStandby("Realtek standby");
        saverCountdownView.setVisibility(View.GONE);
        restoreBacklightAfterIdle();
        ensureQuickControlWorker();
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    Object tvManager = getSystemService("tv");
                    Object result = tvManager.getClass().getMethod("standBy").invoke(tvManager);
                    Log.i(TAG, "Realtek TvManager.standBy invoked after one hour idle; reported=" + result);
                    if (Boolean.FALSE.equals(result)) showStandbyFailure("A TV não aceitou o standby automático");
                } catch (Throwable t) {
                    Log.e(TAG, "Realtek standby request failed after idle timer", t);
                    showStandbyFailure("Standby automático indisponível");
                }
            }
        });
    }

    private void showStandbyFailure(final String message) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                saverStandbyUnavailable = true;
                if (isIdleScreensaverVisible()) {
                    saverCountdownView.setText(message);
                    saverCountdownView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void scheduleHomeIdleMonitor() {
        foregroundHandler.removeCallbacks(homeIdleMonitor);
        if (homeActive) foregroundHandler.postDelayed(homeIdleMonitor, 15000L);
    }

    private LinearLayout createWeatherCard() {
        LinearLayout card = new LinearLayout(this);
        weatherCard = card;
        card.setId(R.id.weather_box);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(14), dp(10), dp(16), dp(10));
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setClickable(true);
        card.setContentDescription("Clima atual. OK atualiza; pressione e segure para trocar a cidade.");
        card.setBackgroundDrawable(weatherGlassBackground(false));
        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean focused) {
                view.setBackgroundDrawable(weatherGlassBackground(focused));
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { refreshWeather(true, true); }
        });
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) { askForCity(); return true; }
        });

        LinearLayout conditionPanel = new LinearLayout(this);
        conditionPanel.setGravity(Gravity.CENTER);
        conditionPanel.setOrientation(LinearLayout.VERTICAL);
        weatherDescriptionView = text("Buscando previsão…", 14, 0xFFF7F9FC,
                Typeface.create("sans-serif", Typeface.BOLD));
        weatherDescriptionView.setShadowLayer(dp(1), 0, dp(1), 0x55000000);
        weatherDescriptionView.setSingleLine(true);
        weatherDescriptionView.setEllipsize(TextUtils.TruncateAt.END);
        weatherDescriptionView.setGravity(Gravity.CENTER);
        weatherDescriptionView.setIncludeFontPadding(false);
        conditionPanel.addView(weatherDescriptionView, new LinearLayout.LayoutParams(-1, dp(22)));

        weatherGlyph = new ImageView(this);
        weatherGlyph.setScaleType(ImageView.ScaleType.FIT_CENTER);
        weatherGlyph.setImageResource(R.drawable.weather_cloud);
        conditionPanel.addView(weatherGlyph, new LinearLayout.LayoutParams(dp(64), dp(64)));
        card.addView(conditionPanel, new LinearLayout.LayoutParams(dp(116), -1));

        // The right column receives the remaining width; city, temperature and range share a clear hierarchy.
        LinearLayout currentPanel = new LinearLayout(this);
        currentPanel.setGravity(Gravity.CENTER_VERTICAL);
        currentPanel.setOrientation(LinearLayout.VERTICAL);
        currentPanel.setPadding(dp(4), 0, 0, 0);

        LinearLayout cityRow = new LinearLayout(this);
        cityRow.setGravity(Gravity.CENTER_VERTICAL);
        cityView = text(DEFAULT_CITY, 16, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        cityView.setSingleLine(true);
        cityView.setEllipsize(TextUtils.TruncateAt.END);
        cityRow.addView(cityView, new LinearLayout.LayoutParams(0, dp(22), 1f));
        weatherUpdatedView = text("--:--", 10, 0xFFE5EDF5,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        weatherUpdatedView.setSingleLine(true);
        weatherUpdatedView.setIncludeFontPadding(false);
        weatherUpdatedView.setGravity(Gravity.CENTER);
        weatherUpdatedView.setPadding(dp(5), dp(2), dp(5), dp(2));
        GradientDrawable updateBadge = new GradientDrawable();
        updateBadge.setShape(GradientDrawable.RECTANGLE);
        updateBadge.setCornerRadius(dp(7));
        updateBadge.setColor(0x227A8EA3);
        updateBadge.setStroke(dp(1), 0x337FE5F2);
        weatherUpdatedView.setBackgroundDrawable(updateBadge);
        cityRow.addView(weatherUpdatedView, new LinearLayout.LayoutParams(dp(88), dp(22)));
        currentPanel.addView(cityRow, new LinearLayout.LayoutParams(-1, dp(22)));

        LinearLayout temperatureRow = new LinearLayout(this);
        temperatureRow.setGravity(Gravity.CENTER_VERTICAL);
        temperatureView = text("--°C", 42, Color.WHITE, displayTypeface());
        temperatureView.setSingleLine(true);
        temperatureView.setIncludeFontPadding(false);
        temperatureView.setShadowLayer(dp(2), 0, dp(1), 0x66000000);
        temperatureRow.addView(temperatureView, new LinearLayout.LayoutParams(0, dp(50), 1f));
        LinearLayout highLowPanel = new LinearLayout(this);
        highLowPanel.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        highLowPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout highLowMaxRow = new LinearLayout(this);
        highLowMaxRow.setGravity(Gravity.CENTER_VERTICAL);
        highLowMaxLabelView = text("Máx.", 14, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD));
        highLowMaxLabelView.setSingleLine(true);
        highLowMaxLabelView.setIncludeFontPadding(false);
        highLowMaxLabelView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        highLowMaxRow.addView(highLowMaxLabelView, new LinearLayout.LayoutParams(dp(38), dp(20)));
        highLowMaxView = text("", 15, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD));
        highLowMaxView.setSingleLine(true);
        highLowMaxView.setIncludeFontPadding(false);
        highLowMaxView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        highLowMaxView.setShadowLayer(dp(1), 0, dp(1), 0x55000000);
        highLowMaxRow.addView(highLowMaxView, new LinearLayout.LayoutParams(dp(46), dp(20)));
        highLowPanel.addView(highLowMaxRow, new LinearLayout.LayoutParams(dp(84), dp(20)));
        LinearLayout highLowMinRow = new LinearLayout(this);
        highLowMinRow.setGravity(Gravity.CENTER_VERTICAL);
        highLowMinLabelView = text("Min.", 14, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD));
        highLowMinLabelView.setSingleLine(true);
        highLowMinLabelView.setIncludeFontPadding(false);
        highLowMinLabelView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        highLowMinRow.addView(highLowMinLabelView, new LinearLayout.LayoutParams(dp(38), dp(20)));
        highLowMinView = text("", 15, Color.WHITE, Typeface.create("sans-serif", Typeface.BOLD));
        highLowMinView.setSingleLine(true);
        highLowMinView.setIncludeFontPadding(false);
        highLowMinView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        highLowMinView.setShadowLayer(dp(1), 0, dp(1), 0x55000000);
        highLowMinRow.addView(highLowMinView, new LinearLayout.LayoutParams(dp(46), dp(20)));
        highLowPanel.addView(highLowMinRow, new LinearLayout.LayoutParams(dp(84), dp(20)));
        temperatureRow.addView(highLowPanel, new LinearLayout.LayoutParams(dp(84), dp(40)));
        currentPanel.addView(temperatureRow, new LinearLayout.LayoutParams(-1, dp(50)));

        TextView attribution = text(getString(R.string.weather_attribution), 8, 0xDDEBF3FF,
                Typeface.create("sans-serif", Typeface.NORMAL));
        attribution.setSingleLine(true);
        attribution.setIncludeFontPadding(false);
        currentPanel.addView(attribution, new LinearLayout.LayoutParams(-1, dp(10)));
        card.addView(currentPanel, new LinearLayout.LayoutParams(0, -1, 1f));

        card.setNextFocusLeftId(R.id.shortcut_scenes);
        card.setNextFocusRightId(R.id.shortcut_settings);
        card.setNextFocusUpId(R.id.shortcut_standby);
        card.setNextFocusDownId(R.id.tile_smarttube);
        return card;
    }

    private HorizontalScrollView createDock() {
        dockScroll = new HorizontalScrollView(this);
        dockScroll.setHorizontalScrollBarEnabled(false);
        dockScroll.setFillViewport(true);
        dockScroll.setClipChildren(false);
        dockScroll.setClipToPadding(false);
        dockScroll.setFocusable(false);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        // Full-width viewport plus the original 64dp safe margins keeps all eight tiles visible.
        row.setPadding(dp(64), dp(8), dp(64), dp(8));
        row.setClipChildren(false);
        addTile(row, "SmartTube", "YouTube", TvGlyphView.PLAY, 0xFFFF4B55,
                R.id.tile_smarttube, new View.OnClickListener() {
                    @Override public void onClick(View v) { launchSmartTube(); }
                });
        addTile(row, "AR", "TV aberta", TvGlyphView.ANTENNA, 0xFF5DB7FF,
                R.id.tile_ar, new View.OnClickListener() {
                    @Override public void onClick(View v) { switchInputOrOpenMenu("TV aberta", 27, true); }
                });
        addTile(row, "Cabo", "TV a cabo", TvGlyphView.RECEIVER, 0xFF48D89A,
                R.id.tile_cable, new View.OnClickListener() {
                    @Override public void onClick(View v) { openCableMode(); }
                });
        addTile(row, "AV", "Composto", TvGlyphView.RCA, 0xFFFFB84C,
                R.id.tile_av, new View.OnClickListener() {
                    @Override public void onClick(View v) { switchInputOrOpenMenu("AV", 5); }
                });
        addTile(row, "HDMI 1", "Entrada 1", TvGlyphView.HDMI1, 0xFF53DCE8,
                R.id.tile_hdmi1, new View.OnClickListener() {
                    @Override public void onClick(View v) { switchInputOrOpenMenu("HDMI 1", 18); }
                });
        addTile(row, "HDMI 2", "Entrada 2", TvGlyphView.HDMI2, 0xFFAC83FF,
                R.id.tile_hdmi2, new View.OnClickListener() {
                    @Override public void onClick(View v) { switchInputOrOpenMenu("HDMI 2", 17); }
                });
        addTile(row, "Apps", "Sistema", TvGlyphView.APPS, 0xFFF27CC4,
                R.id.tile_apps, new View.OnClickListener() {
                    @Override public void onClick(View v) { showAppDrawer(); }
                });
        addTile(row, "Ajustes", "→ Música", TvGlyphView.SETTINGS, 0xFFD6E2F2,
                R.id.tile_settings, new View.OnClickListener() {
                    @Override public void onClick(View v) { showQuickControls(); }
                });
        dockScroll.addView(row, new FrameLayout.LayoutParams(-2, -1));
        return dockScroll;
    }

    private FrameLayout createHdmiPortIcon(String port, int accent) {
        FrameLayout icon = new FrameLayout(this);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ImageView connector = new ImageView(this);
        connector.setImageResource(R.drawable.philco_hdmi);
        connector.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams connectorParams = new FrameLayout.LayoutParams(dp(45), dp(45),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        icon.addView(connector, connectorParams);
        TextView portNumber = text(port, 12, accent,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        portNumber.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams numberParams = new FrameLayout.LayoutParams(-2, dp(15),
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        icon.addView(portNumber, numberParams);
        return icon;
    }

    private void addTile(LinearLayout row, String title, String subtitle, int glyph,
                         int accent, int id, View.OnClickListener action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setId(id);
        tile.setGravity(Gravity.CENTER);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setFocusable(true);
        tile.setFocusableInTouchMode(true);
        tile.setClickable(true);
        tile.setContentDescription(title + ". " + subtitle);
        tile.setPadding(dp(9), dp(10), dp(9), dp(8));
        tile.setBackgroundDrawable(glassBackground(false, accent, 22));
        tile.setOnFocusChangeListener(focusListener(accent, 22));
        tile.setOnClickListener(action);
        int[] ids = {R.id.tile_smarttube, R.id.tile_ar, R.id.tile_cable, R.id.tile_av,
                R.id.tile_hdmi1, R.id.tile_hdmi2, R.id.tile_apps, R.id.tile_settings};
        int index = 0;
        while (index < ids.length && ids[index] != id) index++;
        int left = ids[(index + ids.length - 1) % ids.length];
        // The Home row's right edge leads to the next full-screen page (Music),
        // rather than wrapping focus back to SmartTube.
        int right = id == R.id.tile_settings ? R.id.tile_settings
                : ids[(index + 1) % ids.length];
        tile.setNextFocusLeftId(left);
        tile.setNextFocusRightId(right);
        tile.setNextFocusUpId(R.id.weather_box);
        tile.setNextFocusDownId(id);
        if (id == R.id.tile_settings) {
            tile.setContentDescription("Ajustes. Sistema. Pressione direita para abrir Música.");
            tile.setOnKeyListener(new View.OnKeyListener() {
                @Override public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if (keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                        returnFocusToMusicEntry = true;
                        try {
                            startActivity(new Intent(MainActivity.this,
                                    com.philco.ultra777.home.media.MusicActivity.class));
                            overridePendingTransition(R.anim.page_enter_from_right,
                                    R.anim.page_exit_to_left);
                        } catch (Throwable t) {
                            returnFocusToMusicEntry = false;
                            Log.w(TAG, "Could not open Music page", t);
                        }
                    }
                    return true;
                }
            });
        }
        View icon = (glyph == TvGlyphView.HDMI1 || glyph == TvGlyphView.HDMI2)
                ? createHdmiPortIcon(glyph == TvGlyphView.HDMI1 ? "1" : "2", accent)
                : new TvGlyphView(this, glyph, accent);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(60), dp(60)));
        TextView titleView = text(title, 15, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = wrap();
        titleParams.topMargin = dp(5);
        tile.addView(titleView, titleParams);
        TextView subView = text(subtitle, 11, 0xFFDCE2F0,
                Typeface.create("sans-serif", Typeface.NORMAL));
        subView.setGravity(Gravity.CENTER);
        tile.addView(subView, wrap());
        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(dp(130), dp(134));
        tileParams.leftMargin = (id == R.id.tile_smarttube ? 0 : dp(16));
        tileParams.rightMargin = 0;
        row.addView(tile, tileParams);
        tile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(final View view, boolean focused) {
                view.setBackgroundDrawable(glassBackground(focused, accent, 22));
                view.animate().cancel();
                boolean edgeTile = view.getId() == R.id.tile_smarttube
                        || view.getId() == R.id.tile_settings;
                float focusScale = focused && !edgeTile ? 1.045f : 1f;
                view.animate().scaleX(focusScale).scaleY(focusScale)
                        .setDuration(135).start();
                if (focused && dockScroll != null) {
                    dockScroll.post(new Runnable() {
                        @Override public void run() {
                            int target = Math.max(0, view.getLeft() - dp(12));
                            if (target < dockScroll.getScrollX() ||
                                    target + view.getWidth() + dp(12) > dockScroll.getScrollX() + dockScroll.getWidth())
                                dockScroll.smoothScrollTo(target, 0);
                        }
                    });
                }
            }
        });
    }

    private View.OnFocusChangeListener focusListener(final int accent, final int radius) {
        return new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean focused) {
                view.setBackgroundDrawable(glassBackground(focused, accent, radius));
            }
        };
    }

    private GradientDrawable glassBackground(boolean focused, int accent, int radiusDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(focused ? 0xE52C3A4D : 0xD0101926);
        shape.setCornerRadius(dp(radiusDp));
        shape.setStroke(dp(focused ? 2.5f : 1f), focused ? 0xFFF8FCFF : ((accent & 0x00FFFFFF) | 0x77000000));
        return shape;
    }

    private GradientDrawable weatherGlassBackground(boolean focused) {
        // Keep a restrained graphite gradient for every weather condition; the icon carries the forecast color.
        int start = 0xFF454B53;
        int end = 0xFF747B84;
        if (focused) {
            start = blendWeatherColor(start, 0xFF101923, 0.16f);
            end = blendWeatherColor(end, 0xFF101923, 0.16f);
        }
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{start, end});
        shape.setCornerRadius(dp(20));
        shape.setStroke(dp(focused ? 3f : 1f), focused ? 0xFFFFE8A0 : 0x66FFFFFF);
        return shape;
    }

    private int blendWeatherColor(int foreground, int background, float amount) {
        int r = (int)(Color.red(foreground) * (1f - amount) + Color.red(background) * amount);
        int g = (int)(Color.green(foreground) * (1f - amount) + Color.green(background) * amount);
        int b = (int)(Color.blue(foreground) * (1f - amount) + Color.blue(background) * amount);
        return Color.rgb(r, g, b);
    }

    private LinearLayout createNowPlayingBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setPadding(dp(9), dp(3), dp(9), dp(3));
        bar.setBackgroundDrawable(glassBackground(false, 0xFFD6BC75, 16));
        bar.setVisibility(View.GONE);
        bar.setClickable(true);
        bar.setOnClickListener(v -> openMusicPage());
        bar.setContentDescription("Mini player. Abrir Música");

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        bar.addView(row, new LinearLayout.LayoutParams(-1, 0, 1f));
        TextView note = text("♫", 22, 0xFFFFE3A0,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        note.setGravity(Gravity.CENTER);
        note.setBackgroundDrawable(glassBackground(false, 0xFFD6BC75, 10));
        row.addView(note, new LinearLayout.LayoutParams(dp(36), dp(36)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(0, -1, 1f);
        labelsParams.leftMargin = dp(8);
        labelsParams.rightMargin = dp(5);
        row.addView(labels, labelsParams);

        nowPlayingPrevious = createNowPlayingSkipButton("|◀", "Faixa anterior");
        nowPlayingPrevious.setOnClickListener(v -> skipHomeTrack(false));
        row.addView(nowPlayingPrevious, new LinearLayout.LayoutParams(dp(34), dp(42)));

        nowPlayingTitleView = text("", 15, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        nowPlayingTitleView.setSingleLine(true);
        nowPlayingTitleView.setEllipsize(TextUtils.TruncateAt.END);
        labels.addView(nowPlayingTitleView, new LinearLayout.LayoutParams(-1, -2));

        nowPlayingStatusView = text("", 11, 0xFFE5D39C,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        nowPlayingStatusView.setSingleLine(true);
        nowPlayingStatusView.setEllipsize(TextUtils.TruncateAt.END);
        labels.addView(nowPlayingStatusView, new LinearLayout.LayoutParams(-1, -2));

        nowPlayingPlayPause = text("▶", 20, 0xFFFFE3A0,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        nowPlayingPlayPause.setId(View.generateViewId());
        nowPlayingPlayPause.setGravity(Gravity.CENTER);
        nowPlayingPlayPause.setFocusable(true);
        nowPlayingPlayPause.setFocusableInTouchMode(true);
        nowPlayingPlayPause.setClickable(true);
        nowPlayingPlayPause.setContentDescription("Reproduzir ou pausar música");
        nowPlayingPlayPause.setBackgroundDrawable(glassBackground(false, 0xFFD6BC75, 14));
        nowPlayingPlayPause.setOnFocusChangeListener(focusListener(0xFFD6BC75, 14));
        nowPlayingPlayPause.setOnClickListener(v -> toggleHomePlayback());
        nowPlayingPlayPause.setNextFocusUpId(R.id.weather_box);
        nowPlayingPlayPause.setNextFocusDownId(R.id.tile_settings);
        View weatherBox = findViewById(R.id.weather_box);
        if (weatherBox != null) weatherBox.setNextFocusDownId(R.id.tile_smarttube);
        row.addView(nowPlayingPlayPause, new LinearLayout.LayoutParams(dp(36), dp(42)));
        nowPlayingNext = createNowPlayingSkipButton("▶|", "Próxima faixa");
        nowPlayingNext.setOnClickListener(v -> skipHomeTrack(true));
        row.addView(nowPlayingNext, new LinearLayout.LayoutParams(dp(34), dp(42)));
        nowPlayingPrevious.setNextFocusRightId(nowPlayingPlayPause.getId());
        nowPlayingPlayPause.setNextFocusLeftId(nowPlayingPrevious.getId());
        nowPlayingPlayPause.setNextFocusRightId(nowPlayingNext.getId());
        nowPlayingNext.setNextFocusLeftId(nowPlayingPlayPause.getId());
        nowPlayingPrevious.setNextFocusUpId(R.id.weather_box);
        nowPlayingPlayPause.setNextFocusUpId(R.id.weather_box);
        nowPlayingNext.setNextFocusUpId(R.id.weather_box);
        nowPlayingPrevious.setNextFocusDownId(R.id.tile_settings);
        nowPlayingPlayPause.setNextFocusDownId(R.id.tile_settings);
        nowPlayingNext.setNextFocusDownId(R.id.tile_settings);

        nowPlayingProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        nowPlayingProgress.setMax(1000);
        android.graphics.drawable.GradientDrawable progressTrack = new android.graphics.drawable.GradientDrawable();
        progressTrack.setColor(0x55ffffff);
        progressTrack.setCornerRadius(dp(2));
        android.graphics.drawable.GradientDrawable progressFill = new android.graphics.drawable.GradientDrawable();
        progressFill.setColor(0xffffdc86);
        progressFill.setCornerRadius(dp(2));
        android.graphics.drawable.ClipDrawable clipped = new android.graphics.drawable.ClipDrawable(
                progressFill, Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
        android.graphics.drawable.LayerDrawable progressLayers = new android.graphics.drawable.LayerDrawable(
                new android.graphics.drawable.Drawable[]{progressTrack, clipped});
        progressLayers.setId(0, android.R.id.background);
        progressLayers.setId(1, android.R.id.progress);
        nowPlayingProgress.setProgressDrawable(progressLayers);
        bar.addView(nowPlayingProgress, new LinearLayout.LayoutParams(-1, dp(2)));
        return bar;
    }

    private void updateNowPlaying(PlaybackState state) {
        updateSaverNowPlaying(state);
        if (nowPlayingBar == null) return;
        boolean active = state != null && state.track != null
                && (state.status == PlaybackState.Status.PLAYING
                || state.status == PlaybackState.Status.PAUSED);
        nowPlayingBar.setVisibility(active ? View.VISIBLE : View.GONE);
        View weatherBox = findViewById(R.id.weather_box);
        if (weatherBox != null) weatherBox.setNextFocusDownId(active
                ? nowPlayingPlayPause.getId() : R.id.tile_smarttube);
        if (!active) return;

        String title = TextUtils.isEmpty(state.track.title) ? "Faixa sem título" : state.track.title;
        boolean playing = state.status == PlaybackState.Status.PLAYING;
        nowPlayingTitleView.setText(title);
        nowPlayingStatusView.setText(TextUtils.isEmpty(state.track.artist)
                ? (playing ? "Tocando agora" : "Pausada") : state.track.artist);
        nowPlayingPlayPause.setText(playing ? "Ⅱ" : "▶");
        nowPlayingPlayPause.setContentDescription(playing ? "Pausar música" : "Retomar música");
        nowPlayingProgress.setProgress(state.durationMs > 0
                ? (int)((long)state.positionMs * nowPlayingProgress.getMax() / state.durationMs) : 0);
        nowPlayingBar.setContentDescription(title + " — " + state.track.artist + ". "
                + (playing ? "Tocando agora. OK abre Música." : "Pausada. OK abre Música."));
    }

    private void openMusicPage() {
        try { startActivity(new Intent(this, com.philco.ultra777.home.media.MusicActivity.class)); }
        catch (Throwable t) { Log.e(TAG, "Could not open Music from Home mini-player", t); }
    }

    private TextView createNowPlayingSkipButton(String glyph, String description) {
        TextView button = text(glyph, 16, 0xFFFFE3A0,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setId(View.generateViewId());
        button.setGravity(Gravity.CENTER);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setClickable(true);
        button.setContentDescription(description);
        button.setBackgroundDrawable(glassBackground(false, 0xFFD6BC75, 12));
        button.setOnFocusChangeListener(focusListener(0xFFD6BC75, 12));
        return button;
    }

    private void skipHomeTrack(boolean next) {
        try {
            if (homeMusicService == null) { toast("Player de música indisponível"); return; }
            boolean changed = next ? homeMusicService.skipNext() : homeMusicService.skipPrevious();
            if (!changed && next) toast("Esta é a última faixa da fila");
            updateNowPlaying(homeMusicService.getState());
        } catch (Throwable t) {
            Log.w(TAG, "Home mini-player track navigation failed", t);
            toast("Não foi possível trocar a faixa");
        }
    }

    private void toggleHomePlayback() {
        try {
            if (homeMusicService == null) {
                toast("Player de música indisponível");
                return;
            }
            if (homeMusicService.isPlaying()) homeMusicService.pause();
            else homeMusicService.resume();
            updateNowPlaying(homeMusicService.getState());
        } catch (Throwable t) { Log.w(TAG, "Home mini-player control failed", t); toast("Não foi possível controlar a música"); }
    }

    private void scheduleHomePlaybackProgress(PlaybackState state) {
        foregroundHandler.removeCallbacks(homePlaybackProgressTick);
        if (homeActive && homeMusicService != null && state != null
                && state.status == PlaybackState.Status.PLAYING)
            foregroundHandler.postDelayed(homePlaybackProgressTick, 1000L);
    }

    private void addActionButton(LinearLayout parent, String label, int glyph, int accent,
                                 int id, View.OnClickListener action) {
        LinearLayout button = new LinearLayout(this);
        button.setId(id);
        button.setGravity(Gravity.CENTER);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setPadding(dp(14), dp(8), dp(14), dp(8));
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setClickable(true);
        button.setContentDescription(label);
        button.setBackgroundDrawable(glassBackground(false, accent, 15));
        button.setOnFocusChangeListener(focusListener(accent, 15));
        button.setOnClickListener(action);
        TvGlyphView icon = new TvGlyphView(this, glyph, accent);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.rightMargin = dp(7);
        button.addView(icon, iconParams);
        button.addView(text(label, 14, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL)), wrap());
        LinearLayout.LayoutParams params = wrap();
        params.leftMargin = dp(8);
        parent.addView(button, params);
        if (id == R.id.shortcut_scenes) {
            button.setNextFocusLeftId(id);
            button.setNextFocusRightId(R.id.shortcut_settings);
            button.setNextFocusDownId(R.id.weather_box);
            button.setNextFocusUpId(id);
        } else if (id == R.id.shortcut_settings) {
            button.setNextFocusLeftId(R.id.shortcut_scenes);
            button.setNextFocusRightId(R.id.shortcut_standby);
            button.setNextFocusDownId(R.id.weather_box);
            button.setNextFocusUpId(id);
        } else if (id == R.id.shortcut_standby) {
            button.setNextFocusLeftId(R.id.shortcut_settings);
            button.setNextFocusRightId(id);
            button.setNextFocusDownId(R.id.weather_box);
            button.setNextFocusUpId(id);
        } else {
            button.setNextFocusLeftId(R.id.shortcut_scenes);
            button.setNextFocusRightId(id);
            button.setNextFocusDownId(R.id.weather_box);
            button.setNextFocusUpId(id);
        }
    }

    private TextView text(String value, float size, int color, Typeface face) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setTypeface(face);
        view.setIncludeFontPadding(false);
        view.setShadowLayer(dp(1.5f), 0, dp(1), 0x66000000);
        return view;
    }

    private LinearLayout.LayoutParams wrap() { return new LinearLayout.LayoutParams(-2, -2); }
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
    private SharedPreferences preferences() { return getSharedPreferences(PREFS, MODE_PRIVATE); }

    private void updateClock() {
        if (clockView == null || dateView == null) return;
        // IANA Etc/GMT signs are reversed: Etc/GMT+3 is fixed UTC-03:00.
        TimeZone displayZone = TimeZone.getTimeZone("Etc/GMT+3");
        Calendar now = Calendar.getInstance(displayZone);
        clockView.setText(String.format(Locale.getDefault(), "%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));
        SimpleDateFormat homeDateFormat = new SimpleDateFormat("EEEE '·' dd/MM/yyyy",
                new Locale("pt", "BR"));
        homeDateFormat.setTimeZone(displayZone);
        String date = homeDateFormat.format(now.getTime());
        if (date.length() > 0) date = date.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + date.substring(1);
        dateView.setText(date);
        if (saverClockView != null) {
            saverClockView.setText(clockView.getText());
            SimpleDateFormat saverDateFormat = new SimpleDateFormat(
                    "EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
            saverDateFormat.setTimeZone(displayZone);
            String saverDate = saverDateFormat.format(now.getTime());
            saverDate = saverDate.replace(".,", ",");
            if (saverDate.length() > 0) saverDate = saverDate.substring(0, 1)
                    .toUpperCase(new Locale("pt", "BR")) + saverDate.substring(1);
            saverDateView.setText(saverDate);
        }
    }

    private void updateWeatherDisplay(String status) {
        if (cityView == null) return;
        SharedPreferences prefs = preferences();
        String city = prefs.getString(PREF_CITY, DEFAULT_CITY);
        String cached = prefs.getString(PREF_WEATHER, "");
        cityView.setText(compactCityName(city));
        long updatedAt = prefs.getLong(PREF_TIME, 0L);
        if (weatherUpdatedView != null) {
            String updated = updatedAt > 0
                    ? android.text.format.DateFormat.format("'Atualizado' HH:mm", updatedAt).toString()
                    : "Sem atualização";
            weatherUpdatedView.setText(updated);
            weatherUpdatedView.setContentDescription(updatedAt > 0
                    ? "Última atualização do clima às "
                            + android.text.format.DateFormat.format("HH:mm", updatedAt)
                    : "Clima ainda não atualizado");
        }
        if (highLowMaxView != null && highLowMinView != null) {
            if (prefs.contains(PREF_TEMP_MAX) && prefs.contains(PREF_TEMP_MIN)) {
                highLowMaxView.setText(prefs.getInt(PREF_TEMP_MAX, 0) + "°");
                highLowMinView.setText(prefs.getInt(PREF_TEMP_MIN, 0) + "°");
            } else {
                highLowMaxView.setText("");
                highLowMinView.setText("");
            }
        }
        if (!TextUtils.isEmpty(prefs.getString(PREF_TEMP, ""))) {
            String temperature = prefs.getString(PREF_TEMP, "--°C");
            if (temperature.endsWith("°")) temperature += "C";
            temperatureView.setText(temperature);
            weatherDescriptionView.setText(status == null
                    ? prefs.getString(PREF_DESC, "Condição indisponível") : status);
            setWeatherIcon(prefs.getInt(PREF_CODE, -1));
        } else if (!TextUtils.isEmpty(cached)) {
            String[] parts = cached.split("·", 2);
            String temperature = parts[0].trim();
            if (temperature.endsWith("°")) temperature += "C";
            temperatureView.setText(temperature);
            weatherDescriptionView.setText(status == null && parts.length > 1 ? parts[1].trim()
                    : (status == null ? "Último dado salvo" : status));
            setWeatherIcon(prefs.getInt(PREF_CODE, -1));
        } else {
            temperatureView.setText("--°");
            weatherDescriptionView.setText(status == null ? "Toque para atualizar" : status);
            setWeatherIcon(-1);
        }
        cardDescription(city, temperatureView.getText().toString(), weatherDescriptionView.getText().toString());
        updateIdleScreensaverContent();
    }

    private String compactCityName(String city) {
        if (TextUtils.isEmpty(city)) return DEFAULT_CITY;
        int separator = city.indexOf(',');
        return (separator < 0 ? city : city.substring(0, separator)).trim();
    }

    private void setWeatherIcon(int code) {
        currentWeatherCode = code;
        if (weatherGlyph != null) weatherGlyph.setImageResource(weatherDrawableForCode(code));
        if (saverWeatherGlyph != null)
            saverWeatherGlyph.setImageResource(screensaverWeatherDrawableForCode(code));
        if (weatherCard != null) weatherCard.setBackgroundDrawable(weatherGlassBackground(weatherCard.hasFocus()));
    }

    private int screensaverWeatherDrawableForCode(int code) {
        if (code == 0) return R.drawable.weather_sunny;
        if (code == 1 || code == 2) return R.drawable.weather_partly_cloudy_day;
        if (code == 45 || code == 48) return R.drawable.weather_foggy;
        if ((code >= 71 && code <= 77) || code == 85 || code == 86)
            return R.drawable.weather_snowy;
        if (code >= 95) return R.drawable.weather_thunderstorm;
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82))
            return R.drawable.weather_rainy;
        return R.drawable.weather_cloud;
    }

    private int weatherDrawableForCode(int code) {
        if (code == 0) return R.drawable.weather_sunny;
        if (code == 1 || code == 2) return R.drawable.weather_3d_partly;
        if (code == 45 || code == 48) return R.drawable.weather_foggy;
        if ((code >= 71 && code <= 77) || code == 85 || code == 86)
            return R.drawable.weather_snowy;
        if (code >= 95) return R.drawable.weather_thunderstorm;
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82))
            return R.drawable.weather_3d_rainy;
        return R.drawable.weather_3d_cloud;
    }

    private void cardDescription(String city, String temperature, String condition) {
        View card = findViewById(R.id.weather_box);
        if (card != null) card.setContentDescription("Clima em " + city + ": " + temperature + ", " + condition + ". OK atualiza; pressione e segure para trocar a cidade.");
    }

    private void askForCity() {
        try {
            final EditText input = new EditText(this);
            input.setSingleLine(true);
            input.setHint("Ex.: Joinville, SC");
            input.setText(preferences().getString(PREF_CITY, DEFAULT_CITY));
            new AlertDialog.Builder(this).setTitle("Cidade do clima")
                    .setMessage("A previsão usa Open-Meteo por HTTPS e fica em cache neste aparelho.")
                    .setView(input).setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            try {
                                String city = input.getText().toString().trim();
                                if (city.length() == 0) return;
                                preferences().edit().putString(PREF_CITY, city).remove(PREF_WEATHER)
                                        .remove(PREF_TEMP).remove(PREF_TEMP_MAX).remove(PREF_TEMP_MIN)
                                        .remove(PREF_DAILY_FETCHED).remove(PREF_DESC).remove(PREF_WEATHER_ATTEMPT)
                                        .putLong(PREF_TIME, 0).apply();
                                updateWeatherDisplay("Buscando previsão…");
                                refreshWeather(true, true);
                            } catch (Throwable t) { Log.e(TAG, "Could not save weather city", t); toast("Não foi possível salvar a cidade."); }
                        }
                    }).show();
        } catch (Throwable t) { Log.e(TAG, "Could not open the city selector", t); toast("Clima: não foi possível abrir as opções."); }
    }

    private void refreshWeather(boolean force, boolean reportFailure) {
        refreshWeather(force, reportFailure, false);
    }

    private void refreshWeather(boolean force, boolean reportFailure, boolean connectivityTrigger) {
        try {
            final SharedPreferences prefs = preferences();
            final String city = prefs.getString(PREF_CITY, DEFAULT_CITY);
            long now = System.currentTimeMillis();
            long lastSuccess = prefs.getLong(PREF_TIME, 0);
            if (weatherTask != null) {
                if (reportFailure) toast("Atualização do clima já em andamento.");
                return;
            }
            boolean dailyDataNotFetched = !prefs.getBoolean(PREF_DAILY_FETCHED, false);
            if (!force && !dailyDataNotFetched && lastSuccess > 0 && now >= lastSuccess
                    && now - lastSuccess < WEATHER_TTL_MS) return;
            if (!force && !isNetworkConnected()) return;
            if (!force && connectivityTrigger) {
                long lastAttempt = prefs.getLong(PREF_WEATHER_ATTEMPT, 0);
                if (lastAttempt > 0 && now >= lastAttempt && now - lastAttempt < WEATHER_CONNECTIVITY_RETRY_MS) return;
            }
            prefs.edit().putLong(PREF_WEATHER_ATTEMPT, now).apply();
            if (reportFailure || TextUtils.isEmpty(prefs.getString(PREF_WEATHER, "")))
                updateWeatherDisplay("Atualizando previsão…");
            Log.i(TAG, "Weather refresh started (manual=" + reportFailure + ")");
            weatherTask = new AsyncTask<Void, Void, WeatherResult>() {
                @Override protected WeatherResult doInBackground(Void... ignored) {
                    try { return fetchWeather(city, prefs); }
                    catch (Throwable t) { Log.w(TAG, "Weather request failed; using cached value", t); return WeatherResult.error("NETWORK"); }
                }
                @Override protected void onPostExecute(WeatherResult result) {
                    weatherTask = null;
                    try {
                        if (isFinishing()) return;
                        if (!city.equalsIgnoreCase(prefs.getString(PREF_CITY, DEFAULT_CITY))) {
                            refreshWeather(true, reportFailure);
                            return;
                        }
                        if (result != null && result.ok) {
                            Log.i(TAG, "Weather refresh succeeded; updating cached conditions");
                            SharedPreferences.Editor weatherEditor = prefs.edit()
                                    .putString(PREF_TEMP, result.temperature + "°C")
                                    .putString(PREF_DESC, result.description).putInt(PREF_CODE, result.code)
                                    .putString(PREF_WEATHER, result.temperature + "°C · " + result.description)
                                    .putBoolean(PREF_DAILY_FETCHED, true)
                                    .putLong(PREF_TIME, System.currentTimeMillis());
                            if (result.hasDailyRange) {
                                weatherEditor.putInt(PREF_TEMP_MAX, result.temperatureMax)
                                        .putInt(PREF_TEMP_MIN, result.temperatureMin);
                            } else {
                                weatherEditor.remove(PREF_TEMP_MAX).remove(PREF_TEMP_MIN);
                            }
                            weatherEditor.apply();
                            updateWeatherDisplay(null);
                        } else {
                            Log.w(TAG, "Weather refresh returned no usable data (" + (result == null ? "null" : result.error) + ")");
                            String cached = prefs.getString(PREF_WEATHER, "");
                            updateWeatherDisplay(TextUtils.isEmpty(cached)
                                    ? (result != null && "CITY_NOT_FOUND".equals(result.error) ? "Cidade não encontrada" : "Sem dados · toque para tentar")
                                    : "Último dado salvo · offline");
                            if (reportFailure) toast("Previsão indisponível. O último dado salvo foi mantido.");
                        }
                        schedulePeriodicWeatherRefresh();
                    } catch (Throwable t) { Log.e(TAG, "Weather display update failed", t); }
                }
                @Override protected void onCancelled(WeatherResult result) {
                    weatherTask = null;
                    schedulePeriodicWeatherRefresh();
                }
            }.execute();
        } catch (Throwable t) { weatherTask = null; Log.e(TAG, "Could not start weather refresh", t); if (reportFailure) toast("Previsão indisponível."); }
    }

    private WeatherResult fetchWeather(String city, SharedPreferences prefs) throws Exception {
        double lat = Double.NaN, lon = Double.NaN;
        if (city.equalsIgnoreCase(prefs.getString(PREF_COORD_CITY, ""))) {
            lat = Double.longBitsToDouble(prefs.getLong(PREF_LAT, 0));
            lon = Double.longBitsToDouble(prefs.getLong(PREF_LON, 0));
        }
        if (Double.isNaN(lat) || Double.isNaN(lon) || (lat == 0d && lon == 0d)) {
            String encoded = URLEncoder.encode(city, "UTF-8");
            JSONObject geocode = new JSONObject(readHttps(
                    "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=1&language=pt&format=json"));
            JSONArray results = geocode.optJSONArray("results");
            if (results == null || results.length() == 0) return WeatherResult.error("CITY_NOT_FOUND");
            JSONObject place = results.getJSONObject(0);
            lat = place.getDouble("latitude"); lon = place.getDouble("longitude");
        }
        String forecast = readHttps("https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                + "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min"
                + "&forecast_days=1&temperature_unit=celsius&timezone=auto");
        JSONObject response = new JSONObject(forecast);
        JSONObject current = response.getJSONObject("current");
        WeatherResult result = new WeatherResult();
        result.ok = true;
        result.temperature = (int)Math.round(current.getDouble("temperature_2m"));
        result.code = current.getInt("weather_code");
        result.description = describeWeather(result.code);
        JSONObject daily = response.optJSONObject("daily");
        if (daily != null) {
            JSONArray maximums = daily.optJSONArray("temperature_2m_max");
            JSONArray minimums = daily.optJSONArray("temperature_2m_min");
            if (maximums != null && minimums != null && maximums.length() > 0 && minimums.length() > 0
                    && !maximums.isNull(0) && !minimums.isNull(0)) {
                double maximum = maximums.getDouble(0);
                double minimum = minimums.getDouble(0);
                if (!Double.isNaN(maximum) && !Double.isInfinite(maximum)
                        && !Double.isNaN(minimum) && !Double.isInfinite(minimum)) {
                    result.temperatureMax = (int)Math.round(maximum);
                    result.temperatureMin = (int)Math.round(minimum);
                    result.hasDailyRange = true;
                }
            }
        }
        prefs.edit().putLong(PREF_LAT, Double.doubleToLongBits(lat)).putLong(PREF_LON, Double.doubleToLongBits(lon))
                .putString(PREF_COORD_CITY, city).apply();
        return result;
    }

    private String readHttps(String address) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection)new URL(address).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "PhilcoOS-Ultra777/1.0 (Android 4.4)");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Connection", "close");
            if (connection instanceof HttpsURLConnection)
                ((HttpsURLConnection)connection).setSSLSocketFactory(new Tls12SocketFactory(getApplicationContext()));
            else throw new SecurityException("Weather provider must use HTTPS");
            int response = connection.getResponseCode();
            if (response < 200 || response >= 300) throw new java.io.IOException("Weather HTTP status " + response);
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (body.length() + line.length() > 128 * 1024) throw new java.io.IOException("Weather response too large");
                body.append(line);
            }
            return body.toString();
        } finally {
            if (reader != null) try { reader.close(); } catch (Throwable ignored) { }
            if (connection != null) connection.disconnect();
        }
    }

    private String describeWeather(int code) {
        if (code == 0) return "Céu limpo";
        if (code == 1) return "Predomínio de sol";
        if (code == 2) return "Parcialmente nublado";
        if (code == 3) return "Nublado";
        if (code == 45 || code == 48) return "Neblina";
        if (code >= 51 && code <= 57) return "Garoa";
        if (code >= 61 && code <= 67) return "Chuva";
        if (code >= 71 && code <= 77) return "Neve";
        if (code >= 80 && code <= 82) return "Pancadas de chuva";
        if (code >= 85 && code <= 86) return "Neve";
        if (code >= 95) return "Trovoadas";
        return "Condição variável";
    }

    private void showScenePicker() {
        try {
            final String[] scenes = {"Lago alpino", "Luz dourada", "Mar de nuvens",
                    "Árvore ao entardecer", "Luz entre nuvens", "Montanha ao nascer do sol",
                    "Montanha azul", "Montanha noturna", "Lago glacial", "Leão monocromático",
                    "Montanhas douradas", "Cruz na neblina", "Céu violeta", "Mar ao entardecer",
                    "Praia violeta"};
            int selected = preferences().getInt(PREF_SCENE, AuroraBackdropView.AURORA);
            new AlertDialog.Builder(this).setTitle("Paisagem da tela inicial")
                    .setSingleChoiceItems(scenes, selected, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            try {
                                preferences().edit().putInt(PREF_SCENE, which).apply();
                                backdrop.setScene(which);
                                dialog.dismiss();
                            } catch (Throwable t) { Log.e(TAG, "Could not change landscape", t); toast("Não foi possível trocar a paisagem."); }
                        }
                    }).setNegativeButton("Fechar", null).show();
        } catch (Throwable t) { Log.e(TAG, "Could not show landscape picker", t); toast("Paisagens indisponíveis."); }
    }

    private void updateSystemIndicators() {
        updateWifiIndicator();
        updateVolumeIndicator();
    }

    private void ensureQuickControlWorker() {
        if (quickControlWorker != null) return;
        quickControlThread = new HandlerThread("PhilcoRealtekControls");
        quickControlThread.start();
        quickControlWorker = new Handler(quickControlThread.getLooper());
    }

    private boolean isNetworkConnected() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo active = connectivity == null ? null : connectivity.getActiveNetworkInfo();
            return active != null && active.isConnected();
        } catch (Throwable t) {
            Log.w(TAG, "Could not read network connectivity", t);
            return false;
        }
    }

    private void updateWifiIndicator() {
        try {
            WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi == null) {
                setWifiSignal(WifiSignal.unavailable());
                return;
            }
            if (!wifi.isWifiEnabled()) {
                setWifiSignal(WifiSignal.disconnected());
                return;
            }
            ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetwork = connectivity == null ? null
                    : connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            WifiInfo info = wifi.getConnectionInfo();
            boolean connected = wifiNetwork == null
                    ? info != null && info.getNetworkId() >= 0
                    : wifiNetwork.isConnected();
            if (!connected) {
                setWifiSignal(WifiSignal.disconnected());
                return;
            }
            setWifiSignal(info == null ? WifiSignal.unavailable() : WifiSignal.fromRssi(info.getRssi()));
        } catch (Throwable t) {
            Log.w(TAG, "Could not read Wi-Fi state", t);
            setWifiSignal(WifiSignal.unavailable());
        }
    }

    private void setWifiSignal(WifiSignal signal) {
        wifiSignal = signal == null ? WifiSignal.unavailable() : signal;
        applyWifiSignalToViews();
        updateIdleScreensaverContent();
    }

    private int wifiDrawableForBars(int bars) {
        if (bars <= 0) return R.drawable.philco_wifi_off;
        switch (bars) {
            case 1: return R.drawable.philco_wifi_1;
            case 2: return R.drawable.philco_wifi_2;
            case 3: return R.drawable.philco_wifi_3;
            case 4: return R.drawable.philco_wifi_4;
            default: return R.drawable.philco_wifi_off;
        }
    }

    private void applyWifiSignalToViews() {
        if (wifiSignal == null) return;
        int image = wifiDrawableForBars(wifiSignal.bars);
        String label = wifiSignal.label;
        if (wifiGlyph != null) {
            wifiGlyph.setImageResource(image);
            wifiGlyph.setContentDescription(label);
        }
        if (wifiStatusView != null) wifiStatusView.setText(label);
        if (saverWifiGlyph != null) {
            saverWifiGlyph.setImageResource(image);
            saverWifiGlyph.setContentDescription(label);
        }
        if (saverWifiStatusView != null) saverWifiStatusView.setText(label);
    }

    @SuppressLint("WrongConstant") // "tv" is Realtek's vendor Context service name.
    private void updateVolumeIndicator() {
        try {
            ensureQuickControlWorker();
            quickControlWorker.post(new Runnable() {
                @Override public void run() {
                    try {
                        Object tvManager = getSystemService("tv");
                        AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                        final int current = getMediaVolumePercent(audio);
                        final boolean muted = isRealtekMuted(tvManager, audio, -1);
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if (volumeGlyph == null || volumeStatusView == null || isFinishing()) return;
                                boolean silent = muted || current == 0;
                                // Compact single-wave icon through 50%; two waves only above half volume.
                                volumeGlyph.setLevel(silent ? 0 : (current <= 50 ? 2 : 4));
                                volumeGlyph.setMuted(silent);
                                 updateMuteOverlay(silent);
                                volumeStatusView.setText((muted ? "Mudo · " : "") + current + " / 100");
                                volumeGlyph.setContentDescription("Volume da TV " + current + " de 100"
                                        + (muted ? ", mudo" : ""));
                                updateIdleScreensaverContent();
                            }
                        });
                    } catch (final Throwable t) {
                        Log.w(TAG, "Could not read Realtek TV volume", t);
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if (volumeGlyph != null && volumeStatusView != null && !isFinishing()) {
                                    volumeGlyph.setLevel(0);
                                    volumeGlyph.setMuted(false);
                                     updateMuteOverlay(false);
                                    volumeStatusView.setText("Áudio indisponível");
                                    updateIdleScreensaverContent();
                                }
                            }
                        });
                    }
                }
            });
        } catch (Throwable t) { Log.w(TAG, "Could not queue Realtek volume read", t); }
    }

    private void updateMuteOverlay(boolean visible) {
        if (muteOverlay != null) muteOverlay.setVisibility(visible && !isIdleScreensaverVisible()
                ? View.VISIBLE : View.GONE);
    }

    private void registerSystemReceiver() {
        try {
            if (systemReceiver != null) return;
            systemReceiver = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    try {
                        String action = intent == null ? "" : intent.getAction();
                        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                            dismissQuickControlsForStandby("screen off broadcast");
                        } else if (VOLUME_CHANGED_ACTION.equals(action)) {
                            enforcePersistedMediaMute();
                            updateVolumeIndicator();
                        }
                        else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                            updateWifiIndicator();
                            if (isNetworkConnected()) {
                                foregroundHandler.removeCallbacks(connectivityWeatherRefresh);
                                foregroundHandler.postDelayed(connectivityWeatherRefresh,
                                        WEATHER_CONNECTIVITY_DEBOUNCE_MS);
                            }
                        }
                        else updateWifiIndicator();
                    } catch (Throwable t) { Log.w(TAG, "System status update failed", t); }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(VOLUME_CHANGED_ACTION);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(systemReceiver, filter);
        } catch (Throwable t) { systemReceiver = null; Log.w(TAG, "Could not register status receiver", t); }
    }

    private void registerTimeReceiver() {
        try {
            if (timeReceiver != null) return;
            timeReceiver = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    try { updateClock(); } catch (Throwable t) { Log.w(TAG, "Clock update failed", t); }
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(timeReceiver, filter);
        } catch (Throwable t) { timeReceiver = null; Log.w(TAG, "Could not register clock receiver", t); }
    }

    @Override protected void onResume() {
        super.onResume();
        try {
            homeActive = true;
            PlayerStateRepository playbackRepository = PlayerStateRepository.get();
            playbackRepository.addObserver(homePlaybackObserver);
            PlaybackState playback = playbackRepository.getState();
            updateNowPlaying(playback);
            if (playback != null && playback.track != null
                    && (playback.status == PlaybackState.Status.PLAYING
                    || playback.status == PlaybackState.Status.PAUSED)) {
                try {
                    homeMusicBound = bindService(new Intent(this, MusicPlaybackService.class),
                            homeMusicConnection, Context.BIND_AUTO_CREATE);
                } catch (Throwable t) { homeMusicBound = false; Log.w(TAG, "Home mini-player bind failed", t); }
            }
            int pendingBacklight = preferences().getInt(PREF_SAVER_BACKLIGHT_ORIGINAL, -1);
            if (pendingBacklight >= 0) {
                saverOriginalBacklight = pendingBacklight;
                restoreBacklightAfterIdle();
            }
            lastHomeInteraction = SystemClock.elapsedRealtime();
            scheduleHomeIdleMonitor();
            registerTimeReceiver(); registerSystemReceiver();
            recoverPersistedMediaMute();
            updateClock(); updateSystemIndicators(); updateSourceIndicator();
            if (refreshWeatherWhenHomeResumes) {
                refreshWeatherWhenHomeResumes = false;
                refreshWeather(true, false);
            } else {
                refreshWeather(false, false);
            }
            schedulePeriodicWeatherRefresh();
            if (returnFocusToMusicEntry) {
                returnFocusToMusicEntry = false;
                final View musicEntry = findViewById(R.id.tile_settings);
                if (musicEntry != null) musicEntry.post(new Runnable() {
                    @Override public void run() {
                        if (!isIdleScreensaverVisible()) musicEntry.requestFocus();
                    }
                });
            }
        } catch (Throwable t) { Log.e(TAG, "Resume recovery failed", t); }
    }

    @Override protected void onPause() {
        dismissQuickControlsForStandby("activity pause");
        homeActive = false;
        PlayerStateRepository.get().removeObserver(homePlaybackObserver);
        foregroundHandler.removeCallbacks(homePlaybackProgressTick);
        if (homeMusicBound) {
            try { unbindService(homeMusicConnection); } catch (Throwable t) { Log.w(TAG, "Mini-player unbind failed", t); }
            homeMusicBound = false;
        }
        homeMusicService = null;
        foregroundHandler.removeCallbacks(homeIdleMonitor);
        foregroundHandler.removeCallbacks(periodicWeatherRefresh);
        foregroundHandler.removeCallbacks(connectivityWeatherRefresh);
        if (isIdleScreensaverVisible()) finishDismissIdleScreensaver();
        else restoreBacklightAfterIdle();
        unregisterSafely(timeReceiver); timeReceiver = null;
        if (!preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)) {
            unregisterSafely(systemReceiver); systemReceiver = null;
        }
        if (weatherTask != null) {
            try { weatherTask.cancel(true); } catch (Throwable t) { Log.w(TAG, "Weather task cancellation failed", t); }
            weatherTask = null;
        }
        super.onPause();
    }

    @Override protected void onDestroy() {
        try {
            foregroundHandler.removeCallbacks(homeIdleMonitor);
            foregroundHandler.removeCallbacks(homePlaybackProgressTick);
            if (homeMusicBound) {
                try { unbindService(homeMusicConnection); } catch (Throwable ignored) { }
                homeMusicBound = false;
            }
            restoreBacklightAfterIdle();
            unregisterSafely(systemReceiver); systemReceiver = null;
            unregisterSafely(timeReceiver); timeReceiver = null;
            // Let the queued backlight restoration run before the worker exits.
            if (quickControlThread != null) quickControlThread.quitSafely();
        } catch (Throwable t) { Log.w(TAG, "Could not stop quick-control worker", t); }
        super.onDestroy();
    }

    private void unregisterSafely(BroadcastReceiver receiver) {
        if (receiver == null) return;
        try { unregisterReceiver(receiver); } catch (Throwable t) { Log.w(TAG, "Receiver cleanup failed", t); }
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && volumeUpUnmuteKeyHeld) {
            volumeUpUnmuteKeyHeld = false;
            return true;
        }
        if (preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)
                && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) return true;
        return super.onKeyUp(keyCode, event);
    }

    private void recoverPersistedMediaMute() {
        if (!preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)) return;
        ensureQuickControlWorker();
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    SharedPreferences prefs = preferences();
                    if (!prefs.getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)) return;
                    int saved = prefs.getInt(PREF_MEDIA_MUTE_VOLUME, -1);
                    if (saved <= 0) throw new IllegalStateException("Persisted mute has no valid restore volume");
                    AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    int current = getMediaVolumeIndex(audio);
                    if (current != 0) setMediaVolumeIndex(audio, 0);
                    int readBack = getMediaVolumeIndex(audio);
                    Log.i(TAG, "Persisted media mute recovery: saved=" + saved + ", before=" + current
                            + ", readBack=" + readBack);
                    if (readBack != 0) throw new IllegalStateException("Could not reapply persisted media mute");
                } catch (Throwable t) { Log.e(TAG, "Persisted media mute recovery failed", t); }
            }
        });
    }

    private void enforcePersistedMediaMute() {
        if (!preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)) return;
        ensureQuickControlWorker();
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    SharedPreferences prefs = preferences();
                    if (!prefs.getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)) return;
                    AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    int current = getMediaVolumeIndex(audio);
                    if (current != 0) {
                        int saved = prefs.getInt(PREF_MEDIA_MUTE_VOLUME, -1);
                        if (saved > 0) {
                            setMediaVolumeIndex(audio, saved);
                            if (getMediaVolumeIndex(audio) != saved)
                                throw new IllegalStateException("Could not restore saved media volume after volume-up");
                            Log.i(TAG, "Positive STREAM_MUSIC volume cleared persisted mute and restored saved index=" + saved);
                        } else {
                            Log.w(TAG, "Positive STREAM_MUSIC volume cleared persisted mute; saved index unavailable");
                        }
                        if (!prefs.edit().putBoolean(PREF_MEDIA_MUTE_ACTIVE, false)
                                .remove(PREF_MEDIA_MUTE_VOLUME).commit())
                            throw new IllegalStateException("Could not clear persisted media mute after volume-up");
                        return;
                    }
                    if (getMediaVolumeIndex(audio) != 0)
                        throw new IllegalStateException("Could not maintain persisted media mute");
                } catch (Throwable t) { Log.e(TAG, "Could not maintain persisted media mute", t); }
            }
        });
    }

    private void launchSmartTube() {
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage("org.smarttube.stable");
            startExternal(intent, "SmartTube não encontrado. Instale uma versão compatível.");
        } catch (Throwable t) { Log.e(TAG, "SmartTube launch failed", t); toast("Não foi possível abrir o SmartTube."); }
    }

    private void launchTv() {
        openVerifiedActivity("com.tsb.tv", "com.tsb.tv.Tv_strategy",
                "O aplicativo original de TV digital não está disponível.");
    }

    private void launchTvStrategy(String input) {
        try {
            // Match the OEM SourceMenu: explicit component, empty Intent, NEW_TASK only.
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName("com.tsb.tv", "com.tsb.tv.Tv_strategy"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(getPackageManager()) == null)
                throw new IllegalStateException("Tv_strategy is not resolvable");
            Log.i(TAG, "Launching OEM Tv_strategy for " + input + " after source dispatch/persistence");
            startActivity(intent);
        } catch (Throwable t) {
            Log.e(TAG, "Could not launch Realtek DVO activity for " + input, t);
            toast("A entrada foi selecionada, mas a tela de vídeo da TV não abriu.");
            openSourceMenu(input);
        }
    }

    private void updateSourceIndicator() {
        try {
            if (brandView == null) return;
            int source = Settings.System.getInt(getContentResolver(), "PlayingSource", -1);
            String name;
            switch (source) {
                case 1: name = "TV A CABO"; break;
                case 5: name = "AV"; break;
                case 17: name = "HDMI 2"; break;
                case 18: name = "HDMI 1"; break;
                case 27: name = "TV ABERTA"; break;
                default: name = null;
            }
            brandView.setText(name == null ? "PHILCO  /  ULTRA 777"
                    : "PHILCO  /  ULTRA 777     ·     " + name + " ATIVA");
        } catch (Throwable t) { Log.w(TAG, "Could not update active source label", t); }
    }

    private void openCableMode() {
        switchInputOrOpenMenu("TV a cabo", 1, true);
    }

    private void openSourceMenu(String requestedInput) {
        try {
            toast("Mova o foco para " + requestedInput + " e pressione OK no seletor.");
            openVerifiedActivity("com.hikeen.xymenu", "com.hikeen.xymenu.SourceMenu",
                    "O seletor original de entradas não está disponível.");
        } catch (Throwable t) { Log.e(TAG, "Source menu launch failed", t); toast("Não foi possível abrir o seletor de entradas."); }
    }

    /** IDs are taken from the factory SourceMenu DEX: cable=1, AV=5, HDMI2=17, HDMI1=18, AIR=27. */
    @SuppressLint("WrongConstant")
    private void switchInputOrOpenMenu(String input, int sourceId) {
        switchInputOrOpenMenu(input, sourceId, true);
    }

    @SuppressLint("WrongConstant")
    private void switchInputOrOpenMenu(final String input, final int sourceId, final boolean openTvOnSuccess) {
        try {
            Object tvManager = getSystemService("tv");
            if (tvManager == null) throw new IllegalStateException("Realtek TV service unavailable");
            Method setSource = tvManager.getClass().getMethod("setSource", Integer.TYPE);
            Object result = setSource.invoke(tvManager, Integer.valueOf(sourceId));
            Method getCurrentSource = tvManager.getClass().getMethod("getCurLiveSource");
            Log.i(TAG, "Requested Realtek source " + sourceId + " (" + input + "), result=" + result);
            try {
                boolean saved = Settings.System.putInt(getContentResolver(), "PlayingSource", sourceId);
                int readBack = Settings.System.getInt(getContentResolver(), "PlayingSource", -1);
                Log.i(TAG, "Realtek PlayingSource persist for " + input + ": write=" + saved
                        + ", readBack=" + readBack);
            } catch (Throwable t) { Log.w(TAG, "Could not persist Realtek PlayingSource", t); }
            try { sendBroadcast(new Intent(SOURCE_CHANGE_ACTION)); }
            catch (Throwable t) { Log.w(TAG, "Could not broadcast Realtek source change", t); }

            if (pendingInputVerification != null) sourceHandler.removeCallbacks(pendingInputVerification);
            final Object manager = tvManager;
            final Method currentSourceMethod = getCurrentSource;
            if (openTvOnSuccess) launchTvStrategy(input);
            else Log.w(TAG, "No OEM video activity requested for source " + input);
            toast("Abrindo entrada: " + input);
            pendingInputVerification = new Runnable() {
                private int attempts;
                @Override public void run() {
                    try {
                        int current = ((Integer)currentSourceMethod.invoke(manager)).intValue();
                        Log.i(TAG, "Realtek source check " + input + ": expected=" + sourceId + ", current=" + current);
                        if (current == sourceId) {
                            pendingInputVerification = null;
                            preferences().edit().putInt(PREF_LAST_INPUT, sourceId).apply();
                            updateSourceIndicator();
                            Log.i(TAG, "Realtek input verified after OEM launch: " + input + " (" + current + ")");
                            return;
                        }
                    } catch (Throwable t) {
                        pendingInputVerification = null;
                        Log.w(TAG, "Could not verify Realtek source " + input, t);
                        if (homeActive) toast("Não foi possível confirmar a entrada " + input + ".");
                        return;
                    }
                    if (++attempts < 20) sourceHandler.postDelayed(this, 500L);
                    else {
                        pendingInputVerification = null;
                        Log.w(TAG, "Realtek source did not reach expected ID " + sourceId + " for " + input);
                        if (homeActive) toast("A TV não confirmou a entrada " + input + ".");
                    }
                }
            };
            sourceHandler.postDelayed(pendingInputVerification, 500L);
        } catch (Throwable t) {
            Log.w(TAG, "Could not switch directly to " + input + "; opening OEM source menu", t);
            openSourceMenu(input);
        }
    }

    /** Compact D-pad control sheet backed by the TV's Realtek TV/audio service. */
    @SuppressLint("WrongConstant") // "tv" resolves to the Realtek TvManager on this firmware.
    private void showQuickControls() {
        try {
            ensureQuickControlWorker();
            final QuickControlViews controls = new QuickControlViews();
            controls.tvManager = getSystemService("tv");
            controls.audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if (controls.tvManager == null || controls.audio == null)
                throw new IllegalStateException("Realtek TV control service unavailable");
            final Dialog dialog = new Dialog(this);
            controls.dialog = dialog;
            quickControlsDialog = dialog;
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override public void onDismiss(DialogInterface dismissed) {
                    if (quickControlsDialog == dialog) quickControlsDialog = null;
                }
            });
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            LinearLayout panel = new LinearLayout(this);
            panel.setOrientation(LinearLayout.VERTICAL);
            panel.setPadding(dp(26), dp(18), dp(26), dp(16));
            panel.setBackgroundDrawable(panelGlassBackground());

            TextView title = text("Central de controle", 25, Color.WHITE,
                    Typeface.create("sans-serif-medium", Typeface.NORMAL));
            panel.addView(title, wrap());
            controls.volumeValue = addQuickSliderSection(panel, "Volume da TV", TvGlyphView.VOLUME,
                    "Volume da televisão, de zero a cem", controls, 0xFF80DEEA);
            controls.volumeSlider = lastQuickSlider;
            controls.brightnessValue = addQuickSliderSection(panel, "Brilho da imagem", TvGlyphView.BRIGHTNESS,
                    "Brilho da imagem, de zero a cem", controls, 0xFFFFD25E);
            controls.brightnessSlider = lastQuickSlider;
            controls.backlightValue = addQuickSliderSection(panel, "Retroiluminação do painel", TvGlyphView.BACKLIGHT,
                    "Retroiluminação do painel, de zero a cem", controls, 0xFF70DCD2);
            controls.backlightSlider = lastQuickSlider;

            controls.muteButton = quickPanelButton("Mute: lendo estado…", 0xFF80DEEA);
            controls.muteButton.setEnabled(false);
            controls.muteButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (!controls.ready) return;
                    quickControlWorker.post(new Runnable() {
                        @Override public void run() {
                            try {
                                boolean requested = !isRealtekMuted(controls.tvManager, controls.audio, controls.sourceType);
                                setRealtekMute(controls.tvManager, controls.audio, requested, controls.sourceType);
                                final boolean actual = isRealtekMuted(controls.tvManager, controls.audio, controls.sourceType);
                                runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        if (!controls.dialog.isShowing() || isFinishing()) return;
                                        if (actual != requested) {
                                            toast("A TV não confirmou o estado físico de Mute.");
                                            return;
                                        }
                                        controls.muteButton.setText(actual ? "Ativar som" : "Silenciar");
                                        controls.muteButton.setContentDescription(actual
                                                ? "Ativar som da televisão" : "Silenciar a televisão");
                                        controls.volumeIcon.setLevel(actual || controls.volumeSlider.getProgress() == 0
                                                ? 0 : Math.max(1, controls.volumeSlider.getProgress() * 4 / 100));
                                        controls.volumeIcon.setMuted(actual || controls.volumeSlider.getProgress() == 0);
                                        updateVolumeIndicator();
                                    }
                                });
                            } catch (final Throwable t) {
                                Log.e(TAG, "Realtek mute toggle failed", t);
                                runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        if (controls.dialog.isShowing() && !isFinishing())
                                            toast("Mute da TV indisponível.");
                                    }
                                });
                            }
                        }
                    });
                }
            });
            LinearLayout.LayoutParams muteParams = new LinearLayout.LayoutParams(-1, dp(44));
            muteParams.topMargin = dp(3);
            panel.addView(controls.muteButton, muteParams);

            final TextView tvSettingsButton = quickPanelButton("Configurações da TV", 0xFFF0D693);
            controls.settingsButton = tvSettingsButton;
            LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(-1, dp(46));
            settingsParams.topMargin = dp(9);
            panel.addView(tvSettingsButton, settingsParams);
            tvSettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    dialog.dismiss();
                    launchSettings();
                }
            });
            final TextView pictureSettingsButton = quickPanelButton("Imagem", 0xFFFFD25E);
            final TextView soundSettingsButton = quickPanelButton("Som", 0xFF70DCD2);
            LinearLayout oemSettingsRow = new LinearLayout(this);
            oemSettingsRow.setOrientation(LinearLayout.HORIZONTAL);
            oemSettingsRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams pictureParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
            pictureParams.rightMargin = dp(4);
            oemSettingsRow.addView(pictureSettingsButton, pictureParams);
            LinearLayout.LayoutParams soundParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
            soundParams.leftMargin = dp(4);
            oemSettingsRow.addView(soundSettingsButton, soundParams);
            LinearLayout.LayoutParams oemSettingsParams = new LinearLayout.LayoutParams(-1, dp(44));
            oemSettingsParams.topMargin = dp(6);
            panel.addView(oemSettingsRow, oemSettingsParams);
            pictureSettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (launchOemTvSettings("picture")) dialog.dismiss();
                }
            });
            soundSettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (launchOemTvSettings("sound")) dialog.dismiss();
                }
            });
            final TextView restoreInputButton = quickPanelButton("Ao ligar: restaurar última entrada", 0xFF80DEEA);
            final Runnable updateRestoreInputLabel = new Runnable() {
                @Override public void run() {
                    boolean enabled = preferences().getBoolean(PREF_RESTORE_LAST_INPUT, true);
                    restoreInputButton.setText(enabled
                            ? "Ao ligar: restaurar última entrada  ✓"
                            : "Ao ligar: manter entrada padrão");
                    restoreInputButton.setContentDescription(enabled
                            ? "Restauração da última entrada ao ligar ativada"
                            : "Restauração da última entrada ao ligar desativada");
                }
            };
            updateRestoreInputLabel.run();
            LinearLayout.LayoutParams restoreInputParams = new LinearLayout.LayoutParams(-1, dp(44));
            restoreInputParams.topMargin = dp(7);
            panel.addView(restoreInputButton, restoreInputParams);
            restoreInputButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    boolean enabled = !preferences().getBoolean(PREF_RESTORE_LAST_INPUT, true);
                    preferences().edit().putBoolean(PREF_RESTORE_LAST_INPUT, enabled).apply();
                    updateRestoreInputLabel.run();
                    toast(enabled ? "A última entrada será restaurada ao ligar."
                            : "A restauração automática da entrada foi desativada.");
                }
            });
            final TextView powerActionsButton = hasDevicePowerPermission()
                    ? quickPanelButton("Energia da TV", 0xFFFFB977) : null;
            if (powerActionsButton != null) {
                LinearLayout.LayoutParams powerParams = new LinearLayout.LayoutParams(-1, dp(44));
                powerParams.topMargin = dp(6);
                panel.addView(powerActionsButton, powerParams);
                powerActionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) { showPowerActions(); }
                });
            }
            TextView footer = text("← → ajustar    OK selecionar    VOLTAR fechar", 12,
                    0xFFA8B5C8, Typeface.create("sans-serif", Typeface.NORMAL));
            LinearLayout.LayoutParams footerParams = wrap();
            footerParams.topMargin = dp(8);
            panel.addView(footer, footerParams);

            controls.volumeSlider.setNextFocusUpId(controls.volumeSlider.getId());
            controls.volumeSlider.setNextFocusDownId(controls.brightnessSlider.getId());
            controls.brightnessSlider.setNextFocusUpId(controls.volumeSlider.getId());
            controls.brightnessSlider.setNextFocusDownId(controls.backlightSlider.getId());
            controls.backlightSlider.setNextFocusUpId(controls.brightnessSlider.getId());
            controls.backlightSlider.setNextFocusDownId(controls.muteButton.getId());
            controls.muteButton.setNextFocusUpId(controls.backlightSlider.getId());
            controls.muteButton.setNextFocusDownId(tvSettingsButton.getId());
            tvSettingsButton.setNextFocusUpId(controls.muteButton.getId());
            tvSettingsButton.setNextFocusDownId(pictureSettingsButton.getId());
            pictureSettingsButton.setNextFocusUpId(tvSettingsButton.getId());
            pictureSettingsButton.setNextFocusDownId(soundSettingsButton.getId());
            pictureSettingsButton.setNextFocusLeftId(pictureSettingsButton.getId());
            pictureSettingsButton.setNextFocusRightId(soundSettingsButton.getId());
            soundSettingsButton.setNextFocusUpId(pictureSettingsButton.getId());
            soundSettingsButton.setNextFocusDownId(restoreInputButton.getId());
            soundSettingsButton.setNextFocusLeftId(pictureSettingsButton.getId());
            soundSettingsButton.setNextFocusRightId(soundSettingsButton.getId());
            restoreInputButton.setNextFocusUpId(soundSettingsButton.getId());
            restoreInputButton.setNextFocusDownId(powerActionsButton == null
                    ? restoreInputButton.getId() : powerActionsButton.getId());
            restoreInputButton.setNextFocusLeftId(restoreInputButton.getId());
            restoreInputButton.setNextFocusRightId(restoreInputButton.getId());
            if (powerActionsButton != null) {
                powerActionsButton.setNextFocusUpId(restoreInputButton.getId());
                powerActionsButton.setNextFocusDownId(powerActionsButton.getId());
                powerActionsButton.setNextFocusLeftId(powerActionsButton.getId());
                powerActionsButton.setNextFocusRightId(powerActionsButton.getId());
            }
            controls.volumeSlider.setNextFocusLeftId(controls.volumeSlider.getId());
            controls.volumeSlider.setNextFocusRightId(controls.volumeSlider.getId());
            controls.brightnessSlider.setNextFocusLeftId(controls.brightnessSlider.getId());
            controls.brightnessSlider.setNextFocusRightId(controls.brightnessSlider.getId());
            controls.backlightSlider.setNextFocusLeftId(controls.backlightSlider.getId());
            controls.backlightSlider.setNextFocusRightId(controls.backlightSlider.getId());

            dialog.setContentView(panel);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams attrs = window.getAttributes();
                attrs.dimAmount = 0.66f;
                window.setAttributes(attrs);
                window.setLayout(Math.min(dp(650), getResources().getDisplayMetrics().widthPixels - dp(64)),
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
            controls.volumeSlider.requestFocus();
            loadQuickControlState(controls);
        } catch (Throwable t) {
            Log.e(TAG, "Could not open quick controls", t);
            toast("Central de controle indisponível.");
        }
    }

    private void dismissQuickControlsForStandby(String reason) {
        final Dialog dialog = quickControlsDialog;
        if (dialog == null) return;
        quickControlsDialog = null;
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
                Log.i(TAG, "Quick controls closed for standby: " + reason);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Could not close quick controls for standby: " + reason, t);
        }
    }

    private SeekBar lastQuickSlider;

    private TextView addQuickSliderSection(LinearLayout panel, String label, int glyph,
                                           String description, final QuickControlViews controls,
                                           int accent) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TvGlyphView icon = new TvGlyphView(this, glyph, accent);
        row.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView title = text(label, 14, 0xFFD8E3F2,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1f);
        titleParams.leftMargin = dp(9);
        row.addView(title, titleParams);
        TextView value = text("Lendo…", 13, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        row.addView(value, wrap());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(24));
        rowParams.topMargin = dp(8);
        panel.addView(row, rowParams);

        final SeekBar slider = new SeekBar(this);
        slider.setId(View.generateViewId());
        slider.setMax(100);
        slider.setProgressDrawable(quickSliderDrawable());
        slider.setThumb(quickSliderThumb());
        slider.setPadding(dp(12), dp(4), dp(12), dp(4));
        slider.setFocusable(true);
        slider.setFocusableInTouchMode(true);
        slider.setContentDescription(description);
        slider.setKeyProgressIncrement(1);
        slider.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean focused) {
                slider.setThumb(quickSliderThumb(focused));
            }
        });
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(-1, dp(39));
        panel.addView(slider, sliderParams);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || !controls.ready) return;
                value.setText(progress + "%");
                queueRealtekSettingChange(controls, slider, value, progress, label);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        if ("Volume da TV".equals(label)) {
            controls.volumeIcon = icon;
            controls.volumeValue = value;
            lastQuickSlider = slider;
        } else if ("Brilho da imagem".equals(label)) {
            controls.brightnessValue = value;
            lastQuickSlider = slider;
        } else {
            controls.backlightValue = value;
            lastQuickSlider = slider;
        }
        return value;
    }

    private void loadQuickControlState(final QuickControlViews controls) {
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    final int source = getRealtekSourceType(controls.tvManager);
                    final int volume = getMediaVolumePercent(controls.audio);
                    final boolean muted = isRealtekMuted(controls.tvManager, controls.audio, source);
                    final int brightness = readRealtekValue(controls.tvManager, "getBrightness");
                    final int backlight = readRealtekValue(controls.tvManager, "getBacklight");
                    if (!validPictureLevel(brightness) || !validPictureLevel(backlight))
                        throw new IllegalStateException("Realtek picture level outside verified 0..100 range");
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!controls.dialog.isShowing() || isFinishing()) return;
                            controls.sourceType = source;
                            controls.ready = true;
                            controls.volumeSlider.setProgress(volume);
                            controls.brightnessSlider.setProgress(brightness);
                            controls.backlightSlider.setProgress(backlight);
                            controls.volumeValue.setText(volume + "%");
                            controls.brightnessValue.setText(brightness + "%");
                            controls.backlightValue.setText(backlight + "%");
                            controls.volumeIcon.setLevel(muted || volume == 0 ? 0 : Math.max(1, volume * 4 / 100));
                            controls.volumeIcon.setMuted(muted || volume == 0);
                            controls.muteButton.setText(muted ? "Ativar som" : "Silenciar");
                            controls.muteButton.setContentDescription(muted
                                    ? "Ativar som da televisão" : "Silenciar a televisão");
                            controls.muteButton.setEnabled(true);
                            controls.volumeSlider.requestFocus();
                        }
                    });
                } catch (final Throwable t) {
                    Log.e(TAG, "Could not read Realtek quick-control state", t);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!controls.dialog.isShowing() || isFinishing()) return;
                            controls.volumeValue.setText("indisponível");
                            controls.brightnessValue.setText("indisponível");
                            controls.backlightValue.setText("indisponível");
                            controls.volumeSlider.setEnabled(false);
                            controls.brightnessSlider.setEnabled(false);
                            controls.backlightSlider.setEnabled(false);
                            controls.muteButton.setText("Áudio da TV indisponível");
                            controls.muteButton.setEnabled(false);
                        }
                    });
                }
            }
        });
    }

    private void queueRealtekSettingChange(final QuickControlViews controls, final SeekBar slider,
                                          final TextView value, final int target, final String label) {
        quickControlWorker.post(new Runnable() {
            @Override public void run() {
                try {
                    int actual;
                    if ("Volume da TV".equals(label)) {
                        setRealtekVolume(controls.tvManager, controls.audio, target);
                        actual = getMediaVolumePercent(controls.audio);
                    } else {
                        String setter = "Brilho da imagem".equals(label) ? "setBrightness" : "setBacklight";
                        writeRealtekValue(controls.tvManager, setter, target);
                        actual = readRealtekValue(controls.tvManager,
                                "Brilho da imagem".equals(label) ? "getBrightness" : "getBacklight");
                    }
                    final int readBack = actual;
                    final boolean muted = isRealtekMuted(controls.tvManager, controls.audio, controls.sourceType);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!controls.dialog.isShowing() || isFinishing()) return;
                            if (readBack != target) toast(label + ": a TV não confirmou o valor.");
                            slider.setProgress(readBack);
                            value.setText(readBack + "%");
                            if ("Volume da TV".equals(label)) {
                                controls.volumeIcon.setLevel(muted || readBack == 0 ? 0 : Math.max(1, readBack * 4 / 100));
                                controls.volumeIcon.setMuted(muted || readBack == 0);
                                controls.muteButton.setText(muted ? "Ativar som" : "Silenciar");
                                controls.muteButton.setContentDescription(muted
                                        ? "Ativar som da televisão" : "Silenciar a televisão");
                                updateVolumeIndicator();
                            }
                        }
                    });
                } catch (final Throwable t) {
                    Log.e(TAG, "Realtek quick-control write failed: " + label, t);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!controls.dialog.isShowing() || isFinishing()) return;
                            value.setText("erro");
                            toast(label + " indisponível.");
                        }
                    });
                }
            }
        });
    }

    private GradientDrawable panelGlassBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xF3111B2A);
        background.setCornerRadius(dp(24));
        background.setStroke(dp(1), 0x667CB9E8);
        return background;
    }

    private Drawable quickSliderDrawable() {
        GradientDrawable track = new GradientDrawable();
        track.setColor(0x557C8EA5);
        track.setCornerRadius(dp(4));
        GradientDrawable fill = new GradientDrawable();
        fill.setColor(0xFF70DCD2);
        fill.setCornerRadius(dp(4));
        ClipDrawable clipped = new ClipDrawable(fill, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        LayerDrawable layers = new LayerDrawable(new Drawable[]{track, clipped});
        layers.setId(0, android.R.id.background);
        layers.setId(1, android.R.id.progress);
        return layers;
    }

    private Drawable quickSliderThumb() {
        return quickSliderThumb(false);
    }

    private Drawable quickSliderThumb(boolean focused) {
        GradientDrawable thumb = new GradientDrawable();
        thumb.setShape(GradientDrawable.OVAL);
        thumb.setColor(focused ? 0xFF70DCD2 : Color.WHITE);
        thumb.setStroke(dp(focused ? 3 : 2), focused ? Color.WHITE : 0xFF70DCD2);
        thumb.setSize(dp(focused ? 28 : 22), dp(focused ? 28 : 22));
        return thumb;
    }

    private TextView quickPanelButton(String label, int accent) {
        TextView button = text(label, 16, Color.WHITE,
                Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setClickable(true);
        button.setBackgroundDrawable(glassBackground(false, accent, 16));
        button.setOnFocusChangeListener(focusListener(accent, 16));
        button.setId(View.generateViewId());
        return button;
    }

    private boolean launchOemTvSettings(String setting) {
        String label = "sound".equals(setting) ? "Som" : "Imagem";
        try {
            Intent intent = new Intent();
            intent.setClassName("com.tsb.tv", "com.tsb.tv.LiteActivity");
            intent.putExtra("setting", setting);
            if (intent.resolveActivity(getPackageManager()) == null) {
                toast("Menu de " + label + " da TV indisponível.");
                return false;
            }
            startActivity(intent);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Could not open OEM TV " + label + " settings", t);
            toast("Não foi possível abrir o menu de " + label + " da TV.");
            return false;
        }
    }

    private int getRealtekSourceType(Object tvManager) throws Exception {
        Object result = tvManager.getClass().getMethod("getCurSourceType").invoke(tvManager);
        if (!(result instanceof Number)) throw new IllegalStateException("Invalid Realtek source type");
        return ((Number)result).intValue();
    }

    private int getMediaVolumeIndex(AudioManager audio) throws Exception {
        if (audio == null) throw new IllegalStateException("Android AudioManager unavailable");
        return audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private int getMediaVolumePercent(AudioManager audio) throws Exception {
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (max <= 0) throw new IllegalStateException("Invalid STREAM_MUSIC maximum volume");
        return Math.max(0, Math.min(100, Math.round(getMediaVolumeIndex(audio) * 100f / max)));
    }

    private int readRealtekValue(Object tvManager, String methodName) throws Exception {
        Object result = tvManager.getClass().getMethod(methodName).invoke(tvManager);
        if (!(result instanceof Number)) throw new IllegalStateException("Invalid Realtek " + methodName + " result");
        return ((Number)result).intValue();
    }

    private boolean validPictureLevel(int value) { return value >= 0 && value <= 100; }

    private void writeRealtekValue(Object tvManager, String methodName, int value) throws Exception {
        tvManager.getClass().getMethod(methodName, Integer.TYPE).invoke(tvManager, Integer.valueOf(value));
    }

    private boolean isRealtekMuted(Object tvManager, AudioManager audio, int sourceType) throws Exception {
        if (audio == null) throw new IllegalStateException("Android AudioManager unavailable");
        boolean requested = preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false);
        int current = getMediaVolumeIndex(audio);
        // API state alone is not acoustic proof. Our QCC state is valid only while the
        // actual SmartTube/media stream remains at zero through the OEM volume bridge.
        return requested && current == 0;
    }

    private void setRealtekMute(Object tvManager, AudioManager audio, boolean muted, int sourceType) throws Exception {
        if (audio == null) throw new IllegalStateException("Android AudioManager unavailable");
        SharedPreferences prefs = preferences();
        int before = getMediaVolumeIndex(audio);
        int saved = prefs.getInt(PREF_MEDIA_MUTE_VOLUME, -1);
        boolean mutePersisted = prefs.getBoolean(PREF_MEDIA_MUTE_ACTIVE, false);
        if (muted && mutePersisted) {
            if (before != 0) setMediaVolumeIndex(audio, 0);
            int enforced = getMediaVolumeIndex(audio);
            Log.i(TAG, "Repeated media mute: saved=" + saved + ", before=" + before + ", readBack=" + enforced);
            if (enforced != 0) throw new IllegalStateException("Could not reapply media mute");
            return;
        }
        if (muted) {
            if (before <= 0) throw new IllegalStateException("Cannot mute: media stream has no positive volume to preserve");
            if (!prefs.edit().putInt(PREF_MEDIA_MUTE_VOLUME, before).commit())
                throw new IllegalStateException("Could not persist the pre-mute media volume");
            try {
                setMediaVolumeIndex(audio, 0);
                int after = getMediaVolumeIndex(audio);
                Log.i(TAG, "Media mute: source=" + sourceType + ", stream=3, before=" + before + ", requested=0, readBack=" + after);
                if (after != 0) throw new IllegalStateException("Realtek media volume did not reach zero");
                if (!prefs.edit().putBoolean(PREF_MEDIA_MUTE_ACTIVE, true).commit())
                    throw new IllegalStateException("Could not persist media mute state");
            } catch (Throwable failure) {
                try { setMediaVolumeIndex(audio, before); } catch (Throwable restore) { failure.addSuppressed(restore); }
                prefs.edit().putBoolean(PREF_MEDIA_MUTE_ACTIVE, false).commit();
                if (failure instanceof Exception) throw (Exception)failure;
                throw new RuntimeException(failure);
            }
            return;
        }

        if (!prefs.getBoolean(PREF_MEDIA_MUTE_ACTIVE, false) && before != 0) return;
        if (saved <= 0) throw new IllegalStateException("Cannot unmute: saved media volume is unavailable");
        setMediaVolumeIndex(audio, saved);
        int after = getMediaVolumeIndex(audio);
        Log.i(TAG, "Media unmute: source=" + sourceType + ", stream=3, requested=" + saved + ", readBack=" + after);
        if (after != saved) throw new IllegalStateException("Previous media volume was not restored");
        if (!prefs.edit().putBoolean(PREF_MEDIA_MUTE_ACTIVE, false).remove(PREF_MEDIA_MUTE_VOLUME).commit())
            throw new IllegalStateException("Could not persist restored media state");
    }

    private void setMediaVolumeIndex(AudioManager audio, int index) throws Exception {
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (index < 0 || index > max) throw new IllegalArgumentException("Media volume index outside 0.." + max);
        try {
            audio.getClass().getMethod("setStreamVolumeForL4300", Integer.TYPE, Integer.TYPE,
                            Integer.TYPE, Boolean.TYPE)
                    .invoke(audio, Integer.valueOf(AudioManager.STREAM_MUSIC), Integer.valueOf(index),
                            Integer.valueOf(1), Boolean.TRUE);
        } catch (NoSuchMethodException unavailable) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, index, 1);
        }
    }

    /** Uses the same vendor AudioManager bridge used by the factory SourceMenu on this TV. */
    private void setRealtekVolume(Object tvManager, AudioManager audio, int volume) throws Exception {
        if (preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false))
            setRealtekMute(tvManager, audio, false, getRealtekSourceType(tvManager));
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int index = Math.max(0, Math.min(max, Math.round(volume * max / 100f)));
        setMediaVolumeIndex(audio, index);
        int actual = getMediaVolumePercent(audio);
        if (Math.abs(actual - volume) > Math.max(1, Math.round(100f / max)))
            throw new IllegalStateException("Media volume did not read back near requested percentage");
    }

    private static final class QuickControlViews {
        Dialog dialog;
        Object tvManager;
        AudioManager audio;
        SeekBar volumeSlider, brightnessSlider, backlightSlider;
        TextView volumeValue, brightnessValue, backlightValue, muteButton, settingsButton;
        TvGlyphView volumeIcon;
        int sourceType = -1;
        boolean ready;
    }

    private boolean hasDevicePowerPermission() {
        return checkCallingOrSelfPermission("android.permission.DEVICE_POWER")
                == PackageManager.PERMISSION_GRANTED;
    }

    private void showPowerActions() {
        if (!hasDevicePowerPermission()) {
            toast("O firmware não concedeu permissão para controlar a energia da TV.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Energia da TV")
                .setItems(new CharSequence[]{"Desligar tela / standby", "Reiniciar TV"},
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                confirmPowerAction(which == 1);
                            }
                        })
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void confirmPowerAction(final boolean reboot) {
        new AlertDialog.Builder(this)
                .setTitle(reboot ? "Reiniciar a TV?" : "Desligar a tela da TV?")
                .setMessage(reboot
                        ? "A televisão será reiniciada."
                        : "A TV entrará em standby. Para acordá-la, use o joystick/botão físico da TV.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        executePowerAction(reboot);
                    }
                })
                .show();
    }

    private void executePowerAction(boolean reboot) {
        if (!hasDevicePowerPermission()) {
            toast("Permissão de energia indisponível neste firmware.");
            return;
        }
        dismissQuickControlsForStandby(reboot ? "TV reboot" : "PowerManager standby");
        try {
            PowerManager power = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (power == null) throw new IllegalStateException("PowerManager indisponível");
            if (reboot) {
                PowerManager.class.getMethod("reboot", String.class).invoke(power, "PhilcoOS");
            } else {
                PowerManager.class.getMethod("goToSleep", Long.TYPE)
                        .invoke(power, Long.valueOf(SystemClock.uptimeMillis()));
            }
            Log.i(TAG, reboot ? "User requested TV reboot" : "User requested TV standby via PowerManager");
        } catch (Throwable t) {
            Throwable cause = t.getCause() == null ? t : t.getCause();
            Log.e(TAG, "TV power action failed", cause);
            toast("O firmware recusou a operação de energia.");
        }
    }

    private void launchSettings() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
            if (intent == null) intent = new Intent(Settings.ACTION_SETTINGS);
            startExternal(intent, "As configurações da TV não estão disponíveis.");
        } catch (Throwable t) { Log.e(TAG, "Settings launch failed", t); toast("As configurações da TV não estão disponíveis."); }
    }

    private void openVerifiedActivity(String packageName, String className, String error) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(packageName, className);
            startExternal(intent, error);
        } catch (Throwable t) { Log.e(TAG, "OEM activity " + className + " failed", t); toast(error); }
    }

    private void startExternal(Intent intent, String error) {
        try {
            if (intent == null || intent.resolveActivity(getPackageManager()) == null) { toast(error); return; }
            startActivity(intent);
        } catch (Throwable t) { Log.e(TAG, "External activity launch failed", t); toast(error); }
    }

    private void showAppDrawer() {
        try {
            final PackageManager pm = getPackageManager();
            final ArrayList<LaunchEntry> entries = new ArrayList<LaunchEntry>();
            for (ApplicationInfo app : pm.getInstalledApplications(0)) {
                if (app == null || getPackageName().equals(app.packageName)) continue;
                Intent launch = pm.getLaunchIntentForPackage(app.packageName);
                if (launch == null || launch.resolveActivity(pm) == null) continue;
                CharSequence label = app.loadLabel(pm);
                Drawable icon;
                try { icon = app.loadIcon(pm); } catch (Throwable ignored) { icon = getApplicationInfo().loadIcon(pm); }
                entries.add(new LaunchEntry(label == null ? app.packageName : label.toString(), launch, icon));
            }
            Collections.sort(entries, new Comparator<LaunchEntry>() {
                @Override public int compare(LaunchEntry a, LaunchEntry b) {
                    return a.label.toLowerCase(new Locale("pt", "BR")).compareTo(b.label.toLowerCase(new Locale("pt", "BR")));
                }
            });
            HashSet<String> visibleLabels = new HashSet<String>();
            Iterator<LaunchEntry> appIterator = entries.iterator();
            while (appIterator.hasNext()) {
                LaunchEntry entry = appIterator.next();
                String packageName = entry.intent.getComponent() == null ? "" : entry.intent.getComponent().getPackageName();
                String normalized = entry.label.trim().toLowerCase(new Locale("pt", "BR"));
                // Keep the drawer user-facing: SmartTube is the chosen YouTube client;
                // raw package-name labels are vendor diagnostics, not usable app names.
                if (packageName.startsWith("com.google.android.youtube") || normalized.equals(packageName.toLowerCase(Locale.US))
                        || !visibleLabels.add(normalized)) appIterator.remove();
            }
            if (entries.isEmpty()) { toast("Nenhum aplicativo inicializável foi encontrado."); return; }

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            LinearLayout panel = new LinearLayout(this);
            panel.setOrientation(LinearLayout.VERTICAL);
            panel.setPadding(dp(22), dp(8), dp(22), dp(12));
            GradientDrawable panelBackground = new GradientDrawable();
            panelBackground.setColor(0xF3111B2A);
            panelBackground.setCornerRadius(dp(24));
            panelBackground.setStroke(dp(1), 0x667CB9E8);
            panel.setBackgroundDrawable(panelBackground);
            TextView title = text("Aplicativos", 24, Color.WHITE,
                    Typeface.create("sans-serif-medium", Typeface.NORMAL));
            panel.addView(title, wrap());
            TextView hint = text("Escolha um aplicativo", 14, 0xFFCAD5E5,
                    Typeface.create("sans-serif", Typeface.NORMAL));
            LinearLayout.LayoutParams hintParams = wrap();
            hintParams.topMargin = dp(3);
            hintParams.bottomMargin = dp(12);
            panel.addView(hint, hintParams);

            final ScrollView scroll = new ScrollView(this);
            scroll.setFillViewport(true);
            scroll.setFocusable(false);
            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(5);
            grid.setOrientation(GridLayout.HORIZONTAL);
            grid.setClipChildren(false);
            int[] accents = {0xFF59C8F2,0xFF7CDEA8,0xFFF0BB68,0xFFB79AFF,0xFFEC88B8};
            for (int i = 0; i < entries.size(); i++) {
                final LaunchEntry entry = entries.get(i);
                final int itemId = 0x700000 + i;
                LinearLayout item = new LinearLayout(this);
                item.setId(itemId);
                item.setGravity(Gravity.CENTER);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setPadding(dp(10), dp(8), dp(10), dp(8));
                item.setFocusable(true);
                item.setFocusableInTouchMode(true);
                item.setClickable(true);
                item.setContentDescription(entry.label);
                final int accent = accents[i % accents.length];
                item.setBackgroundDrawable(glassBackground(false, accent, 18));
                item.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override public void onFocusChange(View view, boolean focused) {
                        view.setBackgroundDrawable(glassBackground(focused, accent, 18));
                        view.animate().cancel();
                        view.animate().scaleX(focused ? 1.035f : 1f).scaleY(focused ? 1.035f : 1f)
                                .setDuration(120).start();
                    }
                });
                item.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        try { dialog.dismiss(); startExternal(entry.intent, "Este aplicativo não pôde ser aberto."); }
                        catch (Throwable t) { Log.e(TAG, "App drawer launch failed: " + entry.label, t); toast("Não foi possível abrir " + entry.label + "."); }
                    }
                });
                ImageView icon = new ImageView(this);
                icon.setImageDrawable(entry.icon);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                item.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));
                TextView label = text(entry.label, 14, Color.WHITE,
                        Typeface.create("sans-serif-medium", Typeface.NORMAL));
                label.setGravity(Gravity.CENTER);
                label.setMaxLines(2);
                label.setEllipsize(android.text.TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
                labelParams.topMargin = dp(5);
                item.addView(label, labelParams);

                int column = i % 5, row = i / 5;
                int previous = column == 0 ? itemId : itemId - 1;
                int next = column == 4 || i + 1 == entries.size() ? itemId : itemId + 1;
                int above = row == 0 ? itemId : itemId - 5;
                int below = i + 5 >= entries.size() ? itemId : itemId + 5;
                item.setNextFocusLeftId(previous);
                item.setNextFocusRightId(next);
                item.setNextFocusUpId(above);
                item.setNextFocusDownId(below);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(column));
                params.width = dp(184);
                params.height = dp(112);
                params.setMargins(dp(5), dp(5), dp(5), dp(5));
                grid.addView(item, params);
            }
            scroll.addView(grid, new ScrollView.LayoutParams(-1, -2));
            panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
            TextView footer = text("← → ↑ ↓ navegar     OK abrir     VOLTAR fechar", 12, 0xFFA8B5C8,
                    Typeface.create("sans-serif", Typeface.NORMAL));
            LinearLayout.LayoutParams footerParams = wrap();
            footerParams.topMargin = dp(10);
            panel.addView(footer, footerParams);
            dialog.setContentView(panel);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                int width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(72), dp(1120));
                int height = Math.min(getResources().getDisplayMetrics().heightPixels - dp(64), dp(620));
                window.setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.dimAmount = 0.68f;
                window.setAttributes(attributes);
                window.setLayout(width, height);
            }
            final View first = grid.getChildAt(0);
            if (first != null) scroll.post(new Runnable() {
                @Override public void run() { first.requestFocus(); }
            });
        } catch (Throwable t) { Log.e(TAG, "Could not build application list", t); toast("Lista de aplicativos indisponível."); }
    }

    private void showSafeFallback() {
        try {
            TextView safe = text("Philco Home\nOK abre a TV", 28, Color.WHITE,
                    Typeface.create("sans-serif-medium", Typeface.NORMAL));
            safe.setGravity(Gravity.CENTER);
            safe.setFocusable(true); safe.setFocusableInTouchMode(true); safe.setClickable(true);
            safe.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { launchTv(); }
            });
            setContentView(safe);
            safe.requestFocus();
        } catch (Throwable ignored) { }
    }

    private void toast(String message) {
        try { Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show(); }
        catch (Throwable ignored) { }
    }

    @Override public void onUserInteraction() {
        super.onUserInteraction();
        lastHomeInteraction = SystemClock.elapsedRealtime();
        if (!isIdleScreensaverVisible() && (saverBacklightDimmed || saverOriginalBacklight >= 0))
            restoreBacklightAfterIdle();
        scheduleHomeIdleMonitor();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (isIdleScreensaverVisible()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) dismissIdleScreensaver();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if (isIdleScreensaverVisible()) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) dismissIdleScreensaver();
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && volumeUpUnmuteKeyHeld) return true;
        if (preferences().getBoolean(PREF_MEDIA_MUTE_ACTIVE, false)
                && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                volumeUpUnmuteKeyHeld = true;
                try {
                    AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    setRealtekMute(null, audio, false, -1);
                    updateVolumeIndicator();
                } catch (Throwable t) {
                    Log.e(TAG, "Volume-up could not clear persisted media mute", t);
                    toast("Não foi possível restaurar o áudio. Tente novamente.");
                }
            }
            // The first volume-up restores the exact pre-mute level. Consume both
            // volume keys until mute is cleared so no key changes the saved level.
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) { launchSettings(); return true; }
        return super.onKeyDown(keyCode, event);
    }

    private static final class WeatherResult {
        boolean ok;
        boolean hasDailyRange;
        int temperature, temperatureMax, temperatureMin, code;
        String description, error;
        static WeatherResult error(String value) { WeatherResult result = new WeatherResult(); result.error = value; return result; }
    }

    private static final class LaunchEntry {
        final String label; final Intent intent; final Drawable icon;
        LaunchEntry(String label, Intent intent, Drawable icon) { this.label=label; this.intent=intent; this.icon=icon; }
    }
}
