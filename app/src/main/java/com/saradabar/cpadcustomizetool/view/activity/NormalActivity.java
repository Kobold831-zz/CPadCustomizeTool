package com.saradabar.cpadcustomizetool.view.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;

import com.saradabar.cpadcustomizetool.data.crash.CrashLogger;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class NormalActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(this));

        if (!startCheck()) {
            Toast.toast(this, R.string.toast_not_completed_settings);
            finishAndRemoveTask();
            return;
        }

        if (!setSystemSettings()) {
            Toast.toast(this, R.string.toast_not_install_launcher);
            finishAndRemoveTask();
            return;
        }

        if (!setDchaSettings()) {
            finishAndRemoveTask();
            return;
        }
        Toast.toast(this, R.string.toast_execution);
        finishAndRemoveTask();
    }

    private boolean startCheck() {
        return Preferences.GET_SETTINGS_FLAG(this);
    }

    private boolean setSystemSettings() {
        ContentResolver resolver = getContentResolver();

        if (Preferences.isNormalModeSettingsDchaState(this)) Settings.System.putInt(resolver, Constants.DCHA_STATE, 0);

        if (Preferences.isNormalModeSettingsNavigationBar(this)) Settings.System.putInt(resolver, Constants.HIDE_NAVIGATION_BAR, 0);

        if (Objects.equals(Preferences.GET_NORMAL_LAUNCHER(this), null)) return false;

        if (Preferences.isNormalModeSettingsActivity(this)) {
            try {
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(Preferences.GET_NORMAL_LAUNCHER(this));
                startActivity(intent);
            } catch (Exception ignored) {
                return false;
            }
        }
        return true;
    }

    private boolean setDchaSettings() {
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);

        if (!Preferences.GET_DCHASERVICE_FLAG(getApplicationContext())) {
            Toast.toast(getApplicationContext(), R.string.toast_use_not_dcha);
            return false;
        }

        bindService(new Intent(Constants.DCHA_SERVICE).setPackage(Constants.PACKAGE_DCHA_SERVICE), new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                ActivityInfo activityInfo = null;
                IDchaService mDchaService = IDchaService.Stub.asInterface(service);

                if (resolveInfo != null) activityInfo = resolveInfo.activityInfo;

                if (Preferences.isNormalModeSettingsLauncher(getApplicationContext())) {
                    try {
                        if (activityInfo != null) {
                            mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                            mDchaService.setDefaultPreferredHomeApp(Preferences.GET_NORMAL_LAUNCHER(getApplicationContext()));
                        }
                    } catch (RemoteException ignored) {
                        Toast.toast(getApplicationContext(), R.string.toast_not_install_launcher);
                        finishAndRemoveTask();
                    }
                }
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                unbindService(this);
            }
        }, Context.BIND_AUTO_CREATE);
        return true;
    }
}