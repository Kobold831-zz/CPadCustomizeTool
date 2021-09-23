package com.saradabar.cpadcustomizetool.common;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
        public static final int FLAG_SET_DCHA_SERVICE = 10;

        public static String DOWNLOAD_FILE_URL;
        public static String UPDATE_CHECK_URL = "https://github.com/saradabar/Touch2_Custom_Tool/raw/master/Update.xml";
        public static String SUPPORT_CHECK_URL = "https://raw.githubusercontent.com/saradabar/Touch2_Custom_Tool/master/Support.xml";
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
        public static final String DCHA_STATE = "dcha_state";
        public static final String HIDE_NAVIGATION_BAR = "hide_navigation_bar";
        public static final String KEY_EMERGENCY_SETTINGS = "Emergency_Settings";
        public static final String KEY_NORMAL_MODE_SETTINGS = "Normal_Mode_Settings";
        public static final String SHARED_PREFERENCE_KEY = "CustomizeTool";
        public static final String KEY_ENABLED_KEEP_SERVICE = "enabled_keep_service";
        public static final String KEY_ENABLED_KEEP_MARKET_APP_SERVICE = "enabled_keep_market_app_service";
        public static final String KEY_ENABLED_KEEP_DCHA_STATE = "enabled_keep_dcha_state";
        public static final String KEY_ENABLED_KEEP_USB_DEBUG = "enabled_keep_usb_debug";
        public static final String KEY_ENABLED_KEEP_HOME = "enabled_keep_home";
        public static final String KEY_SAVE_KEEP_HOME = "save_keep_home";
    }

    public static ComponentName getAdministratorComponent(Context context) {
        return new ComponentName(context, com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver.class);
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
}

