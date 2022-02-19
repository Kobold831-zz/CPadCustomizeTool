package com.saradabar.cpadcustomizetool.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saradabar.cpadcustomizetool.flagment.MainOtherFragment;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            if (MainOtherFragment.OwnerInstallTask.mListener != null) {
                MainOtherFragment.OwnerInstallTask.mListener.onSuccess();
            }
        }
    }
}