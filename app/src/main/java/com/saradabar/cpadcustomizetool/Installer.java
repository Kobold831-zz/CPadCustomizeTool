package com.saradabar.cpadcustomizetool;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;

import com.saradabar.cpadcustomizetool.data.service.DeviceOwnerService;
import com.saradabar.cpadcustomizetool.data.service.InstallService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Installer {

    public static class Result {
        public final boolean bl;
        public final String errMsg;

        public Result(boolean result, String errMsg, int i1, int i2) {
            this.bl = result;
            this.errMsg = errMsg;
        }
    }

    public static class SessionId {
        public final boolean bl;
        public final int i;

        public SessionId(boolean result, int i) {
            this.bl = result;
            this.i = i;
        }
    }

    public SessionId splitCreateSession(Context context) throws Exception {
        int sessionId = createSession(context.getPackageManager().getPackageInstaller());
        if (sessionId < 0) {
            return new SessionId(false, -1);
        }
        return new SessionId(true, sessionId);
    }

    public Result splitWriteSession(Context context, File apkFile, int sessionId) throws Exception {
        return new Result(true, "", writeSession(context.getPackageManager().getPackageInstaller(), sessionId, apkFile), 0);
    }

    public Result splitCommitSession(Context context, int sessionId) {
        return new Result(commitSession(context.getPackageManager().getPackageInstaller(), sessionId, context), "", 0, 0);
    }

    private int createSession(PackageInstaller packageInstaller) throws IOException {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setInstallLocation(PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY);
        return packageInstaller.createSession(params);
    }

    private int writeSession(PackageInstaller packageInstaller, int sessionId, File apkFile) throws IOException {
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

    private boolean commitSession(PackageInstaller packageInstaller, int sessionId, Context context) {
        PackageInstaller.Session session = null;
        try {
            session = packageInstaller.openSession(sessionId);
            Intent intent = new Intent(context, InstallService.class).putExtra("isAuroraInstall", false);
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

    private String getRandomString() {
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