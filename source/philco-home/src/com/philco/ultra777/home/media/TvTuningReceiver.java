package com.philco.ultra777.home.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Hidden, shell-only entry point for reversible one-shot Realtek picture/audio tuning. */
public final class TvTuningReceiver extends BroadcastReceiver {
    public static final String ACTION_INSPECT = "com.philco.ultra777.home.action.TV_TUNING_INSPECT";
    public static final String ACTION_APPLY = "com.philco.ultra777.home.action.TV_TUNING_APPLY";
    public static final String ACTION_RESTORE = "com.philco.ultra777.home.action.TV_TUNING_RESTORE";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (ACTION_INSPECT.equals(action)) TvTuningProfile.inspect(context);
        else if (ACTION_APPLY.equals(action)) TvTuningProfile.apply(context);
        else if (ACTION_RESTORE.equals(action)) TvTuningProfile.restore(context);
    }
}
