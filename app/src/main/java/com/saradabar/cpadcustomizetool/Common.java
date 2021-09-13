package com.saradabar.cpadcustomizetool;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public final class Common {

    public static final class Customizetool {

        public static int start_flag;
        public static int NOT_USE;

        public static String DOWNLOAD_FILE_URL;

        public static DevicePolicyManager mDevicePolicyManager;
        public static ComponentName mComponentName;
        public static IDchaService mDchaService;

        public static final int UPDATE_FLAG = 0;
        public static final int SETTINGS_NOT_COMPLETED = 0;
        public static final int USE_NOT_DCHASERVICE = 0;
        public static final int SETTINGS_COMPLETED = 1;
        public static final int CHECK_OK_TAB3 = 1;
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
}

