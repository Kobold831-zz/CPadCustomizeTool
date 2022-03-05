package com.saradabar.cpadcustomizetool.data.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.menu.CrashDetection;

public class ProtectKeepService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashDetection(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Thread.setDefaultUncaughtExceptionHandler(new CrashDetection(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            startService(new Intent(getApplicationContext(), ProtectKeepService.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}