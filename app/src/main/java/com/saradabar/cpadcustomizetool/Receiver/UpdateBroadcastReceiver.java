package com.saradabar.cpadcustomizetool.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saradabar.cpadcustomizetool.Service.KeepDchaService;
import com.saradabar.cpadcustomizetool.Service.KeepHomeService;
import com.saradabar.cpadcustomizetool.Service.KeepMarketAppService;
import com.saradabar.cpadcustomizetool.Service.KeepNavigationBarService;
import com.saradabar.cpadcustomizetool.Service.KeepUsbDebugService;

public class UpdateBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            context.startService(new Intent(context, KeepDchaService.class));
            context.startService(new Intent(context, KeepMarketAppService.class));
            context.startService(new Intent(context, KeepNavigationBarService.class));
            context.startService(new Intent(context, KeepUsbDebugService.class));
            context.startService(new Intent(context, KeepHomeService.class));
        }
    }
}
