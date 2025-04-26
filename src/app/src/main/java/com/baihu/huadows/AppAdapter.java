package com.baihu.huadows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AppAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> appNames;
    private final List<String> appFolders;
    private final LruCache<String, Bitmap> imageCache; // 用于缓存图片

    public AppAdapter(Context context, List<String> appNames, List<String> appFolders) {
        super(context, R.layout.item_app, appNames);
        this.context = context;
        this.appNames = appNames;
        this.appFolders = appFolders;

        // 初始化 LruCache，用于缓存图片
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8; // 使用 1/8 的内存作为缓存
        imageCache = new LruCache<>(cacheSize);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
            holder = new ViewHolder();
            holder.appNameTextView = convertView.findViewById(R.id.appNameTextView);
            holder.appIconImageView = convertView.findViewById(R.id.appIconImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置应用名称
        String appName = appNames.get(position);
        String appFolder = appFolders.get(position);
        holder.appNameTextView.setText(appName);

        // 获取图标的 URL
        String iconUrl = "https://huadows.cn/apps/" + appFolder + "/icon.png";

        // 尝试从缓存中获取图片
        Bitmap cachedBitmap = imageCache.get(iconUrl);

        if (cachedBitmap != null) {
            // 如果缓存中有图片，直接使用
            holder.appIconImageView.setImageBitmap(cachedBitmap);
        } else {
            // 如果缓存中没有图片，显示默认图片，并异步加载
            holder.appIconImageView.setImageResource(R.mipmap.ic_launcher);
            loadImageFromUrl(iconUrl, holder.appIconImageView);
        }

        return convertView;
    }

    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // 将图片缓存到 LruCache 中
                if (bitmap != null) {
                    imageCache.put(urlString, bitmap);
                }

                // 在主线程更新 UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        // 加载失败时，保持默认图标
                        imageView.setImageResource(R.mipmap.ic_launcher);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                // 加载失败，显示默认图片
                new Handler(Looper.getMainLooper()).post(() -> imageView.setImageResource(R.mipmap.ic_launcher));
            }
        }).start();
    }

    // ViewHolder 内部类，用于复用视图
    static class ViewHolder {
        TextView appNameTextView;
        ImageView appIconImageView;
    }
}
