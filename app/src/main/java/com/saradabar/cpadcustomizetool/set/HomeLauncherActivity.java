package com.saradabar.cpadcustomizetool.set;

import static com.saradabar.cpadcustomizetool.Common.Variable.DCHA_SERVICE;
import static com.saradabar.cpadcustomizetool.Common.Variable.PACKAGE_DCHASERVICE;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.saradabar.cpadcustomizetool.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class HomeLauncherActivity extends Activity {

    private IDchaService mDchaService;

    private String setHomeApp;

    private static HomeLauncherActivity instance = null;

    public static HomeLauncherActivity getInstance() {
        return instance;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        instance = this;
        Intent setPackageName = new Intent();
        setPackageName.setAction(Intent.ACTION_MAIN);
        setPackageName.addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> installedAppList = pm.queryIntentActivities(setPackageName, 0);
        final List<AppData> dataList = new ArrayList<>();
        for (ResolveInfo app : installedAppList) {
            AppData data = new AppData();
            data.label = app.loadLabel(pm).toString();
            data.icon = app.loadIcon(pm);
            data.packName = app.activityInfo.packageName;
            dataList.add(data);
        }

        final ListView listView = findViewById(R.id.launcher_list);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new AppListAdapter(this, dataList));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ResolveInfo app = installedAppList.get(position);
            setHomeApp = Uri.fromParts("package", app.activityInfo.packageName, null).toString().replace("package:", "");
            bindDchaService();
        });
    }

    private String getLauncherPackage() {
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

    /* 接続 */
    private final ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            try {
                mDchaService.clearDefaultPreferredApp(getLauncherPackage());
                mDchaService.setDefaultPreferredHomeApp(setHomeApp);
            } catch (RemoteException ignored) {
            }
            /* ListViewの更新 */
            final ListView listView = findViewById(R.id.launcher_list);
            listView.invalidateViews();
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    private static class AppData {
        String label;
        Drawable icon;
        String packName;
    }

    private static class AppListAdapter extends ArrayAdapter<AppData> {

        private final LayoutInflater mInflater;

        @SuppressLint("StaticFieldLeak")
        public static View view;

        public AppListAdapter(Context context, List<AppData> dataList) {
            super(context, R.layout.launcher_item);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder();


            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.launcher_item, parent, false);
                holder.textLabel = convertView.findViewById(R.id.launcher_text);
                holder.imageIcon = convertView.findViewById(R.id.launcher_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            view = convertView;

            final AppData data = getItem(position);

            holder.textLabel.setText(data.label);
            holder.imageIcon.setImageDrawable(data.icon);

            /* RadioButtonの更新 */
            RadioButton button = convertView.findViewById(R.id.launcher_button);
            button.setChecked(isLauncher(data.packName));

            return convertView;
        }

        /* ランチャーに設定されているかの確認 */
        private boolean isLauncher(String s1) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            PackageManager pm = HomeLauncherActivity.getInstance().getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(home, 0);
            ActivityInfo activityInfo = Objects.requireNonNull(resolveInfo).activityInfo;
            return Objects.equals(s1, activityInfo.packageName);
        }
    }

    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
    }

    /* メニュー選択 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}