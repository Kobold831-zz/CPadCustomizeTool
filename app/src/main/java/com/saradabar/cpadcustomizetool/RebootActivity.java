package com.saradabar.cpadcustomizetool;

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

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.mDchaService;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.toast;

public class RebootActivity extends Activity {

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Reboot();
    }

    private void Reboot() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
                d.setTitle(R.string.dialog_title_reboot)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    Intent intent = new Intent(DCHA_SERVICE);
                    intent.setPackage(PACKAGE_DCHASERVICE);
                    bindService(intent, new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            mDchaService = IDchaService.Stub.asInterface(service);
                            if(toast != null) {
                                toast.cancel();
                            }
                            toast = Toast.makeText(getApplicationContext(), R.string.toast_reboot, Toast.LENGTH_SHORT);
                            toast.show();
                            try {
                                mDchaService.rebootPad(0,null);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                        }
                    }, Context.BIND_AUTO_CREATE);
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        finishAndRemoveTask();
    }
}