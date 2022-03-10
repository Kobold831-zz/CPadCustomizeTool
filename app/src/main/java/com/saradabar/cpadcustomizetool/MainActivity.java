package com.saradabar.cpadcustomizetool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.saradabar.cpadcustomizetool.data.connection.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.data.connection.Checker;
import com.saradabar.cpadcustomizetool.data.handler.ProgressHandler;
import com.saradabar.cpadcustomizetool.data.connection.Updater;
import com.saradabar.cpadcustomizetool.data.event.UpdateEventListener;
import com.saradabar.cpadcustomizetool.data.crash.CrashLogger;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;
import com.saradabar.cpadcustomizetool.util.Variables;
import com.saradabar.cpadcustomizetool.view.activity.StartActivity;
import com.saradabar.cpadcustomizetool.view.activity.WelAppActivity;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;
import java.util.Objects;

public class MainActivity extends Activity implements UpdateEventListener {

    ProgressDialog loadingDialog;
    boolean result = true;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(this));
        /* ネットワークチェック */
        if (!isNetWork()) {
            netWorkError();
            return;
        }

        /* アップデートチェックの可否を確認 */
        if (Preferences.GET_UPDATE_FLAG(this)) updateCheck();
        else supportCheck();
    }

    /* ネットワークの接続を確認 */
    private boolean isNetWork() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /* ネットワークエラー */
    private void netWorkError() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_wifi_error)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .setNeutralButton(R.string.dialog_common_continue, (dialog, which) -> {
                    result = false;
                    if (Preferences.GET_SETTINGS_FLAG(this)) {
                        if (checkModel()) checkDcha();
                        else errorNotTab2Or3();
                    } else new WelcomeHelper(this, WelAppActivity.class).forceShow();
                })
                .show();
    }

    private void updateCheck() {
        showLoadingDialog();
        new Updater(this, Constants.URL_UPDATE_CHECK, 1).updateCheck();
    }

    private void supportCheck() {
        if (!Preferences.GET_UPDATE_FLAG(this)) showLoadingDialog();
        new Checker(this, Constants.URL_SUPPORT_CHECK).supportCheck();
    }

    @Override
    public void onUpdateApkDownloadComplete() {
        new Handler().post(() -> new Updater(this, Constants.URL_UPDATE_CHECK, 1).installApk(this));
    }

    @Override
    public void onUpdateAvailable(String string) {
    }

    @Override
    public void onUpdateUnavailable() {
    }

    public void onSupportAvailable() {
        cancelLoadingDialog();
        showSupportDialog();
    }

    public void onSupportUnavailable() {
        cancelLoadingDialog();
        if (Preferences.GET_SETTINGS_FLAG(this)) {
            if (checkModel()) checkDcha();
            else errorNotTab2Or3();
        } else {
            new WelcomeHelper(this, WelAppActivity.class).forceShow();
        }
    }

    @Override
    public void onUpdateAvailable1(String string) {
        cancelLoadingDialog();
        showUpdateDialog(string);
    }

    @Override
    public void onUpdateUnavailable1() {
        supportCheck();
    }

    @Override
    public void onDownloadError() {
        cancelLoadingDialog();
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_error)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    @Override
    public void onConnectionError() {
        cancelLoadingDialog();
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_connection_error)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    private void showUpdateDialog(String string) {
        if (!Preferences.GET_SETTINGS_FLAG(this)) {
            switch (Build.MODEL) {
                case "TAB-A05-BD":
                case "TAB-A05-BA1":
                    Preferences.SET_MODEL_ID(2, this);
                    break;
            }
        }
        View view = getLayoutInflater().inflate(R.layout.view_update, null);
        TextView mTextView = view.findViewById(R.id.update_information);
        mTextView.setText(string);
        view.findViewById(R.id.update_info_button).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_UPDATE_INFO)).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
                Toast.toast(this, R.string.toast_unknown_activity);
            }
        });

        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    AsyncFileDownload asyncFileDownload = initFileLoader();
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(R.string.dialog_title_update);
                    progressDialog.setMessage("アップデートファイルをサーバーからダウンロード中・・・");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setProgress(0);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", (dialog2, which2) -> {
                        asyncFileDownload.cancel(true);
                        if (isNetWork()) {
                            showLoadingDialog();
                            supportCheck();
                        } else netWorkError();
                    });
                    progressDialog.show();
                    ProgressHandler progressHandler = new ProgressHandler();
                    progressHandler.progressDialog = progressDialog;
                    progressHandler.asyncfiledownload = asyncFileDownload;
                    progressHandler.sendEmptyMessage(0);
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                    if (isNetWork()) {
                        showLoadingDialog();
                        supportCheck();
                    } else netWorkError();
                })
                .show();
    }

    private AsyncFileDownload initFileLoader() {
        AsyncFileDownload asyncfiledownload = new AsyncFileDownload(this, Variables.DOWNLOAD_FILE_URL, new File(new File(getExternalCacheDir(), "update.apk").getPath()));
        asyncfiledownload.execute();
        return asyncfiledownload;
    }

    private void showSupportDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_not_use)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .setNeutralButton(R.string.dialog_common_continue, (dialog, which) -> {
                    result = false;
                    if (Preferences.GET_SETTINGS_FLAG(this)) {
                        if (checkModel()) checkDcha();
                        else errorNotTab2Or3();
                    } else new WelcomeHelper(this, WelAppActivity.class).forceShow();
                })
                .show();
    }

    private void showLoadingDialog() {
        loadingDialog = ProgressDialog.show(this, "", "通信中です・・・", true);
        loadingDialog.show();
    }

    private void cancelLoadingDialog() {
        try {
            if (loadingDialog != null) loadingDialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    /* 端末チェック */
    private boolean checkModel() {
        String[] modelName = {"TAB-A03-BS", "TAB-A03-BR", "TAB-A03-BR2", "TAB-A03-BR2B", "TAB-A03-BR3", "TAB-A04-BR3", "TAB-A05-BD", "TAB-A05-BA1"};
        for (String string : modelName) if (Objects.equals(string, Build.MODEL)) return true;
        return false;
    }

    /* 端末チェックエラー */
    private void errorNotTab2Or3() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setMessage(R.string.dialog_error_not_pad2)
                .setIcon(R.drawable.alert)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    /* DchaService動作チェック */
    private void checkDcha() {
        if (!Preferences.GET_DCHASERVICE_FLAG(this)) {
            switch (Build.MODEL) {
                case "TAB-A03-BR3":
                case "TAB-A04-BR3":
                    checkSettingsTab3();
                    break;
                case "TAB-A05-BD":
                case "TAB-A05-BA1":
                    checkSettingsTabNeo();
                    break;
                default:
                    checkSettingsTab2();
                    break;
            }
            return;
        }

        /* DchaServiceの使用可否を確認 */
        if (!bindDchaService()) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.dialog_title_common_error)
                    .setMessage(R.string.dialog_error_not_dchaservice)
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                    .setNeutralButton(R.string.dialog_common_continue, (dialogInterface, i) -> {
                        Preferences.SET_DCHASERVICE_FLAG(false, this);
                        switch (Build.MODEL) {
                            case "TAB-A03-BR3":
                            case "TAB-A04-BR3":
                                checkSettingsTab3();
                                break;
                            case "TAB-A05-BD":
                            case "TAB-A05-BA1":
                                checkSettingsTabNeo();
                                break;
                            default:
                                checkSettingsTab2();
                                break;
                        }
                    })
                    .show();
            return;
        }

        switch (Build.MODEL) {
            case "TAB-A03-BR3":
            case "TAB-A04-BR3":
                checkSettingsTab3();
                break;
            case "TAB-A05-BD":
            case "TAB-A05-BA1":
                checkSettingsTabNeo();
                break;
            default:
                checkSettingsTab2();
                break;
        }
    }

    /* Pad2起動設定チェック */
    private void checkSettingsTab2() {
        Preferences.SET_MODEL_ID(0, this);
        if (Preferences.GET_SETTINGS_FLAG(this)) {
            startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", result));
            overridePendingTransition(0, 0);
            finish();
        } else startCheck();
    }

    /* Pad3起動設定チェック */
    private void checkSettingsTab3() {
        Preferences.SET_MODEL_ID(1, this);
        if (Preferences.GET_SETTINGS_FLAG(this)) {
            if (permissionSettings()) {
                startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", result));
                overridePendingTransition(0, 0);
                finish();
            }
        } else startCheck();
    }

    /* PadNeo起動設定チェック */
    private void checkSettingsTabNeo() {
        Preferences.SET_MODEL_ID(2, this);
        if (Preferences.GET_SETTINGS_FLAG(this)) {
            if (permissionSettings()) {
                startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", result));
                overridePendingTransition(0, 0);
                finish();
            }
        } else startCheck();
    }

    /* 初回起動お知らせ */
    public void startCheck() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_notice_start)
                .setMessage(R.string.dialog_notice_start)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> {
                    if (permissionSettings()) {
                        Preferences.SET_SETTINGS_FLAG(true, this);
                        startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", result));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                })
                .show();
    }

    /* 権限設定 */
    private boolean permissionSettings() {
        if (checkWriteSystemSettings()) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.dialog_title_grant_permission)
                    .setMessage(R.string.dialog_no_permission)
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(R.string.dialog_common_settings, (dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getPackageName(), null)).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), Constants.REQUEST_PERMISSION);
                    })
                    .setNeutralButton(R.string.dialog_common_exit, (dialogInterface, i) -> finishAndRemoveTask())
                    .show();
            return false;
        } else {
            return true;
        }
    }

    /* システム設定変更権限チェック */
    private boolean checkWriteSystemSettings() {
        boolean canWrite = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            canWrite = Settings.System.canWrite(this);
        return !canWrite;
    }

    private boolean bindDchaService() {
        return bindService(new Intent(Constants.DCHA_SERVICE).setPackage(Constants.PACKAGE_DCHA_SERVICE), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                unbindService(this);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_UPDATE:
                if (isNetWork()) {
                    showLoadingDialog();
                    supportCheck();
                } else netWorkError();
                break;
            case WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST:
            case Constants.REQUEST_PERMISSION:
                if (checkModel()) checkDcha();
                else errorNotTab2Or3();
                break;
        }
    }
}