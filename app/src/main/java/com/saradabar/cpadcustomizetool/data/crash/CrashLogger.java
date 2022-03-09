package com.saradabar.cpadcustomizetool.data.crash;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.saradabar.cpadcustomizetool.util.Preferences;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CrashLogger implements Thread.UncaughtExceptionHandler {

    Context mContext;
    Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    public CrashLogger(Context context) {
        mContext = context;
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void uncaughtException(@NonNull Thread thread, Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();

        String[] str;
        if (Preferences.GET_CRASH_LOG(mContext) != null) {
            str = new String[]{String.join(",", Preferences.GET_CRASH_LOG(mContext)).replace("    ", "") + getNowDate() + stackTrace + "\n"};
        } else {
            str = new String[]{getNowDate() + stackTrace + "\n"};
        }
        Preferences.SAVE_CRASH_LOG(mContext, str);

        mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }

    public String getNowDate(){
        DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss.SSS z yyyy :\n", Locale.ENGLISH);
        return df.format(System.currentTimeMillis());
    }
}