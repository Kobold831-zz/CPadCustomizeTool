package com.saradabar.cpadcustomizetool.mode.emergency;

import static com.saradabar.cpadcustomizetool.Common.Variable.*;
import static com.saradabar.cpadcustomizetool.Common.*;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import com.saradabar.cpadcustomizetool.flagment.MainFragment;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class EmergencyActivity extends Activity {

    private ActivityInfo activityInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Common.GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(this, R.string.toast_not_completed_settings, Toast.LENGTH_SHORT);
            toast.show();
            finishAndRemoveTask();
        }else {
            ContentResolver resolver = getContentResolver();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            final String whatCourse = sp.getString("emergency_mode", "");
            Intent clearPackageName = new Intent(Intent.ACTION_MAIN);
            clearPackageName.addCategory(Intent.CATEGORY_HOME);
            PackageManager pm = getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(clearPackageName, 0);
            if (resolveInfo != null) {
                activityInfo = resolveInfo.activityInfo;
            }
            if (whatCourse.contains("1")) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("jp.co.benesse.touch.allgrade.b003.touchhomelauncher", "jp.co.benesse.touch.allgrade.b003.touchhomelauncher.HomeLauncherActivity");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(this, R.string.toast_not_course, Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (isEmergencySettings_Dcha_State(this)) {
                    Settings.System.putInt(resolver, DCHA_STATE, 3);
                }
                if (isEmergencySettings_Hide_NavigationBar(this)) {
                    Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 1);
                }
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
                }
                if (isEmergencySettings_Dcha_State(this)) {
                    Settings.System.putInt(resolver, DCHA_STATE, 3);
                }
                if (isEmergencySettings_Hide_NavigationBar(this)) {
                    Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 1);
                }
            }

            final Intent intent = new Intent(DCHA_SERVICE);
            intent.setPackage(PACKAGE_DCHASERVICE);
            bindService(intent, new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                    if (Common.GET_DCHASERVICE_FLAG(getApplicationContext()) == USE_DCHASERVICE) {
                        if (whatCourse.contains("1")) {
                            if (isEmergencySettings_Change_Home(getApplicationContext())) {
                                try {
                                    mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                                    mDchaService.setDefaultPreferredHomeApp("jp.co.benesse.touch.allgrade.b003.touchhomelauncher");
                                } catch (RemoteException ignored) {
                                }
                            }
                            if (isEmergencySettings_Remove_Task(getApplicationContext())) {
                                try {
                                    mDchaService.removeTask(null);
                                } catch (RemoteException ignored) {
                                }
                            }
                        } else if (whatCourse.contains("2")) {
                            if (isEmergencySettings_Change_Home(getApplicationContext())) {
                                try {
                                    mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                                    mDchaService.setDefaultPreferredHomeApp("jp.co.benesse.touch.home");
                                } catch (RemoteException ignored) {
                                }
                            }
                            if (isEmergencySettings_Remove_Task(getApplicationContext())) {
                                try {
                                    mDchaService.removeTask(null);
                                } catch (RemoteException ignored) {
                                }
                            }
                        }
                    } else {
                        if (toast != null) {
                            toast.cancel();
                        }
                        toast = Toast.makeText(getApplicationContext(), R.string.toast_use_not_dcha, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(getApplicationContext(), R.string.toast_execution, Toast.LENGTH_SHORT);
                    toast.show();
                    unbindService(this);
                    finishAndRemoveTask();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    unbindService(this);
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }
}