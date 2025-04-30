package com.baihu.huadows;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.baihu.huadows.adblib.WiFiAdbShell; // 导入WiFiAdbShell类
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AppOperationActivity extends AppCompatActivity {

    private PackageManager packageManager;
    private String packageName;

    private TextView appNameTextView;
    private TextView appVersionTextView;
    private TextView appIsSystemTextView;
    private TextView appPackageNameTextView; // 包名显示栏
    private TextView appDisabledTextView; // 应用禁用状态显示
    private ImageView appIconImageView;
    private Button permissionsButton;
    private Button uninstallButton; // 卸载按钮
    private Button launchButton; // 启动应用按钮
    private Button adbUninstallButton; // 无线调试卸载按钮
    private Button adbDisableButton; // 无线调试禁用按钮
private Button enableAppButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_operation);

        packageManager = getPackageManager();

        appNameTextView = findViewById(R.id.app_name);
        appVersionTextView = findViewById(R.id.app_version);
        appIsSystemTextView = findViewById(R.id.app_is_system);
        appPackageNameTextView = findViewById(R.id.app_package_name); // 绑定包名显示栏
        appDisabledTextView = findViewById(R.id.app_disabled_status); // 绑定禁用状态显示
        appIconImageView = findViewById(R.id.app_icon);
        permissionsButton = findViewById(R.id.permissions_button);
        uninstallButton = findViewById(R.id.uninstall_button); // 绑定卸载按钮
        launchButton = findViewById(R.id.launch_button); // 绑定启动应用按钮
        adbUninstallButton = findViewById(R.id.adb_uninstall_button); // 绑定无线调试卸载按钮
        adbDisableButton = findViewById(R.id.adb_disable_button); // 绑定无线调试禁用按钮
