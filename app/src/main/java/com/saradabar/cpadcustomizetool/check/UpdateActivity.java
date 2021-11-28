package com.saradabar.cpadcustomizetool.check;

import static com.saradabar.cpadcustomizetool.Common.GET_MODEL_ID;
import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.check.event.UpdateEventListener;

import java.io.File;

public class UpdateActivity extends Activity implements UpdateEventListener {

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        showLoadingDialog();
        new Updater(this, UPDATE_CHECK_URL, 0).updateCheck();
    }

    @Override
    public void onUpdateAvailable(String string) {
        cancelLoadingDialog();
        new Handler().post(() -> showUpdateDialog(string));
    }

    @Override
    public void onUpdateUnavailable() {
        cancelLoadingDialog();
        new Handler().post(this::showNoUpdateDialog);
    }

    @Override
    public void onUpdateApkDownloadComplete() {
        new Handler().post(() -> new Updater(this, UPDATE_CHECK_URL, 0).installApk(this));
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
        cancelLoadingDialog();
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
        cancelLoadingDialog();
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_connection_error)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finish())
                .show();
    }

    private void showUpdateDialog(String mString) {
        View view = getLayoutInflater().inflate(R.layout.update_dialog, null);
        TextView mTextView = view.findViewById(R.id.update_information);
        mTextView.setText(mString);
        view.findViewById(R.id.update_info_button).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_INFO_URL)));
            } catch (ActivityNotFoundException ignored) {
                if (toast != null) toast.cancel();
                toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    if (GET_MODEL_ID(this) != 2) {
                        AsyncFileDownload asyncFileDownload = initFileLoader();
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setTitle(R.string.dialog_title_update);
                        progressDialog.setMessage("アップデートファイルをサーバーからダウンロード中・・・");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setProgress(0);
                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", (dialog2, which2) -> {
                            asyncFileDownload.cancel(true);
                            finish();
                        });
                        ProgressHandler progressHandler = new ProgressHandler();
                        progressHandler.progressDialog = progressDialog;
                        progressHandler.asyncfiledownload = asyncFileDownload;
                        progressHandler.sendEmptyMessage(0);
                    } else {
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(R.string.dialog_title_update)
                                .setMessage(R.string.dialog_update_caution)
                                .setPositiveButton(R.string.dialog_common_yes, (dialog2, which2) -> {
                                    try {
                                        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL)), REQUEST_UPDATE);
                                    } catch (ActivityNotFoundException ignored) {
                                        if (toast != null) toast.cancel();
                                        toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                                        toast.show();
                                        finish();
                                    }
                                })
                                .show();
                    }
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> finish())
                .show();
    }

    private void showNoUpdateDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setMessage(R.string.dialog_no_update)
                .setPositiveButton(R.string.dialog_common_ok,
                        (dialog, which) -> finish())
                .show();
    }

    private AsyncFileDownload initFileLoader() {
        AsyncFileDownload asyncfiledownload = new AsyncFileDownload(this, DOWNLOAD_FILE_URL, new File(new File(getExternalCacheDir(), "update.apk").getPath()));
        asyncfiledownload.execute();
        return asyncfiledownload;
    }

    private void showLoadingDialog() {
        loadingDialog = ProgressDialog.show(this, "", "アプリの更新を確認中・・・", true);
    }

    private void cancelLoadingDialog() {
        if (loadingDialog != null) loadingDialog.cancel();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE) {
            finish();
        }
    }
}