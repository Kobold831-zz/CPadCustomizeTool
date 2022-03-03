package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.Common.Variable.REQUEST_INSTALL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.Installer;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.StartActivity;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class DeviceOwnerFragment extends PreferenceFragment {

    String[] splitInstallData = new String[256];

    double totalByte;

    Preference preferenceBlockToUninstallSettings,
            preferenceDisableDeviceOwner,
            preferenceNowSetOwnerApp,
            preferenceOwnerSilentInstall;

    SwitchPreference switchPreferencePermissionForced;

    @SuppressLint("StaticFieldLeak")
    private static DeviceOwnerFragment instance = null;

    public static DeviceOwnerFragment getInstance() {
        return instance;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_device_owner, rootKey);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        instance = this;
        preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        preferenceDisableDeviceOwner = findPreference("disable_device_owner");
        switchPreferencePermissionForced = findPreference("permission_forced");
        preferenceNowSetOwnerApp = findPreference("now_set_owner_package");
        preferenceOwnerSilentInstall = findPreference("owner_silent_install");

        preferenceBlockToUninstallSettings.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent(getActivity(), BlockerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        switchPreferencePermissionForced.setOnPreferenceChangeListener((preference, o) -> {
            if ((boolean) o) {
                switchPreferencePermissionForced.setChecked(true);
                switchPreferencePermissionForced.setSummary("PERMISSION_POLICY_AUTO_GRANT");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    devicePolicyManager.setPermissionPolicy(new ComponentName(getActivity(), AdministratorReceiver.class), DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT);
            } else {
                switchPreferencePermissionForced.setChecked(false);
                switchPreferencePermissionForced.setSummary("PERMISSION_POLICY_PROMPT");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    devicePolicyManager.setPermissionPolicy(new ComponentName(getActivity(), AdministratorReceiver.class), DevicePolicyManager.PERMISSION_POLICY_PROMPT);
            }
            return true;
        });

        preferenceDisableDeviceOwner.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.dialog_question_device_owner)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        devicePolicyManager.clearDeviceOwnerApp(getActivity().getPackageName());
                        getActivity().finish();
                        getActivity().overridePendingTransition(0, 0);
                        startActivity(getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", true));
                    })
                    .setNegativeButton(R.string.dialog_common_no, null)
                    .show();
            return false;
        });

        preferenceOwnerSilentInstall.setOnPreferenceClickListener(preference -> {
            preferenceOwnerSilentInstall.setEnabled(false);
            try {
                startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/*"}).addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true), ""), REQUEST_INSTALL);
            } catch (ActivityNotFoundException ignored) {
                preferenceOwnerSilentInstall.setEnabled(true);
                new AlertDialog.Builder(getActivity())
                        .setMessage("ファイルブラウザがインストールされていません")
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
            return false;
        });

        if (getNowOwnerPackage() != null) {
            preferenceNowSetOwnerApp.setSummary("DeviceOwnerは" + getNowOwnerPackage() + "に設定されています");
        } else preferenceNowSetOwnerApp.setSummary("DeviceOwnerはデバイスに設定されていません");

        switch (Common.GET_MODEL_ID(getActivity())) {
            case 0:
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceOwnerSilentInstall.setEnabled(false);
                preferenceOwnerSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                setPreferenceSettings();
                break;
            case 1:
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceDisableDeviceOwner.setEnabled(false);
                preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceOwnerSilentInstall.setEnabled(false);
                preferenceOwnerSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                break;
            case 2:
                setPreferenceSettings();
                break;
        }
    }

    private void setPreferenceSettings() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!devicePolicyManager.isDeviceOwnerApp(getActivity().getPackageName())) {
            preferenceBlockToUninstallSettings.setEnabled(false);
            preferenceDisableDeviceOwner.setEnabled(false);
            switchPreferencePermissionForced.setEnabled(false);
            preferenceOwnerSilentInstall.setEnabled(false);
            preferenceBlockToUninstallSettings.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
            preferenceDisableDeviceOwner.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
            switchPreferencePermissionForced.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
            preferenceOwnerSilentInstall.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                switch (devicePolicyManager.getPermissionPolicy(new ComponentName(getActivity(), AdministratorReceiver.class))) {
                    case 0:
                        switchPreferencePermissionForced.setChecked(false);
                        switchPreferencePermissionForced.setSummary("PERMISSION_POLICY_PROMPT");
                        break;
                    case 1:
                        switchPreferencePermissionForced.setChecked(true);
                        switchPreferencePermissionForced.setSummary("PERMISSION_POLICY_AUTO_GRANT");
                        break;
                }
            }
        }
    }

    private String getNowOwnerPackage() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        for (ApplicationInfo app : getActivity().getPackageManager().getInstalledApplications(0)) {
            /* ユーザーアプリか確認 */
            if (app.sourceDir.startsWith("/data/app/")) {
                if (devicePolicyManager.isDeviceOwnerApp(app.packageName)) {
                    return app.loadLabel(getActivity().getPackageManager()).toString();
                }
            }
        }
        return null;
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        switch (Common.GET_MODEL_ID(getActivity())) {
            case 0:
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceOwnerSilentInstall.setEnabled(false);
                preferenceOwnerSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                setPreferenceSettings();
                break;
            case 1:
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary("DeviceOwnerではないためこの機能は使用できません\nこの機能を使用するにはADBでDeviceOwnerを許可してください");
                preferenceBlockToUninstallSettings.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceDisableDeviceOwner.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceBlockToUninstallSettings.setEnabled(false);
                preferenceDisableDeviceOwner.setEnabled(false);
                preferenceOwnerSilentInstall.setEnabled(false);
                preferenceOwnerSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                break;
            case 2:
                setPreferenceSettings();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INSTALL) {
            preferenceOwnerSilentInstall.setEnabled(true);
            if (selectInstallFiles(data)) {
                String str = new File(splitInstallData[0]).getName();
                /* ファイルの拡張子 */
                switch (str.substring(str.lastIndexOf("."))) {
                    case ".apk":
                        DeviceOwnerFragment.OwnerInstallTask ownerInstallTask = new DeviceOwnerFragment.OwnerInstallTask();
                        ownerInstallTask.setListener(StartActivity.getInstance().OwnerInstallCreateListener());
                        ownerInstallTask.execute();
                        return;
                    case ".XAPK":
                    case ".xapk":
                        try {
                            /* 一時ファイルを消去 */
                            FileUtils.deleteDirectory(DeviceOwnerFragment.getInstance().getActivity().getExternalFilesDir(null));
                        } catch (IOException ignored) {
                            break;
                        }
                        TryXApkTask tryXApkTask = new TryXApkTask();
                        tryXApkTask.setListener(StartActivity.getInstance().XApkListener());
                        tryXApkTask.execute();
                        return;
                }
            }
            new AlertDialog.Builder(getActivity())
                    .setMessage("ファイルデータを取得できませんでした")
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    /* インストールファイルの取得と解凍 */
    private boolean selectInstallFiles(Intent data) {
        try {
            ClipData clipData = data.getClipData();
            if (clipData == null) {
                /* シングルApk */
                /* 選択されたファイルを取得 */
                splitInstallData[0] = getInstallData(getActivity(), data.getData());
                if (splitInstallData[0] != null) {
                    String str = new File(splitInstallData[0]).getName();
                    /* ファイルの拡張子 */
                    switch (str.substring(str.lastIndexOf("."))) {
                        case ".apk":
                        case ".XAPK":
                        case ".xapk":
                            break;
                        default:
                            /* 未対応またはインストールファイルでないなら終了 */
                            return false;
                    }
                } else {
                    return false;
                }
            } else {
                /* マルチApk */
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    /* 処理 */
                    splitInstallData[i] = getInstallData(getActivity(), clipData.getItemAt(i).getUri());
                }
            }
            return splitInstallData != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    /* 選択したファイルデータを取得 */
    private String getInstallData(Context context, Uri uri) {
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String[] str = DocumentsContract.getDocumentId(uri).split(":");
                switch (uri.getAuthority()) {
                    case "com.android.externalstorage.documents":
                        return Environment.getExternalStorageDirectory() + "/" + str[1];
                    case "com.android.providers.downloads.documents":
                        return str[1];
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressLint("NewApi")
    private double getDirectorySize(File file) {
        double fileSize = 0;
        File[] list = file.listFiles();
        try {
            for (File value : list != null ? list : new File[0]) {
                if (!value.isDirectory()) {
                    fileSize += Files.size(Paths.get(value.getPath()));
                } else {
                    File[] obbName = new File(value.getPath() + "/obb").listFiles();
                    File[] obbFile = obbName != null ? obbName[0].listFiles() : new File[0];
                    fileSize += Files.size(Paths.get(obbFile != null ? obbFile[0].getPath() : null));
                }
            }
        } catch (IOException ignored) {
        }
        return fileSize;
    }

    /* インストールタスク */
    public static class TryXApkTask extends AsyncTask<Object, Void, Object> {

        public static TryXApkTask.Listener mListener;

        public static String obbPath1;
        public static String obbPath2;

        @Override
        protected void onPreExecute() {
            mListener.onShow();
        }

        @Override
        protected Object doInBackground(Object... value) {
            String str = new File(DeviceOwnerFragment.getInstance().splitInstallData[0]).getParent() + File.separator + new File(DeviceOwnerFragment.getInstance().splitInstallData[0]).getName().replaceFirst("\\..*", ".zip");
            /* 拡張子.xapkを.zipに変更 */
            onProgressUpdate("拡張子を変更しています・・・");
            if (new File(DeviceOwnerFragment.getInstance().splitInstallData[0]).renameTo(new File(str))) {
                File file = new File(DeviceOwnerFragment.getInstance().getActivity().getExternalFilesDir(null) + "/temp");
                /* zipを解凍して外部ディレクトリに一時保存 */
                onProgressUpdate("圧縮ファイルを解凍しています・・・");
                getInstance().totalByte = new File(str).length();
                ZipUtil.unpack(new File(str), file);
                File[] list = file.listFiles();
                if (list != null) {
                    int c = 0;
                    /* ディレクトリのなかのファイルを取得 */
                    for (int i = 0; i < list.length; i++) {
                        /* obbデータを取得 */
                        if (list[i].isDirectory()) {
                            c++;
                            try {
                                /* obbデータをコピー */
                                onProgressUpdate("obbデータをコピーしています・・・");
                                File[] obbName = new File(list[i].getPath() + "/obb").listFiles();
                                File[] obbFile = Objects.requireNonNull(obbName)[0].listFiles();
                                //getInstance().totalByte = Objects.requireNonNull(obbFile)[0].length();
                                getInstance().totalByte = 1733650831.00;
                                obbPath1 = obbName[0].getName();
                                Log.i("TAG", obbPath1);
                                obbPath2 = Objects.requireNonNull(obbFile)[0].getName();
                                Log.i("TAG", obbPath2);
                                FileUtils.copyDirectory(new File(list[i].getPath() + "/obb/"), new File(Environment.getExternalStorageDirectory() + "/Android/obb"));
                            } catch (IOException ignored) {
                                return false;
                            }
                        } else {
                            onProgressUpdate("ファイルを確認しています・・・");
                            str = list[i].getName();
                            /* apkファイルならパスをインストールデータへ */
                            if (str.substring(str.lastIndexOf(".")).equalsIgnoreCase(".apk")) {
                                DeviceOwnerFragment.getInstance().splitInstallData[i - c] = list[i].getPath();
                            } else {
                                /* apkファイルでなかったときのリストの順番を修正 */
                                c++;
                            }
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        private void onProgressUpdate(String str) {
            mListener.onProgressUpdate(str);
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result == null) {
                mListener.onError("エラー\n不明なエラーが発生しました");
                return;
            }
            if (result.equals(true)) {
                mListener.onSuccess();
                return;
            }
            if (result.equals(false)) {
                mListener.onFailure();
                return;
            }
            mListener.onError(result.toString());
        }

        public void setListener(TryXApkTask.Listener listener) {
            mListener = listener;
        }

        /* StartActivity */
        public interface Listener {
            void onShow();

            void onSuccess();

            void onFailure();

            void onError(String str);

            void onProgressUpdate(String str);
        }

        @SuppressLint("NewApi")
        public int getLoadedBytePercent() {
            double fileSize = 0;
            if (getInstance().totalByte <= 0) return 0;
            if (obbPath1 == null) {
                fileSize = getInstance().getDirectorySize(new File(DeviceOwnerFragment.getInstance().getActivity().getExternalFilesDir(null) + "/temp"));
            } else {
                try {
                    fileSize = Files.size(Paths.get(Environment.getExternalStorageDirectory() + "/Android/obb/" + obbPath1 + "/" + obbPath2));
                } catch (IOException ignored) {
                }
            }
            return (int) Math.floor(100 * fileSize / DeviceOwnerFragment.getInstance().totalByte);
        }
    }

    /* インストールタスク */
    public static class OwnerInstallTask extends AsyncTask<Object, Void, Object> {
        public static DeviceOwnerFragment.OwnerInstallTask.Listener mListener;

        @Override
        protected void onPreExecute() {
            mListener.onShow();
        }

        @Override
        protected Object doInBackground(Object... value) {
            Installer installer = new Installer();
            int sessionId;
            try {
                sessionId = installer.splitCreateSession(DeviceOwnerFragment.getInstance().getActivity()).i;
                if (sessionId < 0) {
                    return false;
                }
            } catch (Exception e) {
                return e.getMessage();
            }
            /* インストールデータの長さ回数繰り返す */
            for (String str : DeviceOwnerFragment.getInstance().splitInstallData) {
                /* 配列の中身を確認 */
                if (str != null) {
                    try {
                        if (!installer.splitWriteSession(DeviceOwnerFragment.getInstance().getActivity(), new File(str), sessionId).bl) {
                            return false;
                        }
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                } else {
                    /* つぎの配列がnullなら終了 */
                    break;
                }
            }
            try {
                return installer.splitCommitSession(DeviceOwnerFragment.getInstance().getActivity(), PendingIntent.getActivity(DeviceOwnerFragment.getInstance().getActivity(), 0, new Intent("TEST"), 0), sessionId).bl;
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result == null) {
                mListener.onError("エラー\n不明なエラーが発生しました");
                return;
            }
            if (result.equals(true)) {
                return;
            }
            if (result.equals(false)) {
                mListener.onFailure();
                return;
            }
            mListener.onError(result.toString());
        }

        public void setListener(DeviceOwnerFragment.OwnerInstallTask.Listener listener) {
            mListener = listener;
        }

        /* StartActivity */
        public interface Listener {
            void onShow();

            void onSuccess();

            void onFailure();

            void onError(String str);
        }
    }
}