package com.saradabar.cpadcustomizetool.flagment;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.saradabar.cpadcustomizetool.Common.GET_CHANGE_SETTINGS_DCHA_FLAG;
import static com.saradabar.cpadcustomizetool.Common.GET_CONFIRMATION;
import static com.saradabar.cpadcustomizetool.Common.GET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.Common.GET_MODEL_ID;
import static com.saradabar.cpadcustomizetool.Common.GET_NORMAL_LAUNCHER;
import static com.saradabar.cpadcustomizetool.Common.SET_CONFIRMATION;
import static com.saradabar.cpadcustomizetool.Common.SET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.Common.SET_NORMAL_LAUNCHER;
import static com.saradabar.cpadcustomizetool.Common.Variable.COUNT_DCHA_COMPLETED_FILE;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_MODE;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_UTIL_MODE;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_UTIL_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_CHECK;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_MARKET_APP_FALSE;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_MARKET_APP_TRUE;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_REBOOT;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_RESOLUTION;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_SET_DCHA_STATE_0;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_SET_DCHA_STATE_3;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_SET_LAUNCHER;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_TEST;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_USB_DEBUG_FALSE;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_USB_DEBUG_TRUE;
import static com.saradabar.cpadcustomizetool.Common.Variable.FLAG_VIEW_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.Common.Variable.HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.Common.Variable.IGNORE_DCHA_COMPLETED_FILE;
import static com.saradabar.cpadcustomizetool.Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE;
import static com.saradabar.cpadcustomizetool.Common.Variable.KEY_ENABLED_KEEP_HOME;
import static com.saradabar.cpadcustomizetool.Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.KEY_ENABLED_KEEP_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG;
import static com.saradabar.cpadcustomizetool.Common.Variable.KEY_SAVE_KEEP_HOME;
import static com.saradabar.cpadcustomizetool.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.PACKAGE_DCHA_UTIL_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.REQUEST_ADMIN;
import static com.saradabar.cpadcustomizetool.Common.Variable.REQUEST_INSTALL;
import static com.saradabar.cpadcustomizetool.Common.Variable.SHARED_PREFERENCE_KEY;
import static com.saradabar.cpadcustomizetool.Common.Variable.toast;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.StartActivity;
import com.saradabar.cpadcustomizetool.service.KeepService;
import com.saradabar.cpadcustomizetool.set.HomeLauncherActivity;
import com.saradabar.cpadcustomizetool.set.NormalLauncherActivity;

import java.util.ArrayList;
import java.util.List;

import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.dchautilservice.IDchaUtilService;

public class MainFragment extends PreferenceFragment {
    private int width, height;
    private boolean isObserberStateEnable = false;
    private boolean isObserberHideEnable = false;
    private boolean isObserberMarketEnable = false;
    private boolean isObserberUsbEnable = false;

    private String setLauncherPackage, installData;
    private ListView mListView;
    private IDchaService mDchaService;
    private IDchaUtilService mIDchaUtilService;

    SwitchPreference switchDchaState,
            switchKeepDchaState,
            switchHideBar,
            switchEnableService,
            switchMarketApp,
            switchKeepMarketApp,
            switchUsbDebug,
            switchKeepUsbDebug,
            switchKeepHome,
            switchDeviceAdministrator;

    Preference preferenceDchaService,
            preferenceEmergencyManual,
            preferenceNormalLauncher,
            preferenceNormalManual,
            preferenceOtherSettings,
            preferenceReboot,
            preferenceRebootShortCut,
            preferenceSilentInstall,
            preferenceChangeHome,
            preferenceResolution,
            preferenceResolutionReset,
            preferenceCopy,
            preferenceDeviceOwner;

    @SuppressLint("StaticFieldLeak")
    private static MainFragment instance = null;

    public static MainFragment getInstance() {
        return instance;
    }

