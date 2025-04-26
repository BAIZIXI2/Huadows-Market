package com.baihu.huadows;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.baihu.huadows.adblib.WiFiAdbShell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InstallActivity extends AppCompatActivity {
    private TextView commandTextView;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install); // 创建布局文件

        commandTextView = findViewById(R.id.command_text_view);

        // 获取安装路径
        String apkPath = getIntent().getStringExtra("APK_PATH");
        if (apkPath != null) {
            // 复制APK到应用内部存储目录
            String newApkPath = copyApkToInternalStorage(apkPath);
            if (newApkPath != null) {
                String packageName = getPackageNameFromApk(newApkPath);
                if (packageName != null) {
                    // 检查是否已安装相同包名的应用
                    if (isAppInstalled(packageName)) {
                        // 检查版本号
                        PackageInfo installedPackageInfo = getInstalledPackageInfo(packageName);
                        if (installedPackageInfo != null) {
                            // 获取安装包的版本号
                            PackageInfo apkPackageInfo = getApkPackageInfo(newApkPath);
                            if (apkPackageInfo != null) {
                                if (installedPackageInfo.versionCode >= apkPackageInfo.versionCode) {
                                    // 清理临时文件
                                    deleteTempFile(newApkPath);
                                    appendMessage("已安装的应用版本高于或等于当前安装包，取消安装。");
                                    return; // 取消安装
                                }
                            }
                        }
                    }
                    // 显示正在执行的命令
                    appendMessage("正在执行命令: pm install " + newApkPath);
                    // 执行 ADB 安装命令
                    executeInstallCommand(newApkPath);
                } else {
                    appendMessage("获取包名失败！");
                }
            } else {
                appendMessage("复制文件失败！");
            }
        }
    }

    private String copyApkToInternalStorage(String apkPath) {
        File sourceFile = new File(apkPath);
        File targetFile = new File(getExternalFilesDir(null), "app.apk");

        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return targetFile.getAbsolutePath(); // 返回新的APK路径
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void executeInstallCommand(String apkPath) {
        new Thread(() -> {
            // 执行命令（确保替换为实际的命令执行方法）
            String command = "pm install " + apkPath; // 确保此命令可执行
            WiFiAdbShell.getInstance(InstallActivity.this).executeShellCommand(command);;
            // 等待安装完成，假设等待时间为10秒
            try {
                Thread.sleep(10000); // 等待安装完成
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 删除临时的app.apk文件
            deleteTempFile(apkPath);

            // 自动关闭界面
            runOnUiThread(() -> finish());
        }).start();
    }

    private void deleteTempFile(String apkPath) {
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            boolean deleted = apkFile.delete();
            if (deleted) {
                appendMessage("临时文件已删除。");
            } else {
                appendMessage("删除临时文件失败。");
            }
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true; // 应用已安装
        } catch (PackageManager.NameNotFoundException e) {
            return false; // 应用未安装
        }
    }

    private PackageInfo getInstalledPackageInfo(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null; // 获取失败
        }
    }

    private PackageInfo getApkPackageInfo(String apkPath) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            return null; // 获取失败
        }
    }

    private String getPackageNameFromApk(String apkPath) {
        PackageInfo packageInfo = getApkPackageInfo(apkPath);
        return packageInfo != null ? packageInfo.packageName : null;
    }

    private void appendMessage(String message) {
        runOnUiThread(() -> {
            commandTextView.append("\n" + message);
            commandTextView.scrollTo(0, commandTextView.getBottom()); // 滚动到底部
        });
    }
}
