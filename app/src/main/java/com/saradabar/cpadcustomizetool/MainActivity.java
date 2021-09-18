package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.SETTINGS_COMPLETED;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.SETTINGS_NOT_COMPLETED;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.SUPPORT_CHECK_URL;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.UPDATE_CHECK_URL;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_NOT_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.toast;
import static com.saradabar.cpadcustomizetool.common.Common.GET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.GET_MODEL_NAME;
import static com.saradabar.cpadcustomizetool.common.Common.GET_SETTINGS_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.GET_UPDATE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.SET_CHANGE_SETTINGS_DCHA_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.SET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.common.Common.SET_MODEL_NAME;
import static com.saradabar.cpadcustomizetool.common.Common.SET_SETTINGS_FLAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.common.Common;
import com.saradabar.cpadcustomizetool.menu.check.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.menu.check.Checker;
import com.saradabar.cpadcustomizetool.menu.check.ProgressHandler;
import com.saradabar.cpadcustomizetool.menu.check.Updater;
import com.saradabar.cpadcustomizetool.menu.check.event.UpdateEventListener;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;

public class MainActivity extends Activity implements UpdateEventListener {

    private Handler handler;
    private WelcomeHelper welcomeScreen;
    private Updater updater;
    private ProgressHandler progressHandler;
    private AsyncFileDownload asyncfiledownload;
    private ProgressDialog progress, loading;

    private static boolean bindDchaService(Context context, ServiceConnection dchaServiceConnection) {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        return context.bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* 接続 */
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.Variable.START_FLAG = 0;
        Common.Variable.USE_FLAG = 0;
        handler = new Handler();
        /* ネットワークチェック */
        if (isNetWork()) {
            /* アップデートチェックの可否を確認 */
            if (GET_UPDATE_FLAG(this) == 0) {
                autoUpdateCheck();
            }else {
                checkSupport();
            }
        } else {
            netWorkError();
        }
    }

