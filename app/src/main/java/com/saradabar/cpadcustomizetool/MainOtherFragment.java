package com.saradabar.cpadcustomizetool;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.BlockToUninstall.BlockToUninstall;

public class MainOtherFragment extends PreferenceFragment {

    private DevicePolicyManager dpm;

    public int GET_CHECK_TAB_ID() {
        int CHECK_TAB_ID;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CHECK_TAB_ID = sp.getInt("CHECK_TAB_ID", 0);
        return CHECK_TAB_ID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_main_setting);

        dpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        final Preference preferenceOtherSettings = findPreference("intent_android_settings");
        final Preference preferenceSysUiAdjustment = findPreference("intent_sys_ui_adjustment");
        final Preference preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        final Preference preferenceDisableDeviceOwner = findPreference("disable_device_owner");
        
        preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                startActivity(intent);
            }catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceSysUiAdjustment.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.systemui", "com.android.systemui.DemoMode");
                startActivity(intent);
            }catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceBlockToUninstallSettings.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent(getActivity(), BlockToUninstall.class);
                startActivity(intent);
            }catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceDisableDeviceOwner.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_question_device_owner)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        dpm.clearDeviceOwnerApp(getActivity().getPackageName());
                        Toast.makeText(getActivity(), R.string.toast_notice_disable_own, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), StartActivity.class);
                        getActivity().finish();
                        startActivity(intent);
                    })
                    .setNeutralButton(R.string.dialog_common_no, null)
                    .show();
            return false;
        });

        if (GET_CHECK_TAB_ID() != 2) {
            preferenceSysUiAdjustment.setEnabled(false);
            preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
        }

        if (GET_CHECK_TAB_ID() == 1) {
            preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceBlockToUninstallSettings.setEnabled(false);
            preferenceDisableDeviceOwner.setEnabled(false);
        }else {
            if (!dpm.isDeviceOwnerApp(getActivity().getPackageName())) {
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceDisableDeviceOwner.setEnabled(false);
                preferenceBlockToUninstallSettings.setSummary("Device-Ownerではないためこの機能は使用できません\nこの機能を使用するにはADBでDevice-Ownerを許可してください");
                preferenceDisableDeviceOwner.setSummary("Device-Ownerではないためこの機能は使用できません\nこの機能を使用するにはADBでDevice-Ownerを許可してください");
            }
        }
    }

    //再表示
    @Override
    public void onResume() {
        super.onResume();

        dpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        final Preference preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        final Preference preferenceDisableDeviceOwner = findPreference("disable_device_owner");
        if (GET_CHECK_TAB_ID() == 1) {
            preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
            preferenceBlockToUninstallSettings.setEnabled(false);
            preferenceDisableDeviceOwner.setEnabled(false);
        }else {
            if (!dpm.isDeviceOwnerApp(getActivity().getPackageName())) {
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceDisableDeviceOwner.setEnabled(false);
                preferenceBlockToUninstallSettings.setSummary("Device-Ownerではないためこの機能は使用できません\nこの機能を使用するにはADBでDevice-Ownerを許可してください");
                preferenceDisableDeviceOwner.setSummary("Device-Ownerではないためこの機能は使用できません\nこの機能を使用するにはADBでDevice-Ownerを許可してください");
            }
        }
    }
}
