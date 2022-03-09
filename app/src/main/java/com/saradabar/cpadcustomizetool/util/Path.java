package com.saradabar.cpadcustomizetool.util;

import android.content.Context;

public class Path {
    public static String getTemporaryPath(Context context) {
        return context.getExternalCacheDir().getPath() + "/tmp";
    }
}