package com.saradabar.cpadcustomizetool.menu.check;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.common.Common;
import com.saradabar.cpadcustomizetool.menu.check.event.UpdateEventListener;

import java.io.File;
import java.util.Objects;

public class UpdateActivity extends Activity implements UpdateEventListener {

    private Handler handler;
    private Updater updater;

    private ProgressDialog progressDialog;
    private ProgressDialog loadingDialog;

    private ProgressHandler progressHandler;
    private AsyncFileDownload asyncfiledownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        showLoadingDialog_Xml();
        handler = new Handler();
        updater = new Updater(this, Common.Variable.UPDATE_CHECK_URL,0);
        updater.updateCheck();
    }

    @Override
    public void onUpdateAvailable(final String d) {
        cancelLoadingDialog_Xml();
        handler.post(() -> showUpdateDialog(d));
    }

    @Override
    public void onUpdateUnavailable() {
        cancelLoadingDialog_Xml();
        handler.post(this::showNoUpdateDialog);
    }

    @Override
    public void onUpdateApkDownloadComplete() {
        handler.post(() -> updater.installApk(this));
    }

    @Override
    public void onSupportAvailable() {

    }

    @Override
    public void onSupportUnavailable() {

    }

    @Override
    public void onUpdateAvailable1(String d) {

    }

    @Override
    public void onUpdateUnavailable1() {

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

    private void showUpdateDialog(String d){
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("アップデートがあります。アップデートしますか？\nアップデートをすることを推奨します。\n\n更新情報：\n" + d)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    initFileLoader();
                    showDialog(0);
                    progressHandler = new ProgressHandler();
                    progressHandler.progressDialog = progressDialog;
                    progressHandler.asyncfiledownload = asyncfiledownload;

                    if (progressDialog != null && asyncfiledownload != null){
                        progressDialog.setProgress(0);
                        progressHandler.sendEmptyMessage(0);
                    }else{
                        Toast ts = Toast.makeText(UpdateActivity.this, "NULLエラー", Toast.LENGTH_LONG);
                        ts.show();
                    }
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> finish())
                .show();
    }

    private void showNoUpdateDialog(){
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(R.string.dialog_no_update)
                .setPositiveButton(R.string.dialog_common_ok,
                        (dialog, which) -> finish())
                .show();
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if (id == 0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("アプリの更新");
            progressDialog.setMessage("アップデートファイルをサーバーからダウンロード中...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", (dialog, which) -> {
                cancelLoad();
                finish();
            });
        }
        return progressDialog;
    }

    private void initFileLoader()
    {
        File outputFile = new File(new File(getExternalCacheDir(), "update.apk").getPath());
        asyncfiledownload = new AsyncFileDownload(this, Common.Variable.DOWNLOAD_FILE_URL, outputFile);
        asyncfiledownload.execute();
    }

    private void cancelLoad()
    {
        if(asyncfiledownload != null){
            asyncfiledownload.cancel(true);
        }
    }

    private void showLoadingDialog_Xml() {
        loadingDialog = ProgressDialog.show(this, "", "アプリの更新を確認中...", true);
    }

    private void cancelLoadingDialog_Xml(){
        if(loadingDialog!=null) loadingDialog.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){
        super.onPause();
        cancelLoad();
    }

    @Override
    protected void onStop(){
        super.onStop();
        cancelLoad();
    }
}