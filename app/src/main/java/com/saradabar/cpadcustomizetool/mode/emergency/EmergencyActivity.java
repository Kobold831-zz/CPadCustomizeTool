package com.saradabar.cpadcustomizetool.mode.emergency;

import static com.saradabar.cpadcustomizetool.Common.Variable.*;
import static com.saradabar.cpadcustomizetool.Common.*;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.service.KeepService;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class EmergencyActivity extends Activity {

    private ActivityInfo activityInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String whatCourse = PreferenceManager.getDefaultSharedPreferences(this).getString("emergency_mode", "");

        if (!startCheck()) {
            finishAndRemoveTask();
        }

        if (!setSystemSettings(true)) {
            finishAndRemoveTask();
        }

        if (whatCourse.contains("1")) {
            if (setDchaSettings("jp.co.benesse.touch.allgrade.b003.touchhomelauncher", "jp.co.benesse.touch.allgrade.b003.touchhomelauncher.HomeLauncherActivity")) {
                finishAndRemoveTask();
            } else {
                finishAndRemoveTask();
            }
        }

        if (whatCourse.contains("2")) {
            if (setDchaSettings("jp.co.benesse.touch.home", "jp.co.benesse.touch.home.LoadingActivity")) {
                finishAndRemoveTask();
            } else {
                finishAndRemoveTask();
            }
        }
    }

    private boolean startCheck() {
        if (Common.GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(this, R.string.toast_not_completed_settings, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        } else return true;
    }

    private boolean setSystemSettings(boolean study) {

        ContentResolver resolver = getContentResolver();

        if (study) {
            SharedPreferences sp = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            if (sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false)) {
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false);
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false);
                spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false);
                spe.apply();
                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

                for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                    if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                        KeepService.getInstance().stopService(1);
                        KeepService.getInstance().stopService(2);
                        KeepService.getInstance().stopService(5);
                    }
                }
            }

            try {
                if (isEmergencySettings_Dcha_State(this)) {
                    Settings.System.putInt(resolver, DCHA_STATE, 3);
                }
                if (isEmergencySettings_Hide_NavigationBar(this)) {
                    Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 1);
                }
                return true;
            } catch (SecurityException ignored) {
                return false;
            }
        } else {
            try {
                if (isEmergencySettings_Dcha_State(this)) {
                    Settings.System.putInt(resolver, DCHA_STATE, 0);
                }
                if (isEmergencySettings_Hide_NavigationBar(this)) {
                    Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 0);
                }
                return true;
            } catch (SecurityException ignored) {
                return false;
            }
        }
    }

    private boolean setDchaSettings(String packageName, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);

        if (resolveInfo != null) {
            activityInfo = resolveInfo.activityInfo;
        }

        try {
            intent = new Intent();
            intent.setClassName(packageName, className);
            startActivity(intent);
        } catch (Exception e) {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(this, R.string.toast_not_course, Toast.LENGTH_SHORT);
            toast.show();
            setSystemSettings(false);
            return false;
        }

        if (!isEmergencySettings_Change_Home(this) && !isEmergencySettings_Remove_Task(this))
            return true;

        intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        bindService(intent, new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                if (Common.GET_DCHASERVICE_FLAG(getApplicationContext()) == USE_DCHASERVICE) {
                    if (isEmergencySettings_Change_Home(getApplicationContext())) {
                        try {
                            mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                            mDchaService.setDefaultPreferredHomeApp(packageName);
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (isEmergencySettings_Remove_Task(getApplicationContext())) {
                        try {
                            mDchaService.removeTask(null);
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(getApplicationContext(), R.string.toast_execution, Toast.LENGTH_SHORT);
                    toast.show();
                    unbindService(this);
                } else {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(getApplicationContext(), R.string.toast_use_not_dcha, Toast.LENGTH_SHORT);
                    toast.show();
                    setSystemSettings(false);
                    unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                unbindService(this);
            }
        }, Context.BIND_AUTO_CREATE);
        return true;
    }
}