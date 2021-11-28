package com.saradabar.cpadcustomizetool.mode.normal;

import static com.saradabar.cpadcustomizetool.Common.Variable.*;
import static com.saradabar.cpadcustomizetool.Common.*;

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
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Common;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class NormalActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!startCheck()) {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(this, R.string.toast_not_completed_settings, Toast.LENGTH_SHORT);
            toast.show();
            finishAndRemoveTask();
            return;
        }

        if (!setSystemSettings()) {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(this, R.string.toast_not_install_launcher, Toast.LENGTH_SHORT);
            toast.show();
            finishAndRemoveTask();
            return;
        }

        if (!setDchaSettings()) {
            finishAndRemoveTask();
            return;
        }

        if (toast != null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), R.string.toast_execution, Toast.LENGTH_SHORT);
        toast.show();
        finishAndRemoveTask();
    }

    private boolean startCheck() {
        return Common.GET_SETTINGS_FLAG(this);
    }

    private boolean setSystemSettings() {
        ContentResolver resolver = getContentResolver();

        if (isNormalModeSettings_Dcha_State(this)) Settings.System.putInt(resolver, DCHA_STATE, 0);

        if (isNormalModeSettings_Hide_NavigationBar(this)) Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 0);

        if (Objects.equals(GET_NORMAL_LAUNCHER(this), null)) return false;

        if (isNormalModeSettings_Change_Activity(this)) {
            try {
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(Common.GET_NORMAL_LAUNCHER(this));
                startActivity(intent);
            } catch (Exception ignored) {
                return false;
            }
        }
        return true;
    }

    private boolean setDchaSettings() {
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);

        if (!GET_DCHASERVICE_FLAG(getApplicationContext())) {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(getApplicationContext(), R.string.toast_use_not_dcha, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        bindService(new Intent(DCHA_SERVICE).setPackage(PACKAGE_DCHASERVICE), new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                ActivityInfo activityInfo = null;
                IDchaService mDchaService = IDchaService.Stub.asInterface(service);

                if (resolveInfo != null) activityInfo = resolveInfo.activityInfo;

                if (isNormalModeSettings_Change_Home(getApplicationContext())) {
                    try {
                        if (activityInfo != null) {
                            mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                            mDchaService.setDefaultPreferredHomeApp(GET_NORMAL_LAUNCHER(getApplicationContext()));
                        }
                    } catch (RemoteException ignored) {
                        if (toast != null) toast.cancel();
                        toast = Toast.makeText(getApplicationContext(), R.string.toast_not_install_launcher, Toast.LENGTH_SHORT);
                        toast.show();
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