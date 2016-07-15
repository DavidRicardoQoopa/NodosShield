package com.qoopa.nodosshield;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by diego.saavedra on 18/03/2016.
 */
public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "OnReceiver");
        context.startService(new Intent(context.getApplicationContext(), LockService.class));
    }
}
