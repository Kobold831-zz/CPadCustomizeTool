package com.saradabar.cpadcustomizetool.data.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;

import com.saradabar.cpadcustomizetool.data.crash.CrashLogger;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class KeepService extends Service {

    private Handler mHandler;
    private Runnable mRunnable;
    IDchaService mDchaService;

    private boolean isObserberHideEnable = false;
    private boolean isObserberStateEnable = false;
    private boolean isObserberMarketEnable = false;
    private boolean isObserberUsbEnable = false;
    private boolean isObserberHomeEnable = false;

    static KeepService instance = null;

    public static KeepService getInstance() {
        return instance;
    }

    private void loopKeepHome() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
                    if (!getHome().equals(sp.getString(Constants.KEY_SAVE_KEEP_HOME, null))) {
                        try {
                            mDchaService.clearDefaultPreferredApp(getHome());
                            mDchaService.setDefaultPreferredHomeApp(getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE).getString(Constants.KEY_SAVE_KEEP_HOME, null));
                        } catch (RemoteException ignored) {
                        }
                    }
                    mHandler.postDelayed(this, 5000);
                }else {
                    mHandler.removeCallbacks(mRunnable);
                }
            }
        };
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            mHandler.post(mRunnable);
        }else {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private String getHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(home, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.packageName;
    }

    ServiceConnection mDchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    public void bindDchaService() {
        bindService(Constants.DCHA_SERVICE, mDchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ContentObserver DchaStateObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.System.getInt(getContentResolver(), Constants.DCHA_STATE) == 3) {
                    Settings.System.putInt(getContentResolver(), Constants.DCHA_STATE, 0);
                }
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    private final ContentObserver NavigationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.System.getInt(getContentResolver(), Constants.HIDE_NAVIGATION_BAR) == 1) {
                    Settings.System.putInt(getContentResolver(), Constants.HIDE_NAVIGATION_BAR, 0);
                }
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    private final ContentObserver MarketObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 0) {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                }
            } catch (SecurityException | Settings.SettingNotFoundException e) {
                e.printStackTrace();
                Toast.toast(getApplicationContext(), "権限を付与してから再試行してください");
                SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false);
                spe.apply();
                stopSelf();
            }
        }
    };

    private final ContentObserver UsbDebugObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED) == 0) {
                    if (Preferences.GET_MODEL_ID(getApplicationContext()) == 2) {
                        Settings.System.putInt(getContentResolver(), Constants.DCHA_STATE, 3);
                    }
                    Thread.sleep(100);
                    Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                    if (Preferences.GET_MODEL_ID(getApplicationContext()) == 2) {
                        Settings.System.putInt(getContentResolver(), Constants.DCHA_STATE, 0);
                    }
                }
            } catch (SecurityException | Settings.SettingNotFoundException | InterruptedException e) {
                e.printStackTrace();
                if (Preferences.GET_MODEL_ID(getApplicationContext()) == 2) {
                    Settings.System.putInt(getContentResolver(), Constants.DCHA_STATE, 0);
                }
                Toast.toast(getApplicationContext(), "権限を付与してから再試行してください");
                SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false);
                spe.apply();
                stopSelf();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(getApplicationContext()));
        bindDchaService();
        instance = this;
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        /* オブザーバーを有効化 */
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false)) {
            isObserberHideEnable = true;
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Constants.HIDE_NAVIGATION_BAR), false, NavigationObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false)) {
            isObserberStateEnable = true;
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Constants.DCHA_STATE), false, DchaStateObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false)) {
            isObserberMarketEnable = true;
            getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS), false, MarketObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false)) {
            isObserberUsbEnable = true;
            getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.ADB_ENABLED), false, UsbDebugObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            isObserberHomeEnable = true;
            loopKeepHome();
        }
        if (!sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            startService(new Intent(getApplicationContext(), KeepService.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startService() {
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        /* オブザーバーを有効化 */
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false)) {
            isObserberHideEnable = true;
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Constants.HIDE_NAVIGATION_BAR), false, NavigationObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false)) {
            isObserberStateEnable = true;
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Constants.DCHA_STATE), false, DchaStateObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false)) {
            isObserberMarketEnable = true;
            getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS), false, MarketObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false)) {
            isObserberUsbEnable = true;
            getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.ADB_ENABLED), false, UsbDebugObserver);
        }
        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            isObserberHomeEnable = true;
            loopKeepHome();
        }
    }

    public void stopService(int stopCode) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        /* オブサーバーを無効化 */
        switch (stopCode) {
            case 1:
                if (isObserberHideEnable) {
                    getContentResolver().unregisterContentObserver(NavigationObserver);
                    isObserberHideEnable = false;
                }
                break;
            case 2:
                if (isObserberStateEnable) {
                    getContentResolver().unregisterContentObserver(DchaStateObserver);
                    isObserberStateEnable = false;
                }
                break;
            case 3:
                if (isObserberMarketEnable) {
                    getContentResolver().unregisterContentObserver(MarketObserver);
                    isObserberMarketEnable = false;
                }
                break;
            case 4:
                if (isObserberUsbEnable) {
                    getContentResolver().unregisterContentObserver(UsbDebugObserver);
                    isObserberUsbEnable = false;
                }
                break;
            case 5:
                if (isObserberHomeEnable) {
                    mHandler.removeCallbacks(mRunnable);
                    isObserberHomeEnable = false;
                }
                break;
            case 6:
                if (isObserberHideEnable) {
                    getContentResolver().unregisterContentObserver(NavigationObserver);
                    isObserberHideEnable = false;
                }
                if (isObserberStateEnable) {
                    getContentResolver().unregisterContentObserver(DchaStateObserver);
                    isObserberStateEnable = false;
                }
                if (isObserberMarketEnable) {
                    getContentResolver().unregisterContentObserver(MarketObserver);
                    isObserberMarketEnable = false;
                }
                if (isObserberUsbEnable) {
                    getContentResolver().unregisterContentObserver(UsbDebugObserver);
                    isObserberUsbEnable = false;
                }
                if (isObserberHomeEnable) {
                    mHandler.removeCallbacks(mRunnable);
                    isObserberHomeEnable = false;
                }
                stopService(new Intent(getApplicationContext(), ProtectKeepService.class));
                stopSelf();
                break;
        }
        if (!sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) && !sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            stopService(new Intent(getApplicationContext(), ProtectKeepService.class));
            stopSelf();
        }
    }
}