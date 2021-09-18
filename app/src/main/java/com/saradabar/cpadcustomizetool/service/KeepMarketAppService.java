package com.saradabar.cpadcustomizetool.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.common.Common;

public class KeepMarketAppService extends Service {
    private ContentResolver resolver;
    private ContentObserver observer;
    private final Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
    Toast toast;
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) == 0) {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                }
            } catch (SecurityException | Settings.SettingNotFoundException e) {
                e.printStackTrace();
                toast = Toast.makeText(getApplication(), "権限を付与してから再試行してください。", Toast.LENGTH_SHORT);
                toast.show();
                SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false);
                spe.apply();
                stopSelf();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (!sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (observer != null) {
            resolver.unregisterContentObserver(observer);
            observer = null;
        }
        resolver = getContentResolver();
        observer = mObserver;
        resolver.registerContentObserver(contentMarketApp, false, observer);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (observer != null) {
            resolver.unregisterContentObserver(observer);
            observer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}