package com.saradabar.cpadcustomizetool;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.Service.KeepDchaService;
import com.saradabar.cpadcustomizetool.Service.KeepHomeService;
import com.saradabar.cpadcustomizetool.Service.KeepMarketAppService;
import com.saradabar.cpadcustomizetool.Service.KeepNavigationBarService;
import com.saradabar.cpadcustomizetool.Service.KeepUsbDebugService;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static android.content.Context.DEVICE_POLICY_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_NOT_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.mComponentName;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.mDevicePolicyManager;

public class MainFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    //定義
    private AlertDialog alertDialog;

    private int connectionFlag;
    private static final int FLAG_TEST = 0;
    private static final int FLAG_SET_DCHA_STATE_0 = 1;
    private static final int FLAG_SET_DCHA_STATE_3 = 2;
    private static final int FLAG_HIDE_NAVIGATION_BAR = 3;
    private static final int FLAG_VIEW_NAVIGATION_BAR = 4;
    private static final int FLAG_REBOOT = 5;
    private static final int FLAG_USB_DEBUG_TRUE = 6;
    private static final int FLAG_USB_DEBUG_FALSE = 7;
    private static final int FLAG_MARKET_APP_TRUE = 8;
    private static final int FLAG_MARKET_APP_FALSE = 9;

    private ContentResolver resolver;

    private final String dchaStateString = DCHA_STATE;
    private final String hideNavigationBarString = HIDE_NAVIGATION_BAR;

    private final Uri contentDchaState = Settings.System.getUriFor(dchaStateString);
    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
    private final Uri contentUsbDebug = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);

    private boolean isObserberStateEnable = false;
    private boolean isObserberHideEnable = false;
    private boolean isObserberMarketEnable = false;
    private boolean isObserberUsbEnable = false;

    private SwitchPreference switchDchaState;
    private SwitchPreference switchKeepDchaState;
    private SwitchPreference switchHideBar;
    private SwitchPreference switchEnableService;
    private SwitchPreference switchMarketApp;
    private SwitchPreference switchKeepMarketApp;
    private SwitchPreference switchUsbDebug;
    private SwitchPreference switchKeepUsbDebug;
    private SwitchPreference switchKeepHome;
    private SwitchPreference switchDeviceAdministrator;

    private Preference preferenceChangeHome;

    private IDchaService mDchaService;

    Toast toast;

    //***データ管理
    private void SET_USE_DCHASERVICE(int USE_DCHASERVICE) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putInt("USE_DCHASERVICE", USE_DCHASERVICE).apply();
    }

    private int GET_USE_DCHASERVICE() {
        int USE_DCHASERVICE;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        USE_DCHASERVICE = sp.getInt("USE_DCHASERVICE", 0);
        return USE_DCHASERVICE;
    }

    public int GET_CHECK_TAB_ID() {
        int CHECK_TAB_ID;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CHECK_TAB_ID = sp.getInt("CHECK_TAB_ID", 0);
        return CHECK_TAB_ID;
    }

    private int GET_CHANGE_SETTINGS_USE_DCHA() {
        int IS_USE_DCHA;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        IS_USE_DCHA = sp.getInt("IS_USE_DCHA", 0);
        return IS_USE_DCHA;
    }
    //***

    private static Set<String> getEmergencySettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getStringSet(Common.Customizetool.KEY_EMERGENCY_SETTINGS, null);
    }

    public static boolean isEmergencySettings_Dcha_State(Context context) {
        final String value = "1";
        Set<String> Emergency_Settings = getEmergencySettings(context);
        if (Emergency_Settings != null) {
            return Emergency_Settings.contains(value);
        }
        return false;
    }

    public static boolean isEmergencySettings_Hide_NavigationBar(Context context) {
        final String value = "2";
        Set<String> Emergency_Settings_Hide_NavigationBar = getEmergencySettings(context);
        if (Emergency_Settings_Hide_NavigationBar != null) {
            return Emergency_Settings_Hide_NavigationBar.contains(value);
        }
        return false;
    }

    public static boolean isEmergencySettings_Change_Home(Context context) {
        final String value = "3";
        Set<String> Emergency_Settings_Change_Home = getEmergencySettings(context);
        if (Emergency_Settings_Change_Home != null) {
            return Emergency_Settings_Change_Home.contains(value);
        }
        return false;
    }

    public static boolean isEmergencySettings_Remove_Task(Context context) {
        final String value = "4";
        Set<String> Emergency_Settings_Remove_Task = getEmergencySettings(context);
        if (Emergency_Settings_Remove_Task != null) {
            return Emergency_Settings_Remove_Task.contains(value);
        }
        return false;
    }

    private static Set<String> getNormalModeSettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getStringSet(Common.Customizetool.KEY_NORMAL_MODE_SETTINGS, null);
    }

    public static boolean isNormalModeSettings_Dcha_State(Context context) {
        final String value = "1";
        Set<String> NormalModeSettings_DchaState = getNormalModeSettings(context);
        if (NormalModeSettings_DchaState != null) {
            return NormalModeSettings_DchaState.contains(value);
        }
        return false;
    }

    public static boolean isNormalModeSettings_Hide_NavigationBar(Context context) {
        final String value = "2";
        Set<String> NormalModeSettings_HideNavigationBar = getNormalModeSettings(context);
        if (NormalModeSettings_HideNavigationBar != null) {
            return NormalModeSettings_HideNavigationBar.contains(value);
        }
        return false;
    }

    public static boolean isNormalModeSettings_Change_Home(Context context) {
        final String value = "3";
        Set<String> NormalModeSettings_ChangeHome = getNormalModeSettings(context);
        if (NormalModeSettings_ChangeHome != null) {
            return NormalModeSettings_ChangeHome.contains(value);
        }
        return false;
    }

    public static boolean isNormalModeSettings_Change_Activity(Context context) {
        final String value = "4";
        Set<String> NormalModeSettings_ChangeActivity = getNormalModeSettings(context);
        if (NormalModeSettings_ChangeActivity != null) {
            return NormalModeSettings_ChangeActivity.contains(value);
        }
        return false;
    }

    //システムUIオブザーバー
    private final ContentObserver observerState = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    //ナビゲーションバーオブザーバー
    private final ContentObserver observerHide = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    //提供元オブザーバー
    private final ContentObserver observerMarket = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    //USBDebugオブザーバー
    private final ContentObserver observerUsb = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
            } catch (Settings.SettingNotFoundException ignored) {
            }
        }
    };

    //接続
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            try {
                switch (connectionFlag) {
                    case FLAG_SET_DCHA_STATE_0:
                        mDchaService.setSetupStatus(0);
                        break;
                    case FLAG_SET_DCHA_STATE_3:
                        mDchaService.setSetupStatus(3);
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
                    case FLAG_TEST:
                        break;
                }
            } catch (RemoteException ignored) {
            }
            getActivity().unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    //設定変更
    private void settingsFlag(int flag) {
        switch (flag) {
            case FLAG_SET_DCHA_STATE_0:
                Settings.System.putInt(resolver, dchaStateString, 0);
                break;
            case FLAG_SET_DCHA_STATE_3:
                Settings.System.putInt(resolver, dchaStateString, 3);
                break;
            case FLAG_HIDE_NAVIGATION_BAR:
                Settings.System.putInt(resolver, hideNavigationBarString, 1);
                break;
            case FLAG_VIEW_NAVIGATION_BAR:
                Settings.System.putInt(resolver, hideNavigationBarString, 0);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pre_main);
        findPreference("Android_Setting").setOnPreferenceClickListener(this);

        resolver = getActivity().getContentResolver();
        switchDchaState = (SwitchPreference) findPreference("switch1");
        switchKeepDchaState = (SwitchPreference) findPreference("switch2");
        switchHideBar = (SwitchPreference) findPreference("switch3");
        switchEnableService = (SwitchPreference) findPreference("switch4");
        switchMarketApp = (SwitchPreference) findPreference("switch5");
        switchKeepMarketApp = (SwitchPreference) findPreference("switch6");
        switchUsbDebug = (SwitchPreference) findPreference("switch7");
        switchKeepUsbDebug = (SwitchPreference) findPreference("switch8");
        switchKeepHome = (SwitchPreference) findPreference("switch9");
        switchDeviceAdministrator = (SwitchPreference) findPreference("switch10");
        preferenceChangeHome = findPreference("Android_Home");
        final Preference preferenceDchaService = findPreference("Dcha_Service");
        final Preference preferenceOtherSettings = findPreference("Android_Setting");
        final Preference preferenceReboot = findPreference("Android_Reboot");
        final Preference preferenceRebootShortCut = findPreference("Android_Reboot_ShortCut");
        final Preference preferenceEmergencyManual = findPreference("Emergency_Manual");
        final Preference preferenceNormalManual = findPreference("Normal_Manual");
        final Preference preferenceTEST = findPreference("TEST");
        //        final Preference preferenceResolutionSettings = findPreference("Android_resolution_Settings");

        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 3);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
            SharedPreferences sp = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            switchEnableService.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_SERVICE, false));
            switchKeepMarketApp.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false));
            switchKeepDchaState.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_DCHA_STATE, false));
            switchKeepUsbDebug.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_USB_DEBUG, false));
            switchKeepHome.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_HOME, false));
            switchDeviceAdministrator.setChecked(mDevicePolicyManager.isAdminActive(mComponentName));

            // オブサーバーを有効化
            isObserberStateEnable = true;
            resolver.registerContentObserver(contentDchaState, false, observerState);
            isObserberHideEnable = true;
            resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);
            isObserberMarketEnable = true;
            resolver.registerContentObserver(contentMarketApp, false, observerMarket);
            isObserberMarketEnable = true;
            resolver.registerContentObserver(contentUsbDebug, false, observerUsb);

            // リスナーを有効化
            switchDchaState.setOnPreferenceChangeListener((preference, o) -> {
                if (GET_CHANGE_SETTINGS_USE_DCHA() == 0) {
                    if ((boolean) o) {
                        settingsFlag(FLAG_SET_DCHA_STATE_3);
                    } else {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                }else if (GET_CHANGE_SETTINGS_USE_DCHA() == 1) {
                    if ((boolean) o) {
                        bindDchaService(FLAG_SET_DCHA_STATE_3);
                    } else {
                        bindDchaService(FLAG_SET_DCHA_STATE_0);
                    }
                }
                return true;
            });

            switchHideBar.setOnPreferenceChangeListener((preference, o) -> {
                if (GET_CHANGE_SETTINGS_USE_DCHA() == 0) {
                    if ((boolean) o) {
                        settingsFlag(FLAG_HIDE_NAVIGATION_BAR);
                    } else {
                        settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                    }
                }else if (GET_CHANGE_SETTINGS_USE_DCHA() == 1) {
                    if ((boolean) o) {
                        bindDchaService(FLAG_HIDE_NAVIGATION_BAR);
                    } else {
                        bindDchaService(FLAG_VIEW_NAVIGATION_BAR);
                    }
                }
                return true;
            });

            switchEnableService.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp15 = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp15.edit();
                spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_SERVICE, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepNavigationBarService.class.getName().equals(serviceInfo.service.getClassName())) {
                            return true;
                        }
                    }
                    settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                    getActivity().startService(new Intent(getActivity(), KeepNavigationBarService.class));
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepNavigationBarService.class.getName().equals(serviceInfo.service.getClassName())) {
                            getActivity().stopService(new Intent(getActivity(), KeepNavigationBarService.class));
                            return true;
                        }
                    }
                }
                return true;
            });

            switchKeepMarketApp.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp14 = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp14.edit();
                spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepMarketAppService.class.getName().equals(serviceInfo.service.getClassName())) {
                            return true;
                        }
                    }
                    try {
                        Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                        getActivity().startService(new Intent(getActivity(), KeepMarketAppService.class));
                    }catch (SecurityException e){
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                        toast.show();
                        spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false);
                        spe.apply();
                        switchKeepMarketApp.setChecked(false);
                        return false;
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepMarketAppService.class.getName().equals(serviceInfo.service.getClassName())) {
                            getActivity().stopService(new Intent(getActivity(), KeepMarketAppService.class));
                            return true;
                        }
                    }
                }
                return true;
            });

            switchKeepUsbDebug.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp13 = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp13.edit();
                spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_USB_DEBUG, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepUsbDebugService.class.getName().equals(serviceInfo.service.getClassName())) {
                            return true;
                        }
                    }
                    try {
                        Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                        getActivity().startService(new Intent(getActivity(), KeepUsbDebugService.class));
                    }catch (SecurityException e){
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                        toast.show();
                        spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_USB_DEBUG, false);
                        spe.apply();
                        switchKeepUsbDebug.setChecked(false);
                        return false;
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE))
                        if (KeepUsbDebugService.class.getName().equals(serviceInfo.service.getClassName())) {
                            getActivity().stopService(new Intent(getActivity(), KeepUsbDebugService.class));
                            return true;
                        }
                }
                return true;
            });

            switchKeepDchaState.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp12 = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp12.edit();
                spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_DCHA_STATE, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepDchaService.class.getName().equals(serviceInfo.service.getClassName())) {
                            return true;
                        }
                    }
                    settingsFlag(FLAG_SET_DCHA_STATE_0);
                    getActivity().startService(new Intent(getActivity(), KeepDchaService.class));
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepDchaService.class.getName().equals(serviceInfo.service.getClassName())) {
                            getActivity().stopService(new Intent(getActivity(), KeepDchaService.class));
                            return true;
                        }
                    }
                }
                return true;
            });

            switchKeepHome.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp1 = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp1.edit();
                spe.putBoolean(Common.Customizetool.KEY_ENABLED_KEEP_HOME, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepHomeService.class.getName().equals(serviceInfo.service.getClassName())) {
                            return true;
                        }
                    }
                    spe.putString(Common.Customizetool.KEY_SAVE_KEEP_HOME, getHome());
                    spe.apply();
                    getActivity().startService(new Intent(getActivity(), KeepHomeService.class));
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepHomeService.class.getName().equals(serviceInfo.service.getClassName())) {
                            getActivity().stopService(new Intent(getActivity(), KeepHomeService.class));
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
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                        toast.show();
                        try {
                            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
                        } catch (Settings.SettingNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    try {
                        settingsFlag(FLAG_MARKET_APP_FALSE);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                        toast.show();
                        try {
                            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
                        } catch (Settings.SettingNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                return false;
            });

            switchUsbDebug.setOnPreferenceChangeListener((preference, o) -> {
                if ((boolean) o) {
                    try {
                        settingsFlag(FLAG_USB_DEBUG_TRUE);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                        toast.show();
                        try {
                            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
                        } catch (Settings.SettingNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    try {
                        settingsFlag(FLAG_USB_DEBUG_FALSE);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                        toast.show();
                        try {
                            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
                        } catch (Settings.SettingNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                return false;
            });

            switchDeviceAdministrator.setOnPreferenceChangeListener((preference, o) -> {
                if ((boolean) o) {
                    if (!mDevicePolicyManager.isAdminActive(mComponentName)) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "DchaServiceによってこのアプリがアンインストールされないようにするにはこの端末管理アプリを有効にしてください。");
                        startActivityForResult(intent, 1);
                    }
                }else {
                    switchDeviceAdministrator.setChecked(true);
                    if (alertDialog != null && alertDialog.isShowing()) return false;
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle(R.string.dialog_title_dcha_service)
                            .setMessage(R.string.dialog_question_device_admin)
                            .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                                mDevicePolicyManager.removeActiveAdmin(new ComponentName(getActivity(), AdministratorReceiver.class));
                                switchDeviceAdministrator.setChecked(false);
                            })

                            .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                                switchDeviceAdministrator.setChecked(true);
                                dialog.dismiss();
                            });
                    alertDialog = b.create();
                    alertDialog.show();
                }
                return false;
            });

            preferenceEmergencyManual.setOnPreferenceClickListener(preference -> {
                if (alertDialog != null && alertDialog.isShowing()) return true;
                TextView alertView = new TextView(getActivity());
                alertView.setText(R.string.dialog_emergency_manual_red);
                alertView.setTextSize(16);
                alertView.setTextColor(Color.RED);
                alertView.setPadding(20, 0, 40, 20);
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.dialog_title_emergency_manual)
                        .setMessage(R.string.dialog_emergency_manual)
                        .setView(alertView)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                alertDialog = b.create();
                alertDialog.show();
                return true;
            });

            preferenceNormalManual.setOnPreferenceClickListener(preference -> {
                if (alertDialog != null && alertDialog.isShowing()) return true;
                TextView alertViewNormal = new TextView(getActivity());
                alertViewNormal.setText(R.string.dialog_normal_manual_red);
                alertViewNormal.setTextSize(16);
                alertViewNormal.setTextColor(Color.RED);
                alertViewNormal.setPadding(20, 0, 40, 20);
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.dialog_title_normal_manual)
                        .setMessage(R.string.dialog_normal_manual)
                        .setView(alertViewNormal)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                alertDialog = b.create();
                alertDialog.show();
                return true;
            });

            preferenceReboot.setOnPreferenceClickListener(preference -> {
                if (alertDialog != null && alertDialog.isShowing()) return true;
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.dialog_title_reboot)
                        .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                            preferenceReboot.setSummary(R.string.main_pre_sum_reboot);
                            bindDchaService(FLAG_REBOOT);
                        })

                        .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss());
                alertDialog = b.create();
                alertDialog.show();
                return true;
            });

            preferenceRebootShortCut.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String Name = "再起動";
                    Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
                    shortcutIntent.setClassName(getActivity(), "com.saradabar.cpadcustomizetool.RebootActivity");
                    Icon icon = Icon.createWithResource(getActivity(), R.drawable.reboot);
                    ShortcutInfo shortcut = new ShortcutInfo.Builder(getActivity(), Name)
                            .setShortLabel(Name)
                            .setIcon(icon)
                            .setIntent(shortcutIntent)
                            .build();
                    ShortcutManager shortcutManager = getActivity().getSystemService(ShortcutManager.class);
                    shortcutManager.requestPinShortcut(shortcut, null);
                    Toast.makeText(getActivity(), R.string.toast_common_success, Toast.LENGTH_SHORT).show();
                }else {
                    rebootShortCut();
                }
                return true;
            });

            preferenceDchaService.setOnPreferenceClickListener(preference -> {
                if (alertDialog != null && alertDialog.isShowing()) return true;
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.dialog_title_dcha_service)
                        .setMessage(R.string.dialog_dcha_service)
                        .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                            if (StartActivity.bindDchaService(getActivity(), dchaServiceConnection)) {
                                Toast.makeText(getActivity(), R.string.toast_not_install_dcha, Toast.LENGTH_SHORT).show();
                            }else {
                                SET_USE_DCHASERVICE(USE_DCHASERVICE);
                                Intent intent = new Intent(getActivity(), StartActivity.class);
                                getActivity().finish();
                                startActivity(intent);
                            }
                        })

                        .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss());
                alertDialog = b.create();
                alertDialog.show();
                return true;
            });

            preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
                transitionFragment(new MainOtherFragment());
                return false;
            });

            preferenceTEST.setOnPreferenceClickListener(preference -> {
                bindDchaService(FLAG_TEST);
                return false;
            });

