package com.baihu.huadows;




import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityPermissionManagerActivity extends AppCompatActivity {

    private List<String> accessibilityServiceList;
    private ListView accessibilityListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_permission_manager);

        accessibilityListView = findViewById(R.id.accessibilityListView);
        Button allowButton = findViewById(R.id.allowButton);
        Button denyButton = findViewById(R.id.denyButton);

        // 加载所有可用的无障碍服务列表
        loadAllAccessibilityServices();

        // 允许选中的无障碍服务
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = accessibilityListView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    String selectedService = accessibilityServiceList.get(selectedPosition);
                    changeAccessibilityService(selectedService, true);
                } else {
                    Toast.makeText(AccessibilityPermissionManagerActivity.this, "请选择一个服务", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 拒绝选中的无障碍服务
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = accessibilityListView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    String selectedService = accessibilityServiceList.get(selectedPosition);
                    changeAccessibilityService(selectedService, false);
                } else {
                    Toast.makeText(AccessibilityPermissionManagerActivity.this, "请选择一个服务", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 加载所有可用的无障碍服务
    private void loadAllAccessibilityServices() {
        accessibilityServiceList = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // 获取所有无障碍服务的 Intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_ACCESSIBILITY_SERVICE);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentServices(intent, 0);

        // 遍历所有无障碍服务
        for (ResolveInfo resolveInfo : resolveInfoList) {
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            String packageName = serviceInfo.packageName;
            String className = serviceInfo.name;

            // 使用 PackageManager 加载服务的描述
            String serviceDescription = packageManager.getServiceInfo(serviceInfo, PackageManager.GET_META_DATA).loadDescription(packageManager).toString();

            // 组合服务名称、包名和描述
            accessibilityServiceList.add(packageName + "/" + className + ": " + serviceDescription);
        }

        // 如果没有找到服务，给出提示
        if (accessibilityServiceList.isEmpty()) {
            accessibilityServiceList.add("未找到可用的无障碍服务");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, accessibilityServiceList);
        accessibilityListView.setAdapter(adapter);
    }

    // 改变无障碍服务的权限
    private void changeAccessibilityService(String service, boolean allow) {
        try {
            String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            String newEnabledServices;

            if (allow) {
                if (TextUtils.isEmpty(enabledServices)) {
                    newEnabledServices = service;
                } else {
                    newEnabledServices = enabledServices + ":" + service;
                }
                Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, newEnabledServices);
                Toast.makeText(this, "已允许无障碍服务: " + service, Toast.LENGTH_SHORT).show();
            } else {
                if (enabledServices != null && enabledServices.contains(service)) {
                    newEnabledServices = enabledServices.replace(service, "").replace("::", ":").replaceAll(":$", "").replaceAll("^:", "");
                    Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, newEnabledServices);
                    Toast.makeText(this, "已拒绝无障碍服务: " + service, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "无障碍服务未启用: " + service, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "没有权限修改无障碍服务", Toast.LENGTH_SHORT).show();
        }
    }
}
