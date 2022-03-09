package com.saradabar.cpadcustomizetool.view.flagment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;

public class MainOtherFragment extends PreferenceFragment {

    Preference preferenceOtherSettings,
            preferenceSysUiAdjustment,
            preferenceDevelopmentSettings;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_main_setting, rootKey);
        preferenceOtherSettings = findPreference("intent_android_settings");
        preferenceSysUiAdjustment = findPreference("intent_sys_ui_adjustment");
        preferenceDevelopmentSettings = findPreference("intent_development_settings");

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
                Toast.toast(getActivity(), R.string.toast_no_development_option);
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

        if (Preferences.GET_MODEL_ID(getActivity()) == 0) {
            preferenceSysUiAdjustment.setEnabled(false);
            preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
        }
    }
}