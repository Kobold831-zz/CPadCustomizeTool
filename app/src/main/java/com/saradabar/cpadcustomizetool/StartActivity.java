package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.USE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.mComponentName;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.mDchaService;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.mDevicePolicyManager;
import static com.saradabar.cpadcustomizetool.MainActivity.toast;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.BlockToUninstall.BlockToUninstall;
import com.saradabar.cpadcustomizetool.Menu.AppInfoActivity;
import com.saradabar.cpadcustomizetool.Menu.Update.UpdateActivity;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class StartActivity extends Activity {

    //データ読み込み
    private int GET_USE_DCHASERVICE() {
        int USE_DCHASERVICE;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        USE_DCHASERVICE = sp.getInt("USE_DCHASERVICE", 0);
        return USE_DCHASERVICE;
    }

    //設定画面表示
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, AdministratorReceiver.class);
        if (Common.Customizetool.NOT_USE == 1) {
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
                    b.setNeutralButton(R.string.dialog_common_no, null);
                    b.show();
                });

                button2.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(view.getContext(), BlockToUninstall.class);
                        startActivity(intent);
                    }catch (ActivityNotFoundException ignored) {
                    }
                });
            }
        } else {
            setContentView(R.layout.main_home);
            transitionFragment(new MainFragment());
        }
        if (toast != null) {
            toast.cancel();
        }
    }

    //メニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //メニュー選択
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_info_1:
                Intent intent = new Intent(this, AppInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.app_info_2:
                Intent intent2 = new Intent(this, UpdateActivity.class);
                startActivity(intent2);
                return true;
            case R.id.app_info_3:
                if (Common.Customizetool.NOT_USE == 0) {
                    transitionFragment(new AppSettingsFragment());
                }else Toast.makeText(this, R.string.toast_error_not_use, Toast.LENGTH_SHORT).show();
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
                    .addToBackStack(null)
                    .replace(R.id.layout_main, nextPreferenceFragment)
                    .commit();
            ActionBar actionBar = getActionBar();
            Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
    }

    //DchaServiceバインド
    public static boolean bindDchaService(Context context, ServiceConnection dchaServiceConnection) {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        return !context.bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //接続
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

    //再表示
    @Override
    public void onResume() {
        super.onResume();
        if (GET_USE_DCHASERVICE() == USE_DCHASERVICE) {
            //DchaServiceが機能するか確認
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