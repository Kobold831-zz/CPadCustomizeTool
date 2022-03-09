package com.saradabar.cpadcustomizetool.util;

import android.content.ComponentName;
import android.content.Context;

public class Common {
    public static ComponentName getAdministratorComponent(Context context) {
        return new ComponentName(context, com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver.class);
    }
}