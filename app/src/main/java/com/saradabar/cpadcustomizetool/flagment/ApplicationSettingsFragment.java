package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.common.Common.GET_CHANGE_SETTINGS_DCHA_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.GET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.GET_MODEL_NAME;
import static com.saradabar.cpadcustomizetool.common.Common.SET_CHANGE_SETTINGS_DCHA_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.SET_UPDATE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_NOT_DCHASERVICE;

import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.R;

public class ApplicationSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_app_settings, rootKey);

        SwitchPreference autoUpdateCheck = (SwitchPreference) findPreference("switch_auto_update_check");
        SwitchPreference changeSettingsDcha = (SwitchPreference) findPreference("switch_is_change_settings_use_dcha");

        changeSettingsDcha.setChecked(GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()) == 1);

        autoUpdateCheck.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                SET_UPDATE_FLAG(1, getActivity());
            } else {
                SET_UPDATE_FLAG(0, getActivity());
            }
            return true;
        });

        changeSettingsDcha.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                SET_CHANGE_SETTINGS_DCHA_FLAG(1, getActivity());
            } else {
                SET_CHANGE_SETTINGS_DCHA_FLAG(0, getActivity());
            }
            return true;
        });

        if (GET_DCHASERVICE_FLAG(getActivity()) == USE_NOT_DCHASERVICE) {
            if (GET_MODEL_NAME(getActivity()) == 2) {
                changeSettingsDcha.setSummary(Build.MODEL + "では問題が発生しているため、自動で有効になっています");
                changeSettingsDcha.setEnabled(false);
            } else {
                SET_CHANGE_SETTINGS_DCHA_FLAG(0, getActivity());
                changeSettingsDcha.setChecked(false);
                changeSettingsDcha.setSummary("この機能を使用するには、”DchaServiceの機能を使用”を押して内容を確認してください");
                changeSettingsDcha.setEnabled(false);
            }
        }else {
            if (GET_MODEL_NAME(getActivity()) == 2) {
                changeSettingsDcha.setSummary(Build.MODEL + "では問題が発生しているため、自動で有効になっています");
            }
        }
    }
}