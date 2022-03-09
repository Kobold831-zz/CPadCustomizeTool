package com.saradabar.cpadcustomizetool.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.data.crash.CrashLogger;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class RebootActivity extends Activity {

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(this));
        if (Preferences.GET_DCHASERVICE_FLAG(this)) {
            startReboot();
        } else {
            Toast.toast(this, R.string.toast_use_not_dcha);
            finishAndRemoveTask();
        }
    }

    private void startReboot() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.dialog_title_reboot)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> bindService(new Intent(Constants.DCHA_SERVICE).setPackage(Constants.PACKAGE_DCHA_SERVICE), new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                        try {
                            mDchaService.rebootPad(0, null);
                        } catch (RemoteException ignored) {
                        }
                        unbindService(this);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        unbindService(this);
                    }
                }, Context.BIND_AUTO_CREATE))
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> finishAndRemoveTask())
                .show();
    }
}