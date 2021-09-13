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

import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;

public class RebootActivity extends Activity {

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Reboot();
    }

    private void Reboot() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.dialog_title_reboot)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    Intent intent = new Intent(DCHA_SERVICE);
                    intent.setPackage(PACKAGE_DCHASERVICE);
                    bindService(intent, new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                            Toast.makeText(getApplication(), R.string.toast_reboot, Toast.LENGTH_SHORT).show();
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
    public final void onPause() {
        super.onPause();
        finishAndRemoveTask();
    }
}