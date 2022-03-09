package com.saradabar.cpadcustomizetool.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class Preferences {
    /* マルチリストのデータ取得 */
    public static Set<String> getEmergencySettings(Context context) {
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(Constants.KEY_EMERGENCY_SETTINGS, null);
    }

    public static boolean isEmergencySettingsDchaState(Context context) {
        Set<String> set = getEmergencySettings(context);
        if (set != null) {
            return set.contains(Integer.toString(1));
        }
        return false;
    }

    public static boolean isEmergencySettingsNavigationBar(Context context) {
        Set<String> set = getEmergencySettings(context);
        if (set != null) {
            return set.contains(Integer.toString(2));
        }
        return false;
    }

    public static boolean isEmergencySettingsLauncher(Context context) {
        Set<String> set = getEmergencySettings(context);
        if (set != null) {
            return set.contains(Integer.toString(3));
        }
        return false;
    }

    public static boolean isEmergencySettingsRemoveTask(Context context) {
        Set<String> set = getEmergencySettings(context);
        if (set != null) {
            return set.contains(Integer.toString(4));
        }
        return false;
    }

    private static Set<String> getNormalModeSettings(Context context) {
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(Constants.KEY_NORMAL_SETTINGS, null);
    }

    public static boolean isNormalModeSettingsDchaState(Context context) {
        Set<String> set = getNormalModeSettings(context);
        if (set != null) {
            return set.contains(Integer.toString(1));
        }
        return false;
    }

    public static boolean isNormalModeSettingsNavigationBar(Context context) {
        Set<String> set = getNormalModeSettings(context);
        if (set != null) {
            return set.contains(Integer.toString(2));
        }
        return false;
    }

    public static boolean isNormalModeSettingsLauncher(Context context) {
        Set<String> set = getNormalModeSettings(context);
        if (set != null) {
            return set.contains(Integer.toString(3));
        }
        return false;
    }

    public static boolean isNormalModeSettingsActivity(Context context) {
        Set<String> set = getNormalModeSettings(context);
        if (set != null) {
            return set.contains(Integer.toString(4));
        }
        return false;
    }

    /* データ管理 */
    public static void SET_UPDATE_FLAG(boolean FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("update", FLAG).apply();
    }

    public static boolean GET_UPDATE_FLAG(Context context) {
        boolean bl;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        bl = sp.getBoolean("update", true);
        return bl;
    }

    public static void SET_SETTINGS_FLAG(boolean FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("settings", FLAG).apply();
    }

    public static boolean GET_SETTINGS_FLAG(Context context) {
        boolean bl;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        bl = sp.getBoolean("settings", false);
        return bl;
    }

    public static void SET_MODEL_ID(int MODEL_ID, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("model_name", MODEL_ID).apply();
    }

    public static int GET_MODEL_ID(Context context) {
        int id;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        id = sp.getInt("model_name", 0);
        return id;
    }

    public static void SET_DCHASERVICE_FLAG(boolean FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("dcha_service", FLAG).apply();
    }

    public static boolean GET_DCHASERVICE_FLAG(Context context) {
        boolean bl;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        bl = sp.getBoolean("dcha_service", false);
        return bl;
    }

    public static void SET_CHANGE_SETTINGS_DCHA_FLAG(boolean FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("settings_dcha", FLAG).apply();
    }

    public static boolean GET_CHANGE_SETTINGS_DCHA_FLAG(Context context) {
        boolean bl;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        bl = sp.getBoolean("settings_dcha", false);
        return bl;
    }

    public static void SET_NORMAL_LAUNCHER(String string, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("normal_launcher", string).apply();
    }

    public static String GET_NORMAL_LAUNCHER(Context context) {
        String string;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        string = sp.getString("normal_launcher", null);
        return string;
    }

    public static void SET_CONFIRMATION(boolean bl, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("confirmation", bl).apply();
    }

    public static boolean GET_CONFIRMATION(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return !sp.getBoolean("confirmation", false);
    }

    public static void SAVE_CRASH_LOG(Context context, String[] array){
        StringBuilder buffer = new StringBuilder();
        for(String item : array){
            buffer.append(item).append(",");
        };
        String buf = buffer.toString();
        String str = buf.substring(0, buf.length() - 1);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("crash_log", str).apply();
    }

    public static String[] GET_CRASH_LOG(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String str = sp.getString("crash_log","");
        if(str != null && str.length() != 0){
            return str.split(",");
        }else{
            return null;
        }
    }

    public static boolean REMOVE_CRASH_LOG(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().remove("crash_log").apply();
        return true;
    }
}