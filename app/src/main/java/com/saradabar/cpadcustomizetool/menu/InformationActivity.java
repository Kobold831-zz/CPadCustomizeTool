package com.saradabar.cpadcustomizetool.menu;

import static com.saradabar.cpadcustomizetool.Common.Variable.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.saradabar.cpadcustomizetool.R;

public class InformationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        showInformation();
        findViewById(R.id.info_button).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WIKI_URL)));
            } catch (ActivityNotFoundException ignored) {
                if(toast != null) toast.cancel();
                toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        findViewById(R.id.download_button).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)));
            } catch (ActivityNotFoundException ignored) {
                if(toast != null) toast.cancel();
                toast = Toast.makeText(this, R.string.toast_unknown_activity, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showInformation() {
        PackageInfo pi = new PackageInfo();
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (Exception ignored) {
        }
        TextView text1 = findViewById(R.id.menu_text_1),
                text2 = findViewById(R.id.menu_text_2),
                text3 = findViewById(R.id.menu_text_3),
                text4 = findViewById(R.id.menu_text_4),
                text5 = findViewById(R.id.menu_text_5);
        text1.setText("アプリ名：" + getApplicationInfo().loadLabel(getPackageManager()).toString());
        text2.setText("パッケージ名：" + pi.packageName);
        text3.setText("バージョン：" + pi.versionName);
        text4.setText("バージョンコード：" + pi.versionCode);
        if (getIntent().getBooleanExtra("result", false)) text5.setText(R.string.info_app_state_use);
        else text5.setText(R.string.info_app_state_not_use);
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