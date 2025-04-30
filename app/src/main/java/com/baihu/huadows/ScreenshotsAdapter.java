package com.baihu.huadows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotsAdapter extends RecyclerView.Adapter<ScreenshotsAdapter.ScreenshotViewHolder> {

    private Context context;
    private String appFolder;
    private int maxScreenshots;
    private List<String> screenshotUrls = new ArrayList<>(); // 保存有效的截图链接

    public ScreenshotsAdapter(Context context, String appFolder, int maxScreenshots) {
        this.context = context;
        this.appFolder = appFolder;
        this.maxScreenshots = maxScreenshots;

        // 开始探测截图
        loadScreenshotUrls();
    }

    // 探测可用的截图链接
    private void loadScreenshotUrls() {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                List<String> availableUrls = new ArrayList<>();
                for (int i = 1; i <= maxScreenshots; i++) {
                    String imageUrl = "https://huadows.cn/apps/" + appFolder + "/" + i + ".png";
                    if (isImageAvailable(imageUrl)) {
                        availableUrls.add(imageUrl); // 只有链接可用时才添加
                    }
                }
                return availableUrls;
            }

            @Override
            protected void onPostExecute(List<String> availableUrls) {
                if (availableUrls.isEmpty()) {
                    // 如果没有有效的截图链接，提示并隐藏截图栏
                   // Toast.makeText(context, "没有可用的截图", Toast.LENGTH_SHORT).show();
                    CustomToast.showCustomToast(context, "该应用没有截图");
                } else {
                    screenshotUrls.addAll(availableUrls); // 添加有效的截图链接
                    notifyDataSetChanged(); // 更新适配器以显示可用的图片
                }
            }
        }.execute();
    }

    // 检测图片是否存在（返回 404 表示不可用）
    private boolean isImageAvailable(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 当无法访问时返回 false
        }
    }

    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.screenshot_item, parent, false);
        return new ScreenshotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        String imageUrl = screenshotUrls.get(position);
        holder.loadImage(imageUrl);
    }

    @Override
    public int getItemCount() {
        return screenshotUrls.size(); // 返回有效的截图数量
    }

    public class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        ImageView screenshotImageView;

        public ScreenshotViewHolder(@NonNull View itemView) {
            super(itemView);
            screenshotImageView = itemView.findViewById(R.id.screenshotImageView);
        }

        public void loadImage(String imageUrl) {
            new LoadImageTask().execute(imageUrl);
        }

        // 异步任务加载图片
        private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(String... strings) {
                String imageUrl = strings[0];
                Bitmap bitmap = null;
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    screenshotImageView.setImageBitmap(bitmap);
                } else {
                    // 加载失败时显示占位图
                    screenshotImageView.setImageResource(R.drawable.fixed_image); // 占位图
                }
            }
        }
    }
}
