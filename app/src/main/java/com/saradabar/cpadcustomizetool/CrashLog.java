package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.Common.getCrashLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;

import com.saradabar.cpadcustomizetool.check.UpdateActivity;
import com.saradabar.cpadcustomizetool.flagment.ApplicationSettingsFragment;
import com.saradabar.cpadcustomizetool.flagment.MainFragment;
import com.saradabar.cpadcustomizetool.menu.InformationActivity;

public class CrashLog extends Activity {

    TextView textView;
    ScrollView scrollView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_log);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        textView = (TextView)findViewById(R.id.textView);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        if (getCrashLog(this) != null) {
            addText(String.join(",", getCrashLog(this)));
        } else {
            addText("Empty crash log");
        }
    }

    private void addText(String status)
    {
        textView.append(status);
        int bottom = textView.getBottom() + scrollView.getPaddingBottom();
        int sy = scrollView.getScrollY();
        int sh = scrollView.getHeight();
        int delta = bottom - (sy + sh);
        scrollView.smoothScrollBy(0, delta);
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