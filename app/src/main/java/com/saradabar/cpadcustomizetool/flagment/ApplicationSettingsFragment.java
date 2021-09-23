package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.common.Common.GET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.SET_CHANGE_SETTINGS_DCHA_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.SET_UPDATE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_NOT_DCHASERVICE;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.R;

public class ApplicationSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_app_settings);

        SwitchPreference autoUpdateCheck = (SwitchPreference) findPreference("switch_auto_update_check");
        SwitchPreference changeSettingsDcha = (SwitchPreference) findPreference("switch_is_change_settings_use_dcha");

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
            SET_CHANGE_SETTINGS_DCHA_FLAG(0, getActivity());
            changeSettingsDcha.setSummary("この機能を使用するには、”DchaServiceの機能を使用”を押して内容を確認してください");
            changeSettingsDcha.setChecked(false);
            changeSettingsDcha.setEnabled(false);
        } else if (GET_DCHASERVICE_FLAG(getActivity()) == USE_DCHASERVICE) {
            changeSettingsDcha.setEnabled(true);
        }
    }
}