package com.saradabar.cpadcustomizetool.Service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.saradabar.cpadcustomizetool.Common;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;

public class KeepHomeService extends Service {

    Timer timer;

    private IDchaService mDchaService;

    public void loopKeepHome(final String saveKeepHome) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!getHome().equals(saveKeepHome)) {
                    bindDchaService();
                }
            }
        }, 0, 2000);
    }

    private String getHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(home, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.packageName;
    }

    private void bindDchaService() {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //接続
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            SharedPreferences sp = getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            try {
                mDchaService.clearDefaultPreferredApp(getHome());
                mDchaService.setDefaultPreferredHomeApp(sp.getString(Common.Customizetool.KEY_SAVE_KEEP_HOME, null));
            } catch (RemoteException ignored) {
            }
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (!sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_HOME, false)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        loopKeepHome(sp.getString(Common.Customizetool.KEY_SAVE_KEEP_HOME, null));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != timer) timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}