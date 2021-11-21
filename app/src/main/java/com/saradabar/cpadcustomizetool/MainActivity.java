package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.Common.*;
import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.check.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.check.Checker;
import com.saradabar.cpadcustomizetool.check.ProgressHandler;
import com.saradabar.cpadcustomizetool.check.Updater;
import com.saradabar.cpadcustomizetool.check.event.UpdateEventListener;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;

public class MainActivity extends Activity implements UpdateEventListener {

    private Handler handler;
    private WelcomeHelper welcomeScreen;
    private Updater updater;
    private ProgressHandler progressHandler;
    private AsyncFileDownload asyncfiledownload;
    private ProgressDialog progressDialog, loadingDialog;

    private static boolean bindDchaService(Context context, ServiceConnection dchaServiceConnection) {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        return context.bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* 接続 */
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unbindService(this);
        }
    };

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        START_FLAG = 0;
        USE_FLAG = 0;
        handler = new Handler();
        /* ネットワークチェック */
        if (isNetWork()) {
            /* アップデートチェックの可否を確認 */
            if (GET_UPDATE_FLAG(this) == 0) {
                autoUpdateCheck();
            } else {
                checkSupport();
            }
        } else {
            netWorkError();
        }
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
                    Common.Variable.USE_FLAG = 1;
                    if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
                        welcomeScreen = new WelcomeHelper(this, WelAppActivity.class);
                        welcomeScreen.forceShow();
                    } else {
                        checkModel();
                    }
                })
                .show();
    }

    private void autoUpdateCheck() {
        updater = new Updater(this, UPDATE_CHECK_URL, 1);
        updater.updateCheck();
        showLoadingDialog(1);
    }

    private void checkSupport() {
        showLoadingDialog(2);
        Checker checker = new Checker(this, SUPPORT_CHECK_URL);
        checker.supportCheck();
    }

    @Override
    public void onUpdateApkDownloadComplete() {
        handler.post(() -> updater.installApk(this));
    }

    @Override
    public void onUpdateAvailable(String mString) {
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
        if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            welcomeScreen = new WelcomeHelper(
                    this, WelAppActivity.class);
            welcomeScreen.forceShow();
        } else {
            checkModel();
        }
    }

    @Override
    public void onUpdateAvailable1(String mString) {
        cancelLoadingDialog();
        showUpdateDialog(mString);
    }

    @Override
    public void onUpdateUnavailable1() {
        checkSupport();
    }

    @Override
    public void onDownloadError() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_error)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onConnectionError() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_connection_error)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finish())
                .show();
    }

    private void showUpdateDialog(String mString) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.update_dialog, null);
        TextView mTextView = view.findViewById(R.id.update_information);
        mTextView.setText(mString);
        Button button = view.findViewById(R.id.update_info_button);
        button.setOnClickListener(v -> {
            Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_INFO_URL));
            try {
                startActivity(mIntent);
            } catch (ActivityNotFoundException ignored) {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    if (GET_MODEL_NAME(this) != 2) {
                        initFileLoader();
                        showDialog(0);
                        progressHandler = new ProgressHandler();
                        progressHandler.progressDialog = progressDialog;
                        progressHandler.asyncfiledownload = asyncfiledownload;

                        if (progressDialog != null && asyncfiledownload != null) {
                            progressDialog.setProgress(0);
                            progressHandler.sendEmptyMessage(0);
                        } else {
                            if (asyncfiledownload != null) asyncfiledownload.cancel(true);
                            if (progressDialog != null) progressDialog.dismiss();
                            onDownloadError();
                        }
                    } else {
                        Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL));
                        try {
                            startActivity(mIntent);
                        } catch (ActivityNotFoundException ignored) {
                            if (toast != null) {
                                toast.cancel();
                            }
                            toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                            toast.show();
                            if (isNetWork()) {
                                showLoadingDialog(1);
                                checkSupport();
                            } else {
                                netWorkError();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                    if (isNetWork()) {
                        showLoadingDialog(1);
                        checkSupport();
                    } else {
                        netWorkError();
                    }
                })
                .show();
    }

    private void initFileLoader() {
        File mFile = new File(new File(getExternalCacheDir(), "update.apk").getPath());
        asyncfiledownload = new AsyncFileDownload(this, Common.Variable.DOWNLOAD_FILE_URL, mFile);
        asyncfiledownload.execute();
    }

    private void cancelDownload() {
        if (asyncfiledownload != null) asyncfiledownload.cancel(true);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.dialog_title_update);
            progressDialog.setMessage("アップデートファイルをサーバーからダウンロード中・・・");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", (dialog, which) -> {
                cancelDownload();
                if (isNetWork()) {
                    showLoadingDialog(1);
                    checkSupport();
                } else {
                    netWorkError();
                }
            });
        }
        return progressDialog;
    }

    private void showSupportDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_not_use)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .setNeutralButton(R.string.dialog_common_continue, (dialog, which) -> {
                    Common.Variable.USE_FLAG = 1;
                    if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
                        welcomeScreen = new WelcomeHelper(
                                MainActivity.this, WelAppActivity.class);
                        welcomeScreen.forceShow();
                    } else {
                        checkModel();
                    }
                })
                .show();
    }

    private void showLoadingDialog(int code) {
        if (code == 1) {
            loadingDialog = ProgressDialog.show(this, "", "通信中です・・・", true);
        } else if (GET_UPDATE_FLAG(this) == 1 && code == 2) {
            loadingDialog = ProgressDialog.show(this, "", "通信中です・・・", true);
        }
    }

    private void cancelLoadingDialog() {
        try {
            if (loadingDialog != null) loadingDialog.cancel();
        } catch (Exception ignored) {
        }
    }

    /* 端末チェック */
    public void checkModel() {
        switch (Build.MODEL) {
            case "TAB-A03-BS":
            case "TAB-A03-BR":
            case "TAB-A03-BR2":
            case "TAB-A03-BR2B":
            case "TAB-A03-BR3":
            case "TAB-A04-BR3":
            case "TAB-A05-BD":
                checkDcha();
                break;
            default:
                errorNotTab2Or3();
                break;
        }
    }

    /* 端末チェックエラー */
    private void errorNotTab2Or3() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setMessage(R.string.dialog_error_not_pad2)
                .setIcon(R.drawable.alert)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    /* DchaService動作チェック */
    private void checkDcha() {
        /* DchaServiceの使用可否を確認 */
        if (GET_DCHASERVICE_FLAG(this) == USE_DCHASERVICE) {
            if (!bindDchaService(this, dchaServiceConnection)) {
                AlertDialog.Builder d = new AlertDialog.Builder(this);
                d.setCancelable(false)
                        .setTitle(R.string.dialog_title_common_error)
                        .setMessage(R.string.dialog_error_not_dchaservice)
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                        .setNeutralButton(R.string.dialog_common_continue, (dialogInterface, i) -> {
                            SET_DCHASERVICE_FLAG(USE_NOT_DCHASERVICE, this);
                            switch (Build.MODEL) {
                                case "TAB-A03-BR3":
                                case "TAB-A04-BR3":
                                    checkSettingsTab3();
                                    break;
                                case "TAB-A05-BD":
                                    checkSettingsTabNeo();
                                    break;
                                default:
                                    checkSettingsTab2();
                                    break;
                            }
                        })
                        .show();
            } else {
                switch (Build.MODEL) {
                    case "TAB-A03-BR3":
                    case "TAB-A04-BR3":
                        checkSettingsTab3();
                        break;
                    case "TAB-A05-BD":
                        checkSettingsTabNeo();
                        break;
                    default:
                        checkSettingsTab2();
                        break;
                }
            }
        } else {
            switch (Build.MODEL) {
                case "TAB-A03-BR3":
                case "TAB-A04-BR3":
                    checkSettingsTab3();
                    break;
                case "TAB-A05-BD":
                    checkSettingsTabNeo();
                    break;
                default:
                    checkSettingsTab2();
                    break;
            }
        }
    }

    /* Pad2起動設定チェック */
    private void checkSettingsTab2() {
        SET_MODEL_NAME(0, this);
        if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            startCheck();
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* Pad3起動設定チェック */
    private void checkSettingsTab3() {
        SET_MODEL_NAME(1, this);
        if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            startCheck();
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* PadNeo起動設定チェック */
    private void checkSettingsTabNeo() {
        SET_MODEL_NAME(2, this);
        if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            startCheck();
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* 初回起動お知らせ */
    public void startCheck() {
        SET_CHANGE_SETTINGS_DCHA_FLAG(0, this);
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setCancelable(false)
                .setTitle(R.string.dialog_title_notice_start)
                .setMessage(R.string.dialog_notice_start)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> permissionSettings()).show();
    }

    /* システム設定変更権限チェック */
    private boolean checkWriteSystemSettings() {
        boolean canWrite = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canWrite = Settings.System.canWrite(this);
        }
        return !canWrite;
    }

    /* 権限設定 */
    public void permissionSettings() {
        Intent intent = new Intent(this, StartActivity.class);
        switch (GET_MODEL_NAME(this)) {
            case 0:
                SET_SETTINGS_FLAG(SETTINGS_COMPLETED, this);
                startActivity(intent);
                finish();
                break;
            case 1:
            case 2:
                if (checkWriteSystemSettings()) {
                    AlertDialog.Builder d = new AlertDialog.Builder(this);
                    d.setCancelable(false)
                            .setTitle(R.string.dialog_title_grant_permission)
                            .setMessage(R.string.dialog_no_permission)
                            .setIcon(R.drawable.alert)
                            .setPositiveButton(R.string.dialog_common_settings, (dialog, which) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
                                }
                            })
                            .setNeutralButton(R.string.dialog_common_exit, (dialogInterface, i) -> finishAndRemoveTask()).show();
                } else {
                    SET_SETTINGS_FLAG(SETTINGS_COMPLETED, this);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE) {
            if (isNetWork()) {
                showLoadingDialog(1);
                checkSupport();
            } else {
                netWorkError();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(dchaServiceConnection);
        } catch (IllegalArgumentException ignored) {
        }
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        if (Common.Variable.START_FLAG == 1) {
            checkModel();
        }
    }
}