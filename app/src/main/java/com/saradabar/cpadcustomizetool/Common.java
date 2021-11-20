package com.saradabar.cpadcustomizetool;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Set;

public final class Common {

    public static final class Variable {

        public static int START_FLAG, USE_FLAG;

        public static final int FLAG_TEST = 0;
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

        public static final int DCHA_MODE = 1;
        public static final int DCHA_UTIL_MODE = 2;

        public static String DOWNLOAD_FILE_URL;
        public static String UPDATE_CHECK_URL = "https://github.com/Kobold831/Server/raw/main/CPadCustomizeTool_Update.xml";
        public static String SUPPORT_CHECK_URL = "https://github.com/Kobold831/Server/raw/main/CPadCustomizeTool_Support.xml";
        public static String UPDATE_INFO_URL = "https://docs.google.com/document/d/1uh-FrHM5o84uh7zXw3W_FRIDuzJo8NcVnUD8Rrw4CMQ/";
        public static String UPDATE_URL = "https://is.gd/W5XR2Z";
        public static String WIKI_URL = "https://ctabwiki.nerrog.net/";
        public static String installData;

        public static Toast toast;

        public static DevicePolicyManager mDevicePolicyManager;

        public static ComponentName mComponentName;

        public static final int SETTINGS_NOT_COMPLETED = 0;
        public static final int USE_NOT_DCHASERVICE = 0;
        public static final int SETTINGS_COMPLETED = 1;
        public static final int USE_DCHASERVICE = 1;

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
    public static void SET_UPDATE_FLAG(int FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("UPDATE_FLAG", FLAG).apply();
    }

    public static int GET_UPDATE_FLAG(Context context) {
        int UPDATE_FLAG;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        UPDATE_FLAG = sp.getInt("UPDATE_FLAG", 0);
        return UPDATE_FLAG;
    }

    public static void SET_SETTINGS_FLAG(int FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("SETTINGS_FLAG", FLAG).apply();
    }

    public static int GET_SETTINGS_FLAG(Context context) {
        int SETTINGS_FLAG;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SETTINGS_FLAG = sp.getInt("SETTINGS_FLAG", 0);
        return SETTINGS_FLAG;
    }

    public static void SET_MODEL_NAME(int ID, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("MODEL_NAME", ID).apply();
    }

    public static int GET_MODEL_NAME(Context context) {
        int MODEL_NAME;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        MODEL_NAME = sp.getInt("MODEL_NAME", 0);
        return MODEL_NAME;
    }

    public static void SET_DCHASERVICE_FLAG(int FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("DCHASERVICE_FLAG", FLAG).apply();
    }

    public static int GET_DCHASERVICE_FLAG(Context context) {
        int DCHASERVICE_FLAG;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        DCHASERVICE_FLAG = sp.getInt("DCHASERVICE_FLAG", 0);
        return DCHASERVICE_FLAG;
    }

    public static void SET_CHANGE_SETTINGS_DCHA_FLAG(int FLAG, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("CHANGE_SETTINGS_DCHA_FLAG", FLAG).apply();
    }

    public static int GET_CHANGE_SETTINGS_DCHA_FLAG(Context context) {
        int CHANGE_SETTINGS_DCHA_FLAG;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        CHANGE_SETTINGS_DCHA_FLAG = sp.getInt("CHANGE_SETTINGS_DCHA_FLAG", 0);
        return CHANGE_SETTINGS_DCHA_FLAG;
    }

    public static void SET_NORMAL_LAUNCHER(String mString, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("NORMAL_LAUNCHER", mString).apply();
    }

    public static String GET_NORMAL_LAUNCHER(Context context) {
        String NORMAL_LAUNCHER;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        NORMAL_LAUNCHER = sp.getString("NORMAL_LAUNCHER", null);
        return NORMAL_LAUNCHER;
    }
}

