package com.saradabar.cpadcustomizetool.data.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.StartActivity;
import com.saradabar.cpadcustomizetool.data.event.InstallEventListener;
import com.saradabar.cpadcustomizetool.data.event.InstallEventListenerList;

public class InstallService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        postStatus(intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1), intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME), intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
        stopSelf();
        return START_NOT_STICKY;
    }

    private void postStatus(int status, String packageName, String extra) {
        InstallEventListenerList installEventListener = new InstallEventListenerList();
        installEventListener.addEventListener((InstallEventListener) StartActivity.getInstance());
        switch (status) {
            case PackageInstaller.STATUS_SUCCESS:
                installEventListener.installSuccessNotify();
                break;
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                installEventListener.installFailureNotify(getErrorMessage(this, status));
                break;
            default:
                installEventListener.installErrorNotify(getErrorMessage(this, status));
                break;
        }
    }

    private String getErrorMessage(Context context, int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                return context.getString(R.string.installer_status_user_action);
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return context.getString(R.string.installer_status_failure_blocked);
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return context.getString(R.string.installer_status_failure_conflict);
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                return context.getString(R.string.installer_status_failure_incompatible);
            case PackageInstaller.STATUS_FAILURE_INVALID:
                return context.getString(R.string.installer_status_failure_invalid);
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return context.getString(R.string.installer_status_failure_storage);
            default:
                return context.getString(R.string.installer_status_failure);
        }
    }
}