    /* ネットワークの接続を確認 */
    private boolean isNetWork() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /* ネットワークエラー */
    private void netWorkError() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("通信エラーが発生しました。\nネットワークに接続してください。\n続行する場合機能が制限されます。")
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
        showLoadingDialog_Update();
    }

    private void checkSupport() {
        showLoadingDialog_Xml();
        Checker checker = new Checker(this, SUPPORT_CHECK_URL);
        checker.supportCheck();
    }

    @Override
    public void onUpdateApkDownloadComplete() {
        handler.post(() -> updater.installApk(this));
    }

    @Override
    public void onUpdateAvailable(String d) {
    }

    @Override
    public void onUpdateUnavailable() {

    }

    public void onSupportAvailable() {
        cancelLoadingDialog_Xml();
        showSupportDialog();
    }

    public void onSupportUnavailable() {
        cancelLoadingDialog_Xml();
        if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            welcomeScreen = new WelcomeHelper(
                    this, WelAppActivity.class);
            welcomeScreen.forceShow();
        } else {
            checkModel();
        }
    }

    @Override
    public void onUpdateAvailable1(String d) {
        cancelLoadingDialog_Update();
        showUpdateDialog(d);
    }

    @Override
    public void onUpdateUnavailable1() {
        cancelLoadingDialog_Update();
        checkSupport();
    }

    @Override
    public void onDownloadError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("通信エラーが発生しました。\nネットワークに接続してください。")
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finish())
                .show();
    }

    private void showUpdateDialog(String d) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("アップデートがあります。アップデートしますか？\nアップデートをすることを推奨します。\n\n更新情報：\n" + d)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    progressHandler = new ProgressHandler();
                    initFileLoader();
                    showDialog(0);
                    progressHandler.progressDialog = progress;
                    progressHandler.asyncfiledownload = asyncfiledownload;

                    if (progress != null && asyncfiledownload != null) {
                        progress.setProgress(0);
                        progressHandler.sendEmptyMessage(0);
                    }
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                    if (isNetWork()) {
                        checkSupport();
                    } else {
                        netWorkError();
                    }
                })
                .show();
    }

    private void initFileLoader() {
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/UpdateFolder");
        if (!directory.exists()) {
            directory.mkdir();
        }
        File outputFile = new File(directory, "UpdateFile.apk");
        asyncfiledownload = new AsyncFileDownload(this, Common.Variable.DOWNLOAD_FILE_URL, outputFile);
        asyncfiledownload.execute();
    }

    private void cancelLoad()
    {
        if(asyncfiledownload != null){
            asyncfiledownload.cancel(true);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if (id == 0) {
            progress = new ProgressDialog(this);
            progress.setTitle("アプリの更新");
            progress.setMessage("アップデートファイルをサーバーからダウンロード中...");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", (dialog, which) -> {
                cancelLoad();
                checkSupport();
            });
        }
        return progress;
    }

    private void showSupportDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("このアプリは現在使用できません。\n続行する場合機能が制限されます。")
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

    private void showLoadingDialog_Update() {
        loading = ProgressDialog.show(this, "", "アプリの更新を確認中...", true);
    }

    private void cancelLoadingDialog_Update(){
        if(loading!=null) loading.cancel();
    }

    private void showLoadingDialog_Xml() {
        loading = ProgressDialog.show(this, "", "通信中です...", true);
    }

    private void cancelLoadingDialog_Xml() {
        if (loading != null) loading.cancel();
    }

    /* 端末チェック */
    public void checkModel() {
        if (null != toast) toast.cancel();
        toast = Toast.makeText(this, R.string.start_check_model, Toast.LENGTH_SHORT);
        toast.show();
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
        if (null != toast) toast.cancel();
        toast = Toast.makeText(this, R.string.start_check_common_error, Toast.LENGTH_SHORT);
        toast.show();
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
        if (null != toast) toast.cancel();
        toast = Toast.makeText(this, R.string.start_check_dchaservice, Toast.LENGTH_SHORT);
        toast.show();
        /* DchaServiceの使用可否を確認 */
        if (GET_DCHASERVICE_FLAG(this) == USE_DCHASERVICE) {
            if (!bindDchaService(this, dchaServiceConnection)) {
                if (null != toast) toast.cancel();
                toast = Toast.makeText(this, R.string.start_check_common_error, Toast.LENGTH_SHORT);
                toast.show();
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
        if (null != toast) toast.cancel();
        toast = Toast.makeText(this, R.string.start_check_permission, Toast.LENGTH_SHORT);
        toast.show();
        if (GET_SETTINGS_FLAG(this) == SETTINGS_NOT_COMPLETED) {
            startCheck();
        } else {
            if (null != toast) toast.cancel();
            toast = Toast.makeText(this, R.string.start_starting_main, Toast.LENGTH_SHORT);
            toast.show();
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
            if (null != toast) toast.cancel();
            toast = Toast.makeText(this, R.string.start_starting_main, Toast.LENGTH_SHORT);
            toast.show();
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
            if (null != toast) toast.cancel();
            toast = Toast.makeText(this, R.string.start_starting_main, Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* 初回起動お知らせ */
    public void startCheck() {
        SET_CHANGE_SETTINGS_DCHA_FLAG(0, this);
        if (toast != null) toast.cancel();
        toast = Toast.makeText(this, R.string.start_check_important_news, Toast.LENGTH_SHORT);
        toast.show();
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
                if (null != toast) toast.cancel();
                toast = Toast.makeText(this, R.string.start_starting_main, Toast.LENGTH_SHORT);
                toast.show();
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
                            .setMessage("システム設定の変更が許可されていません。\n”設定”を押した後に表示されるスイッチを有効にし許可してください")
                            .setIcon(R.drawable.alert)
                            .setPositiveButton(R.string.dialog_common_settings, (dialog, which) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
                                }
                            })
                            .setNeutralButton(R.string.dialog_common_exit, (dialogInterface, i) -> finishAndRemoveTask()).show();
                } else {
                    if (null != toast) toast.cancel();
                    toast = Toast.makeText(this, R.string.start_starting_main, Toast.LENGTH_SHORT);
                    toast.show();
                    SET_SETTINGS_FLAG(SETTINGS_COMPLETED, this);
                    startActivity(intent);
                    finish();
                }
                break;
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
        switch (Common.Variable.START_FLAG) {
            case 1:
            case 3:
                cancelLoadingDialog_Xml();
                finish();
                break;
            case 2:
                checkModel();
                break;
        }
    }
}