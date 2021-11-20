package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragment;

import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.flagment.ApplicationSettingsFragment;
import com.saradabar.cpadcustomizetool.flagment.MainFragment;
import com.saradabar.cpadcustomizetool.menu.InformationActivity;
import com.saradabar.cpadcustomizetool.check.UpdateActivity;
import com.saradabar.cpadcustomizetool.service.KeepService;
import com.saradabar.cpadcustomizetool.set.BlockerActivity;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class StartActivity extends Activity {

    public IDchaService mDchaService;
    public ProgressDialog mProgress;
    private final String dchaStateString = DCHA_STATE;
    private final String hideNavigationBarString = HIDE_NAVIGATION_BAR;
    private ContentResolver resolver;
    private int i;
    private Menu mMenu;

    private static StartActivity instance = null;

    public static StartActivity getInstance() {
        return instance;
    }

    /* 設定画面表示 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(false);

        instance = this;
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, AdministratorReceiver.class);

        if (Common.Variable.USE_FLAG == 1) {
            if (!mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                setContentView(R.layout.main_error_disable_own);
                Button button4 = findViewById(R.id.main_error_button_4);
                button4.setOnClickListener(view -> {
                    AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
                    b.setTitle(R.string.dialog_question_are_you_sure)
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
                                Common.SET_UPDATE_FLAG(0, this);
                                Common.SET_DCHASERVICE_FLAG(USE_NOT_DCHASERVICE, this);
                                resolver = getContentResolver();
                                Settings.System.putInt(resolver, dchaStateString, 0);
                                Settings.System.putInt(resolver, hideNavigationBarString, 0);
                            });
                    b.setNegativeButton(R.string.dialog_common_no, null);
                    b.show();
                });
            } else {
                setContentView(R.layout.main_error_enable_own);
                Button button1 = findViewById(R.id.main_error_button_1);
                Button button2 = findViewById(R.id.main_error_button_2);
                Button button3 = findViewById(R.id.main_error_button_3);
                button1.setOnClickListener(view -> {
                    AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
                    b.setTitle(R.string.dialog_question_device_owner)
                            .setCancelable(false)
                            .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                                mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
                                Intent intent = new Intent(this, StartActivity.class);
                                finish();
                                startActivity(intent);
                            });
                    b.setNegativeButton(R.string.dialog_common_no, null);
                    b.show();
                });
                button2.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(view.getContext(), BlockerActivity.class);
                        startActivity(intent);
                    } catch (ActivityNotFoundException ignored) {
                    }
                });
                button3.setOnClickListener(view -> {
                    AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
                    b.setTitle(R.string.dialog_question_are_you_sure)
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
                                Common.SET_UPDATE_FLAG(0, this);
                                Common.SET_DCHASERVICE_FLAG(USE_NOT_DCHASERVICE, this);
                                resolver = getContentResolver();
                                Settings.System.putInt(resolver, dchaStateString, 0);
                                Settings.System.putInt(resolver, hideNavigationBarString, 0);
                            });
                    b.setNegativeButton(R.string.dialog_common_no, null);
                    b.show();
                });
            }
        } else {
            setContentView(R.layout.main_layout);
            transitionFragment(new MainFragment());
        }
        if (Common.Variable.toast != null) {
            Common.Variable.toast.cancel();
        }
    }

    /* メニュー表示 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (Common.Variable.USE_FLAG != 1) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
        } else {
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
                mMenu.findItem(R.id.app_info_3).setVisible(false);
                mTransitionFragment(new ApplicationSettingsFragment());
                return true;
            case android.R.id.home:
                mMenu.findItem(R.id.app_info_3).setVisible(true);
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transitionFragment(new MainFragment());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        mMenu.findItem(R.id.app_info_3).setVisible(true);
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transitionFragment(new MainFragment());
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
                d.setMessage(R.string.dialog_success_silent_install)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                d.create();
                d.show();
            }

            /* 失敗 */
            @Override
            public void onFailure() {
                AlertDialog.Builder d = new AlertDialog.Builder(StartActivity.this);
                d.setMessage(R.string.dialog_failure_silent_install)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss());
                d.create();
                d.show();
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
                        .setPositiveButton(R.string.dialog_common_yes, (dialog2, which1) -> {
                            mHandler.removeCallbacks(mRunnable);
                        })
                        .setNegativeButton(R.string.dialog_common_no, (dialog3, which2) -> {
                            mHandler.removeCallbacks(mRunnable);
                            MainFragment.getInstance().resetResolution();
                        });
                AlertDialog AlertDialog = mAlertDialog.create();
                AlertDialog.show();
                /* カウント開始 */
                i = 10;
                mHandler = new Handler();
                mRunnable = new Runnable() {
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
                MainFragment.getInstance().resetResolution();
            }
        };
    }

    /* ダイアログの表示 */
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            mProgress = new ProgressDialog(StartActivity.this);
            mProgress.setTitle("");
            mProgress.setMessage("インストール中・・・");
            mProgress.setCancelable(false);
            mProgress.create();
            mProgress.show();
            return mProgress;
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