    /* システムUIオブザーバー */
    private final ContentObserver observerState = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchDchaState.setChecked(Settings.System.getInt(getActivity().getContentResolver(), DCHA_STATE) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    /* ナビゲーションバーオブザーバー */
    private final ContentObserver observerHide = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchHideBar.setChecked(Settings.System.getInt(getActivity().getContentResolver(), HIDE_NAVIGATION_BAR) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    /* 提供元オブザーバー */
    private final ContentObserver observerMarket = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchMarketApp.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    /* UsbDebugオブザーバー */
    private final ContentObserver observerUsb = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchUsbDebug.setChecked(Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    public boolean bindDchaService(int flag, boolean dchaService) {
        Intent intent;
        if (dchaService) {
            intent = new Intent(DCHA_SERVICE).setPackage(PACKAGE_DCHASERVICE);
        } else {
            intent = new Intent(DCHA_UTIL_SERVICE).setPackage(PACKAGE_DCHA_UTIL_SERVICE);
        }
        return !getActivity().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                if (dchaService) {
                    mDchaService = IDchaService.Stub.asInterface(iBinder);
                    try {
                        switch (flag) {
                            case FLAG_SET_DCHA_STATE_0:
                                if (!confirmationDialog()) {
                                    mDchaService.setSetupStatus(0);
                                }
                                break;
                            case FLAG_SET_DCHA_STATE_3:
                                if (!confirmationDialog()) {
                                    mDchaService.setSetupStatus(3);
                                }
                                break;
                            case FLAG_HIDE_NAVIGATION_BAR:
                                mDchaService.hideNavigationBar(true);
                                break;
                            case FLAG_VIEW_NAVIGATION_BAR:
                                mDchaService.hideNavigationBar(false);
                                break;
                            case FLAG_REBOOT:
                                mDchaService.rebootPad(0, null);
                                break;
                            case FLAG_SET_LAUNCHER:
                                mDchaService.clearDefaultPreferredApp(getLauncherPackage());
                                mDchaService.setDefaultPreferredHomeApp(setLauncherPackage);
                                /* listviewの更新 */
                                mListView.invalidateViews();
                                setCheckedSwitch();
                                break;
                            case FLAG_CHECK:
                            case FLAG_TEST:
                                break;
                        }
                    } catch (RemoteException ignored) {
                    }
                } else {
                    mIDchaUtilService = IDchaUtilService.Stub.asInterface(iBinder);
                    try {
                        switch (flag) {
                            case FLAG_CHECK:
                                break;
                            case FLAG_RESOLUTION:
                                mIDchaUtilService.setForcedDisplaySize(width, height);
                                break;
                        }
                    } catch (RemoteException ignored) {
                    }
                }
                getActivity().unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                getActivity().unbindService(this);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    /* 設定変更 */
    private void settingsFlag(int flag) {
        switch (flag) {
            case FLAG_SET_DCHA_STATE_0:
                if (!confirmationDialog()) {
                    Settings.System.putInt(getActivity().getContentResolver(), DCHA_STATE, 0);
                }
                break;
            case FLAG_SET_DCHA_STATE_3:
                if (!confirmationDialog()) {
                    Settings.System.putInt(getActivity().getContentResolver(), DCHA_STATE, 3);
                }
                break;
            case FLAG_HIDE_NAVIGATION_BAR:
                Settings.System.putInt(getActivity().getContentResolver(), HIDE_NAVIGATION_BAR, 1);
                break;
            case FLAG_VIEW_NAVIGATION_BAR:
                Settings.System.putInt(getActivity().getContentResolver(), HIDE_NAVIGATION_BAR, 0);
                break;
            case FLAG_USB_DEBUG_TRUE:
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                break;
            case FLAG_USB_DEBUG_FALSE:
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                break;
            case FLAG_MARKET_APP_TRUE:
                Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                break;
            case FLAG_MARKET_APP_FALSE:
                Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
                break;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_main, rootKey);

        instance = this;
        Uri contentDchaState = Settings.System.getUriFor(DCHA_STATE);
        Uri contentHideNavigationBar = Settings.System.getUriFor(HIDE_NAVIGATION_BAR);
        Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
        Uri contentUsbDebug = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);

        switchDchaState = findPreference("switch1");
        switchKeepDchaState = findPreference("switch2");
        switchHideBar = findPreference("switch3");
        switchEnableService = findPreference("switch4");
        switchMarketApp = findPreference("switch5");
        switchKeepMarketApp = findPreference("switch6");
        switchUsbDebug = findPreference("switch7");
        switchKeepUsbDebug = findPreference("switch8");
        switchKeepHome = findPreference("switch9");
        switchDeviceAdministrator = findPreference("switch10");
        preferenceChangeHome = findPreference("android_home");
        preferenceDchaService = findPreference("dcha_service");
        preferenceOtherSettings = findPreference("android_settings");
        preferenceReboot = findPreference("android_reboot");
        preferenceRebootShortCut = findPreference("android_reboot_shortcut");
        preferenceEmergencyManual = findPreference("emergency_manual");
        preferenceNormalLauncher = findPreference("normal_mode_launcher");
        preferenceNormalManual = findPreference("normal_manual");
        preferenceSilentInstall = findPreference("android_silent_install");
        preferenceResolution = findPreference("android_resolution");
        preferenceResolutionReset = findPreference("android_resolution_reset");
        preferenceCopy = findPreference("android_copy");
        preferenceDeviceOwner = findPreference("device_owner");

        /* オブサーバーを有効化 */
        isObserberStateEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentDchaState, false, observerState);
        isObserberHideEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentHideNavigationBar, false, observerHide);
        isObserberMarketEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentMarketApp, false, observerMarket);
        isObserberUsbEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentUsbDebug, false, observerUsb);

