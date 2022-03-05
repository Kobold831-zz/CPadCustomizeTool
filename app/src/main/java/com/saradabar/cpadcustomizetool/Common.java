package com.saradabar.cpadcustomizetool;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.util.Set;

public final class Common {

    public static final class Variable {
        public static final int FLAG_TEST = -1;
        public static final int FLAG_CHECK = 0;
        public static final int FLAG_SET_DCHA_STATE_0 = 1;
        public static final int FLAG_SET_DCHA_STATE_3 = 2;
        public static final int FLAG_HIDE_NAVIGATION_BAR = 3;
        public static final int FLAG_VIEW_NAVIGATION_BAR = 4;
        public static final int FLAG_REBOOT = 5;
        public static final int FLAG_USB_DEBUG_TRUE = 6;
        public static final int FLAG_USB_DEBUG_FALSE = 7;
        public static final int FLAG_MARKET_APP_TRUE = 8;
        public static final int FLAG_MARKET_APP_FALSE = 9;
        public static final int FLAG_SET_LAUNCHER = 10;
        public static final int FLAG_RESOLUTION = 20;

        public static final int REQUEST_UPDATE = 0;
        public static final int REQUEST_ADMIN = 1;
        public static final int REQUEST_INSTALL = 2;
        public static final int REQUEST_PERMISSION = 3;

        public static final boolean DCHA_MODE = true;
        public static final boolean DCHA_UTIL_MODE = false;

        public static String DOWNLOAD_FILE_URL;
        public static String UPDATE_CHECK_URL = "https://github.com/Kobold831/Server/raw/main/CPadCustomizeTool_Update.xml";
        public static String SUPPORT_CHECK_URL = "https://github.com/Kobold831/Server/raw/main/CPadCustomizeTool_Support.xml";
        public static String UPDATE_INFO_URL = "https://docs.google.com/document/d/1uh-FrHM5o84uh7zXw3W_FRIDuzJo8NcVnUD8Rrw4CMQ/";
        public static String UPDATE_URL = "https://is.gd/W5XR2Z";
        public static String WIKI_URL = "https://ctabwiki.nerrog.net/?Discord";
        public static String GITHUB_URL = "https://github.com/Kobold831/CPadCustomizeTool";

        public static Toast toast;

        public static final String DCHA_SERVICE = "jp.co.benesse.dcha.dchaservice.DchaService";
        public static final String PACKAGE_DCHASERVICE = "jp.co.benesse.dcha.dchaservice";
        public static final String DCHA_UTIL_SERVICE = "jp.co.benesse.dcha.dchautilservice.DchaUtilService";
        public static final String PACKAGE_DCHA_UTIL_SERVICE = "jp.co.benesse.dcha.dchautilservice";
        public static final String DCHA_STATE = "dcha_state";
        public static final String HIDE_NAVIGATION_BAR = "hide_navigation_bar";
        public static final String KEY_EMERGENCY_SETTINGS = "emergency_settings";
        public static final String KEY_NORMAL_SETTINGS = "normal_settings";
        public static final String SHARED_PREFERENCE_KEY = "CustomizeTool";
        public static final String KEY_ENABLED_KEEP_SERVICE = "enabled_keep_service";
        public static final String KEY_ENABLED_KEEP_MARKET_APP_SERVICE = "enabled_keep_market_app_service";
        public static final String KEY_ENABLED_KEEP_DCHA_STATE = "enabled_keep_dcha_state";
        public static final String KEY_ENABLED_KEEP_USB_DEBUG = "enabled_keep_usb_debug";
        public static final String KEY_ENABLED_KEEP_HOME = "enabled_keep_home";
        public static final String KEY_SAVE_KEEP_HOME = "save_keep_home";
        public static final String KEY_ENABLED_AUTO_USB_DEBUG = "enabled_auto_usb_debug";

        public static final File IGNORE_DCHA_COMPLETED_FILE = new File("/factory/ignore_dcha_completed");
        public static final File COUNT_DCHA_COMPLETED_FILE = new File("/factory/count_dcha_completed");
    }

