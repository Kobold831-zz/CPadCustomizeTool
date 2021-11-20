package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.Common.Variable.mComponentName;
import static com.saradabar.cpadcustomizetool.Common.Variable.mDevicePolicyManager;
import static com.saradabar.cpadcustomizetool.Common.Variable.toast;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.StartActivity;
import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

public class MainOtherFragment extends PreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_main_setting, rootKey);

        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        final Preference preferenceOtherSettings = findPreference("intent_android_settings");
        final Preference preferenceSysUiAdjustment = findPreference("intent_sys_ui_adjustment");
        final Preference preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        final Preference preferenceDisableDeviceOwner = findPreference("disable_device_owner");
        final Preference preferenceDevelopmentSettings = findPreference("intent_development_settings");
        final SwitchPreference switchPreferencePermissionForced = (SwitchPreference) findPreference("permission_forced");

        preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceDevelopmentSettings.setOnPreferenceClickListener(preference -> {
            if (Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                }
            } else {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(getActivity(), R.string.toast_no_development_option, Toast.LENGTH_SHORT);
                toast.show();
            }
            return false;
        });

        preferenceSysUiAdjustment.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.systemui", "com.android.systemui.DemoMode");
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceBlockToUninstallSettings.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent(getActivity(), BlockerActivity.class);
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        switchPreferencePermissionForced.setOnPreferenceChangeListener((preference, o) -> {
            if ((boolean) o) {
                switchPreferencePermissionForced.setChecked(true);
                switchPreferencePermissionForced.setTitle("PERMISSION_GRANT_STATE_GRANTED");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mDevicePolicyManager.setPermissionPolicy(mComponentName, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                }
            } else {
                switchPreferencePermissionForced.setChecked(false);
                switchPreferencePermissionForced.setTitle("PERMISSION_GRANT_STATE_DEFAULT");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mDevicePolicyManager.setPermissionPolicy(mComponentName, DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT);
                }
            }
            return true;
        });

        preferenceDisableDeviceOwner.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_question_device_owner)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        mDevicePolicyManager.clearDeviceOwnerApp(getActivity().getPackageName());
                        Intent intent = new Intent(getActivity(), StartActivity.class);
                        getActivity().finish();
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.dialog_common_no, null)
                    .show();
            return false;
        });

        if (Common.GET_MODEL_NAME(getActivity()) == 0) {
            switchPreferencePermissionForced.setEnabled(false);
            switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
        }

        if (Common.GET_MODEL_NAME(getActivity()) != 2) {
            preferenceSysUiAdjustment.setEnabled(false);
            preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
        }

        if (Common.GET_MODEL_NAME(getActivity()) == 1) {
            preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceBlockToUninstallSettings.setEnabled(false);
            preferenceDisableDeviceOwner.setEnabled(false);
        } else {
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
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();

        Common.Variable.mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        final SwitchPreference switchPreferencePermissionForced = (SwitchPreference) findPreference("permission_forced");
        final Preference preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        final Preference preferenceDisableDeviceOwner = findPreference("disable_device_owner");

        if (Common.GET_MODEL_NAME(getActivity()) == 0) {
            switchPreferencePermissionForced.setEnabled(false);
            switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
        }
        if (Common.GET_MODEL_NAME(getActivity()) == 1) {
            preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceBlockToUninstallSettings.setEnabled(false);
            preferenceDisableDeviceOwner.setEnabled(false);
        } else {
            if (!Common.Variable.mDevicePolicyManager.isDeviceOwnerApp(getActivity().getPackageName())) {
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceDisableDeviceOwner.setEnabled(false);
                switchPreferencePermissionForced.setEnabled(false);
                preferenceBlockToUninstallSettings.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
                preferenceDisableDeviceOwner.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
                switchPreferencePermissionForced.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
            }
        }
    }
}