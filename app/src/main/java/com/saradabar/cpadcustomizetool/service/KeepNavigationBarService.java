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

import com.saradabar.cpadcustomizetool.common.Common;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.HIDE_NAVIGATION_BAR;

public class KeepNavigationBarService extends Service {
    private ContentResolver resolver;
    private ContentObserver observer;
    private final String hideNavigationBarString = HIDE_NAVIGATION_BAR;
    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.System.getInt(resolver, hideNavigationBarString) == 1) {
                    Settings.System.putInt(resolver, hideNavigationBarString, 0);
                }
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (! sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (observer != null) {
            resolver.unregisterContentObserver(observer);
            observer = null;
        }
        resolver = getContentResolver();
        observer = mObserver;
        resolver.registerContentObserver(contentHideNavigationBar, false, observer);
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