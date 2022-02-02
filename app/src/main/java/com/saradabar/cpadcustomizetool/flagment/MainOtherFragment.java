package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.Common.Variable.mComponentName;
import static com.saradabar.cpadcustomizetool.Common.Variable.mDevicePolicyManager;
import static com.saradabar.cpadcustomizetool.Common.Variable.toast;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

public class MainOtherFragment extends PreferenceFragment {

    Preference preferenceOtherSettings,
            preferenceSysUiAdjustment,
            preferenceBlockToUninstallSettings,
            preferenceDisableDeviceOwner,
            preferenceDevelopmentSettings,
            preferenceNowSetOwnerApp;

    SwitchPreference switchPreferencePermissionForced;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_main_setting, rootKey);

        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        preferenceOtherSettings = findPreference("intent_android_settings");
        preferenceSysUiAdjustment = findPreference("intent_sys_ui_adjustment");
        preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        preferenceDisableDeviceOwner = findPreference("disable_device_owner");
        preferenceDevelopmentSettings = findPreference("intent_development_settings");
        switchPreferencePermissionForced = findPreference("permission_forced");
        preferenceNowSetOwnerApp = findPreference("now_set_owner_package");

        preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent().setClassName("com.android.settings", "com.android.settings.Settings").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceDevelopmentSettings.setOnPreferenceClickListener(preference -> {
            if (Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1) {
                try {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                } catch (ActivityNotFoundException ignored) {
                }
            } else {
                if (toast != null) toast.cancel();
                toast = Toast.makeText(getActivity(), R.string.toast_no_development_option, Toast.LENGTH_SHORT);
                toast.show();
            }
            return false;
        });

        preferenceSysUiAdjustment.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent().setClassName("com.android.systemui", "com.android.systemui.DemoMode").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceBlockToUninstallSettings.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent(getActivity(), BlockerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        switchPreferencePermissionForced.setOnPreferenceChangeListener((preference, o) -> {
            if ((boolean) o) {
                switchPreferencePermissionForced.setChecked(true);
                switchPreferencePermissionForced.setTitle("PERMISSION_GRANT_STATE_GRANTED");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mDevicePolicyManager.setPermissionPolicy(mComponentName, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
            } else {
                switchPreferencePermissionForced.setChecked(false);
                switchPreferencePermissionForced.setTitle("PERMISSION_GRANT_STATE_DEFAULT");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mDevicePolicyManager.setPermissionPolicy(mComponentName, DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT);
            }
            return true;
        });

        preferenceDisableDeviceOwner.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_question_device_owner)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        mDevicePolicyManager.clearDeviceOwnerApp(getActivity().getPackageName());
                        getActivity().finish();
                        getActivity().overridePendingTransition(0, 0);
                        startActivity(getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", true));
                    })
                    .setNegativeButton(R.string.dialog_common_no, null)
                    .show();
            return false;
        });

        if (getNowOwnerPackage() != null) {
            preferenceNowSetOwnerApp.setSummary("DeviceOwnerは" + getNowOwnerPackage() + "に設定されています");
        } else preferenceNowSetOwnerApp.setSummary("DeviceOwnerはデバイスに設定されていません");

        switch (Common.GET_MODEL_ID(getActivity())) {
            case 0:
                preferenceSysUiAdjustment.setEnabled(false);
                preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
                setPreferenceSettings();
                break;
            case 1:
                preferenceSysUiAdjustment.setEnabled(false);
                preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceDisableDeviceOwner.setEnabled(false);
                preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
                break;
            case 2:
                setPreferenceSettings();
                break;
        }
    }

    private void setPreferenceSettings() {
        if (!mDevicePolicyManager.isDeviceOwnerApp(getActivity().getPackageName())) {
            preferenceBlockToUninstallSettings.setEnabled(false);
            preferenceDisableDeviceOwner.setEnabled(false);
            switchPreferencePermissionForced.setEnabled(false);
            preferenceBlockToUninstallSettings.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
            preferenceDisableDeviceOwner.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
            switchPreferencePermissionForced.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                switch (mDevicePolicyManager.getPermissionPolicy(mComponentName)) {
                    case 0:
                        switchPreferencePermissionForced.setChecked(false);
                        switchPreferencePermissionForced.setTitle("PERMISSION_GRANT_STATE_DEFAULT");
                        break;
                    case 1:
                        switchPreferencePermissionForced.setChecked(true);
                        switchPreferencePermissionForced.setTitle("PERMISSION_GRANT_STATE_GRANTED");
                        break;
                }
            }
        }
    }

    private String getNowOwnerPackage() {
        for (ApplicationInfo app : getActivity().getPackageManager().getInstalledApplications(0)) {
            /* ユーザーアプリか確認 */
            if (app.sourceDir.startsWith("/data/app/")) {
                if (mDevicePolicyManager.isDeviceOwnerApp(app.packageName)) {
                    return app.loadLabel(getActivity().getPackageManager()).toString();
                }
            }
        }
        return null;
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();

        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        switchPreferencePermissionForced = findPreference("permission_forced");
        preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        preferenceDisableDeviceOwner = findPreference("disable_device_owner");

        switch (Common.GET_MODEL_ID(getActivity())) {
            case 0:
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
                setPreferenceSettings();
                break;
            case 1:
                preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceDisableDeviceOwner.setEnabled(false);
                break;
            case 2:
                setPreferenceSettings();
                break;
        }
    }
}