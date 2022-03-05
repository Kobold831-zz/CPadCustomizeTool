package com.saradabar.cpadcustomizetool.data.service;

import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DeviceOwnerService extends Service {

    protected IDeviceOwnerService.Stub mDeviceOwnerServiceStub = new IDeviceOwnerService.Stub() {

        @Override
        public boolean isDeviceOwnerApp() {
            DevicePolicyManager dPM = (DevicePolicyManager) getBaseContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                return dPM.isDeviceOwnerApp(getPackageName());
            } catch (SecurityException ignored) {
                return false;
            }
        }

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

        @Override
        public boolean installPackages(String str, List<Uri> uriList) {
            int sessionId;
            try {
                sessionId = createSession(getPackageManager().getPackageInstaller());
                if (sessionId < 0) {
                    getPackageManager().getPackageInstaller().abandonSession(sessionId);
                    return false;
                }
            } catch (IOException ignored) {
                return false;
            }
            for (Uri uri : uriList) {
                try {
                    writeSession(getPackageManager().getPackageInstaller(), sessionId, new File(Environment.getExternalStorageDirectory() + uri.getPath().replace("/external_files", "")));
                } catch (IOException ignored) {
                    getPackageManager().getPackageInstaller().abandonSession(sessionId);
                    return false;
                }
            }
            try {
                commitSession(getPackageManager().getPackageInstaller(), sessionId, getBaseContext());
            } catch (IOException ignored) {
                getPackageManager().getPackageInstaller().abandonSession(sessionId);
                return false;
            }
            return true;
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

    public static int createSession(PackageInstaller packageInstaller) throws IOException {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setInstallLocation(PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY);
        return packageInstaller.createSession(params);
    }

    public static int writeSession(PackageInstaller packageInstaller, int sessionId, File apkFile) throws IOException {
        long sizeBytes = -1;
        String apkPath = apkFile.getAbsolutePath();

        File file = new File(apkPath);
        if (file.isFile()) {
            sizeBytes = file.length();
        }

        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            session = packageInstaller.openSession(sessionId);
            in =
            in = new FileInputStream(apkPath);
            out = session.openWrite(getRandomString(), 0, sizeBytes);
            byte[] buffer = new byte[65536];
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            return 0;
        } finally {
            if (out != null) {
                out.close();
                in.close();
                session.close();
            }
        }
    }

    public static boolean commitSession(PackageInstaller packageInstaller, int sessionId, Context context) throws IOException {
        PackageInstaller.Session session = null;
        try {
            session = packageInstaller.openSession(sessionId);
            Intent intent = new Intent(context, DeviceOwnerService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            session.commit(pendingIntent.getIntentSender());
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public static String getRandomString() {
        String theAlphaNumericS;
        StringBuilder builder;
        theAlphaNumericS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        builder = new StringBuilder(5);
        for (int m = 0; m < 5; m++) {
            int myindex = (int) (theAlphaNumericS.length() * Math.random());
            builder.append(theAlphaNumericS.charAt(myindex));
        }
        return builder.toString();
    }
}