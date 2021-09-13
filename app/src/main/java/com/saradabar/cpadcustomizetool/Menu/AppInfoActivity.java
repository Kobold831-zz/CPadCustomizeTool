package com.saradabar.cpadcustomizetool.Menu;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.saradabar.cpadcustomizetool.Common;
import com.saradabar.cpadcustomizetool.R;

import java.util.Objects;

public class AppInfoActivity extends Activity
{
    String appName;
    String appPackageName;
    String appVersion;
    int appVersionCode;
    PackageInfo pi;
    PackageManager pm;
    ApplicationInfo ap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        ActionBar actionBar = getActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        this.getPackageAppVersion();
    }

    @SuppressLint("SetTextI18n")
    private void getPackageAppVersion() {
        try {
            String packageName = getPackageName();
            pi = getPackageManager().getPackageInfo(packageName, 0);
            pm = getPackageManager();
            ap = getApplicationInfo();
        } catch (Exception ignored) {
        }
        appName = ap.loadLabel(pm).toString();
        appPackageName = pi.packageName;
        appVersion = pi.versionName;
        appVersionCode = pi.versionCode;
        TextView text1 = findViewById(R.id.menu_text_1);
        TextView text2 = findViewById(R.id.menu_text_2);
        TextView text3 = findViewById(R.id.menu_text_3);
        TextView text4 = findViewById(R.id.menu_text_4);
        TextView text5 = findViewById(R.id.menu_text_5);
        text1.setText("アプリ名:" + appName);
        text2.setText("パッケージ名:" + appPackageName);
        text3.setText("バージョン:" + appVersion);
        text4.setText("バージョンコード:" + appVersionCode);
        if (Common.Customizetool.NOT_USE == 1) {
            text5.setText(R.string.info_app_state_not_use);
        } else if (Common.Customizetool.NOT_USE == 0) {
            text5.setText(R.string.info_app_state_use);
        }
    }

    public void web(View view)
    {
        Uri uri = Uri.parse("https://ctabwiki.ml");
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}