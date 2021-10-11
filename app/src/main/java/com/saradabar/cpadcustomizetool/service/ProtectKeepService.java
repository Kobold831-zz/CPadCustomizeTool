package com.saradabar.cpadcustomizetool.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.Common;

public class ProtectKeepService extends Service {

    SharedPreferences sp;

    public class MyServiceLocalBinder extends Binder {
        ProtectKeepService getService() {
            return ProtectKeepService.this;
        }
    }

    private final IBinder mBinder = new MyServiceLocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException ignored) {
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            Intent i = new Intent(getApplicationContext(), KeepService.class);
            bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
            return START_STICKY;
        }else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException ignored) {
        }
        Intent i = new Intent(getApplicationContext(), KeepService.class);
        bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
        return mBinder;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
                startService(new Intent(getApplicationContext(), KeepService.class));
            }else {
                try {
                    unbindService(mServiceConnection);
                } catch (IllegalArgumentException ignored) {
                }
                stopSelf();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            startService(new Intent(this, ProtectKeepService.class));
        }else {
            try {
                unbindService(mServiceConnection);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}