    public static File TMP_DIRECTORY(Context context) {
        if (context != null) {
            File file = new File(context.getExternalCacheDir() + "/tmp");
            if (!file.exists()) file.mkdir();
            return file;
        } else return null;
    }

    public static ComponentName getAdministratorComponent(Context context) {
        return new ComponentName(context, com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver.class);
    }

    /* マルチリストのデータ取得 */
    private static Set<String> getEmergencySettings(Context context) {
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(Common.Variable.KEY_EMERGENCY_SETTINGS, null);
    }

    public static boolean isEmergencySettings_Dcha_State(Context context) {
        final String value = "1";
        Set<String> Emergency_Settings = getEmergencySettings(context);
        if (Emergency_Settings != null) {
            return Emergency_Settings.contains(value);
        }
        return false;
    }

    public static boolean isEmergencySettings_Hide_NavigationBar(Context context) {
        final String value = "2";
        Set<String> Emergency_Settings_Hide_NavigationBar = getEmergencySettings(context);
        if (Emergency_Settings_Hide_NavigationBar != null) {
            return Emergency_Settings_Hide_NavigationBar.contains(value);
        }
        return false;
    }

    public static boolean isEmergencySettings_Change_Home(Context context) {
        final String value = "3";
        Set<String> Emergency_Settings_Change_Home = getEmergencySettings(context);
        if (Emergency_Settings_Change_Home != null) {
            return Emergency_Settings_Change_Home.contains(value);
        }
        return false;
    }

    public static boolean isEmergencySettings_Remove_Task(Context context) {
        final String value = "4";
        Set<String> Emergency_Settings_Remove_Task = getEmergencySettings(context);
        if (Emergency_Settings_Remove_Task != null) {
            return Emergency_Settings_Remove_Task.contains(value);
        }
        return false;
    }

    private static Set<String> getNormalModeSettings(Context context) {
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(Common.Variable.KEY_NORMAL_SETTINGS, null);
    }

    public static boolean isNormalModeSettings_Dcha_State(Context context) {
        final String value = "1";
        Set<String> NormalModeSettings_DchaState = getNormalModeSettings(context);
        if (NormalModeSettings_DchaState != null) {
            return NormalModeSettings_DchaState.contains(value);
        }
        return false;
    }

    public static boolean isNormalModeSettings_Hide_NavigationBar(Context context) {
        final String value = "2";
        Set<String> NormalModeSettings_HideNavigationBar = getNormalModeSettings(context);
        if (NormalModeSettings_HideNavigationBar != null) {
            return NormalModeSettings_HideNavigationBar.contains(value);
        }
        return false;
    }

    public static boolean isNormalModeSettings_Change_Home(Context context) {
        final String value = "3";
        Set<String> NormalModeSettings_ChangeHome = getNormalModeSettings(context);
        if (NormalModeSettings_ChangeHome != null) {
            return NormalModeSettings_ChangeHome.contains(value);
        }
        return false;
    }

    public static boolean isNormalModeSettings_Change_Activity(Context context) {
        final String value = "4";
        Set<String> NormalModeSettings_ChangeActivity = getNormalModeSettings(context);
        if (NormalModeSettings_ChangeActivity != null) {
            return NormalModeSettings_ChangeActivity.contains(value);
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
        return sp.getBoolean("confirmation", false);
    }

    public static void saveCrashLog(String[] array, Context context){
        StringBuilder buffer = new StringBuilder();
        for(String item : array){
            buffer.append(item).append(",");
        };
        String buf = buffer.toString();
        String str = buf.substring(0, buf.length() - 1);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("crash_log", str).apply();
    }

    public static String[] getCrashLog(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String str = sp.getString("crash_log","");
        if(str != null && str.length() != 0){
            return str.split(",");
        }else{
            return null;
        }
    }

    public static boolean removeCrashLog(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().remove("crash_log").apply();
        return true;
    }
}