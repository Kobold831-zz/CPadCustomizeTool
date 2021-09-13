package com.saradabar.cpadcustomizetool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.ChangeXml.PackagesXml;
import com.saradabar.cpadcustomizetool.Menu.Update.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.Menu.Update.ProgressHandler;
import com.saradabar.cpadcustomizetool.Menu.Update.Updater;
import com.saradabar.cpadcustomizetool.Menu.Update.event.UpdateEventListener;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;
import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.CHECK_OK_TAB3;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.SETTINGS_COMPLETED;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.SETTINGS_NOT_COMPLETED;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_NOT_DCHASERVICE;

public class MainActivity extends Activity implements UpdateEventListener {

    private final Context context = this;
    private Handler handler;
    public static Toast toast;
    private String MODEL_NAME;
    private WelcomeHelper welcomeScreen;
    private Updater updater;
    private ProgressHandler progressHandler;
    private AsyncFileDownload asyncfiledownload;
    private ProgressDialog progressDialog;
    private ProgressDialog loadingDialog;

    private static boolean bindDchaService(Context context, ServiceConnection dchaServiceConnection) {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        return context.bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //接続
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    //***データ管理
    private int GET_UPDATE_FLAG() {
        int UPDATE_FLAG;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        UPDATE_FLAG = sp.getInt("UPDATE_FLAG", Common.Customizetool.UPDATE_FLAG);
        return UPDATE_FLAG;
    }

    private void SET_SETTINGS_COMPLETED(int SETTINGS_COMPLETED) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("SETTINGS_COMPLETED", SETTINGS_COMPLETED).apply();
    }

    private int GET_SETTINGS_COMPLETED() {
        int START_WINDOW;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        START_WINDOW = sp.getInt("SETTINGS_COMPLETED", 0);
        return START_WINDOW;
    }

