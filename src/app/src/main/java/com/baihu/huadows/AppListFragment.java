package com.baihu.huadows;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baihu.huadows.LoadingAnimationUtils;
import com.baihu.huadows.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.util.LruCache; // 导入 LruCache

public class AppListFragment extends Fragment {

    private ListView listView;
    private SearchView searchView;
    private List<String> appNames;
    private List<String> appFolders;
    private static final String APPS_JSON_URL = "https://huadows.cn/apps.json";
    private LruCache<String, Bitmap> imageCache;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.listView);
        searchView = view.findViewById(R.id.searchView);

        appNames = new ArrayList<>();
        appFolders = new ArrayList<>();

        // 初始化 LruCache
        final int cacheSize = 4 * 1024 * 1024; // 4MB
        imageCache = new LruCache<>(cacheSize);

        // 启动加载动画
        LoadingAnimationUtils.startLoadingAnimation(getActivity());

        new Thread(() -> {
            try {
                // 获取应用列表
                String jsonResponse = NetworkUtils.getJsonFromUrl(APPS_JSON_URL);
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray appsArray = jsonObject.getJSONArray("apps");

                for (int i = 0; i < appsArray.length(); i++) {
                    JSONObject app = appsArray.getJSONObject(i);
                    String appName = app.getString("name");
                    String appFolder = app.getString("folder");
                    appNames.add(appName);
                    appFolders.add(appFolder);
                }

                getActivity().runOnUiThread(() -> {
                    AppAdapter adapter = new AppAdapter(getContext(), appNames, appFolders);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((parent, view1, position, id) -> {
    String selectedAppName = appNames.get(position);
    String selectedAppFolder = appFolders.get(position);  // 获取对应的 appFolder
    Intent intent = new Intent(getActivity(), AppDetailsActivity.class);
    intent.putExtra("app_name", selectedAppName);
    intent.putExtra("app_folder", selectedAppFolder);  // 传递 appFolder
    startActivity(intent);
});

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            adapter.getFilter().filter(newText);
                            return true;
                        }
                    });

                    // 数据加载完成，停止动画
                    LoadingAnimationUtils.stopLoadingAnimation(getActivity());
                });

            } catch (Exception e) {
                Log.e("AppListFragment", "Error fetching data", e);
            }
        }).start();
    }

    private void loadImageFromUrl(String urlString, ImageView imageView) {
        Bitmap cachedBitmap = imageCache.get(urlString);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                imageCache.put(urlString, bitmap);

                getActivity().runOnUiThread(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                Log.e("AppListFragment", "Error loading image", e);
            }
        }).start();
    }
}
