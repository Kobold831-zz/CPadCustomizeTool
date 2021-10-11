package com.saradabar.cpadcustomizetool.Receiver;

import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.Common.Variable.HIDE_NAVIGATION_BAR;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.service.KeepService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ContentResolver resolver = context.getContentResolver();
            SharedPreferences sp = context.getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

            /* UsbDebugを有効にするか確認 */
            try {
                if (sp.getBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, false)) {
                    String dchaStateString = DCHA_STATE;
                    try {
                        if (Common.GET_MODEL_NAME(context) == 2) {
                            Settings.System.putInt(resolver, dchaStateString, 3);
                        }
                        Thread.sleep(100);
                        Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1);
                        if (Common.GET_MODEL_NAME(context) == 2) {
                            Settings.System.putInt(resolver, dchaStateString, 0);
                        }
                    } catch (SecurityException | InterruptedException e) {
                        e.printStackTrace();
                        if (Common.GET_MODEL_NAME(context) == 2) {
                            Settings.System.putInt(resolver, dchaStateString, 0);
                        }
                        /* 権限が付与されていないなら機能を無効 */
                        SharedPreferences.Editor spe = sp.edit();
                        spe.putBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, false);
                        spe.apply();
                    }
                }
            }catch (NullPointerException e) {
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_AUTO_USB_DEBUG, false);
                spe.apply();
            }

            /* Serviceを起動するとナビゲーションバーが非表示になってしまうのを防ぐ */
            if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
                Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 0);
            }
            context.startService(new Intent(context, KeepService.class));
        }
    }
}