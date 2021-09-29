package com.saradabar.cpadcustomizetool.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saradabar.cpadcustomizetool.service.KeepService;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            context.startService(new Intent(context, KeepService.class));
        }
    }
}