package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.Common.*;
import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.R;

public class ApplicationSettingsFragment extends PreferenceFragment {

    SwitchPreference autoUpdateCheck,
            changeSettingsDcha,
            autoUsbDebug;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_app_settings, rootKey);
        SharedPreferences sp = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        ContentResolver resolver = getActivity().getContentResolver();

        autoUpdateCheck = findPreference("switch_auto_update_check");
        changeSettingsDcha = findPreference("switch_is_change_settings_use_dcha");
        autoUsbDebug = findPreference("switch_auto_usb_debug");

        autoUpdateCheck.setChecked(!GET_UPDATE_FLAG(getActivity()));
        changeSettingsDcha.setChecked(GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()));

        try {
            autoUsbDebug.setChecked(sp.getBoolean(Variable.KEY_ENABLED_AUTO_USB_DEBUG, false));
        } catch (NullPointerException e) {
            SharedPreferences.Editor spe = sp.edit();
            spe.putBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, false);
            spe.apply();
        }

        autoUpdateCheck.setOnPreferenceChangeListener((preference, newValue) -> {
            SET_UPDATE_FLAG(!((boolean) newValue), getActivity());
            return true;
        });

        changeSettingsDcha.setOnPreferenceChangeListener((preference, newValue) -> {
            SET_CHANGE_SETTINGS_DCHA_FLAG((boolean) newValue, getActivity());
            return true;
        });

        autoUsbDebug.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                if (GET_MODEL_ID(getActivity()) == 2)
                    Settings.System.putInt(resolver, DCHA_STATE, 3);
                Thread.sleep(100);
                Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1);
                if (GET_MODEL_ID(getActivity()) == 2)
                    Settings.System.putInt(resolver, DCHA_STATE, 0);
                sp.edit().putBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, (boolean) newValue).apply();
            } catch (SecurityException | InterruptedException ignored) {
                if (GET_MODEL_ID(getActivity()) == 2)
                    Settings.System.putInt(resolver, DCHA_STATE, 0);
                if (null != toast) toast.cancel();
                toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                toast.show();
                autoUsbDebug.setChecked(false);
                return false;
            }
            return true;
        });

        if (!GET_DCHASERVICE_FLAG(getActivity())) {
            SET_CHANGE_SETTINGS_DCHA_FLAG(false, getActivity());
            changeSettingsDcha.setChecked(false);
            changeSettingsDcha.setSummary("この機能を使用するには、\"DchaServiceを使用\"を押して内容を確認してください");
            changeSettingsDcha.setEnabled(false);
        }
    }
}