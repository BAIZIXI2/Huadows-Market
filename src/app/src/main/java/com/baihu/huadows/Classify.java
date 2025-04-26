package com.baihu.huadows;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baihu.huadows.LoadingAnimationUtils;
import com.baihu.huadows.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Classify extends AppCompatActivity {

    private ListView listView;
    private List<String> appNames;
    private List<String> appFolders;
    private static final String APPS_JSON_URL = "https://huadows.cn/apps.json";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify); // 使用您的布局文件

        listView = findViewById(R.id.listView);
        appNames = new ArrayList<>();
        appFolders = new ArrayList<>();

        // 启动加载动画
        LoadingAnimationUtils.startLoadingAnimation(this);

        // 获取附加值
        String extraData = getIntent().getStringExtra("extra_data");
        if (extraData != null) {
            loadApps(extraData);
        }
    }

    private void loadApps(String extraData) {
    new Thread(() -> {
        try {
            String jsonResponse = NetworkUtils.getJsonFromUrl(APPS_JSON_URL);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray appsArray = jsonObject.getJSONArray("apps");

            HashSet<String> categories = new HashSet<>();
            HashSet<Integer> ids = new HashSet<>();
            HashSet<String> names = new HashSet<>();

            // 处理传入的附加值，解析出 category、id 和 name
            for (String value : extraData.split(",")) {
                if (value.startsWith("id:")) {
                    ids.add(Integer.parseInt(value.substring(3).trim()));
                } else if (value.startsWith("category:")) {
                    categories.add(value.substring(9).trim());
                } else if (value.startsWith("name:")) {
                    names.add(value.substring(5).trim());
                }
            }

            for (int i = 0; i < appsArray.length(); i++) {
                JSONObject app = appsArray.getJSONObject(i);
                String appName = app.getString("name");
                String appFolder = app.getString("folder");
                String category = app.getString("category");
                int id = app.getInt("id");

                // 根据附加值筛选应用
                if (categories.contains(category) || ids.contains(id) || names.contains(appName)) {
                    appNames.add(appName);
                    appFolders.add(appFolder);
                }
            }

            runOnUiThread(() -> {
                AppAdapter adapter = new AppAdapter(this, appNames, appFolders);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedAppName = appNames.get(position);
                    String selectedAppFolder = appFolders.get(position);
                    Intent intent = new Intent(Classify.this, AppDetailsActivity.class);
                    intent.putExtra("app_name", selectedAppName);
                    intent.putExtra("app_folder", selectedAppFolder);
                    startActivity(intent);
                });

                // 数据加载完成，停止动画
                LoadingAnimationUtils.stopLoadingAnimation(Classify.this);
            });

        } catch (Exception e) {
            Log.e("Classify", "Error fetching data", e);
        }
    }).start();
}

}