    private void SET_CHECK_TAB_ID(int SET_TAB_ID) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("CHECK_TAB_ID", SET_TAB_ID).apply();
    }

    public int GET_CHECK_TAB_ID() {
        int CHECK_TAB_ID;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        CHECK_TAB_ID = sp.getInt("CHECK_TAB_ID", 0);
        return CHECK_TAB_ID;
    }

    private void SET_USE_DCHASERVICE(int USE_DCHASERVICE) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("USE_DCHASERVICE", USE_DCHASERVICE).apply();
    }

    private int GET_USE_DCHASERVICE() {
        int USE_DCHASERVICE;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        USE_DCHASERVICE = sp.getInt("USE_DCHASERVICE", 0);
        return USE_DCHASERVICE;
    }

    private void SET_CHANGE_SETTINGS_USE_DCHA(int IS_USE_DCHA) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("IS_USE_DCHA", IS_USE_DCHA).apply();
    }
    //***

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.Customizetool.start_flag = 0;
        Common.Customizetool.NOT_USE = 0;
        handler = new Handler();
        //ネットワークチェック
        if (isNetWork()) {
            //アップデートチェックするか確認
            if (GET_UPDATE_FLAG() == 0) {
                autoUpdateCheck();
            }else {
                checkSupport();
            }
        } else {
            netWorkError();
        }
    }

    //ネットワーク確認
    private boolean isNetWork() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //ネットワークエラー
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void netWorkError() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("通信エラーが発生しました。\nネットワークに接続してください。\n続行する場合機能が制限されます。")
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .setNeutralButton(R.string.dialog_common_continue, (dialog, which) -> {
                    Common.Customizetool.NOT_USE = 1;
                    if (GET_SETTINGS_COMPLETED() == SETTINGS_NOT_COMPLETED) {
                        welcomeScreen = new WelcomeHelper(
                                MainActivity.this, WelAppActivity.class);
                        welcomeScreen.forceShow();
                    } else {
                        checkModel();
                    }
                })
                .show();
    }

    private void autoUpdateCheck() {
        String updateXmlUrl = "https://github.com/saradabar/Touch2_Custom_Tool/raw/master/Update.xml";
        updater = new Updater(this, updateXmlUrl, 1);
        updater.updateCheck();
        showLoadingDialog_Update();
    }

    private void checkSupport() {
        showLoadingDialog_Xml();
        String supportXmlUrl = "https://raw.githubusercontent.com/saradabar/Touch2_Custom_Tool/master/Support.xml";
        Checker checker = new Checker(this, supportXmlUrl);
        checker.supportCheck();
    }

    @Override
    public void onUpdateApkDownloadComplete() {
        handler.post(() -> updater.installApk());
    }

    @Override
    public void onUpdateAvailable(String d) {
    }

    @Override
    public void onUpdateUnavailable() {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onSupportAvailable() {
        cancelLoadingDialog_Xml();
        showSupportDialog();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onSupportUnavailable() {
        cancelLoadingDialog_Xml();
        if (GET_SETTINGS_COMPLETED() == SETTINGS_NOT_COMPLETED) {
            welcomeScreen = new WelcomeHelper(
                    this, WelAppActivity.class);
            welcomeScreen.forceShow();
        } else {
            checkModel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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
                    progressHandler.progressDialog = progressDialog;
                    progressHandler.asyncfiledownload = asyncfiledownload;

                    if (progressDialog != null && asyncfiledownload != null) {
                        progressDialog.setProgress(0);
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
        asyncfiledownload = new AsyncFileDownload(this, Common.Customizetool.DOWNLOAD_FILE_URL, outputFile);
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
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("アプリの更新");
            progressDialog.setMessage("アップデートファイルをサーバーからダウンロード中...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル",
                    (dialog, which) -> {
                        cancelLoad();
                        checkSupport();
                    });
        }
        return progressDialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showSupportDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("このアプリは現在使用できません。\n続行する場合機能が制限されます。")
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .setNeutralButton(R.string.dialog_common_continue, (dialog, which) -> {
                    Common.Customizetool.NOT_USE = 1;
                    if (GET_SETTINGS_COMPLETED() == SETTINGS_NOT_COMPLETED) {
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
        loadingDialog = ProgressDialog.show(this, "", "アプリの更新を確認中...", true);
    }

    private void cancelLoadingDialog_Update(){
        if(loadingDialog!=null) loadingDialog.cancel();
    }

    private void showLoadingDialog_Xml() {
        loadingDialog = ProgressDialog.show(this, "", "通信中です...", true);
    }

    private void cancelLoadingDialog_Xml() {
        if (loadingDialog != null) loadingDialog.cancel();
    }

    //端末チェック
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkModel() {
        if (null != toast) toast.cancel();
        toast = Toast.makeText(context, R.string.start_check_model, Toast.LENGTH_SHORT);
        toast.show();
        MODEL_NAME = Build.MODEL;
        String MODEL = "TAB-A03-B";
        if (MODEL_NAME.contains(MODEL)||MODEL_NAME.contains("TAB-A05-BD")) {
            checkDcha();
        } else {
            errorNotTab2Or3();
        }
    }

    //端末チェックエラー
    private void errorNotTab2Or3() {
        if (null != toast) toast.cancel();
        toast = Toast.makeText(context, R.string.start_check_common_error, Toast.LENGTH_SHORT);
        toast.show();
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setMessage(R.string.dialog_error_not_pad2)
                .setIcon(R.drawable.alert)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    //Dcha動作チェック
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkDcha() {
        if (null != toast) toast.cancel();
        toast = Toast.makeText(context, R.string.start_check_dchaservice, Toast.LENGTH_SHORT);
        toast.show();
        final String TAB3MODEL = "TAB-A03-BR3";
        //Dcha確認
        if (GET_USE_DCHASERVICE() == USE_DCHASERVICE) {
            if (!bindDchaService(this, dchaServiceConnection)) {
                if (null != toast) toast.cancel();
                toast = Toast.makeText(context, R.string.start_check_common_error, Toast.LENGTH_SHORT);
                toast.show();
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setCancelable(false)
                        .setTitle(R.string.dialog_title_common_error)
                        .setMessage(R.string.dialog_error_not_dchaservice)
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                        .setNeutralButton(R.string.dialog_common_continue, (dialogInterface, i) -> {
                            SET_USE_DCHASERVICE(USE_NOT_DCHASERVICE);
                            if (MODEL_NAME.equals(TAB3MODEL)) {
                                checkSettingsTab3();
                            } else {
                                if (MODEL_NAME.contains("TAB-A05-BD")) {
                                    checkSettingsTab_NEO();
                                }else {
                                    checkSettingsTab2();
                                }
                            }
                        })
                        .show();
            } else {
                if (MODEL_NAME.equals(TAB3MODEL)) {
                    checkSettingsTab3();
                } else {
                    if (MODEL_NAME.contains("TAB-A05-BD")) {
                        checkSettingsTab_NEO();
                    }else {
                        checkSettingsTab2();
                    }
                }
            }
        } else {
            if (MODEL_NAME.equals(TAB3MODEL)) {
                checkSettingsTab3();
            } else {
                if (MODEL_NAME.contains("TAB-A05-BD")) {
                    checkSettingsTab_NEO();
                }else {
                    checkSettingsTab2();
                }
            }
        }
    }

    //TAB2起動設定チェック
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkSettingsTab2() {
        SET_CHECK_TAB_ID(0);
        if (null != toast) toast.cancel();
        toast = Toast.makeText(context, R.string.start_check_permission, Toast.LENGTH_SHORT);
        toast.show();
        if (GET_SETTINGS_COMPLETED() == SETTINGS_NOT_COMPLETED) {
            StartCheck();
        } else {
            if (null != toast) toast.cancel();
            toast = Toast.makeText(context, R.string.start_starting_main, Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //TAB3起動設定チェック
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkSettingsTab3() {
        SET_CHECK_TAB_ID(1);
        if (GET_SETTINGS_COMPLETED() == SETTINGS_NOT_COMPLETED) {
            StartCheck();
        } else {
            if (null != toast) toast.cancel();
            toast = Toast.makeText(context, R.string.start_starting_main, Toast.LENGTH_SHORT);
            toast.show();

            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //TAB_NEO起動設定チェック
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkSettingsTab_NEO() {
        SET_CHECK_TAB_ID(2);
        if (GET_SETTINGS_COMPLETED() == SETTINGS_NOT_COMPLETED) {
            StartCheck();
        } else {
            if (null != toast) toast.cancel();
            toast = Toast.makeText(context, R.string.start_starting_main, Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //初回起動お知らせ
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void StartCheck() {
        SET_CHANGE_SETTINGS_USE_DCHA(0);
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, R.string.start_check_important_news, Toast.LENGTH_SHORT);
        toast.show();
        AlertDialog.Builder News = new AlertDialog.Builder(this);
        News.setCancelable(false)
                .setTitle(R.string.dialog_title_notice_start)
                .setMessage(R.string.dialog_notice_start)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> PERMISSION_SETTINGS()).show();
    }

    //TAB3権限チェック
    private boolean checkWriteSystemSettings() {
        boolean canWrite = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canWrite = Settings.System.canWrite(this);
        }
        return !canWrite;
    }

    //権限設定
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void PERMISSION_SETTINGS() {
        if (GET_CHECK_TAB_ID() == CHECK_OK_TAB3||GET_CHECK_TAB_ID() == 2) {
            if (checkWriteSystemSettings()) {
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setCancelable(false)
                        .setTitle(R.string.dialog_title_grant_permission)
                        .setMessage("システム設定の変更が許可されていません。\n”設定”を押した後に表示されるスイッチを有効にし許可してください")
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.dialog_common_settings, (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        })
                        .setNeutralButton(R.string.dialog_common_exit, (dialogInterface, i) -> finishAndRemoveTask()).show();
            } else {
                if (null != toast) toast.cancel();
                toast = Toast.makeText(context, R.string.start_starting_main, Toast.LENGTH_SHORT);
                toast.show();
                SET_SETTINGS_COMPLETED(SETTINGS_COMPLETED);
                Intent intent = new Intent(this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            AlertDialog.Builder permission = new AlertDialog.Builder(this);
            permission.setCancelable(false)
                    .setTitle(R.string.dialog_title_grant_permission)
                    .setMessage(R.string.dialog_grant_permission);
            if (toast != null) toast.cancel();
            toast = Toast.makeText(context, R.string.start_check_settings_permissions, Toast.LENGTH_SHORT);
            toast.show();
            final Intent intent = new Intent(this, StartActivity.class);
            permission.setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                if (null != toast) toast.cancel();
                toast = Toast.makeText(context, R.string.start_starting_main, Toast.LENGTH_SHORT);
                toast.show();
                SET_SETTINGS_COMPLETED(SETTINGS_COMPLETED);
                startActivity(intent);
                finish();
            })
            .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                if (toast != null) toast.cancel();
                toast = Toast.makeText(context, R.string.start_grant_permission, Toast.LENGTH_SHORT);
                toast.show();
                SET_SETTINGS_COMPLETED(SETTINGS_COMPLETED);
                Intent intent1 = new Intent(DCHA_SERVICE);
                intent1.setPackage(PACKAGE_DCHASERVICE);
                bindService(intent1, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        IDchaService mDchaService = IDchaService.Stub.asInterface(service);
                        PackagesXml xml = PackagesXml.inputFromSystem(mDchaService);
                        Objects.requireNonNull(xml).grantPermission("com.saradabar.cpadcustomizetool", "android.permission.WRITE_SECURE_SETTINGS");
                        xml.outputToSystem(mDchaService);
                        Toast.makeText(MainActivity.this, R.string.toast_start_reboot, Toast.LENGTH_SHORT).show();
                        try {
                            mDchaService.rebootPad(0, null);
                        } catch (RemoteException ignored) {
                        }
                    }
                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                    }
                }, Context.BIND_AUTO_CREATE);
            }).show();
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

    //再表示
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        switch (Common.Customizetool.start_flag) {
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