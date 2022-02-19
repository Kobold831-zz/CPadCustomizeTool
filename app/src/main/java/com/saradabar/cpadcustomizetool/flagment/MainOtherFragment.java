package com.saradabar.cpadcustomizetool.flagment;

import static com.saradabar.cpadcustomizetool.Common.Variable.REQUEST_INSTALL;
import static com.saradabar.cpadcustomizetool.Common.Variable.toast;

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
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.Installer;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.StartActivity;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

import java.io.File;

public class MainOtherFragment extends PreferenceFragment {

    String installData;
    String[] splitInstallData = new String[256];

    int count;

    Preference preferenceOtherSettings,
            preferenceSysUiAdjustment,
            preferenceBlockToUninstallSettings,
            preferenceDisableDeviceOwner,
            preferenceDevelopmentSettings,
            preferenceNowSetOwnerApp,
            preferenceOwnerSilentInstall;

    SwitchPreference switchPreferencePermissionForced;

    @SuppressLint("StaticFieldLeak")
    private static MainOtherFragment instance = null;

    public static MainOtherFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_main_setting, rootKey);

        instance = this;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

        preferenceOtherSettings = findPreference("intent_android_settings");
        preferenceSysUiAdjustment = findPreference("intent_sys_ui_adjustment");
        preferenceBlockToUninstallSettings = findPreference("intent_block_to_uninstall_settings");
        preferenceDisableDeviceOwner = findPreference("disable_device_owner");
        preferenceDevelopmentSettings = findPreference("intent_development_settings");
        switchPreferencePermissionForced = findPreference("permission_forced");
        preferenceNowSetOwnerApp = findPreference("now_set_owner_package");
        preferenceOwnerSilentInstall = findPreference("owner_silent_install");


        preferenceOtherSettings.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent().setClassName("com.android.settings", "com.android.settings.Settings").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preferenceDevelopmentSettings.setOnPreferenceClickListener(preference -> {
            if (Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1) {
                try {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                } catch (ActivityNotFoundException ignored) {
                }
            } else {
                if (toast != null) toast.cancel();
                toast = Toast.makeText(getActivity(), R.string.toast_no_development_option, Toast.LENGTH_SHORT);
                toast.show();
            }
            return false;
        });

        preferenceSysUiAdjustment.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent().setClassName("com.android.systemui", "com.android.systemui.DemoMode").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

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
                startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/vnd.android.package-archive").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true), ""), REQUEST_INSTALL);
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
                preferenceSysUiAdjustment.setEnabled(false);
                preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
                switchPreferencePermissionForced.setEnabled(false);
                switchPreferencePermissionForced.setSummary(Build.MODEL + "ではこの機能は使用できません");
                preferenceOwnerSilentInstall.setEnabled(false);
                preferenceOwnerSilentInstall.setSummary(Build.MODEL + "ではこの機能は使用できません");
                setPreferenceSettings();
                break;
            case 1:
                preferenceSysUiAdjustment.setEnabled(false);
                preferenceSysUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
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
            ClipData clipData = null;
            installData = null;
            try {
                clipData = data.getClipData();
            } catch (NullPointerException ignored) {
            }
            if (clipData == null) {
                /* シングルApk */
                count = -1;
                try {
                    installData = getInstallData(getActivity(), data.getData());
                } catch (NullPointerException ignored) {
                }
            } else {
                /* マルチApk */
                int i;
                for (i = 0; i < clipData.getItemCount(); i++) {
                    /* 処理 */
                    splitInstallData[i] = getInstallData(getActivity(), clipData.getItemAt(i).getUri());
                }
                count = i;
            }
            if (clipData == null) {
                if (installData != null) {
                    MainOtherFragment.OwnerInstallTask ownerInstallTask = new MainOtherFragment.OwnerInstallTask();
                    ownerInstallTask.setListener(StartActivity.getInstance().OwnerInstallCreateListener());
                    ownerInstallTask.execute();
                    return;
                }
            } else {
                if (installData == null) {
                    MainOtherFragment.OwnerInstallTask ownerInstallTask = new MainOtherFragment.OwnerInstallTask();
                    ownerInstallTask.setListener(StartActivity.getInstance().OwnerInstallCreateListener());
                    ownerInstallTask.execute();
                    return;
                }
            }
            new AlertDialog.Builder(getActivity())
                    .setMessage("ファイルデータを取得できませんでした\n内部ストレージからファイルを選択してください")
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
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

    /* インストールタスク */
    public static class OwnerInstallTask extends AsyncTask<Object, Void, Object> {
        public static MainOtherFragment.OwnerInstallTask.Listener mListener;

        @Override
        protected void onPreExecute() {
            mListener.onShow();
        }

        @Override
        protected Object doInBackground(Object... value) {
            if (MainOtherFragment.getInstance().count == -1) {
                try {
                    return new Installer().installApk(MainOtherFragment.getInstance().getActivity(), new File(MainOtherFragment.getInstance().installData), PendingIntent.getActivity(MainOtherFragment.getInstance().getActivity().getApplicationContext(), 0, new Intent("TEST"), 0)).bl;
                } catch (Exception e) {
                    return e.getMessage();
                }
            } else {
                int sessionId;
                Installer installer = new Installer();
                try {
                    sessionId = installer.splitCreateSession(MainOtherFragment.getInstance().getActivity()).i;
                    if (sessionId < 0) {
                        return false;
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
                for (int i = 0; i < MainOtherFragment.getInstance().count; i++) {
                    try {
                        if (!installer.splitWriteSession(MainOtherFragment.getInstance().getActivity(), new File(MainOtherFragment.getInstance().splitInstallData[i]), sessionId).bl) {
                            return false;
                        }
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                }
                try {
                    return installer.splitCommitSession(MainOtherFragment.getInstance().getActivity(), PendingIntent.getActivity(MainOtherFragment.getInstance().getActivity(), 0, new Intent("TEST"), 0), sessionId).bl;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result.equals(true)) {
                return;
            }
            if (result.equals(false)) {
                mListener.onFailure();
                return;
            }
            mListener.onError(result.toString());
        }

        public void setListener(MainOtherFragment.OwnerInstallTask.Listener listener) {
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