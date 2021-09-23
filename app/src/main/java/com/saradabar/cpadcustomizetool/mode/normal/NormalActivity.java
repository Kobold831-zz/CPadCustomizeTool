package com.saradabar.cpadcustomizetool.mode.normal;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.SETTINGS_NOT_COMPLETED;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.toast;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.common.Common;
import com.saradabar.cpadcustomizetool.flagment.MainFragment;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class NormalActivity extends Activity {

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
            Intent clearPackageName = new Intent(Intent.ACTION_MAIN);
            clearPackageName.addCategory(Intent.CATEGORY_HOME);
            PackageManager pm = getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(clearPackageName, 0);
            if (resolveInfo != null) {
                activityInfo = resolveInfo.activityInfo;
            }
            if (MainFragment.isNormalModeSettings_Change_Activity(this)) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.teslacoilsw.launcher", "com.teslacoilsw.launcher.NovaLauncher");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(this, R.string.toast_not_install_nova, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            if (MainFragment.isNormalModeSettings_Dcha_State(this)) {
                Settings.System.putInt(resolver, DCHA_STATE, 0);
            }
            if (MainFragment.isNormalModeSettings_Hide_NavigationBar(this)) {
                Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 0);
            }

            final Intent intent = new Intent(DCHA_SERVICE);
            intent.setPackage(PACKAGE_DCHASERVICE);
            bindService(intent, new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                    if (MainFragment.isNormalModeSettings_Change_Home(getApplicationContext())) {
                        if (Common.GET_DCHASERVICE_FLAG(getApplicationContext()) == USE_DCHASERVICE) {
                            try {
                                mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                                mDchaService.setDefaultPreferredHomeApp("com.teslacoilsw.launcher");
                            } catch (RemoteException ignored) {
                            }
                        } else {
                            if (toast != null) {
                                toast.cancel();
                            }
                            toast = Toast.makeText(getApplicationContext(), R.string.toast_use_not_dcha, Toast.LENGTH_SHORT);
                            toast.show();
                        }
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