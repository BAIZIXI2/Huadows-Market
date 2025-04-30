package com.baihu.huadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class DownloadFragment extends Fragment {
    private RecyclerView recyclerView;
    private DownloadAdapter downloadAdapter;
    private List<DownloadTask> downloadTasks = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化 SharedPreferences
        sharedPreferences = getContext().getSharedPreferences("download_prefs", Context.MODE_PRIVATE);

        // 加载保存的下载记录
        loadDownloadTasks();

        // 创建 DownloadAdapter
        downloadAdapter = new DownloadAdapter(getContext(), downloadTasks, this);
        recyclerView.setAdapter(downloadAdapter);

        return view;
    }

    // 添加下载任务，增加 totalSize 参数
    public void addDownloadTask(String appName, String url, long totalSize) {
        // 尝试解码 URL 防止转义符号问题
        String decodedUrl;
        try {
            decodedUrl = URLDecoder.decode(url, "UTF-8"); // 将链接解码
        } catch (Exception e) {
            decodedUrl = url; // 如果解码失败，使用原始 URL
            Log.e("DownloadFragment", "URL Decode Error: " + e.getMessage());
        }

        // 创建新的下载任务
        DownloadTask task = new DownloadTask(appName, decodedUrl, totalSize);
        downloadTasks.add(task);
        downloadAdapter.notifyItemInserted(downloadTasks.size() - 1);

        // 开始下载任务
        new Thread(() -> downloadFile(task)).start();
    }

  // 下载任务逻辑

  
    private void startDownload(DownloadTask task) {
        new Thread(() -> downloadFile(task)).start();
    }

    // 定义 downloadFile 方法
    private void downloadFile(DownloadTask task) {
        InputStream input = null;
        FileOutputStream output = null;

        try {
            // 创建 URL 对象
            URL downloadUrl = new URL(task.getUrl());
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "下载失败，服务器返回: " + responseCode, Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // 使用应用名称作为文件名
            String fileName = task.getAppName() + ".apk";
            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte[] data = new byte[4096];
            int totalDownloaded = 0; // 当前下载的字节数
            int count;

            // 开始下载文件
            while ((count = input.read(data)) != -1) {
                totalDownloaded += count;
                output.write(data, 0, count);

                // 更新进度
                final int progress = (int) ((float) totalDownloaded / task.getTotalSize() * 100);
                task.setProgress(progress);
                task.setDownloadedSize(totalDownloaded); // 更新已下载的字节数

                // 更新 UI
                getActivity().runOnUiThread(() -> {
                    downloadAdapter.notifyItemChanged(downloadTasks.indexOf(task));
                });
            }

            output.flush();
            task.setStatus(DownloadTask.STATUS_COMPLETED);
            getActivity().runOnUiThread(() -> {
                downloadAdapter.notifyItemChanged(downloadTasks.indexOf(task));
                Toast.makeText(getContext(), task.getAppName() + " 下载完成", Toast.LENGTH_SHORT).show();
                    // 下载完成时
downloadAdapter.updateDownloadStatus(downloadTasks.indexOf(task));
            });

            // 保存下载任务
            saveDownloadTasks();

        } catch (Exception e) {
            Log.e("DownloadError", "下载错误: " + e.getMessage(), e);
            task.setStatus(DownloadTask.STATUS_ERROR);
            getActivity().runOnUiThread(() -> {
                downloadAdapter.notifyItemChanged(downloadTasks.indexOf(task));
                Toast.makeText(getContext(), "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } finally {
            // 关闭流
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 其他必要的代码...





    // 保存下载任务记录
    public void saveDownloadTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("task_count", downloadTasks.size());
        for (int i = 0; i < downloadTasks.size(); i++) {
            DownloadTask task = downloadTasks.get(i);
            editor.putString("appName_" + i, task.getAppName());
            editor.putString("url_" + i, task.getUrl());
            editor.putLong("totalSize_" + i, task.getTotalSize());
            editor.putLong("downloadedSize_" + i, task.getDownloadedSize());
            editor.putInt("status_" + i, task.getStatus());
            editor.putInt("progress_" + i, task.getProgress());
            editor.putString("filePath_" + i, task.getFilePath());
        }
        editor.apply();
    }

    // 加载保存的下载任务记录
    // 修改 DownloadFragment.java 中的相关代码
private void loadDownloadTasks() {
    int taskCount = sharedPreferences.getInt("task_count", 0);
    for (int i = 0; i < taskCount; i++) {
        String appName = sharedPreferences.getString("appName_" + i, null);
        String url = sharedPreferences.getString("url_" + i, null);
        long totalSize = sharedPreferences.getLong("totalSize_" + i, 0);
        long downloadedSize = sharedPreferences.getLong("downloadedSize_" + i, 0);
        int status = sharedPreferences.getInt("status_" + i, DownloadTask.STATUS_PENDING);
        int progress = sharedPreferences.getInt("progress_" + i, 0);
        String filePath = sharedPreferences.getString("filePath_" + i, null);

        if (appName != null && url != null && filePath != null) {
            DownloadTask task = new DownloadTask(appName, url, totalSize);
            task.setDownloadedSize(downloadedSize);
            task.setProgress(progress);
            task.setStatus(status);
            // 使用 getFilePath() 方法获取文件路径
            // filePath 不再需要手动赋值
            downloadTasks.add(task);
        }
    }
}

}
