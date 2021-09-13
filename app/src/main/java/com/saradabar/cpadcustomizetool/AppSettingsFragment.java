package com.saradabar.cpadcustomizetool;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_NOT_DCHASERVICE;

public class AppSettingsFragment extends PreferenceFragment
{

    //***データ管理
    private void SET_UPDATE_FLAG(final int UPDATE_FLAG) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putInt("UPDATE_FLAG", UPDATE_FLAG).apply();
    }

    private int GET_USE_DCHASERVICE() {
        int USE_DCHASERVICE;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        USE_DCHASERVICE = sp.getInt("USE_DCHASERVICE", 0);
        return USE_DCHASERVICE;
    }

    private void SET_CHANGE_SETTINGS_USE_DCHA(int IS_USE_DCHA) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putInt("IS_USE_DCHA", IS_USE_DCHA).apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_app_settings);

        SwitchPreference autoUpdateCheck = (SwitchPreference) findPreference("switch_auto_update_check");
        SwitchPreference changeSettingsDcha = (SwitchPreference) findPreference("switch_is_change_settings_use_dcha");

        autoUpdateCheck.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                SET_UPDATE_FLAG(1);
            } else {
                SET_UPDATE_FLAG(0);
            }
            return true;
        });

        changeSettingsDcha.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                SET_CHANGE_SETTINGS_USE_DCHA(1);
            } else {
                SET_CHANGE_SETTINGS_USE_DCHA(0);
            }
            return true;
        });

        if (GET_USE_DCHASERVICE() == USE_NOT_DCHASERVICE) {
            SET_CHANGE_SETTINGS_USE_DCHA(0);
            changeSettingsDcha.setSummary("この機能を使用するには、”DchaServiceの機能を使用”を押して内容を確認してください。");
            changeSettingsDcha.setChecked(false);
            changeSettingsDcha.setEnabled(false);
        }else if (GET_USE_DCHASERVICE() == USE_DCHASERVICE) {
            changeSettingsDcha.setEnabled(true);
        }
    }
}