package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.Common.GET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.menu.CrashDetection;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class RebootActivity extends Activity {

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashDetection(this));
        if (GET_DCHASERVICE_FLAG(this)) {
            startReboot();
        } else {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(this, R.string.toast_use_not_dcha, Toast.LENGTH_SHORT);
            toast.show();
            finishAndRemoveTask();
        }
    }

    private void startReboot() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.dialog_title_reboot)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> bindService(new Intent(DCHA_SERVICE).setPackage(PACKAGE_DCHASERVICE), new ServiceConnection() {
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