enableAppButton = findViewById(R.id.enable_app_button);
        // 获取传入的包名
        packageName = getIntent().getStringExtra("app_package_name");

        loadAppDetails(packageName);

        // 设置图标长按保存功能
        appIconImageView.setOnLongClickListener(v -> {
            saveIconToDCIM(appIconImageView.getDrawable(), appNameTextView.getText().toString());
            return true;
        });

        // 点击按钮打开权限管理界面
        permissionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + packageName));
            startActivity(intent);
        });

        // 点击卸载按钮
        uninstallButton.setOnClickListener(v -> {
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE);
            uninstallIntent.setData(android.net.Uri.parse("package:" + packageName));
            startActivity(uninstallIntent);
        });

        // 点击启动应用按钮
        launchButton.setOnClickListener(v -> {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                startActivity(launchIntent); // 启动应用主活动
            } else {
                Toast.makeText(this, "无法启动应用", Toast.LENGTH_SHORT).show();
            }
        });

        // 点击无线调试卸载按钮
        adbUninstallButton.setOnClickListener(v -> {
            confirmAdbUninstall();
        });

        // 点击无线调试禁用按钮
        adbDisableButton.setOnClickListener(v -> {
            confirmAdbDisable();
        });
       // 无线调试启用按钮
        enableAppButton.setOnClickListener(v -> {
            enableApp();
        });
    }

   private void loadAppDetails(String packageName) {
    try {
        PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        ApplicationInfo appInfo = packageInfo.applicationInfo;

        // 获取应用名称和版本号
        String appName = appInfo.loadLabel(packageManager).toString();
        String appVersion = packageInfo.versionName;

        // 加载应用图标
        Drawable appIcon = appInfo.loadIcon(packageManager);

        // 判断是否为系统应用
        boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

        // 设置文本视图和图标
        appNameTextView.setText(appName);
        appVersionTextView.setText("版本号: " + appVersion);
        appIsSystemTextView.setText(isSystemApp ? "系统应用" : "用户应用");
        appPackageNameTextView.setText("包名: " + packageName); // 设置包名显示

        // 检查应用是否被禁用
        int appEnabledSetting = packageManager.getApplicationEnabledSetting(packageName);
        boolean isDisabled = (appEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                              appEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER);

        if (isDisabled) {
            appDisabledTextView.setText("应用已被禁用");
            appDisabledTextView.setTextColor(getResources().getColor(R.color.red)); // 红色文字
        } else {
            appDisabledTextView.setText("应用正常");
            appDisabledTextView.setTextColor(getResources().getColor(R.color.green)); // 绿色文字
        }

        // 设置应用图标颜色
        appIsSystemTextView.setTextColor(isSystemApp ? getResources().getColor(R.color.orange) : getResources().getColor(R.color.green));
        appIconImageView.setImageDrawable(appIcon);

        // 显示权限信息
        loadPermissions(packageInfo.requestedPermissions);

    } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
        Toast.makeText(this, "应用信息加载失败", Toast.LENGTH_SHORT).show();
    }
}


    private void loadPermissions(String[] permissions) {
        if (permissions != null) {
            StringBuilder permissionsList = new StringBuilder("权限信息:\n");
            for (String permission : permissions) {
                try {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
                    permissionsList.append(permissionInfo.loadLabel(packageManager)).append("\n");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            // 将权限信息添加到底部
            TextView permissionsTextView = findViewById(R.id.permissions_text_view);
            permissionsTextView.setText(permissionsList.toString());
        }
    }

    private void saveIconToDCIM(Drawable drawable, String appName) {
        if (drawable == null) {
            Toast.makeText(this, "图标为空，无法保存", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = drawableToBitmap(drawable);
        File dcimDir = new File(getExternalFilesDir(null).getAbsolutePath() + "/DCIM/");
        if (!dcimDir.exists()) {
            dcimDir.mkdirs(); // 创建DCIM文件夹
        }
        File file = new File(dcimDir, appName + "_icon.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "图标已保存到DCIM", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // 确认无线调试卸载操作
    private void confirmAdbUninstall() {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("确认卸载")
                    .setMessage(isSystemApp ? "此应用是系统应用，卸载可能导致系统不稳定，您确定要卸载吗？" : "您确定要卸载此应用吗？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        if (isSystemApp) {
                            confirmSystemAppUninstall();
                        } else {
                            executeAdbUninstall(packageName);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 二次确认系统应用卸载
    private void confirmSystemAppUninstall() {
        new AlertDialog.Builder(this)
                .setTitle("二次确认")
                .setMessage("您确定要卸载此系统应用吗？")
                .setPositiveButton("确认", (dialog, which) -> executeAdbUninstall(packageName))
                .setNegativeButton("取消", null)
                .show();
    }

    // 执行卸载命令
    private void executeAdbUninstall(String packageName) {
        WiFiAdbShell.getInstance(this).executeShellCommand("pm uninstall " + packageName);
        Toast.makeText(this, "卸载命令已发送", Toast.LENGTH_SHORT).show();
    }

    // 确认无线调试禁用操作
    private void confirmAdbDisable() {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("确认禁用")
                    .setMessage(isSystemApp ? "此应用是系统应用，禁用可能导致系统不稳定，您确定要禁用吗？" : "您确定要禁用此应用吗？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        if (isSystemApp) {
                            confirmSystemAppDisable();
                        } else {
                            executeAdbDisable(packageName);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 二次确认系统应用禁用
    private void confirmSystemAppDisable() {
        new AlertDialog.Builder(this)
                .setTitle("二次确认")
                .setMessage("您确定要禁用此系统应用吗？")
                .setPositiveButton("确认", (dialog, which) -> executeAdbDisable(packageName))
                .setNegativeButton("取消", null)
                .show();
    }

    // 执行禁用命令
    private void executeAdbDisable(String packageName) {
WiFiAdbShell.getInstance(AppOperationActivity.this).executeShellCommand("pm disable-user " + packageName);
        Toast.makeText(this, "禁用命令已发送", Toast.LENGTH_SHORT).show();
    }
    
    
    
  

    private void enableApp() {
    try {
        String packageName = getIntent().getStringExtra("app_package_name"); // 从Intent中获取包名
        WiFiAdbShell.getInstance(this).executeShellCommand("pm enable " + packageName);
        Toast.makeText(this, "应用已启用", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "启用应用失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }
}
    
    
}
