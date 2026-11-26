package com.philco.ultra777.home.media;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/** One-shot, reversible picture/audio tuning through the TV's existing Realtek TvManager. */
final class TvTuningProfile {
    private static final String TAG = "PhilcoTvProfile";
    private static final String PREFS = "philco_tv_tuning";
    private static final int USER_PICTURE_MODE = 0;
    private static final int USER_SOUND_MODE = 4;
    private static final int PROFILE_VERSION = 2;
    private static final int[] EQ_FREQUENCIES = {100, 500, 1200, 3000, 5000, 10000, 18000};

    private TvTuningProfile() { }

    static void inspect(Context context) {
        try {
            Object tv = tvManager(context);
            StringBuilder result = new StringBuilder("Current TV tuning");
            String[] values = {"PictureMode", "Brightness", "Contrast", "Saturation", "Sharpness",
                    "Backlight", "AudioMode", "Bass", "Treble", "Balance", "EqualizerMode"};
            for (String value : values) result.append(' ').append(value).append('=').append(get(tv, "get" + value));
            for (int frequency : EQ_FREQUENCIES)
                result.append(" EQ").append(frequency).append('=').append(getEqualizer(tv, frequency));
            Log.i(TAG, result.toString());
        } catch (Throwable error) {
            Log.e(TAG, "Could not read TV tuning", error);
        }
    }

    static void apply(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Object tv;
        try {
            tv = tvManager(context);
            if (prefs.getBoolean("profile_applied", false)
                    && prefs.getInt("profile_version", 0) == PROFILE_VERSION) {
                inspect(context);
                Log.i(TAG, "Clarity profile already applied; no duplicate write");
                return;
            }
            if (!prefs.getBoolean("backup_valid", false)) {
                saveOriginal(prefs, tv);
            }
            set(tv, "setPictureMode", USER_PICTURE_MODE);
            set(tv, "setAudioMode", USER_SOUND_MODE);
            set(tv, "setBrightness", 50);
            set(tv, "setContrast", 55);
            set(tv, "setSaturation", 52);
            setSharpness(tv, 12);
            // Keep the user's panel luminance unchanged; backlight is not picture brightness.
            set(tv, "setBass", 52);
            set(tv, "setTreble", 58);
            int[] profile = {52, 52, 51, 58, 61, 59, 54};
            for (int i = 0; i < EQ_FREQUENCIES.length; i++)
                setEqualizer(tv, EQ_FREQUENCIES[i], profile[i]);
            verifyApplied(tv, prefs);
            if (!prefs.edit().putBoolean("profile_applied", true)
                    .putInt("profile_version", PROFILE_VERSION).commit())
                throw new IllegalStateException("Could not save TV profile state");
            inspect(context);
            Log.i(TAG, "Clarity PhilcoOS profile applied and read back");
        } catch (Throwable error) {
            Log.e(TAG, "Profile application failed; attempting original-value restore", error);
            if (prefs.getBoolean("backup_valid", false)) restore(context);
        }
    }

