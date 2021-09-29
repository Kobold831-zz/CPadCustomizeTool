package com.saradabar.cpadcustomizetool.service;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.common.Common;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class KeepService extends Service {

    private ContentResolver resolver;
    private SharedPreferences sp;
    private IDchaService mDchaService;
    private Handler mHandler;
    private Runnable mRunnable;

    private final String dchaStateString = DCHA_STATE;
    private final String hideNavigationBarString = HIDE_NAVIGATION_BAR;

    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final Uri contentDchaState = Settings.System.getUriFor(dchaStateString);
    private final Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
    private final Uri contentUsbDebug = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);

    private boolean isObserberHideEnable = false;
    private boolean isObserberStateEnable = false;
    private boolean isObserberMarketEnable = false;
    private boolean isObserberUsbEnable = false;
    private boolean isObserberHomeEnable = false;

    private static KeepService instance = null;

    public static KeepService getInstance() {
        return instance;
    }

    private void loopKeepHome() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
                    if (!getHome().equals(sp.getString(Common.Variable.KEY_SAVE_KEEP_HOME, null))) {
                        bindDchaService();
                    }
                    mHandler.postDelayed(this, 5000);
                }else {
                    mHandler.removeCallbacks(mRunnable);
                }
            }
        };
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
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

    private void bindDchaService() {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            try {
                mDchaService.clearDefaultPreferredApp(getHome());
                mDchaService.setDefaultPreferredHomeApp(sp.getString(Common.Variable.KEY_SAVE_KEEP_HOME, null));
            } catch (RemoteException ignored) {
            }
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    private final ContentObserver DchaStateObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.System.getInt(resolver, dchaStateString) == 3) {
                    Settings.System.putInt(resolver, dchaStateString, 0);
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
                if (Settings.System.getInt(resolver, hideNavigationBarString) == 1) {
                    Settings.System.putInt(resolver, hideNavigationBarString, 0);
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
                if (Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) == 0) {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                }
            } catch (SecurityException | Settings.SettingNotFoundException e) {
                e.printStackTrace();
                Common.Variable.toast = Toast.makeText(getApplication(), "権限を付与してから再試行してください", Toast.LENGTH_SHORT);
                Common.Variable.toast.show();
                SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false);
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
                if (Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) == 0) {
                    if (Common.GET_MODEL_NAME(getApplicationContext()) == 2) {
                        Settings.System.putInt(resolver, dchaStateString, 3);
                    }
                    Thread.sleep(100);
                    Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1);
                    if (Common.GET_MODEL_NAME(getApplicationContext()) == 2) {
                        Settings.System.putInt(resolver, dchaStateString, 0);
                    }
                }
            } catch (SecurityException | Settings.SettingNotFoundException | InterruptedException e) {
                e.printStackTrace();
                if (Common.GET_MODEL_NAME(getApplicationContext()) == 2) {
                    Settings.System.putInt(resolver, dchaStateString, 0);
                }
                Common.Variable.toast = Toast.makeText(getApplication(), "権限を付与してから再試行してください", Toast.LENGTH_SHORT);
                Common.Variable.toast.show();
                SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false);
                spe.apply();
                stopSelf();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        resolver = getContentResolver();
        /* オブザーバーを有効化 */
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false)) {
            isObserberHideEnable = true;
            resolver.registerContentObserver(contentHideNavigationBar, false, NavigationObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false)) {
            isObserberStateEnable = true;
            resolver.registerContentObserver(contentDchaState, false, DchaStateObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false)) {
            isObserberMarketEnable = true;
            resolver.registerContentObserver(contentMarketApp, false, MarketObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false)) {
            isObserberUsbEnable = true;
            resolver.registerContentObserver(contentUsbDebug, false, UsbDebugObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            isObserberHomeEnable = true;
            loopKeepHome();
        }
        if (!sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            try {
                unbindService(mServiceConnection);
            } catch (IllegalArgumentException ignored) {
            }
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(getApplicationContext(), ProtectKeepService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            startService(new Intent(this, KeepService.class));
        } else {
            try {
                unbindService(mServiceConnection);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public class MyServiceLocalBinder extends Binder {
        KeepService getService() {
            return KeepService.this;
        }
    }

    private final IBinder mBinder = new MyServiceLocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException ignored) {
        }
        Intent i = new Intent(getApplicationContext(), ProtectKeepService.class);
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
                startService(new Intent(getApplicationContext(), ProtectKeepService.class));
            } else {
                try {
                    unbindService(this);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    };

    public void startService() {
        /* オブザーバーを有効化 */
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false)) {
            isObserberHideEnable = true;
            resolver.registerContentObserver(contentHideNavigationBar, false, NavigationObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false)) {
            isObserberStateEnable = true;
            resolver.registerContentObserver(contentDchaState, false, DchaStateObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false)) {
            isObserberMarketEnable = true;
            resolver.registerContentObserver(contentMarketApp, false, MarketObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false)) {
            isObserberUsbEnable = true;
            resolver.registerContentObserver(contentUsbDebug, false, UsbDebugObserver);
        }
        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            isObserberHomeEnable = true;
            loopKeepHome();
        }
    }

    public void stopService(int stopCode) {
        /* オブサーバーを無効化 */
        switch (stopCode) {
            case 1:
                if (isObserberHideEnable) {
                    resolver.unregisterContentObserver(NavigationObserver);
                    isObserberHideEnable = false;
                }
                break;
            case 2:
                if (isObserberStateEnable) {
                    resolver.unregisterContentObserver(DchaStateObserver);
                    isObserberStateEnable = false;
                }
                break;
            case 3:
                if (isObserberMarketEnable) {
                    resolver.unregisterContentObserver(MarketObserver);
                    isObserberMarketEnable = false;
                }
                break;
            case 4:
                if (isObserberUsbEnable) {
                    resolver.unregisterContentObserver(UsbDebugObserver);
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
                    resolver.unregisterContentObserver(NavigationObserver);
                    isObserberHideEnable = false;
                }
                if (isObserberStateEnable) {
                    resolver.unregisterContentObserver(DchaStateObserver);
                    isObserberStateEnable = false;
                }
                if (isObserberMarketEnable) {
                    resolver.unregisterContentObserver(MarketObserver);
                    isObserberMarketEnable = false;
                }
                if (isObserberUsbEnable) {
                    resolver.unregisterContentObserver(UsbDebugObserver);
                    isObserberUsbEnable = false;
                }
                if (isObserberHomeEnable) {
                    mHandler.removeCallbacks(mRunnable);
                    isObserberHomeEnable = false;
                }
                try {
                    unbindService(mServiceConnection);
                } catch (IllegalArgumentException ignored) {
                }
                stopSelf();
                break;
        }
        if (!sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) && !sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            try {
                unbindService(mServiceConnection);
            } catch (IllegalArgumentException ignored) {
            }
            stopSelf();
        }
    }
}