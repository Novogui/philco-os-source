package com.philco.ultra777.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

/** Restores the last verified Realtek input after Android finishes booting. */
public final class InputRestoreReceiver extends BroadcastReceiver {
    private static final String TAG = "PhilcoUltra777";
    private static final String PREFS = "philco_home";
    private static final String PREF_LAST_INPUT = "last_confirmed_input_source";
    private static final String PREF_RESTORE_LAST_INPUT = "restore_last_input_on_boot";

    @Override public void onReceive(final Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        final SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(PREF_RESTORE_LAST_INPUT, true)) {
            Log.i(TAG, "Boot input restore disabled by user");
            return;
        }
        int target = prefs.getInt(PREF_LAST_INPUT, -1);
        if (!isKnownSource(target)) {
            target = Settings.System.getInt(context.getContentResolver(), "PlayingSource", -1);
        }
        if (!isKnownSource(target)) {
            Log.i(TAG, "Boot input restore skipped: no valid saved Realtek input");
            return;
        }

        final int sourceId = target;
        final PendingResult pending = goAsync();
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] attempts = new int[]{0};
        final boolean[] finished = new boolean[]{false};
        final Runnable finish = new Runnable() {
            @Override public void run() {
                if (finished[0]) return;
                finished[0] = true;
                pending.finish();
            }
        };
        final Runnable restore = new Runnable() {
            @Override public void run() {
                if (finished[0]) return;
                try {
                    Object tvManager = context.getSystemService("tv");
                    if (tvManager == null) throw new IllegalStateException("Realtek TV service unavailable");
                    Method getCurrent = tvManager.getClass().getMethod("getCurLiveSource");
                    int current = ((Integer)getCurrent.invoke(tvManager)).intValue();
                    if (current == sourceId) {
                        Settings.System.putInt(context.getContentResolver(), "PlayingSource", sourceId);
                        Log.i(TAG, "Boot input restore verified: source=" + sourceId);
                        finish.run();
                        return;
                    }
                    if (attempts[0] >= 6) {
                        Log.w(TAG, "Boot input restore not verified: target=" + sourceId + ", current=" + current);
                        finish.run();
                        return;
                    }
                    Method setSource = tvManager.getClass().getMethod("setSource", Integer.TYPE);
                    Object result = setSource.invoke(tvManager, Integer.valueOf(sourceId));
                    boolean persisted = Settings.System.putInt(context.getContentResolver(), "PlayingSource", sourceId);
                    Log.i(TAG, "Boot input restore attempt " + (attempts[0] + 1) + ": target=" + sourceId
                            + ", current=" + current + ", apiResult=" + result + ", persisted=" + persisted);
                    attempts[0]++;
                    handler.postDelayed(this, 800L);
                } catch (Throwable t) {
                    Log.w(TAG, "Boot input restore attempt failed", t);
                    if (attempts[0]++ < 5) handler.postDelayed(this, 800L);
                    else finish.run();
                }
            }
        };
        Log.i(TAG, "Scheduling boot input restore for source=" + sourceId);
        handler.postDelayed(restore, 2500L);
        handler.postDelayed(finish, 9000L);
    }

    private static boolean isKnownSource(int source) {
        return source == 1 || source == 5 || source == 17 || source == 18 || source == 27;
    }
}