        /* 一括変更 */
        setCheckedSwitch();

        /* リスナーを有効化 */
        switchDchaState.setOnPreferenceChangeListener((preference, o) -> {
            if (!GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    settingsFlag(FLAG_SET_DCHA_STATE_3);
                } else {
                    settingsFlag(FLAG_SET_DCHA_STATE_0);
                }
            } else if (GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    bindDchaService(FLAG_SET_DCHA_STATE_3, DCHA_MODE);
                } else {
                    bindDchaService(FLAG_SET_DCHA_STATE_0, DCHA_MODE);
                }
            }
            return false;
        });

        switchHideBar.setOnPreferenceChangeListener((preference, o) -> {
            if (!GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    settingsFlag(FLAG_HIDE_NAVIGATION_BAR);
                } else {
                    settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                }
            } else if (GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    bindDchaService(FLAG_HIDE_NAVIGATION_BAR, DCHA_MODE);
                } else {
                    bindDchaService(FLAG_VIEW_NAVIGATION_BAR, DCHA_MODE);
                }
            }
            return false;
        });

        switchEnableService.setOnPreferenceChangeListener((preference, o) -> {
            getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_SERVICE, (boolean) o).apply();
            if ((boolean) o) {
                settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                getActivity().startService(new Intent(getActivity(), KeepService.class));
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        try {
                            KeepService.getInstance().startService();
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            } else {
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        KeepService.getInstance().stopService(1);
                        return true;
                    }
                }
            }
            return true;
        });

        switchKeepMarketApp.setOnPreferenceChangeListener((preference, o) -> {
            getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_MARKET_APP_SERVICE, (boolean) o).apply();
            if ((boolean) o) {
                try {
                    Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                    getActivity().startService(new Intent(getActivity(), KeepService.class));
                    for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                        if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            try {
                                KeepService.getInstance().startService();
                            } catch (NullPointerException ignored) {
                            }
                        }
                    }
                } catch (SecurityException e) {
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                    toast.show();
                    getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false).apply();
                    switchKeepMarketApp.setChecked(false);
                    return false;
                }
            } else {
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        KeepService.getInstance().stopService(3);
                        return true;
                    }
                }
            }
            return true;
        });

        switchKeepUsbDebug.setOnPreferenceChangeListener((preference, o) -> {
            if (confirmationDialog()) {
                return false;
            }
            getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_USB_DEBUG, (boolean) o).apply();
            if ((boolean) o) {
                try {
                    if (GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_3);
                    }
                    Thread.sleep(100);
                    Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                    if (GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                    getActivity().startService(new Intent(getActivity(), KeepService.class));
                    for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                        if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            try {
                                KeepService.getInstance().startService();
                            } catch (NullPointerException ignored) {
                            }
                        }
                    }
                } catch (SecurityException | InterruptedException e) {
                    if (GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                    toast.show();
                    getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_USB_DEBUG, false).apply();
                    switchKeepUsbDebug.setChecked(false);
                    return false;
                }
            } else {
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE))
                    if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        KeepService.getInstance().stopService(4);
                        return true;
                    }
            }
            return true;
        });

        switchKeepDchaState.setOnPreferenceChangeListener((preference, o) -> {
            if (confirmationDialog()) {
                return false;
            }
            getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_DCHA_STATE, (boolean) o).apply();
            if ((boolean) o) {
                settingsFlag(FLAG_SET_DCHA_STATE_0);
                getActivity().startService(new Intent(getActivity(), KeepService.class));
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        try {
                            KeepService.getInstance().startService();
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            } else {
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        KeepService.getInstance().stopService(2);
                        return true;
                    }
                }
            }
            return true;
        });

        switchKeepHome.setOnPreferenceChangeListener((preference, o) -> {
            getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED_KEEP_HOME, (boolean) o).apply();
            if ((boolean) o) {
                getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit().putString(KEY_SAVE_KEEP_HOME, getLauncherPackage()).apply();
                getActivity().startService(new Intent(getActivity(), KeepService.class));
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        try {
                            KeepService.getInstance().startService();
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            } else {
                for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                    if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        KeepService.getInstance().stopService(5);
                        return true;
                    }
                }
            }
            return true;
        });

        switchMarketApp.setOnPreferenceChangeListener((preference, o) -> {
            if ((boolean) o) {
                try {
                    settingsFlag(FLAG_MARKET_APP_TRUE);
                } catch (SecurityException ignored) {
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        switchMarketApp.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
                    } catch (Settings.SettingNotFoundException ignored1) {
                    }
                }
            } else {
                try {
                    settingsFlag(FLAG_MARKET_APP_FALSE);
                } catch (SecurityException ignored) {
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        switchMarketApp.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
                    } catch (Settings.SettingNotFoundException ignored1) {
                    }
                }
            }
            return false;
        });

        switchUsbDebug.setOnPreferenceChangeListener((preference, o) -> {
            if (confirmationDialog()) {
                return false;
            }
            if ((boolean) o) {
                try {
                    if (GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_3);
                        Thread.sleep(100);
                    }
                    settingsFlag(FLAG_USB_DEBUG_TRUE);
                    if (GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                } catch (SecurityException | InterruptedException ignored) {
                    if (GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        switchUsbDebug.setChecked(Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED) != 0);
                    } catch (Settings.SettingNotFoundException ignored1) {
                    }
                }
            } else {
                try {
                    settingsFlag(FLAG_USB_DEBUG_FALSE);
                } catch (SecurityException ignored) {
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        switchUsbDebug.setChecked(Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED) != 0);
                    } catch (Settings.SettingNotFoundException ignored1) {
                    }
                }
            }
            return false;
        });

        switchDeviceAdministrator.setOnPreferenceChangeListener((preference, o) -> {
            if ((boolean) o) {
                if (!((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(new ComponentName(getActivity(), AdministratorReceiver.class))) {
                    startActivityForResult(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(getActivity(), AdministratorReceiver.class)), REQUEST_ADMIN);
                }
            } else {
                switchDeviceAdministrator.setChecked(true);
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_title_dcha_service)
                        .setMessage(R.string.dialog_question_device_admin)
                        .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                            ((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).removeActiveAdmin(new ComponentName(getActivity(), AdministratorReceiver.class));
                            switchDeviceAdministrator.setChecked(false);
                        })

                        .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                            switchDeviceAdministrator.setChecked(true);
                            dialog.dismiss();
                        })
                        .show();
            }
            return false;
        });

        preferenceEmergencyManual.setOnPreferenceClickListener(preference -> {
            TextView textView = new TextView(getActivity());
            textView.setText(R.string.dialog_emergency_manual_red);
            textView.setTextSize(16);
            textView.setTextColor(Color.RED);
            textView.setPadding(20, 0, 40, 20);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_emergency_manual)
                    .setMessage(R.string.dialog_emergency_manual)
                    .setView(textView)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceNormalManual.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_normal_manual)
                    .setMessage(R.string.dialog_normal_manual)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceNormalLauncher.setOnPreferenceClickListener(preference -> {
            View view = getActivity().getLayoutInflater().inflate(R.layout.normal_mode_launcher, null);
            List<ResolveInfo> installedAppList = getActivity().getPackageManager().queryIntentActivities(new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
            List<NormalLauncherActivity.AppData> dataList = new ArrayList<>();
            for (ResolveInfo resolveInfo : installedAppList) {
                NormalLauncherActivity.AppData data = new NormalLauncherActivity.AppData();
                data.label = resolveInfo.loadLabel(getActivity().getPackageManager()).toString();
                data.icon = resolveInfo.loadIcon(getActivity().getPackageManager());
                data.packName = resolveInfo.activityInfo.packageName;
                dataList.add(data);
            }
            ListView listView = view.findViewById(R.id.normal_launcher_list);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new NormalLauncherActivity.AppListAdapter(getActivity(), dataList));
            listView.setOnItemClickListener((parent, mView, position, id) -> {
                SET_NORMAL_LAUNCHER(Uri.fromParts("package", installedAppList.get(position).activityInfo.packageName, null).toString().replace("package:", ""), StartActivity.getInstance());
                /* listviewの更新 */
                listView.invalidateViews();
                setCheckedSwitch();
            });
            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.dialog_title_launcher)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceReboot.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.dialog_title_reboot)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> bindDchaService(FLAG_REBOOT, DCHA_MODE))
                    .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceRebootShortCut.setOnPreferenceClickListener(preference -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().getSystemService(ShortcutManager.class).requestPinShortcut(new ShortcutInfo.Builder(getActivity(), "再起動")
                        .setShortLabel("再起動")
                        .setIcon(Icon.createWithResource(getActivity(), R.drawable.reboot))
                        .setIntent(new Intent(Intent.ACTION_MAIN).setClassName(getActivity(), "com.saradabar.cpadcustomizetool.RebootActivity"))
                        .build(), null);
            } else {
                makeRebootShortcut();
            }
            return false;
        });

        preferenceDchaService.setOnPreferenceClickListener(preference -> {
            if (confirmationDialog()) {
                return false;
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_dcha_service)
                    .setMessage(R.string.dialog_dcha_service)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        if (StartActivity.getInstance().bindDchaService()) {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.dialog_error_no_work_dcha)
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                    .show();
                        } else {
                            SET_DCHASERVICE_FLAG(true, getActivity());
                            getActivity().finish();
                            getActivity().overridePendingTransition(0, 0);
                            startActivity(getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", true));
                        }
                    })
                    .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceChangeHome.setOnPreferenceClickListener(preference -> {
            View view = getActivity().getLayoutInflater().inflate(R.layout.launcher_list, null);
            List<ResolveInfo> installedAppList = getActivity().getPackageManager().queryIntentActivities(new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
            List<HomeLauncherActivity.AppData> dataList = new ArrayList<>();
            for (ResolveInfo resolveInfo : installedAppList) {
                HomeLauncherActivity.AppData data = new HomeLauncherActivity.AppData();
                data.label = resolveInfo.loadLabel(getActivity().getPackageManager()).toString();
                data.icon = resolveInfo.loadIcon(getActivity().getPackageManager());
                data.packName = resolveInfo.activityInfo.packageName;
                dataList.add(data);
            }
            mListView = view.findViewById(R.id.launcher_list);
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            mListView.setAdapter(new HomeLauncherActivity.AppListAdapter(getActivity(), dataList));
            mListView.setOnItemClickListener((parent, mView, position, id) -> {
                setLauncherPackage = Uri.fromParts("package", installedAppList.get(position).activityInfo.packageName, null).toString().replace("package:", "");
                bindDchaService(FLAG_SET_LAUNCHER, DCHA_MODE);
            });
            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.dialog_title_launcher)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
            transitionFragment(new MainOtherFragment());
            return false;
        });

        preferenceSilentInstall.setOnPreferenceClickListener(preference -> {
            preferenceSilentInstall.setEnabled(false);
            try {
                startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/vnd.android.package-archive").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false), ""), REQUEST_INSTALL);
            } catch (ActivityNotFoundException ignored) {
                preferenceSilentInstall.setEnabled(true);
                new AlertDialog.Builder(getActivity())
                        .setMessage("ファイルブラウザがインストールされていません")
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
            return false;
        });

        preferenceResolution.setOnPreferenceClickListener(preference -> {
            /* DchaUtilServiceが機能しているか */
            if (bindDchaService(FLAG_CHECK, DCHA_UTIL_MODE)) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.dialog_error_no_work_dcha_util)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
                return false;
            }

            View view = getActivity().getLayoutInflater().inflate(R.layout.resolution_dialog, null);
            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.dialog_title_resolution)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> {
                        ((InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
                        EditText editTextWidth = view.findViewById(R.id.edit_text_1);
                        EditText editTextHeight = view.findViewById(R.id.edit_text_2);
                        try {
                            width = Integer.parseInt(editTextWidth.getText().toString());
                            height = Integer.parseInt(editTextHeight.getText().toString());
                            if (width < 300 || height < 300) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.dialog_title_error)
                                        .setMessage(R.string.dialog_illegal_value)
                                        .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                        .show();
                            } else {
                                MainFragment.setResolutionTask resolutionTask = new MainFragment.setResolutionTask();
                                resolutionTask.setListener(StartActivity.getInstance().mCreateListener());
                                resolutionTask.execute();
                            }
                        } catch (NumberFormatException ignored) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.dialog_title_error)
                                    .setMessage(R.string.dialog_illegal_value)
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_common_cancel, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceResolutionReset.setOnPreferenceClickListener(preference -> {
            resetResolution();
            return false;
        });

        preferenceCopy.setOnPreferenceClickListener(preference -> {
            CopyTask copy = new CopyTask();
            copy.setListener(StartActivity.getInstance().CopyListener());
            copy.execute();
            return false;
        });

        preferenceDeviceOwner.setOnPreferenceClickListener(preference -> {
            transitionFragment(new DeviceOwnerFragment());
            return false;
        });

        switch (GET_MODEL_ID(getActivity())) {
            case 0:
                preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceSilentInstall.setEnabled(false);
                break;
            case 1:
                preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceSilentInstall.setEnabled(false);
                switchDeviceAdministrator.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchDeviceAdministrator.setEnabled(false);
                break;
            case 2:
                switchMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchKeepMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchMarketApp.setEnabled(false);
                switchKeepMarketApp.setEnabled(false);
                break;
        }

        if (((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("DeviceOwnerのためこの機能は使用できません");
        }

        /* DchaServiceを使用するか */
        if (!GET_DCHASERVICE_FLAG(getActivity())) {
            getPreferenceScreen().removePreference(findPreference("android_silent_install"));
            getPreferenceScreen().removePreference(findPreference("android_home"));
            getPreferenceScreen().removePreference(findPreference("switch9"));
            getPreferenceScreen().removePreference(findPreference("category_emergency"));
            getPreferenceScreen().removePreference(findPreference("category_normal"));
            getPreferenceScreen().removePreference(findPreference("android_reboot"));
            getPreferenceScreen().removePreference(findPreference("android_reboot_shortcut"));
            getPreferenceScreen().removePreference(findPreference("android_resolution"));
            getPreferenceScreen().removePreference(findPreference("android_resolution_reset"));
        } else {
            getPreferenceScreen().removePreference(findPreference("dcha_service"));
        }
        getPreferenceScreen().removePreference(findPreference("android_copy"));
    }

    /* 確認ダイアログ */
    private boolean confirmationDialog() {
        if (!COUNT_DCHA_COMPLETED_FILE.exists() && IGNORE_DCHA_COMPLETED_FILE.exists()) {
            if (!GET_CONFIRMATION(getActivity())) {
                new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setTitle("本当によろしいですか？")
                        .setMessage("このデバイスのシステム領域にDchaServiceが恒久的な変更を加えていないことを検出しました\n続行すると一部の動作がAndroidシステムによって制限される可能性があります")
                        .setPositiveButton(R.string.dialog_common_continue, (dialog, which) -> {
                            new AlertDialog.Builder(getActivity())
                                    .setCancelable(false)
                                    .setTitle("最終確認")
                                    .setMessage("続行するとこの操作は取り消せません\n初期化をおこなってもシステム領域の変更、Androidシステムによる制限はもとには戻りません\n続行すると警告は無効になり、設定変更が可能になります")
                                    .setPositiveButton(R.string.dialog_common_continue, (dialog1, which1) -> {
                                        SET_CONFIRMATION(true, getActivity());
                                        dialog1.dismiss();
                                    })
                                    .setNegativeButton(R.string.dialog_common_cancel, (dialog1, which1) -> dialog.dismiss())
                                    .show();
                        })
                        .setNegativeButton(R.string.dialog_common_cancel, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /* ランチャーのパッケージ名を取得 */
    private String getLauncherPackage() {
        return (getActivity().getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)).activityInfo.packageName;
    }

    /* ランチャーの名前を取得 */
    private String getLauncherName() {
        return (getActivity().getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)).activityInfo.loadLabel(getActivity().getPackageManager()).toString();
    }

    /* 再起動ショートカットを作成 */
    private void makeRebootShortcut() {
        getActivity().sendBroadcast(new Intent("com.android.launcher.action.INSTALL_SHORTCUT")
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN)
                        .setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity")
                        .setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity"))
                .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.reboot))
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.reboot));
        if (null != toast) toast.cancel();
        toast = Toast.makeText(getActivity(), R.string.toast_common_success, Toast.LENGTH_SHORT);
        toast.show();
    }

    /* スイッチ一括変更 */
    private void setCheckedSwitch() {
        SharedPreferences sp = getActivity().getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE);
        try {
            switchDchaState.setChecked(Settings.System.getInt(getActivity().getContentResolver(), DCHA_STATE) != 0);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        try {
            switchHideBar.setChecked(Settings.System.getInt(getActivity().getContentResolver(), HIDE_NAVIGATION_BAR) != 0);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        try {
            switchMarketApp.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        try {
            switchUsbDebug.setChecked(Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED) != 0);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        switchDeviceAdministrator.setChecked(((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(new ComponentName(getActivity(), AdministratorReceiver.class)));
        switchEnableService.setChecked(sp.getBoolean(KEY_ENABLED_KEEP_SERVICE, false));
        switchKeepMarketApp.setChecked(sp.getBoolean(KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false));
        switchKeepDchaState.setChecked(sp.getBoolean(KEY_ENABLED_KEEP_DCHA_STATE, false));
        switchKeepUsbDebug.setChecked(sp.getBoolean(KEY_ENABLED_KEEP_USB_DEBUG, false));
        switchKeepHome.setChecked(sp.getBoolean(KEY_ENABLED_KEEP_HOME, false));
        preferenceChangeHome.setSummary(getLauncherName());
        String normalLauncherName = null;
        try {
            normalLauncherName = (String) getActivity().getPackageManager().getApplicationLabel(getActivity().getPackageManager().getApplicationInfo(GET_NORMAL_LAUNCHER(getActivity()), 0));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        if (normalLauncherName == null) {
            preferenceNormalLauncher.setSummary("変更するランチャーは設定されていません");
        } else {
            preferenceNormalLauncher.setSummary("変更するランチャーは" + normalLauncherName + "に設定されています");
        }

        if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
            getActivity().startService(new Intent(getActivity(), KeepService.class));
            for (ActivityManager.RunningServiceInfo serviceInfo : ((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                    try {
                        KeepService.getInstance().startService();
                    } catch (NullPointerException ignored) {
                    }
                }
            }
        }
    }

    /* アクティビティ破棄 */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isObserberStateEnable) {
            getActivity().getContentResolver().unregisterContentObserver(observerState);
            isObserberStateEnable = false;
        }
        if (isObserberHideEnable) {
            getActivity().getContentResolver().unregisterContentObserver(observerHide);
            isObserberHideEnable = false;
        }
        if (isObserberMarketEnable) {
            getActivity().getContentResolver().unregisterContentObserver(observerMarket);
            isObserberMarketEnable = false;
        }
        if (isObserberUsbEnable) {
            getActivity().getContentResolver().unregisterContentObserver(observerUsb);
            isObserberUsbEnable = false;
        }
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        instance = this;
        Uri contentDchaState = Settings.System.getUriFor(DCHA_STATE);
        Uri contentHideNavigationBar = Settings.System.getUriFor(HIDE_NAVIGATION_BAR);
        Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
        Uri contentUsbDebug = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);

        if (getActivity().getActionBar() != null) getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        if (!preferenceChangeHome.isEnabled()) preferenceChangeHome.setEnabled(true);

        /* オブザーバー有効 */
        isObserberStateEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentDchaState, false, observerState);
        isObserberHideEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentHideNavigationBar, false, observerHide);
        isObserberMarketEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentMarketApp, false, observerMarket);
        isObserberUsbEnable = true;
        getActivity().getContentResolver().registerContentObserver(contentUsbDebug, false, observerUsb);

        /* 一括変更 */
        setCheckedSwitch();

        switch (GET_MODEL_ID(getActivity())) {
            case 0:
                preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceSilentInstall.setEnabled(false);
                break;
            case 1:
                preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceSilentInstall.setEnabled(false);
                switchDeviceAdministrator.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchDeviceAdministrator.setEnabled(false);
                break;
            case 2:
                switchMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchKeepMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchMarketApp.setEnabled(false);
                switchKeepMarketApp.setEnabled(false);
                break;
        }

        if (((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("DeviceOwnerのためこの機能は使用できません");
        }
    }

    /* フラグメント切り替え */
    private void transitionFragment(PreferenceFragment preferenceFragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.layout_main, preferenceFragment)
                .commit();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INSTALL) {
            preferenceSilentInstall.setEnabled(true);
            /* シングルApk */
            try {
                installData = getInstallData(getActivity(), data.getData());
            } catch (NullPointerException ignored) {
                installData = null;
            }
            if (installData != null) {
                silentInstallTask silent = new silentInstallTask();
                silent.setListener(StartActivity.getInstance().createListener());
                silent.execute();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setMessage("ファイルデータを取得できませんでした")
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }

    /* 選択したファイルデータを取得 */
    private String getInstallData(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String[] str = DocumentsContract.getDocumentId(uri).split(":");
            switch (uri.getAuthority()) {
                case "com.android.externalstorage.documents":
                    return Environment.getExternalStorageDirectory() + "/" + str[1];
                case "com.android.providers.downloads.documents":
                    return str[1];
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /* コピータスク */
    public static class CopyTask extends AsyncTask<Object, Void, Object> {
        private Listener mListener;

        @Override
        protected void onPreExecute() {
            mListener.onShow();
        }

        @Override
        protected Object doInBackground(Object... value) {
            if (MainFragment.getInstance().copySystemFile()) {
                return new Object();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                mListener.onSuccess();
            } else mListener.onFailure();
        }

        public void setListener(Listener listener) {
            mListener = listener;
        }

        /* StartActivity */
        public interface Listener {
            void onShow();

            void onSuccess();

            void onFailure();
        }
    }

    /* インストールタスク */
    public static class silentInstallTask extends AsyncTask<Object, Void, Object> {
        private Listener mListener;

        @Override
        protected void onPreExecute() {
            mListener.onShow();
        }

        @Override
        protected Object doInBackground(Object... value) {
            if (MainFragment.getInstance().installApp()) {
                return new Object();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                mListener.onSuccess();
            } else mListener.onFailure();
        }

        public void setListener(Listener listener) {
            mListener = listener;
        }

        /* StartActivity */
        public interface Listener {
            void onShow();

            void onSuccess();

            void onFailure();
        }
    }

    /* 解像度タスク */
    public static class setResolutionTask extends AsyncTask<Object, Void, Object> {
        private Listener mListener;

        @Override
        protected Object doInBackground(Object... value) {
            if (MainFragment.getInstance().setResolution()) {
                /* 待機 */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                return new Object();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                mListener.onSuccess();
            } else mListener.onFailure();
        }

        public void setListener(Listener listener) {
            mListener = listener;
        }

        /* StartActivity */
        public interface Listener {
            void onSuccess();

            void onFailure();
        }
    }

    public boolean copySystemFile() {
        if (!bindDchaService(FLAG_CHECK, DCHA_MODE)) {
            /* 一回目に失敗する問題を防ぐ */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            try {
                return mDchaService.copyUpdateImage("", "");
            } catch (RemoteException | NullPointerException ignored) {
                return false;
            }
        } else {
            return false;
        }
    }

    /* サイレントインストール */
    public boolean installApp() {
        if (!bindDchaService(FLAG_CHECK, DCHA_MODE)) {
            /* 一回目に失敗する問題を防ぐ */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            try {
                return mDchaService.installApp(installData, 1);
            } catch (RemoteException | NullPointerException ignored) {
                return false;
            }
        } else {
            return false;
        }
    }

    /* 解像度の変更 */
    public boolean setResolution() {
        if (!bindDchaService(FLAG_CHECK, DCHA_UTIL_MODE)) {
            /* 一回目に失敗する問題を防ぐ */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            try {
                return mIDchaUtilService.setForcedDisplaySize(width, height);
            } catch (RemoteException ignored) {
                return false;
            }
        } else {
            return false;
        }
    }

    /* 解像度のリセット */
    public void resetResolution() {
        switch (GET_MODEL_ID(getActivity())) {
            case 0:
            case 1:
                width = 1280;
                height = 800;
                if (bindDchaService(FLAG_RESOLUTION, DCHA_UTIL_MODE)) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.dialog_error_no_work_dcha_util)
                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                            .show();
                }
                break;
            case 2:
                width = 1920;
                height = 1200;
                if (bindDchaService(FLAG_RESOLUTION, DCHA_UTIL_MODE)) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.dialog_error_no_work_dcha_util)
                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                            .show();
                }
                break;
        }
    }
}