package com.saradabar.cpadcustomizetool.check;

import static com.saradabar.cpadcustomizetool.Common.GET_MODEL_NAME;
import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.check.event.UpdateEventListener;

import java.io.File;

public class UpdateActivity extends Activity implements UpdateEventListener {

    private Handler handler;
    private Updater updater;
    private ProgressDialog progressDialog, loadingDialog;
    private ProgressHandler progressHandler;
    private AsyncFileDownload asyncfiledownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        showLoadingDialog();
        handler = new Handler();
        updater = new Updater(this, UPDATE_CHECK_URL, 0);
        updater.updateCheck();
    }

    @Override
    public void onUpdateAvailable(final String mString) {
        cancelLoadingDialog();
        handler.post(() -> showUpdateDialog(mString));
    }

    @Override
    public void onUpdateUnavailable() {
        cancelLoadingDialog();
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
                .setTitle(R.string.dialog_title_update)
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
                            finish();
                        }
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

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("アプリの更新");
            progressDialog.setMessage("アップデートファイルをサーバーからダウンロード中・・・");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", (dialog, which) -> {
                cancelLoad();
                finish();
            });
        }
        return progressDialog;
    }

    private void initFileLoader() {
        File mFile = new File(new File(getExternalCacheDir(), "update.apk").getPath());
        asyncfiledownload = new AsyncFileDownload(this, Common.Variable.DOWNLOAD_FILE_URL, mFile);
        asyncfiledownload.execute();
    }

    private void cancelLoad() {
        if (asyncfiledownload != null) {
            asyncfiledownload.cancel(true);
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        cancelLoad();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelLoad();
    }
}