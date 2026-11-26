package com.philco.ultra777.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Returns to PhilcoOS after PackageManager finishes replacing its app update. */
public final class PackageReplacedReceiver extends BroadcastReceiver {
    private static final String TAG = "PhilcoUltra777";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) return;

        Intent home = new Intent(context, MainActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            context.startActivity(home);
            Log.i(TAG, "Returned to PhilcoOS after package replacement");
        } catch (RuntimeException e) {
            Log.e(TAG, "Could not return to PhilcoOS after package replacement", e);
        }
    }
}
