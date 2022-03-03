package com.saradabar.cpadcustomizetool.check;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.saradabar.cpadcustomizetool.flagment.DeviceOwnerFragment;

public class ByteProgressHandler extends Handler {

    public ProgressDialog progressDialog;
    public DeviceOwnerFragment.TryXApkTask tryXApkTask;

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        progressDialog.setProgress(tryXApkTask.getLoadedBytePercent());
        sendEmptyMessageDelayed(0, 100);
    }
}