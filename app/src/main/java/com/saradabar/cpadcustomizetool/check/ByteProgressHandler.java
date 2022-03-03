package com.saradabar.cpadcustomizetool.check;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.saradabar.cpadcustomizetool.flagment.DeviceOwnerFragment;

public class ByteProgressHandler extends Handler {

    public ProgressDialog progressDialog;
    public DeviceOwnerFragment.TryXApkTask tryXApkTask;

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (tryXApkTask.isCancelled()) progressDialog.dismiss();
        else if (tryXApkTask.getStatus() == AsyncTask.Status.FINISHED) progressDialog.dismiss();
        else {
            progressDialog.setProgress(tryXApkTask.getLoadedBytePercent());
            sendEmptyMessageDelayed(0, 100);
        }
    }
}