    static void restore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("backup_valid", false)) {
            Log.w(TAG, "No saved TV tuning snapshot; nothing to restore");
            return;
        }
        try {
            Object tv = tvManager(context);
            set(tv, "setPictureMode", prefs.getInt("picture_mode", 0));
            set(tv, "setBrightness", prefs.getInt("brightness", 50));
            set(tv, "setContrast", prefs.getInt("contrast", 50));
            set(tv, "setSaturation", prefs.getInt("saturation", 50));
            setSharpness(tv, prefs.getInt("sharpness", 50));
            set(tv, "setBass", prefs.getInt("bass", 50));
            set(tv, "setTreble", prefs.getInt("treble", 50));
            set(tv, "setBalance", prefs.getInt("balance", 50));
            for (int frequency : EQ_FREQUENCIES)
                setEqualizer(tv, frequency, prefs.getInt("eq_" + frequency, 50));
            // EQ writes can select USER mode as a side effect; restore the prior mode last.
            set(tv, "setAudioMode", prefs.getInt("sound_mode", 0));
            verifyOriginal(tv, prefs);
            prefs.edit().putBoolean("profile_applied", false).commit();
            inspect(context);
            Log.i(TAG, "Original TV picture/audio values restored and read back");
        } catch (Throwable error) {
            Log.e(TAG, "Could not restore original TV tuning; snapshot retained", error);
        }
    }

    private static Object tvManager(Context context) {
        Object tv = context.getSystemService("tv");
        if (tv == null) throw new IllegalStateException("Realtek TV service unavailable");
        return tv;
    }

    private static void saveOriginal(SharedPreferences prefs, Object tv) throws Exception {
        int pictureMode = get(tv, "getPictureMode");
        int soundMode = get(tv, "getAudioMode");
        int brightness = bounded(get(tv, "getBrightness"));
        int contrast = bounded(get(tv, "getContrast"));
        int saturation = bounded(get(tv, "getSaturation"));
        int sharpness = bounded(get(tv, "getSharpness"));
        int backlight = bounded(get(tv, "getBacklight"));
        int bass = bounded(get(tv, "getBass"));
        int treble = bounded(get(tv, "getTreble"));
        int balance = bounded(get(tv, "getBalance"));
        SharedPreferences.Editor editor = prefs.edit()
                .putInt("picture_mode", pictureMode).putInt("sound_mode", soundMode)
                .putInt("brightness", brightness).putInt("contrast", contrast)
                .putInt("saturation", saturation).putInt("sharpness", sharpness)
                .putInt("backlight", backlight).putInt("bass", bass)
                .putInt("treble", treble).putInt("balance", balance);
        for (int frequency : EQ_FREQUENCIES)
            editor.putInt("eq_" + frequency, bounded(getEqualizer(tv, frequency)));
        if (!editor.putBoolean("backup_valid", true).putBoolean("profile_applied", false).commit())
            throw new IllegalStateException("Could not save original TV values before changing them");
        Log.i(TAG, "Saved original TV settings including backlight=" + backlight);
    }

    private static int bounded(int value) {
        if (value < 0 || value > 100) throw new IllegalStateException("Out-of-range TV value: " + value);
        return value;
    }

    private static int get(Object tv, String method) throws Exception {
        Object result = tv.getClass().getMethod(method).invoke(tv);
        if (!(result instanceof Number)) throw new IllegalStateException("Invalid result from " + method);
        return ((Number)result).intValue();
    }

    private static void set(Object tv, String method, int value) throws Exception {
        tv.getClass().getMethod(method, Integer.TYPE).invoke(tv, Integer.valueOf(value));
    }

    private static int getEqualizer(Object tv, int frequency) throws Exception {
        Object result = tv.getClass().getMethod("getEqualizer", Integer.TYPE)
                .invoke(tv, Integer.valueOf(frequency));
        if (!(result instanceof Number)) throw new IllegalStateException("Invalid EQ readback at " + frequency + " Hz");
        return ((Number)result).intValue();
    }

    private static void setEqualizer(Object tv, int frequency, int value) throws Exception {
        tv.getClass().getMethod("setEqualizer", Integer.TYPE, Integer.TYPE)
                .invoke(tv, Integer.valueOf(frequency), Integer.valueOf(value));
    }

    private static void setSharpness(Object tv, int value) throws Exception {
        tv.getClass().getMethod("setSharpness", Boolean.TYPE, Integer.TYPE)
                .invoke(tv, Boolean.TRUE, Integer.valueOf(value));
    }

    private static void verifyApplied(Object tv, SharedPreferences prefs) throws Exception {
        verify(tv, "getPictureMode", USER_PICTURE_MODE);
        verify(tv, "getAudioMode", USER_SOUND_MODE);
        verify(tv, "getBrightness", 50);
        verify(tv, "getContrast", 55);
        verify(tv, "getSaturation", 52);
        verify(tv, "getSharpness", 12);
        verify(tv, "getBalance", prefs.getInt("balance", 0));
        verify(tv, "getBass", 52);
        verify(tv, "getTreble", 58);
        int[] profile = {52, 52, 51, 58, 61, 59, 54};
        for (int i = 0; i < EQ_FREQUENCIES.length; i++)
            if (getEqualizer(tv, EQ_FREQUENCIES[i]) != profile[i])
                throw new IllegalStateException("EQ readback mismatch at " + EQ_FREQUENCIES[i] + " Hz");
    }

    private static void verifyOriginal(Object tv, SharedPreferences prefs) throws Exception {
        verify(tv, "getPictureMode", prefs.getInt("picture_mode", 0));
        verify(tv, "getAudioMode", prefs.getInt("sound_mode", 0));
        verify(tv, "getBrightness", prefs.getInt("brightness", 50));
        verify(tv, "getContrast", prefs.getInt("contrast", 50));
        verify(tv, "getSaturation", prefs.getInt("saturation", 50));
        verify(tv, "getSharpness", prefs.getInt("sharpness", 50));
        verify(tv, "getBass", prefs.getInt("bass", 50));
        verify(tv, "getTreble", prefs.getInt("treble", 50));
        verify(tv, "getBalance", prefs.getInt("balance", 50));
        for (int frequency : EQ_FREQUENCIES)
            if (getEqualizer(tv, frequency) != prefs.getInt("eq_" + frequency, 50))
                throw new IllegalStateException("Original EQ readback mismatch at " + frequency + " Hz");
    }

    private static void verify(Object tv, String getter, int expected) throws Exception {
        int actual = get(tv, getter);
        if (actual != expected) throw new IllegalStateException(getter + " readback " + actual + " != " + expected);
    }
}
