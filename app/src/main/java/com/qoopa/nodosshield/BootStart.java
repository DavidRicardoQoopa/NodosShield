package com.qoopa.nodosshield;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by diego.saavedra on 16/10/2015.
 */
public class BootStart extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {
        Log.e("ON RECEIVER", "ON RECEIVER");
        String deviceId = "";
        deviceId = LockService.getDeviceIdFromFile(context);
        LogUtil.printError("DEVICE ID BOOT",""+deviceId);
        try {
            if (!deviceId.equals("")) {
                LogUtil.printError("REBOOT", "TRUE");
                if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                    Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
                    Intent service = new Intent(context, LockService.class);
                    NotificationEventReceiver.setupAlarm(context);
                    context.startService(service);
                }
            }
            else {
                LogUtil.printError("REBOOT", "FALSE" + " "  + deviceId);
            }
        } catch (Exception e){
            LogUtil.printFullErrorInit("REBOOT","EXCEPTION",e);
        }
    }
}

