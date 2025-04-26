package com.baihu.huadows;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Appmanage extends AppCompatActivity {

    private ListView listView;
    private PackageManager packageManager;
    private List<AppOperationInfo> appList;
    private Appmanage_Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appmanage);

        listView = findViewById(R.id.app_list_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout); // 下拉刷新控件
        packageManager = getPackageManager();

        // 默认加载用户应用
        loadApps(false, false); // 加载用户应用

        // 列表项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                AppOperationInfo appInfo = appList.get(position);
                Intent intent = new Intent(Appmanage.this, AppOperationActivity.class);

                Bitmap appIconBitmap = drawableToBitmap(appInfo.getIcon());
                String appIconBase64 = bitmapToBase64(appIconBitmap);

                intent.putExtra("app_icon", appIconBase64);
                intent.putExtra("app_name", appInfo.getName());
                intent.putExtra("app_package_name", appInfo.getPackageName());
                intent.putExtra("is_system", appInfo.isSystemApp());

                startActivity(intent);
            }
        });

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshAppList(); // 刷新应用列表
            swipeRefreshLayout.setRefreshing(false); // 停止刷新动画
        });

        // 显示系统应用按钮
        Button showSystemAppsButton = findViewById(R.id.show_system_apps_button);
        showSystemAppsButton.setOnClickListener(v -> {
            loadApps(true, false); // 加载系统应用
            Toast.makeText(this, "显示系统应用", Toast.LENGTH_SHORT).show();
        });

        // 显示禁用应用按钮
        Button showDisabledAppsButton = findViewById(R.id.show_disabled_apps_button);
        showDisabledAppsButton.setOnClickListener(v -> {
            loadApps(false, true); // 只加载禁用应用
            Toast.makeText(this, "显示禁用应用", Toast.LENGTH_SHORT).show();
        });
    }

    // 加载应用的函数，支持过滤参数
    private void loadApps(boolean loadSystemApps, boolean loadDisabledApps) {
        new LoadAppsTask(loadSystemApps, loadDisabledApps).execute();
    }

    // 异步任务加载应用
private class LoadAppsTask extends AsyncTask<Void, List<AppOperationInfo>, List<AppOperationInfo>> {
    private boolean loadSystemApps;
    private boolean loadDisabledApps;

    LoadAppsTask(boolean loadSystemApps, boolean loadDisabledApps) {
        this.loadSystemApps = loadSystemApps;
        this.loadDisabledApps = loadDisabledApps;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        LoadingAnimationUtils.startLoadingAnimation(Appmanage.this); // 显示加载动画
    }

    @Override
    protected List<AppOperationInfo> doInBackground(Void... voids) {
        List<AppOperationInfo> apps = new ArrayList<>();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        for (PackageInfo packageInfo : packages) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            String appName = appInfo.loadLabel(packageManager).toString();
            Drawable appIcon = appInfo.loadIcon(packageManager);
            String packageName = appInfo.packageName;
            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            // 检查应用是否被禁用
            int appEnabledSetting = packageManager.getApplicationEnabledSetting(packageName);
            boolean isDisabled = (appEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                                  appEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER);

            // 根据传入参数过滤应用
            if (loadDisabledApps && isDisabled) {
                // 只加载禁用的应用
                apps.add(new AppOperationInfo(appName, appIcon, packageName, isSystemApp));
            } else if (!loadDisabledApps) {
                // 仅当不加载禁用应用时，才加载启用的系统应用或用户应用
                if (loadSystemApps && isSystemApp && !isDisabled) {
                    // 加载启用的系统应用
                    apps.add(new AppOperationInfo(appName, appIcon, packageName, isSystemApp));
                } else if (!loadSystemApps && !isSystemApp && !isDisabled) {
                    // 加载启用的用户应用
                    apps.add(new AppOperationInfo(appName, appIcon, packageName, isSystemApp));
                }
            }
        }
        return apps;
    }

    @Override
    protected void onPostExecute(List<AppOperationInfo> apps) {
        super.onPostExecute(apps);
        LoadingAnimationUtils.stopLoadingAnimation(Appmanage.this); // 隐藏加载动画
        appList = apps;
        adapter = new Appmanage_Adapter(Appmanage.this, appList);
        listView.setAdapter(adapter);
    }
}

    // 刷新应用列表
    private void refreshAppList() {
        loadApps(false, false); // 重新加载用户应用
    }

    // 将Drawable转换为Bitmap
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        drawable.draw(new Canvas(bitmap));
        return bitmap;
    }

    // 将Bitmap转换为Base64字符串
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
