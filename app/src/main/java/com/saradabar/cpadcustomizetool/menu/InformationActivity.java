package com.saradabar.cpadcustomizetool.menu;

import static com.saradabar.cpadcustomizetool.Common.Variable.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Common;

public class InformationActivity extends Activity {

    String appName, appPackageName, appVersion;
    int appVersionCode;
    PackageInfo pi;
    PackageManager pm;
    ApplicationInfo ap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Button button1 = findViewById(R.id.info_button);
        showInformation();
        button1.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://ctabwiki.nerrog.net/");
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(i);
            } catch (ActivityNotFoundException ignored) {
                if(toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showInformation() {
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
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
        text1.setText("アプリ名：" + appName);
        text2.setText("パッケージ名：" + appPackageName);
        text3.setText("バージョン：" + appVersion);
        text4.setText("バージョンコード：" + appVersionCode);
        if (Common.Variable.USE_FLAG == 1) {
            text5.setText(R.string.info_app_state_not_use);
        } else if (Common.Variable.USE_FLAG == 0) {
            text5.setText(R.string.info_app_state_use);
        }
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