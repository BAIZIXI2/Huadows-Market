package com.baihu.huadows;

import android.os.Environment;

public class DownloadTask {
    public static final int STATUS_PENDING = 0;      // 等待下载
    public static final int STATUS_DOWNLOADING = 1;  // 正在下载
    public static final int STATUS_COMPLETED = 2;    // 下载完成
    public static final int STATUS_ERROR = 3;         // 下载失败

    private String appName;        // 应用名称
    private String url;            // 下载链接
    private String filePath;       // 文件路径
    private int progress;          // 下载进度
    private int status;            // 下载状态
    private long totalSize;        // 文件总大小
    private long downloadedSize;   // 已经下载的大小

    public DownloadTask(String appName, String url, long totalSize) {
        this.appName = appName;
        this.url = url;
        this.totalSize = totalSize;
        this.downloadedSize = 0;
        this.progress = 0;
        this.status = STATUS_PENDING;
        this.filePath =  Environment.getExternalStorageDirectory() + "/Android/data/com.baihu.huadows/files/Download/" + appName + ".apk";
    }

    // 新增的获取文件路径的方法
    public String getFilePath() {
        return filePath;
    }

    // 其他已有的方法保持不变



    public String getAppName() {
        return appName;
    }

    public String getUrl() {
        return url;
    }

    
    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
}
