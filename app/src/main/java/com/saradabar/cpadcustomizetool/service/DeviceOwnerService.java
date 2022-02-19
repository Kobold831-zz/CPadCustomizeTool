package com.saradabar.cpadcustomizetool.service;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;

public class DeviceOwnerService extends Service {

    protected IDeviceOwnerService.Stub mDeviceOwnerServiceStub = new IDeviceOwnerService.Stub() {
        @Override
        public void setUninstallBlocked(String str, boolean bl) {
            DevicePolicyManager dPM = (DevicePolicyManager) getBaseContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            dPM.setUninstallBlocked(new ComponentName(getApplicationContext(), AdministratorReceiver.class), str, bl);
        }

        @Override
        public boolean isUninstallBlocked(String str) {
            DevicePolicyManager dPM = (DevicePolicyManager) getBaseContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            return dPM.isUninstallBlocked(new ComponentName(getApplicationContext(), AdministratorReceiver.class), str);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mDeviceOwnerServiceStub;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}