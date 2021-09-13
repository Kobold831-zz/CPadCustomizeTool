package com.saradabar.cpadcustomizetool.SetHome;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.saradabar.cpadcustomizetool.Common.Customizetool.PACKAGE_DCHASERVICE;
import static com.saradabar.cpadcustomizetool.Common.Customizetool.DCHA_SERVICE;

public class SetHomeAppActivity extends Activity
{
    private IDchaService mDchaService;
    private Drawable icon;
    private static Toast toast;
    private final Intent setPackageName = new Intent();
    private String setHomeApp;
    private String setHomeName;
    private PackageManager pm;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_list);
        ActionBar actionBar = getActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        setPackageName.setAction(Intent.ACTION_MAIN);
        setPackageName.addCategory(Intent.CATEGORY_HOME);
        pm = getPackageManager();
        List<CustomData> objects = new ArrayList<>();
        final List<ResolveInfo> appInfoList = pm.queryIntentActivities(setPackageName, 0);
        for (ResolveInfo ri : appInfoList) {
            CustomData itemData = new CustomData();
            //noinspection ResultOfMethodCallIgnored
            ri.loadLabel(pm).toString();
            itemData.setTextData(ri.loadLabel(pm).toString());
            try {
                icon = pm.getApplicationIcon(ri.activityInfo.packageName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            itemData.setImagaData(icon);
            objects.add(itemData);
            CustomAdapter customAdapater = new CustomAdapter(this, 0, objects);
            ListView listView = findViewById(R.id.list);
            listView.setAdapter(customAdapater);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                ResolveInfo ri1 = appInfoList.get(position);
                setHomeApp = Uri.fromParts("package", ri1.activityInfo.packageName, null).toString().replace("package:", "");
                setHomeName = ri1.loadLabel(pm).toString();
                bindDchaService();
            });
        }
    }

    private String getHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = this.getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(home, 0);
        ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
        return activityInfo.packageName;
    }

    private void bindDchaService() {
        Intent intent = new Intent(DCHA_SERVICE);
        intent.setPackage(PACKAGE_DCHASERVICE);
        bindService(intent, dchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //接続
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            try {
                mDchaService.clearDefaultPreferredApp(getHome());
                mDchaService.setDefaultPreferredHomeApp(setHomeApp);
            } catch (RemoteException ignored) {
            }
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(getApplicationContext(), "ホームを" + setHomeName + "に変更しました。", Toast.LENGTH_SHORT);
            toast.show();
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}