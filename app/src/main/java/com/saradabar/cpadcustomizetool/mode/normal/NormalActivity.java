package com.saradabar.cpadcustomizetool.mode.normal;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.flagment.MainFragment;
import com.saradabar.cpadcustomizetool.R;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;

public class NormalActivity extends Activity {
    private static Toast toast;
    @Override
    protected void onStart() {
        super.onStart();
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
                if (MainFragment.isNormalModeSettings_Change_Activity(getApplicationContext())) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName("com.teslacoilsw.launcher", "com.teslacoilsw.launcher.NovaLauncher");
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        if (toast != null) {
                            toast.cancel();
                        }
                        toast = Toast.makeText(getApplicationContext(), R.string.toast_not_install_nova, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                if (MainFragment.isNormalModeSettings_Dcha_State(getApplicationContext())) {
                    try {
                        mDchaService.setSetupStatus(0);
                    } catch (RemoteException ignored) {
                    }
                }
                if (MainFragment.isNormalModeSettings_Hide_NavigationBar(getApplicationContext())) {
                    try {
                        mDchaService.hideNavigationBar(false);
                    } catch (RemoteException ignored) {
                    }
                }
                if (MainFragment.isNormalModeSettings_Change_Home(getApplicationContext())) {
                    try {
                        mDchaService.clearDefaultPreferredApp(clear);
                        mDchaService.setDefaultPreferredHomeApp("com.teslacoilsw.launcher");
                    } catch (RemoteException ignored) {
                    }
                }
                finishAndRemoveTask();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, Context.BIND_AUTO_CREATE);
    }
}