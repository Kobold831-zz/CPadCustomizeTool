package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.Common.GET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.Common.SET_DCHASERVICE_FLAG;
import static com.saradabar.cpadcustomizetool.Common.SET_UPDATE_FLAG;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_STATE;
import static com.saradabar.cpadcustomizetool.Common.Variable.HIDE_NAVIGATION_BAR;
import static com.saradabar.cpadcustomizetool.Common.Variable.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragment;

import com.saradabar.cpadcustomizetool.check.UpdateActivity;
import com.saradabar.cpadcustomizetool.flagment.ApplicationSettingsFragment;
import com.saradabar.cpadcustomizetool.flagment.DeviceOwnerFragment;
import com.saradabar.cpadcustomizetool.flagment.MainFragment;
import com.saradabar.cpadcustomizetool.menu.InformationActivity;
import com.saradabar.cpadcustomizetool.service.KeepService;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

import java.io.File;
import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class StartActivity extends Activity {

    private Menu menu;
    public IDchaService mDchaService;

    private static StartActivity instance = null;

    public static StartActivity getInstance() {
        return instance;
    }

    /* 設定画面表示 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(false);

        if (getIntent().getBooleanExtra("result", false)) {
            setContentView(R.layout.main_layout);
            transitionFragment(new MainFragment());
            if (toast != null) toast.cancel();
            return;
        }

        if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            setContentView(R.layout.main_error_enable_own);
            findViewById(R.id.main_error_button_1).setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
                    .setTitle(R.string.dialog_question_device_owner)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        devicePolicyManager.clearDeviceOwnerApp(getPackageName());
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", false));
                    })
                    .setNegativeButton(R.string.dialog_common_no, null)
                    .show());
            findViewById(R.id.main_error_button_2).setOnClickListener(view -> {
                try {
                    startActivity(new Intent(view.getContext(), BlockerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                } catch (ActivityNotFoundException ignored) {
                }
            });
            findViewById(R.id.main_error_button_3).setOnClickListener(this::setSettings);
        } else {
            setContentView(R.layout.main_error_disable_own);
            findViewById(R.id.main_error_button_4).setOnClickListener(this::setSettings);
        }
        if (toast != null) toast.cancel();
    }

    private void setSettings(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.dialog_question_are_you_sure)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    /* 全サービスの停止 */
                    SharedPreferences sp1 = getSharedPreferences(Common.Variable.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor spe = sp1.edit();
                    spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_SERVICE, false);
                    spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_DCHA_STATE, false);
                    spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false);
                    spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_USB_DEBUG, false);
                    spe.putBoolean(Common.Variable.KEY_ENABLED_KEEP_HOME, false);
                    spe.apply();
                    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo serviceInfo : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
                        if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                            KeepService.getInstance().stopService(6);
                        }
                    }
                    /* 設定リセット */
                    SET_UPDATE_FLAG(true, this);
                    SET_DCHASERVICE_FLAG(false, this);
                    ContentResolver resolver = getContentResolver();
                    Settings.System.putInt(resolver, DCHA_STATE, 0);
                    Settings.System.putInt(resolver, HIDE_NAVIGATION_BAR, 0);
                })
                .setNegativeButton(R.string.dialog_common_no, null)
                .show();
    }

    /* メニュー表示 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        if (getIntent().getBooleanExtra("result", false)) {
            getMenuInflater().inflate(R.menu.main, menu);
        } else {
            getMenuInflater().inflate(R.menu.sub, menu);
        }
        return true;
    }

    /* メニュー選択 */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_info_1:
                startActivity(new Intent(this, InformationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("result", getIntent().getBooleanExtra("result", false)));
                return true;
            case R.id.app_info_2:
                startActivity(new Intent(this, UpdateActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                return true;
            case R.id.app_info_3:
                menu.findItem(R.id.app_info_3).setVisible(false);
                mTransitionFragment(new ApplicationSettingsFragment());
                return true;
            case android.R.id.home:
                menu.findItem(R.id.app_info_3).setVisible(true);
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transitionFragment(new MainFragment());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("result", false)) {
            menu.findItem(R.id.app_info_3).setVisible(true);
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            transitionFragment(new MainFragment());
        }
    }

    private void transitionFragment(PreferenceFragment nextPreferenceFragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main, nextPreferenceFragment)
                .commit();
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void mTransitionFragment(PreferenceFragment nextPreferenceFragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.layout_main, nextPreferenceFragment)
                .commit();
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean bindDchaService() {
        return !bindService(new Intent(DCHA_SERVICE).setPackage(PACKAGE_DCHASERVICE), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mDchaService = IDchaService.Stub.asInterface(service);
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                unbindService(this);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public MainFragment.CopyTask.Listener CopyListener() {
        return new MainFragment.CopyTask.Listener() {
            ProgressDialog progressDialog;

            /* プログレスバーの表示 */
            @Override
            public void onShow() {
                progressDialog = new ProgressDialog(StartActivity.this);
                progressDialog.setTitle("");
                progressDialog.setMessage("コピー中・・・");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            /* 成功 */
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage("成功しました")
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }

            /* 失敗 */
            @Override
            public void onFailure() {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage("失敗しました")
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        };
    }

    public DeviceOwnerFragment.OwnerInstallTask.Listener OwnerInstallCreateListener() {
        return new DeviceOwnerFragment.OwnerInstallTask.Listener() {
            ProgressDialog progressDialog;

            /* プログレスバーの表示 */
            @Override
            public void onShow() {
                progressDialog = new ProgressDialog(StartActivity.this);
                progressDialog.setTitle("");
                progressDialog.setMessage("インストール中・・・");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            /* 成功 */
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage(R.string.dialog_success_silent_install)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }

            /* 失敗 */
            @Override
            public void onFailure() {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage(R.string.dialog_failure_silent_install)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onError(String str) {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage(str)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        };
    }

    public MainFragment.silentInstallTask.Listener createListener() {
        return new MainFragment.silentInstallTask.Listener() {
            ProgressDialog progressDialog;

            /* プログレスバーの表示 */
            @Override
            public void onShow() {
                progressDialog = new ProgressDialog(StartActivity.this);
                progressDialog.setTitle("");
                progressDialog.setMessage("インストール中・・・");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            /* 成功 */
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage(R.string.dialog_success_silent_install)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }

            /* 失敗 */
            @Override
            public void onFailure() {
                progressDialog.dismiss();
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage(R.string.dialog_failure_silent_install)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        };
    }

    public MainFragment.setResolutionTask.Listener mCreateListener() {
        return new MainFragment.setResolutionTask.Listener() {
            private Handler mHandler;
            private Runnable mRunnable;

            /* 成功 */
            @Override
            public void onSuccess() {
                /* 設定変更カウントダウンダイアログ表示 */
                AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(StartActivity.getInstance());
                mAlertDialog.setTitle(R.string.dialog_title_resolution)
                        .setCancelable(false)
                        .setMessage("")
                        .setPositiveButton(R.string.dialog_common_yes, (dialog2, which1) -> mHandler.removeCallbacks(mRunnable))
                        .setNegativeButton(R.string.dialog_common_no, (dialog3, which2) -> {
                            mHandler.removeCallbacks(mRunnable);
                            MainFragment.getInstance().resetResolution();
                        });
                AlertDialog AlertDialog = mAlertDialog.create();
                AlertDialog.show();

                /* カウント開始 */
                mHandler = new Handler();
                mRunnable = new Runnable() {
                    int i = 10;
                    @Override
                    public void run() {
                        AlertDialog.setMessage("変更を適用しますか？\n" + i + "秒後に元の設定に戻ります");
                        mHandler.postDelayed(this, 1000);
                        if (i <= 0) {
                            AlertDialog.dismiss();
                            mHandler.removeCallbacks(this);
                            MainFragment.getInstance().resetResolution();
                        }
                        i--;
                    }
                };
                mHandler.post(mRunnable);
            }

            /* 失敗 */
            @Override
            public void onFailure() {
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage("解像度設定に失敗しました")
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        };
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        instance = this;
        /* DchaServiceの使用可否を確認 */
        if (GET_DCHASERVICE_FLAG(this)) {
            //DchaServiceが機能していないなら終了
            if (bindDchaService()) {
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                finish();
            }
        }
    }
}