//            preferenceResolutionSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//
//                    return false;
//                }
//            });
        } catch (Settings.SettingNotFoundException ignored) {
        }
        preferenceChangeHome.setSummary(MessageFormat.format("ホーム:{0} ({1})", getHomeLabel(), getHome()));
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removePreference(getPreferenceScreen().findPreference("TEST"));

        if (GET_CHECK_TAB_ID() == 1) {
            switchDeviceAdministrator.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchDeviceAdministrator.setEnabled(false);
        }

        if (GET_CHECK_TAB_ID() == 2) {
            switchMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchKeepMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchMarketApp.setEnabled(false);
            switchKeepMarketApp.setEnabled(false);
        }

        DevicePolicyManager dpm = (DevicePolicyManager)getActivity().getSystemService(DEVICE_POLICY_SERVICE);

        if (dpm.isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("Device-Ownerのためこの機能は使用できません\nこの機能を使用するにはその他の設定から”Device-Ownerを無効”を押してください");
        }

        if (Common.Customizetool.NOT_USE == 1) {
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("1"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("2"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Home"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("switch9"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_emergency"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_normal"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_other"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Setting"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Dcha_Service"));
        }

        if (GET_USE_DCHASERVICE() == USE_NOT_DCHASERVICE) {
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Home"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("switch9"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_emergency"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_normal"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Reboot"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Reboot_ShortCut"));
        } else {
            if (GET_USE_DCHASERVICE() == USE_DCHASERVICE) {
                preferenceScreen.removePreference(getPreferenceScreen().findPreference("Dcha_Service"));
            }
        }
    }

    private String getHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(home, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.packageName;
    }

    private String getHomeLabel() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(home, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.loadLabel(pm).toString();
    }

    public void rebootShortCut() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity");
        makeRebootShortCut(intent);
    }

    public void makeRebootShortCut(Intent targetIntent) {
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent);
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.reboot);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.reboot);
        getActivity().sendBroadcast(intent);
        Toast.makeText(getActivity(), R.string.toast_common_success, Toast.LENGTH_SHORT).show();
    }

    //アクティビティ破棄
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isObserberStateEnable) {
            resolver.unregisterContentObserver(observerState);
            isObserberStateEnable = false;
        }
        if (isObserberHideEnable) {
            resolver.unregisterContentObserver(observerHide);
            isObserberHideEnable = false;
        }
        if (isObserberMarketEnable) {
            resolver.unregisterContentObserver(observerMarket);
            isObserberStateEnable = false;
        }
        if (isObserberUsbEnable) {
            resolver.unregisterContentObserver(observerUsb);
            isObserberStateEnable = false;
        }
    }

    //再表示
    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = getActivity().getActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(false);
        //オブザーバー有効
        isObserberStateEnable = true;
        resolver.registerContentObserver(contentDchaState, false, observerState);
        isObserberHideEnable = true;
        resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);
        isObserberMarketEnable = true;
        resolver.registerContentObserver(contentMarketApp, false, observerMarket);
        isObserberUsbEnable = true;
        resolver.registerContentObserver(contentUsbDebug, false, observerUsb);
        //スイッチ変更
        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
            switchDeviceAdministrator.setChecked(mDevicePolicyManager.isAdminActive(mComponentName));
        } catch (Settings.SettingNotFoundException ignored) {
        }
        //スイッチ変更
        SharedPreferences sp = getActivity().getSharedPreferences(Common.Customizetool.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        switchEnableService.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_SERVICE, false));
        switchKeepMarketApp.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false));
        switchKeepDchaState.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_DCHA_STATE, false));
        switchKeepUsbDebug.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_USB_DEBUG, false));
        switchKeepHome.setChecked(sp.getBoolean(Common.Customizetool.KEY_ENABLED_KEEP_HOME, false));
        preferenceChangeHome.setSummary(MessageFormat.format("ホーム:{0} ({1})", getHomeLabel(), getHome()));

        if (GET_CHECK_TAB_ID() == 1) {
            switchDeviceAdministrator.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchDeviceAdministrator.setEnabled(false);
        }

        if (GET_CHECK_TAB_ID() == 2) {
            switchMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchKeepMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchMarketApp.setEnabled(false);
            switchKeepMarketApp.setEnabled(false);
        }

        DevicePolicyManager dpm = (DevicePolicyManager)getActivity().getSystemService(DEVICE_POLICY_SERVICE);

        if (dpm.isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("Device-Ownerのためこの機能は使用できません\nこの機能を使用するにはその他の設定から”Device-Ownerを無効”を押してください");
        }
    }

    //DchaServiceバインド
    private void bindDchaService(int FLAG) {
        connectionFlag = FLAG;
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        getActivity().bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //Preferenceタップ
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("Android_Setting".equals(preference.getKey()))
            transitionFragment(new MainOtherFragment());
        return false;
    }

    //次のフラグメント
    private void transitionFragment(PreferenceFragment nextPreferenceFragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.layout_main, nextPreferenceFragment)
                .commit();
        ActionBar actionBar = getActivity().getActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
    }
}