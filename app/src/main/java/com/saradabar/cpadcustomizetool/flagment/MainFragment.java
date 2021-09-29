package com.saradabar.cpadcustomizetool.flagment;

import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.widget.Toast.LENGTH_SHORT;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_MARKET_APP_FALSE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_MARKET_APP_TRUE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_REBOOT;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_SET_DCHA_STATE_0;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_SET_DCHA_STATE_3;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_TEST;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_USB_DEBUG_FALSE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_USB_DEBUG_TRUE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.FLAG_VIEW_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.REQUEST_ADMIN;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.REQUEST_INSTALL;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_NOT_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.installData;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.mComponentName;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.mDevicePolicyManager;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.toast;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.StartActivity;
import com.saradabar.cpadcustomizetool.common.Common;
import com.saradabar.cpadcustomizetool.service.KeepService;
import com.saradabar.cpadcustomizetool.set.HomeLauncherActivity;

import java.util.Objects;
import java.util.Set;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class MainFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private final String dchaStateString = DCHA_STATE;
    private final String hideNavigationBarString = HIDE_NAVIGATION_BAR;

    private AlertDialog alertDialog;

    private ContentResolver resolver;

    private int connectionFlag;

    private IDchaService mDchaService;

    private final Uri contentDchaState = Settings.System.getUriFor(dchaStateString);
    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
    private final Uri contentUsbDebug = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);

    private boolean isObserberStateEnable = false;
    private boolean isObserberHideEnable = false;
    private boolean isObserberMarketEnable = false;
    private boolean isObserberUsbEnable = false;

    private Preference preferenceChangeHome;

    private SwitchPreference switchDchaState,
            switchKeepDchaState,
            switchHideBar,
            switchEnableService,
            switchMarketApp,
            switchKeepMarketApp,
            switchUsbDebug,
            switchKeepUsbDebug,
            switchKeepHome,
            switchDeviceAdministrator;

    private Preference preferenceDchaService,
            preferenceEmergencyManual,
            preferenceNormalManual,
            preferenceOtherSettings,
            preferenceReboot,
            preferenceRebootShortCut,
            preferenceSilentInstall,
            preferenceTEST;

    private static MainFragment instance = null;

    public static MainFragment getInstance() {
        return instance;
    }

    private static Set<String> getEmergencySettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getStringSet(Common.Variable.KEY_EMERGENCY_SETTINGS, null);
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
        return preferences.getStringSet(Common.Variable.KEY_NORMAL_MODE_SETTINGS, null);
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

    /* システムUIオブザーバー */
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

    /* ナビゲーションバーオブザーバー */
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

    /* 提供元オブザーバー */
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

    /* UsbDebugオブザーバー */
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

    public void bindDchaService(int flag) {
        connectionFlag = flag;
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        getActivity().bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            try {
                switch (connectionFlag) {
                    case Common.Variable.FLAG_SET_DCHA_STATE_0:
                        mDchaService.setSetupStatus(0);
                        break;
                    case Common.Variable.FLAG_SET_DCHA_STATE_3:
                        mDchaService.setSetupStatus(3);
                        break;
                    case Common.Variable.FLAG_HIDE_NAVIGATION_BAR:
                        mDchaService.hideNavigationBar(true);
                        break;
                    case Common.Variable.FLAG_VIEW_NAVIGATION_BAR:
                        mDchaService.hideNavigationBar(false);
                        break;
                    case Common.Variable.FLAG_REBOOT:
                        mDchaService.rebootPad(0, null);
                        break;
                    case Common.Variable.FLAG_TEST:
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

    /* 設定変更 */
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_main, rootKey);
        instance = this;

        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(getActivity(), AdministratorReceiver.class);

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
        preferenceDchaService = findPreference("Dcha_Service");
        preferenceOtherSettings = findPreference("Android_Setting");
        preferenceReboot = findPreference("Android_Reboot");
        preferenceRebootShortCut = findPreference("Android_Reboot_ShortCut");
        preferenceEmergencyManual = findPreference("Emergency_Manual");
        preferenceNormalManual = findPreference("Normal_Manual");
        preferenceSilentInstall = findPreference("android_silent_install");
        preferenceTEST = findPreference("TEST");
        /* final Preference preferenceResolutionSettings = findPreference("Android_resolution_Settings"); */

        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 3);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
            SharedPreferences sp = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            switchEnableService.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false));
            switchKeepMarketApp.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false));
            switchKeepDchaState.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false));
            switchKeepUsbDebug.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false));
            switchKeepHome.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false));
            switchDeviceAdministrator.setChecked(mDevicePolicyManager.isAdminActive(mComponentName));

            /* オブサーバーを有効化 */
            isObserberStateEnable = true;
            resolver.registerContentObserver(contentDchaState, false, observerState);
            isObserberHideEnable = true;
            resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);
            isObserberMarketEnable = true;
            resolver.registerContentObserver(contentMarketApp, false, observerMarket);
            isObserberMarketEnable = true;
            resolver.registerContentObserver(contentUsbDebug, false, observerUsb);

            /* リスナーを有効化 */
            switchDchaState.setOnPreferenceChangeListener((preference, o) -> {
                if (Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()) == 0) {
                    if ((boolean) o) {
                        settingsFlag(FLAG_SET_DCHA_STATE_3);
                    } else {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                } else if (Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()) == 1) {
                    if ((boolean) o) {
                        bindDchaService(FLAG_SET_DCHA_STATE_3);
                    } else {
                        bindDchaService(FLAG_SET_DCHA_STATE_0);
                    }
                }
                return false;
            });

            switchHideBar.setOnPreferenceChangeListener((preference, o) -> {
                if (Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()) == 0) {
                    if ((boolean) o) {
                        settingsFlag(FLAG_HIDE_NAVIGATION_BAR);
                    } else {
                        settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                    }
                } else if (Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()) == 1) {
                    if ((boolean) o) {
                        bindDchaService(FLAG_HIDE_NAVIGATION_BAR);
                    } else {
                        bindDchaService(FLAG_VIEW_NAVIGATION_BAR);
                    }
                }
                return false;
            });

            switchEnableService.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp15 = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp15.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                    getActivity().startService(new Intent(getActivity(), KeepService.class));
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            try {
                                KeepService.getInstance().startService();
                            }catch (NullPointerException ignored) {
                            }
                        }
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            KeepService.getInstance().stopService(1);
                            return true;
                        }
                    }
                }
                return true;
            });

            switchKeepMarketApp.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp14 = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp14.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    try {
                        Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
                        getActivity().startService(new Intent(getActivity(), KeepService.class));
                        for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                            if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                                try {
                                    KeepService.getInstance().startService();
                                }catch (NullPointerException ignored) {
                                }
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, LENGTH_SHORT);
                        toast.show();
                        spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false).apply();
                        switchKeepMarketApp.setChecked(false);
                        return false;
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            KeepService.getInstance().stopService(3);
                            return true;
                        }
                    }
                }
                return true;
            });

            switchKeepUsbDebug.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp13 = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp13.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    try {
                        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                            settingsFlag(FLAG_SET_DCHA_STATE_3);
                        }
                        Thread.sleep(100);
                        Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                            settingsFlag(FLAG_SET_DCHA_STATE_0);
                        }
                        getActivity().startService(new Intent(getActivity(), KeepService.class));
                        for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                            if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                                try {
                                    KeepService.getInstance().startService();
                                }catch (NullPointerException ignored) {
                                }
                            }
                        }
                    } catch (SecurityException | InterruptedException e) {
                        e.printStackTrace();
                        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                            settingsFlag(FLAG_SET_DCHA_STATE_0);
                        }
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, LENGTH_SHORT);
                        toast.show();
                        spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false).apply();
                        switchKeepUsbDebug.setChecked(false);
                        return false;
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE))
                        if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            KeepService.getInstance().stopService(4);
                            return true;
                        }
                }
                return true;
            });

            switchKeepDchaState.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp12 = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp12.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    settingsFlag(FLAG_SET_DCHA_STATE_0);
                    getActivity().startService(new Intent(getActivity(), KeepService.class));
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            try {
                                KeepService.getInstance().startService();
                            }catch (NullPointerException ignored) {
                            }
                        }
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            KeepService.getInstance().stopService(2);
                            return true;
                        }
                    }
                }
                return true;
            });

            switchKeepHome.setOnPreferenceChangeListener((preference, o) -> {
                SharedPreferences sp1 = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor spe = sp1.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, (boolean) o);
                spe.apply();
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                if ((boolean) o) {
                    spe.putString(Common.Variable.KEY_SAVE_KEEP_HOME, getLauncherPackage());
                    spe.apply();
                    getActivity().startService(new Intent(getActivity(), KeepService.class));
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            try {
                                KeepService.getInstance().startService();
                            }catch (NullPointerException ignored) {
                            }
                        }
                    }
                } else {
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
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
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, LENGTH_SHORT);
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
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, LENGTH_SHORT);
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
                        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                            settingsFlag(FLAG_SET_DCHA_STATE_3);
                        }
                        Thread.sleep(100);
                        settingsFlag(FLAG_USB_DEBUG_TRUE);
                        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                            settingsFlag(FLAG_SET_DCHA_STATE_0);
                        }
                    } catch (SecurityException | InterruptedException e) {
                        e.printStackTrace();
                        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                            settingsFlag(FLAG_SET_DCHA_STATE_0);
                        }
                        if (null != toast) toast.cancel();
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, LENGTH_SHORT);
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
                        toast = Toast.makeText(getActivity(), R.string.toast_not_change, LENGTH_SHORT);
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
                        startActivityForResult(intent, REQUEST_ADMIN);
                    }
                } else {
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
                return false;
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
                return false;
            });

            preferenceReboot.setOnPreferenceClickListener(preference -> {
                if (alertDialog != null && alertDialog.isShowing()) return true;
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(R.string.dialog_title_reboot)
                        .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                            bindDchaService(FLAG_REBOOT);
                        })

                        .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss());
                alertDialog = b.create();
                alertDialog.show();
                return false;
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
                } else {
                    makeRebootShortcut();
                }
                return false;
            });

            preferenceDchaService.setOnPreferenceClickListener(preference -> {
                if (alertDialog != null && alertDialog.isShowing()) return true;
                AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
                d.setTitle(R.string.dialog_title_dcha_service)
                        .setMessage(R.string.dialog_dcha_service)
                        .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                            if (StartActivity.bindDchaService(getActivity(), dchaServiceConnection)) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("DchaServiceが機能していないためこの機能は使用できません")
                                        .setPositiveButton(R.string.dialog_common_ok, null)
                                        .show();
                            } else {
                                Common.SET_DCHASERVICE_FLAG(USE_DCHASERVICE, getActivity());
                                getActivity().finish();
                                Intent intent = new Intent(getActivity(), StartActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss());
                alertDialog = d.create();
                alertDialog.show();
                return false;
            });

            preferenceChangeHome.setOnPreferenceClickListener(preference -> {
                preferenceChangeHome.setEnabled(false);
                Intent intent = new Intent(getActivity(), HomeLauncherActivity.class);
                startActivity(intent);
                return false;
            });

            preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
                transitionFragment(new MainOtherFragment());
                return false;
            });

            preferenceSilentInstall.setOnPreferenceClickListener(preference -> {
                preferenceSilentInstall.setEnabled(false);
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("application/vnd.android.package-archive");
                startActivityForResult(intent, REQUEST_INSTALL);
                return false;
            });

            preferenceTEST.setOnPreferenceClickListener(preference -> {
                bindDchaService(FLAG_TEST);
                return false;
            });

            /* ベータ機能
            preferenceResolutionSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    return false;
                }
            }); */
        } catch (Settings.SettingNotFoundException ignored) {
        }

        preferenceChangeHome.setSummary(getLauncherName());
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removePreference(getPreferenceScreen().findPreference("TEST"));

        if (Common.GET_MODEL_NAME(getActivity()) == 0) {
            preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceSilentInstall.setEnabled(false);
        }

        if (Common.GET_MODEL_NAME(getActivity()) == 1) {
            preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceSilentInstall.setEnabled(false);
            switchDeviceAdministrator.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchDeviceAdministrator.setEnabled(false);
        }

        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
            switchMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchKeepMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchMarketApp.setEnabled(false);
            switchKeepMarketApp.setEnabled(false);
        }

        DevicePolicyManager dpm = (DevicePolicyManager) getActivity().getSystemService(DEVICE_POLICY_SERVICE);

        if (dpm.isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("DeviceOwnerのためこの機能は使用できません");
        }

        if (Common.GET_DCHASERVICE_FLAG(getActivity()) == USE_NOT_DCHASERVICE) {
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("android_silent_install"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Home"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("switch9"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_emergency"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("category_normal"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Reboot"));
            preferenceScreen.removePreference(getPreferenceScreen().findPreference("Android_Reboot_ShortCut"));
        } else {
            if (Common.GET_DCHASERVICE_FLAG(getActivity()) == USE_DCHASERVICE) {
                preferenceScreen.removePreference(getPreferenceScreen().findPreference("Dcha_Service"));
            }
        }
    }

    private String getLauncherPackage() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.packageName;
    }

    private String getLauncherName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.loadLabel(pm).toString();
    }

    public void makeRebootShortcut() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity");
        Intent intent2 = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent.setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity"));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.reboot);
        intent2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent2.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.reboot);
        getActivity().sendBroadcast(intent2);
        Toast.makeText(getActivity(), R.string.toast_common_success, LENGTH_SHORT).show();
    }

    /* アクティビティ破棄 */
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

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().getActionBar() != null)
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        if (!preferenceChangeHome.isEnabled()) preferenceChangeHome.setEnabled(true);
        /* オブザーバー有効 */
        isObserberStateEnable = true;
        resolver.registerContentObserver(contentDchaState, false, observerState);
        isObserberHideEnable = true;
        resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);
        isObserberMarketEnable = true;
        resolver.registerContentObserver(contentMarketApp, false, observerMarket);
        isObserberUsbEnable = true;
        resolver.registerContentObserver(contentUsbDebug, false, observerUsb);
        /* スイッチ変更 */
        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
            switchDeviceAdministrator.setChecked(mDevicePolicyManager.isAdminActive(mComponentName));
        } catch (Settings.SettingNotFoundException ignored) {
        }
        /* スイッチ変更 */
        SharedPreferences sp = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        switchEnableService.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false));
        switchKeepMarketApp.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false));
        switchKeepDchaState.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false));
        switchKeepUsbDebug.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false));
        switchKeepHome.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false));
        preferenceChangeHome.setSummary(getLauncherName());

        if (Common.GET_MODEL_NAME(getActivity()) == 0) {
            preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceSilentInstall.setEnabled(false);
        }

        if (Common.GET_MODEL_NAME(getActivity()) == 1) {
            preferenceSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceSilentInstall.setEnabled(false);
            switchDeviceAdministrator.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchDeviceAdministrator.setEnabled(false);
        }

        if (Common.GET_MODEL_NAME(getActivity()) == 2) {
            switchMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchKeepMarketApp.setSummary(Build.MODEL + "ではこの機能は使用できません");
            switchMarketApp.setEnabled(false);
            switchKeepMarketApp.setEnabled(false);
        }

        DevicePolicyManager dpm = (DevicePolicyManager) getActivity().getSystemService(DEVICE_POLICY_SERVICE);

        if (dpm.isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("DeviceOwnerのためこの機能は使用できません");
        }
    }

    /* Preferenceタップ */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("Android_Setting".equals(preference.getKey()))
            transitionFragment(new MainOtherFragment());
        return false;
    }

    /* フラグメント切り替え */
    private void transitionFragment(PreferenceFragment nextPreferenceFragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.layout_main, nextPreferenceFragment)
                .commit();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_INSTALL:
                preferenceSilentInstall.setEnabled(true);
                try {
                    Common.Variable.installData = getInstallData(getActivity(), data.getData());
                } catch (NullPointerException ignored) {
                    return;
                }
                MainFragment.silentInstallTask silent = new MainFragment.silentInstallTask();
                silent.setListener(StartActivity.getInstance().createListener());
                silent.execute();
                break;
            case 3:
                break;
            default:
                break;
        }
    }

    private String getInstallData(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                String[] s1 = DocumentsContract.getDocumentId(uri).split(":");
                String s2 = s1[0];
                if ("primary".equalsIgnoreCase(s2)) {
                    return Environment.getExternalStorageDirectory() + "/" + s1[1];
                }
                return "/storage/" + s2 + "/" + s1[1];
            }
        } else {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static class silentInstallTask extends AsyncTask<Object, Void, Object> {

        private Listener listener;

        @Override
        protected void onPreExecute() {
            listener.onShow();
        }

        @Override
        protected Object doInBackground(Object... value) {
            if (MainFragment.getInstance().installApp(StartActivity.getInstance().mDchaService, installData, 1)) {
                return new Object();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                StartActivity.getInstance().mProgress.dismiss();
                listener.onSuccess();
            } else {
                StartActivity.getInstance().mProgress.dismiss();
                listener.onFailure();
            }
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        public interface Listener {
            void onShow();

            void onSuccess();

            void onFailure();
        }
    }

    /* サイレントインストール */
    public boolean installApp(IDchaService mDchaService, String str, int i) {
        try {
            return mDchaService.installApp(str, i);
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }
}