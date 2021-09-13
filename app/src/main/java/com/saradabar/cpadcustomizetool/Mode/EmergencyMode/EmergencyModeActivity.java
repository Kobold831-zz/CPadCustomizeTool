package com.saradabar.cpadcustomizetool.Mode.EmergencyMode;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.MainFragment;
import com.saradabar.cpadcustomizetool.R;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;

public class EmergencyModeActivity extends Activity {
    private static Toast toast;
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String whatCourse = sp.getString("kinkyu_Mode", "");
        PackageManager pm = getPackageManager();
        Intent clearPackageName = new Intent(Intent.ACTION_MAIN);
        clearPackageName.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = pm.resolveActivity(clearPackageName, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        final String clear = activityInfo.packageName;
        final Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        bindService(intent, new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder service) {
                IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                if (whatCourse.contains("1")) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName("jp.co.benesse.touch.allgrade.b003.touchhomelauncher", "jp.co.benesse.touch.allgrade.b003.touchhomelauncher.HomeLauncherActivity");
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        if (toast != null) {
                            toast.cancel();
                        }
                        toast = Toast.makeText(getApplicationContext(), R.string.toast_not_course, Toast.LENGTH_SHORT);
                        toast.show();
                        finishAndRemoveTask();
                        return;
                    }
                    if (MainFragment.isEmergencySettings_Dcha_State(getApplicationContext())) {
                        try {
                            mDchaService.setSetupStatus(3);
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (MainFragment.isEmergencySettings_Hide_NavigationBar(getApplicationContext())) {
                        try {
                            mDchaService.hideNavigationBar(true);
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (MainFragment.isEmergencySettings_Change_Home(getApplicationContext())) {
                        try {
                            mDchaService.clearDefaultPreferredApp(clear);
                            mDchaService.setDefaultPreferredHomeApp("jp.co.benesse.touch.allgrade.b003.touchhomelauncher");
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (MainFragment.isEmergencySettings_Remove_Task(getApplicationContext())) {
                        try {
                            mDchaService.removeTask(null);
                        } catch (RemoteException ignored) {
                        }
                    }
                    finishAndRemoveTask();
                    return;
                }
                if (whatCourse.contains("2")) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName("jp.co.benesse.touch.home", "jp.co.benesse.touch.home.LoadingActivity");
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        if (toast != null) {
                            toast.cancel();
                        }
                        toast = Toast.makeText(getApplicationContext(), R.string.toast_not_course, Toast.LENGTH_SHORT);
                        toast.show();
                        finishAndRemoveTask();
                        return;
                    }
                    if (MainFragment.isEmergencySettings_Dcha_State(getApplicationContext())) {
                        try {
                            mDchaService.setSetupStatus(3);
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (MainFragment.isEmergencySettings_Hide_NavigationBar(getApplicationContext())) {
                        try {
                            mDchaService.hideNavigationBar(true);
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (MainFragment.isEmergencySettings_Change_Home(getApplicationContext())) {
                        try {
                            mDchaService.clearDefaultPreferredApp(clear);
                            mDchaService.setDefaultPreferredHomeApp("jp.co.benesse.touch.home");
                        } catch (RemoteException ignored) {
                        }
                    }
                    if (MainFragment.isEmergencySettings_Remove_Task(getApplicationContext())) {
                        try {
                            mDchaService.removeTask(null);
                        } catch (RemoteException ignored) {
                        }
                    }
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, Context.BIND_AUTO_CREATE);
    }
}