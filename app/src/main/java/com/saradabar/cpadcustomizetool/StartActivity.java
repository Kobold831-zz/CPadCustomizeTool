package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.common.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.mDevicePolicyManager;
import static com.saradabar.cpadcustomizetool.common.Common.Variable.mComponentName;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.common.Common;
import com.saradabar.cpadcustomizetool.flagment.ApplicationSettingsFragment;
import com.saradabar.cpadcustomizetool.flagment.MainFragment;
import com.saradabar.cpadcustomizetool.menu.InformationActivity;
import com.saradabar.cpadcustomizetool.menu.check.UpdateActivity;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class StartActivity extends Activity {

    public IDchaService mDchaService;
    public  ProgressDialog mProgress;

    private static StartActivity instance = null;

    public static StartActivity getInstance() {
        return instance;
    }

    /* 設定画面表示 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(false);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, AdministratorReceiver.class);
        if (Common.Variable.USE_FLAG == 1) {
            if (!mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                setContentView(R.layout.main_error_disable_own);
            } else {
                setContentView(R.layout.main_error_enable_own);
                Button button1 = findViewById(R.id.main_error_button_1);
                Button button2 = findViewById(R.id.main_error_button_2);
                button1.setOnClickListener(view -> {
                    AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
                    b.setTitle(R.string.dialog_question_device_owner)
                            .setCancelable(false)
                            .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                                mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
                                Toast.makeText(view.getContext(), R.string.toast_notice_disable_own, Toast.LENGTH_SHORT).show();
                            });
                    b.setNegativeButton(R.string.dialog_common_no, null);
                    b.show();
                });
                button2.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(view.getContext(), BlockerActivity.class);
                        startActivity(intent);
                    }catch (ActivityNotFoundException ignored) {
                    }
                });
            }
        } else {
            setContentView(R.layout.main_home);
            transitionFragment(new MainFragment());
        }
        if (Common.Variable.toast != null) {
            Common.Variable.toast.cancel();
        }
    }

    /* メニュー表示 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Common.Variable.USE_FLAG != 1) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
        }else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.sub, menu);
        }
        return true;
    }

    /* メニュー選択 */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_info_1:
                Intent intent = new Intent(this, InformationActivity.class);
                startActivity(intent);
                return true;
            case R.id.app_info_2:
                Intent intent2 = new Intent(this, UpdateActivity.class);
                startActivity(intent2);
                return true;
            case R.id.app_info_3:
                mTransitionFragment(new ApplicationSettingsFragment());
                return true;
            case android.R.id.home:
                transitionFragment(new MainFragment());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionFragment(PreferenceFragment nextPreferenceFragment) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_main, nextPreferenceFragment)
                    .commit();
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void mTransitionFragment(PreferenceFragment nextPreferenceFragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.layout_main, nextPreferenceFragment)
                .commit();
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static boolean bindDchaService(Context context, ServiceConnection dchaServiceConnection) {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        return !context.bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* 接続 */
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    public MainFragment.silentInstallTask.Listener createListener() {
        return new MainFragment.silentInstallTask.Listener() {

            /* プログレスバーの表示 */
            @Override
            public void onShow() {
                showDialog(1);
            }


            /* 成功 */
            @Override
            public void onSuccess() {
                AlertDialog.Builder d = new AlertDialog.Builder(StartActivity.this);
                d.setMessage("サイレントインストールに成功しました")
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                d.create();
                d.show();
            }

            /* 失敗 */
            @Override
            public void onFailure() {
                AlertDialog.Builder d = new AlertDialog.Builder(StartActivity.this);
                d.setMessage("サイレントインストールに失敗しました")
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                d.create();
                d.show();
            }
        };
    }

    /* ダイアログの表示 */
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                mProgress = new ProgressDialog(StartActivity.this);
                mProgress.setTitle("");
                mProgress.setMessage("インストール中・・・");
                mProgress.setCancelable(false);
                mProgress.create();
                mProgress.show();
                return mProgress;
            case 2:
                AlertDialog.Builder d = new AlertDialog.Builder(StartActivity.this);
                d.setMessage("サイレントインストールがキャンセルされました")
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                d.show();
                return d.create();
        }
        return null;
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        /* DchaServiceの使用可否を確認 */
        if (Common.GET_DCHASERVICE_FLAG(this) == USE_DCHASERVICE) {
            if (bindDchaService(this, dchaServiceConnection)) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
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
}