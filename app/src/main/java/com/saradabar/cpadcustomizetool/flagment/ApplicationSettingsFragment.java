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

    private ContentResolver resolver;
    private final String dchaStateString = DCHA_STATE;
    private SharedPreferences sp;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_app_settings, rootKey);

        sp = getActivity().getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        resolver = getActivity().getContentResolver();

        SwitchPreference autoUpdateCheck = findPreference("switch_auto_update_check");
        SwitchPreference changeSettingsDcha = findPreference("switch_is_change_settings_use_dcha");
        SwitchPreference autoUsbDebug = findPreference("switch_auto_usb_debug");

        changeSettingsDcha.setChecked(GET_CHANGE_SETTINGS_DCHA_FLAG(getActivity()) == 1);

        try {
            autoUsbDebug.setChecked(sp.getBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, false));
        } catch (NullPointerException e) {
            SharedPreferences.Editor spe = sp.edit();
            spe.putBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, false);
            spe.apply();
        }

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

        autoUsbDebug.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                    Settings.System.putInt(resolver, dchaStateString, 3);
                }
                Thread.sleep(100);
                Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1);
                if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                    Settings.System.putInt(resolver, dchaStateString, 0);
                }
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, (boolean) newValue);
                spe.apply();
            } catch (SecurityException | InterruptedException e) {
                e.printStackTrace();
                if (Common.GET_MODEL_NAME(getActivity()) == 2) {
                    Settings.System.putInt(resolver, dchaStateString, 0);
                }
                if (null != toast) toast.cancel();
                toast = Toast.makeText(getActivity(), R.string.toast_not_change, Toast.LENGTH_SHORT);
                toast.show();
                autoUsbDebug.setChecked(false);
                return false;
            }
            return true;
        });

        if (GET_DCHASERVICE_FLAG(getActivity()) == USE_NOT_DCHASERVICE) {
            SET_CHANGE_SETTINGS_DCHA_FLAG(0, getActivity());
            changeSettingsDcha.setChecked(false);
            changeSettingsDcha.setSummary("この機能を使用するには、”DchaServiceの機能を使用”を押して内容を確認してください");
            changeSettingsDcha.setEnabled(false);
        }
    }
}