package com.baihu.huadows;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.core.content.FileProvider;
import com.baihu.huadows.DownloadFragment;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import java.util.Iterator;
public class AppDetailsActivity extends AppCompatActivity {

    private ImageView appIconImageView;
    private TextView appNameTextView, publisherTextView, downloadsAndSizeTextView, descriptionTextView, changelogTextView, packageNameTextView;
    private Button downloadButton;
    private ViewPager2 screenshotsViewPager;
    private long downloadId;
    private DownloadManager downloadManager;
    private LinearLayout permissionsLayout;
    private String appFolder; // 声明 appFolder 变量
    private Map<String, String> permissionsMapping = new HashMap<>(); // 权限映射表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);//重写一个动画
        appIconImageView = findViewById(R.id.appIconImageView);
        appNameTextView = findViewById(R.id.appNameTextView);
        publisherTextView = findViewById(R.id.publisherTextView);
        downloadsAndSizeTextView = findViewById(R.id.downloadsAndSizeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        changelogTextView = findViewById(R.id.changelogTextView);
        packageNameTextView = findViewById(R.id.packageNameTextView);
        downloadButton = findViewById(R.id.downloadButton);
        screenshotsViewPager = findViewById(R.id.screenshotsViewPager);
        permissionsLayout = findViewById(R.id.permissionsLayout);

        // 启动加载动画
        LoadingAnimationUtils.startLoadingAnimation(this);
        CustomToast.showCustomToast(this, "如果持续加载请尝试退出页面然后重新进入！");

        Intent intent = getIntent();
        String appName = intent.getStringExtra("app_name");
        appFolder = intent.getStringExtra("app_folder");

        String detailsJsonUrl = "https://huadows.cn/apps/" + appFolder + "/details.json";
        String screenshotsBaseUrl = "https://huadows.cn/apps/" + appFolder + "/";

        // 确保 appFolder 不为空
        if (appFolder == null || appFolder.isEmpty()) {
            CustomToast.showCustomToast(this, "appFolder 为空，无法加载，请反馈给BAIZIXI");
            return;
        }

        // 加载权限映射表
        loadPermissionsMapping();

        new Thread(() -> {
            try {
                String jsonResponse = NetworkUtils.getJsonFromUrl(detailsJsonUrl);
                JSONObject jsonObject = new JSONObject(jsonResponse);

                runOnUiThread(() -> {
                    try {
                        // 填充详细信息
                        appNameTextView.setText(appName);
                        publisherTextView.setText("发布者: " + jsonObject.getString("publisher"));
                        downloadsAndSizeTextView.setText("下载量: " + jsonObject.getInt("downloads") + " | 大小: " + jsonObject.getString("size"));
                        descriptionTextView.setText("应用介绍: " + jsonObject.getString("description"));
                        changelogTextView.setText("更新日志: " + jsonObject.getString("changelog"));
                        packageNameTextView.setText("包名: " + jsonObject.getString("package_name"));

                        // 处理权限
                        String permissions = jsonObject.getString("permissions");
                        loadPermissions(permissions);

                       // 下载按钮
String downloadLink = jsonObject.getString("download_link");
downloadButton.setOnClickListener(v -> {
    // 显示自定义的 Toast 提示
    CustomToast.showCustomToast(this, "已加入下载列表");

    // 启动下载
    startDownload(downloadLink, appName);
});

                        // 加载应用图标
                        String iconUrl = screenshotsBaseUrl + "icon.png";
                        loadImageFromUrl(iconUrl, appIconImageView);

                        // 加载截图，传递 appFolder 到适配器
                        int maxScreenshots = 15;  // 设定最多截图数
                        ScreenshotsAdapter adapter = new ScreenshotsAdapter(AppDetailsActivity.this, appFolder, maxScreenshots);
                        screenshotsViewPager.setAdapter(adapter);

                    } catch (JSONException e) {
                        Log.e("AppDetailsActivity", "Error parsing JSON", e);
                    }
                    // 数据加载完成，停止动画
                    LoadingAnimationUtils.stopLoadingAnimation(this);
                });
            } catch (Exception e) {
                Log.e("AppDetailsActivity", "Error fetching app details", e);
            }
        }).start();
    }

    private void loadPermissionsMapping() {
    new Thread(() -> {
        try {
            String jsonResponse = NetworkUtils.getJsonFromUrl("https://huadows.cn/permissions_mapping.json");
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // 使用 keys() 方法获取键的迭代器
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);
                permissionsMapping.put(key, value);
            }

        } catch (Exception e) {
            Log.e("AppDetailsActivity", "Error loading permissions mapping", e);
        }
    }).start();
}


    private void loadPermissions(String permissionsString) {
        String[] permissionsList = permissionsString.split(",");
        for (String permission : permissionsList) {
            String trimmedPermission = permission.trim().replace("name='", "").replace("'", "");
            // 获取权限对应的中文描述
            String mappedPermission = getMappedPermission(trimmedPermission);
            TextView permissionTextView = new TextView(this);
            permissionTextView.setText(mappedPermission);
            permissionsLayout.addView(permissionTextView);
        }
    }

    private String getMappedPermission(String permission) {
        // 从 permissionsMapping 中查找对应的中文描述
        if (permissionsMapping.containsKey(permission)) {
            return permissionsMapping.get(permission);
        }
        return permission; // 如果映射表中没有该权限，则返回原始权限
    }

   private void startDownload(String url, String appName) {
    // 从 TextView 中获取应用大小，假设它已经被加载
    String sizeText = downloadsAndSizeTextView.getText().toString();
    String size = sizeText.split("\\|")[1].trim().replace("大小: ", "");

    Intent intent = new Intent("com.baihu.huadows.ADD_DOWNLOAD_TASK");
    intent.putExtra("appName", appName);
    intent.putExtra("url", url);
    intent.putExtra("size", size);  // 传递应用大小
    sendBroadcast(intent);
}






    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                Log.e("AppDetailsActivity", "Error loading image", e);
            }
        }).start();
    }

    private boolean isImageAvailable(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Log.e("AppDetailsActivity", "Error checking image availability", e);
            return false;
        }
    }
}
