package com.saradabar.cpadcustomizetool.flagment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.*;
import static com.saradabar.cpadcustomizetool.Common.Variable.*;

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
import android.view.LayoutInflater;
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
import com.saradabar.cpadcustomizetool.set.NormalLauncherActivity.*;
import com.saradabar.cpadcustomizetool.service.KeepService;
import com.saradabar.cpadcustomizetool.set.HomeLauncherActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;
import jp.co.benesse.dcha.dchautilservice.IDchaUtilService;

public class MainFragment extends PreferenceFragment {

    private final String dchaStateString = DCHA_STATE, hideNavigationBarString = HIDE_NAVIGATION_BAR;
    private String setLauncherPackage;

    private ListView mListView;

    private SharedPreferences sp;
    private ContentResolver resolver;

    private int width, height;

    private IDchaService mDchaService;
    private IDchaUtilService mIDchaUtilService;

    private final Uri contentDchaState = Settings.System.getUriFor(dchaStateString);
    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final Uri contentMarketApp = Settings.Secure.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
    private final Uri contentUsbDebug = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);

    private boolean isObserberStateEnable = false;
    private boolean isObserberHideEnable = false;
    private boolean isObserberMarketEnable = false;
    private boolean isObserberUsbEnable = false;

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
            preferenceNormalLauncher,
            preferenceNormalManual,
            preferenceOtherSettings,
            preferenceReboot,
            preferenceRebootShortCut,
            preferenceSilentInstall,
            preferenceChangeHome,
            preferenceResolution,
            preferenceResolutionReset;

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
                switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) != 0);
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

    public boolean bindDchaService(int flag, int dchaMode) {
        Intent intent = new Intent();
        if (dchaMode == DCHA_MODE) {
            intent = new Intent(DCHA_SERVICE);
            intent.setPackage(PACKAGE_DCHASERVICE);
        } else if (dchaMode == DCHA_UTIL_MODE) {
            intent = new Intent(DCHA_UTIL_SERVICE);
            intent.setPackage(PACKAGE_DCHA_UTIL_SERVICE);
        }
        return !getActivity().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                if (dchaMode == DCHA_MODE) {
                    mDchaService = IDchaService.Stub.asInterface(iBinder);
                    try {
                        switch (flag) {
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
                            case FLAG_SET_LAUNCHER:
                                mDchaService.clearDefaultPreferredApp(getLauncherPackage());
                                mDchaService.setDefaultPreferredHomeApp(setLauncherPackage);
                                /* listviewの更新 */
                                mListView.invalidateViews();
                                setCheckedSwitch();
                                break;
                            case FLAG_TEST:
                                break;
                        }
                    } catch (RemoteException ignored) {
                    }
                } else if (dchaMode == DCHA_UTIL_MODE) {
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
        resolver = getActivity().getContentResolver();
        sp = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

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

        /* オブサーバーを有効化 */
        isObserberStateEnable = true;
        resolver.registerContentObserver(contentDchaState, false, observerState);
        isObserberHideEnable = true;
        resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);
        isObserberMarketEnable = true;
        resolver.registerContentObserver(contentMarketApp, false, observerMarket);
        isObserberMarketEnable = true;
        resolver.registerContentObserver(contentUsbDebug, false, observerUsb);

        /* 一括変更 */
        setCheckedSwitch();

        /* リスナーを有効化 */
        switchDchaState.setOnPreferenceChangeListener((preference, o) -> {
            if (!Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    settingsFlag(FLAG_SET_DCHA_STATE_3);
                } else {
                    settingsFlag(FLAG_SET_DCHA_STATE_0);
                }
            } else if (Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    bindDchaService(FLAG_SET_DCHA_STATE_3, DCHA_MODE);
                } else {
                    bindDchaService(FLAG_SET_DCHA_STATE_0, DCHA_MODE);
                }
            }
            return false;
        });

        switchHideBar.setOnPreferenceChangeListener((preference, o) -> {
            if (!Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    settingsFlag(FLAG_HIDE_NAVIGATION_BAR);
                } else {
                    settingsFlag(FLAG_VIEW_NAVIGATION_BAR);
                }
            } else if (Common.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity())) {
                if ((boolean) o) {
                    bindDchaService(FLAG_HIDE_NAVIGATION_BAR, DCHA_MODE);
                } else {
                    bindDchaService(FLAG_VIEW_NAVIGATION_BAR, DCHA_MODE);
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
                        } catch (NullPointerException ignored) {
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
                            } catch (NullPointerException ignored) {
                            }
                        }
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
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
                    if (Common.GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_3);
                    }
                    Thread.sleep(100);
                    Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                    if (Common.GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                    getActivity().startService(new Intent(getActivity(), KeepService.class));
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (!KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            try {
                                KeepService.getInstance().startService();
                            } catch (NullPointerException ignored) {
                            }
                        }
                    }
                } catch (SecurityException | InterruptedException e) {
                    e.printStackTrace();
                    if (Common.GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
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
                        } catch (NullPointerException ignored) {
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
                        } catch (NullPointerException ignored) {
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
                    if (Common.GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_3);
                    }
                    Thread.sleep(100);
                    settingsFlag(FLAG_USB_DEBUG_TRUE);
                    if (Common.GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
                } catch (SecurityException | InterruptedException e) {
                    e.printStackTrace();
                    if (Common.GET_MODEL_ID(getActivity()) == 2) {
                        settingsFlag(FLAG_SET_DCHA_STATE_0);
                    }
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
                    startActivityForResult(intent, REQUEST_ADMIN);
                }
            } else {
                switchDeviceAdministrator.setChecked(true);
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_title_dcha_service)
                        .setMessage(R.string.dialog_question_device_admin)
                        .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                            mDevicePolicyManager.removeActiveAdmin(new ComponentName(getActivity(), AdministratorReceiver.class));
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
            TextView alertView = new TextView(getActivity());
            alertView.setText(R.string.dialog_emergency_manual_red);
            alertView.setTextSize(16);
            alertView.setTextColor(Color.RED);
            alertView.setPadding(20, 0, 40, 20);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_emergency_manual)
                    .setMessage(R.string.dialog_emergency_manual)
                    .setView(alertView)
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
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.normal_mode_launcher, null);

            Intent setPackageName = new Intent();
            setPackageName.setAction(Intent.ACTION_MAIN);
            setPackageName.addCategory(Intent.CATEGORY_HOME);
            final PackageManager pm = getActivity().getPackageManager();
            final List<ResolveInfo> installedAppList = pm.queryIntentActivities(setPackageName, 0);
            final List<AppData> dataList = new ArrayList<>();
            for (ResolveInfo app : installedAppList) {
                AppData data = new AppData();
                data.label = app.loadLabel(pm).toString();
                data.icon = app.loadIcon(pm);
                data.packName = app.activityInfo.packageName;
                dataList.add(data);
            }

            final ListView listView = view.findViewById(R.id.normal_launcher_list);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new AppListAdapter(getActivity(), dataList));
            listView.setOnItemClickListener((parent, mView, position, id) -> {
                ResolveInfo app = installedAppList.get(position);
                final String setLauncherPackage = Uri.fromParts("package", app.activityInfo.packageName, null).toString().replace("package:", "");
                Common.SET_NORMAL_LAUNCHER(setLauncherPackage, StartActivity.getInstance());
                /* listviewの更新 */
                listView.invalidateViews();
                setCheckedSwitch();
            });

            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.dialog_title_launcher)
                    .setPositiveButton(R.string.dialog_common_yes, null)
                    .show();
            return false;
        });

        preferenceReboot.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_reboot)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        bindDchaService(FLAG_REBOOT, DCHA_MODE);
                    })

                    .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preferenceRebootShortCut.setOnPreferenceClickListener(preference -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
                shortcutIntent.setClassName(getActivity(), "com.saradabar.cpadcustomizetool.RebootActivity");
                Icon icon = Icon.createWithResource(getActivity(), R.drawable.reboot);
                ShortcutInfo shortcut = new ShortcutInfo.Builder(getActivity(), "再起動")
                        .setShortLabel("再起動")
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
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_dcha_service)
                    .setMessage(R.string.dialog_dcha_service)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        if (StartActivity.getInstance().bindDchaService()) {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.dialog_error_no_work_dcha)
                                    .setPositiveButton(R.string.dialog_common_ok, null)
                                    .show();
                        } else {
                            SET_DCHASERVICE_FLAG(true, getActivity());
                            getActivity().finish();
                            getActivity().overridePendingTransition(0, 0);
                            startActivity(getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", true));
                        }
                    })

                    .setNegativeButton(R.string.dialog_common_no, null)
                    .show();
            return false;
        });

        preferenceChangeHome.setOnPreferenceClickListener(preference -> {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.launcher_list, null);

            Intent setPackageName = new Intent();
            setPackageName.setAction(Intent.ACTION_MAIN);
            setPackageName.addCategory(Intent.CATEGORY_HOME);
            final PackageManager pm = getActivity().getPackageManager();
            final List<ResolveInfo> installedAppList = pm.queryIntentActivities(setPackageName, 0);
            final List<HomeLauncherActivity.AppData> dataList = new ArrayList<>();
            for (ResolveInfo app : installedAppList) {
                HomeLauncherActivity.AppData data = new HomeLauncherActivity.AppData();
                data.label = app.loadLabel(pm).toString();
                data.icon = app.loadIcon(pm);
                data.packName = app.activityInfo.packageName;
                dataList.add(data);
            }

            mListView = view.findViewById(R.id.launcher_list);
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            mListView.setAdapter(new HomeLauncherActivity.AppListAdapter(getActivity(), dataList));
            mListView.setOnItemClickListener((parent, mView, position, id) -> {
                ResolveInfo app = installedAppList.get(position);
                setLauncherPackage = Uri.fromParts("package", app.activityInfo.packageName, null).toString().replace("package:", "");
                bindDchaService(FLAG_SET_LAUNCHER, DCHA_MODE);
            });

            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.dialog_title_launcher)
                    .setPositiveButton(R.string.dialog_common_yes, null)
                    .show();
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

        preferenceResolution.setOnPreferenceClickListener(preference -> {
            /* DchaUtilServiceが機能しているか */
            if (bindDchaService(FLAG_CHECK, DCHA_UTIL_MODE)) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.dialog_error_no_work_dcha_util)
                        .setPositiveButton(R.string.dialog_common_ok, null)
                        .show();
                return false;
            }

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.resolution_dialog, null);
            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.dialog_title_resolution)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog1, which) -> {
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        EditText mEditText1 = (EditText) view.findViewById(R.id.edit_text_1);
                        EditText mEditText2 = (EditText) view.findViewById(R.id.edit_text_2);
                        try {
                            width = Integer.parseInt(mEditText1.getText().toString());
                            height = Integer.parseInt(mEditText2.getText().toString());
                            if (width < 300 || height < 300) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.dialog_title_error)
                                        .setMessage(R.string.dialog_illegal_value)
                                        .setPositiveButton(R.string.dialog_common_ok, null)
                                        .show();
                            } else {
                                MainFragment.setResolutionTask resolution = new MainFragment.setResolutionTask();
                                resolution.setListener(StartActivity.getInstance().mCreateListener());
                                resolution.execute();
                            }
                        } catch (NumberFormatException ignored) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.dialog_title_error)
                                    .setMessage(R.string.dialog_illegal_value)
                                    .setPositiveButton(R.string.dialog_common_ok, null)
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_common_cancel, null)
                    .show();
            return false;
        });

        preferenceResolutionReset.setOnPreferenceClickListener(preference -> {
            resetResolution();
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

        if (mDevicePolicyManager.isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("DeviceOwnerのためこの機能は使用できません");
        }

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
            if (Common.GET_DCHASERVICE_FLAG(getActivity())) {
                getPreferenceScreen().removePreference(findPreference("dcha_service"));
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

    private void makeRebootShortcut() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity");
        Intent intent2 = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent.setClassName("com.saradabar.cpadcustomizetool", "com.saradabar.cpadcustomizetool.RebootActivity"));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.reboot);
        intent2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent2.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.reboot);
        getActivity().sendBroadcast(intent2);
        Toast.makeText(getActivity(), R.string.toast_common_success, Toast.LENGTH_SHORT).show();
    }

    private void setCheckedSwitch() {
        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) != 0);
            switchMarketApp.setChecked(Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS) != 0);
            switchUsbDebug.setChecked(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED) != 0);
            switchDeviceAdministrator.setChecked(mDevicePolicyManager.isAdminActive(mComponentName));
        } catch (Settings.SettingNotFoundException ignored) {
        }
        switchEnableService.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false));
        switchKeepMarketApp.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false));
        switchKeepDchaState.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false));
        switchKeepUsbDebug.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false));
        switchKeepHome.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false));
        preferenceChangeHome.setSummary(getLauncherName());
        String mString = null;
        try {
            mString = (String) getActivity().getPackageManager().getApplicationLabel(getActivity().getPackageManager().getApplicationInfo(Common.GET_NORMAL_LAUNCHER(getActivity()), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        preferenceNormalLauncher.setSummary("変更するランチャーは" + mString + "に設定されています");
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

        /* 一括変更 */
        setCheckedSwitch();

        switch (Common.GET_MODEL_ID(getActivity())) {
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

        if (mDevicePolicyManager.isDeviceOwnerApp(getActivity().getPackageName())) {
            switchDeviceAdministrator.setEnabled(false);
            switchDeviceAdministrator.setSummary("DeviceOwnerのためこの機能は使用できません");
        }
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

        private Listener mListener;

        @Override
        protected void onPreExecute() {
            mListener.onShow();
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
                mListener.onSuccess();
            } else mListener.onFailure();
        }

        public void setListener(Listener listener) {
            mListener = listener;
        }

        public interface Listener {
            void onShow();
            void onSuccess();
            void onFailure();
        }
    }

    public static class setResolutionTask extends AsyncTask<Object, Void, Object> {

        private Listener mListener;

        @Override
        protected Object doInBackground(Object... value) {
            getInstance().setResolution(MainFragment.getInstance().width, MainFragment.getInstance().height);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return new Object();
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

        public interface Listener {
            void onSuccess();
            void onFailure();
        }
    }

    /* サイレントインストール */
    public boolean installApp(IDchaService mDchaService, String str, int i) {
        try {
            return mDchaService.installApp(str, i);
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /* 解像度の変更 */
    public void setResolution(int i1, int i2) {
        width = i1;
        height = i2;
        bindDchaService(FLAG_RESOLUTION, DCHA_UTIL_MODE);
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
                            .setPositiveButton(R.string.dialog_common_ok, null)
                            .show();
                }
                break;
            case 2:
                width = 1920;
                height = 1200;
                if (bindDchaService(FLAG_RESOLUTION, DCHA_UTIL_MODE)) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.dialog_error_no_work_dcha_util)
                            .setPositiveButton(R.string.dialog_common_ok, null)
                            .show();
                }
                break;
        }
    }
}