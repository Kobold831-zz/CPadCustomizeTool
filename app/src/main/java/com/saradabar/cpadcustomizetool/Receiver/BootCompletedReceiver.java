package com.saradabar.cpadcustomizetool.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saradabar.cpadcustomizetool.service.KeepDchaService;
import com.saradabar.cpadcustomizetool.service.KeepHomeService;
import com.saradabar.cpadcustomizetool.service.KeepMarketAppService;
import com.saradabar.cpadcustomizetool.service.KeepNavigationBarService;
import com.saradabar.cpadcustomizetool.service.KeepUsbDebugService;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startService(new Intent(context, KeepDchaService.class));
            context.startService(new Intent(context, KeepMarketAppService.class));
            context.startService(new Intent(context, KeepNavigationBarService.class));
            context.startService(new Intent(context, KeepUsbDebugService.class));
            context.startService(new Intent(context, KeepHomeService.class));
        }
    }
}
