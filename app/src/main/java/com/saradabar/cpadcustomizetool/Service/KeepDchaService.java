package com.saradabar.cpadcustomizetool.Service;

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

import com.saradabar.cpadcustomizetool.Common;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_STATE;

public class KeepDchaService extends Service {
    private ContentResolver resolver;
    private ContentObserver observer;
    private final String dchaStateString = DCHA_STATE;
    private final Uri contentDchaState = Settings.System.getUriFor(dchaStateString);
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.System.getInt(resolver, dchaStateString) == 3) {
                    Settings.System.putInt(resolver, dchaStateString,0);
            }
        } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (! sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_DCHA_STATE, false)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (observer != null) {
            resolver.unregisterContentObserver(observer);
            observer = null;
        }
        resolver = getContentResolver();
        observer = mObserver;
        resolver.registerContentObserver(contentDchaState, false, observer);
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