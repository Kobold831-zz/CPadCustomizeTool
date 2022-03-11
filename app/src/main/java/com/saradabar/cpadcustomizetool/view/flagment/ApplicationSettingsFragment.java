package com.saradabar.cpadcustomizetool.view.flagment;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.view.activity.CrashLogActivity;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;
import com.saradabar.cpadcustomizetool.view.views.LauncherView;
import com.saradabar.cpadcustomizetool.view.views.SingleListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationSettingsFragment extends PreferenceFragment {

    SwitchPreference autoUpdateCheck,
            changeSettingsDcha,
            autoUsbDebug;

    Preference crashLog,
            crashLogRemove,
            updateMode;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_app_settings, rootKey);
        SharedPreferences sp = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        ContentResolver resolver = getActivity().getContentResolver();

        autoUpdateCheck = findPreference("switch_auto_update_check");
        changeSettingsDcha = findPreference("switch_is_change_settings_use_dcha");
        autoUsbDebug = findPreference("switch_auto_usb_debug");
        crashLog = findPreference("debug_log");
        crashLogRemove = findPreference("debug_log_remove");
        updateMode = findPreference("app_update_mode");

        autoUpdateCheck.setChecked(!Preferences.GET_UPDATE_FLAG(getActivity()));
        changeSettingsDcha.setChecked(Preferences.GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()));

        try {
            autoUsbDebug.setChecked(sp.getBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false));
        } catch (NullPointerException e) {
            SharedPreferences.Editor spe = sp.edit();
            spe.putBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false);
            spe.apply();
        }

        autoUpdateCheck.setOnPreferenceChangeListener((preference, newValue) -> {
            Preferences.SET_UPDATE_FLAG(!((boolean) newValue), getActivity());
            return true;
        });

        changeSettingsDcha.setOnPreferenceChangeListener((preference, newValue) -> {
            Preferences.SET_CHANGE_SETTINGS_DCHA_FLAG((boolean) newValue, getActivity());
            return true;
        });

        autoUsbDebug.setOnPreferenceChangeListener((preference, newValue) -> {
            if (confirmationDialog()) {
                return false;
            }
            try {
                if (Preferences.GET_MODEL_ID(getActivity()) == 2)
                    Settings.System.putInt(resolver, Constants.DCHA_STATE, 3);
                Thread.sleep(100);
                Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1);
                if (Preferences.GET_MODEL_ID(getActivity()) == 2)
                    Settings.System.putInt(resolver, Constants.DCHA_STATE, 0);
                sp.edit().putBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, (boolean) newValue).apply();
            } catch (SecurityException | InterruptedException ignored) {
                if (Preferences.GET_MODEL_ID(getActivity()) == 2)
                    Settings.System.putInt(resolver, Constants.DCHA_STATE, 0);
                Toast.toast(getActivity(), R.string.toast_not_change);
                autoUsbDebug.setChecked(false);
                return false;
            }
            return true;
        });

        crashLog.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), CrashLogActivity.class));
            return false;
        });

        crashLogRemove.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setMessage("消去しますか？")
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        if (Preferences.REMOVE_CRASH_LOG(getActivity())) {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("消去しました")
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        updateMode.setOnPreferenceClickListener(preference -> {
            View v = getActivity().getLayoutInflater().inflate(R.layout.layout_update_list, null);
            List<String> list = new ArrayList<>();
            list.add("パッケージインストーラ");
            list.add("ADB");
            list.add("DchaService");
            list.add("DeviceOwner");
            List<SingleListView.AppData> dataList = new ArrayList<>();
            int i = 0;
            for (String str : list) {
                SingleListView.AppData data = new SingleListView.AppData();
                data.label = str;
                data.updateMode = i;
                dataList.add(data);
                i++;
            }
            ListView listView = v.findViewById(R.id.update_list);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new SingleListView.AppListAdapter(getActivity(), dataList));
            listView.setOnItemClickListener((parent, mView, position, id) -> {
                switch (position) {
                    case 0:
                        if (Preferences.GET_MODEL_ID(getActivity()) != 2) {
                            Preferences.SET_UPDATE_MODE(getActivity(), (int) id);
                            listView.invalidateViews();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("選択されたモードは機能していないため設定できません")
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                    case 1:
                        Preferences.SET_UPDATE_MODE(getActivity(), (int) id);
                        listView.invalidateViews();
                        break;
                    case 2:
                        if (MainFragment.getInstance().bindDchaService(Constants.FLAG_CHECK, true)) {
                            Preferences.SET_UPDATE_MODE(getActivity(), (int) id);
                            listView.invalidateViews();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("選択されたモードは機能していないため設定できません")
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                    case 3:
                        if (((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isDeviceOwnerApp(getActivity().getPackageName())) {
                            Preferences.SET_UPDATE_MODE(getActivity(), (int) id);
                            listView.invalidateViews();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage("選択されたモードは機能していないため設定できません")
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                }
            });
            new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle("モードを選択してください")
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        if (!Preferences.GET_DCHASERVICE_FLAG(getActivity())) {
            Preferences.SET_CHANGE_SETTINGS_DCHA_FLAG(false, getActivity());
            changeSettingsDcha.setChecked(false);
            changeSettingsDcha.setSummary("この機能を使用するには、\"DchaServiceを使用\"を押して内容を確認してください");
            changeSettingsDcha.setEnabled(false);
        }
    }

    /* 確認ダイアログ */
    private boolean confirmationDialog() {
        if (!Constants.COUNT_DCHA_COMPLETED_FILE.exists() && Constants.IGNORE_DCHA_COMPLETED_FILE.exists()) {
            if (Preferences.GET_CONFIRMATION(getActivity())) {
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
                                        Preferences.SET_CONFIRMATION(true, getActivity());
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
}