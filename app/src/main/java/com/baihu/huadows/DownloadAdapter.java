package com.baihu.huadows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {
    private Context context;
    private List<DownloadTask> downloadTasks;
    private DownloadFragment downloadFragment;

    public DownloadAdapter(Context context, List<DownloadTask> downloadTasks, DownloadFragment downloadFragment) {
        this.context = context;
        this.downloadTasks = downloadTasks;
        this.downloadFragment = downloadFragment;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new DownloadViewHolder(view);
    }

 @Override
public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
    DownloadTask task = downloadTasks.get(position);
    holder.appName.setText(task.getAppName());

    // 获取下载进度百分比
    int progress = task.getProgress();

    // 设置进度条最大值为100（表示百分比）
    holder.progressBar.setMax(100);
    holder.progressBar.setProgress(progress);
    holder.progressText.setText(progress + "%");

    // 根据任务状态显示或隐藏进度条
    switch (task.getStatus()) {
        case DownloadTask.STATUS_DOWNLOADING:
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressText.setVisibility(View.VISIBLE);
            holder.progressText.setText("下载中...");
            break;

        case DownloadTask.STATUS_COMPLETED:
            holder.progressBar.setVisibility(View.GONE);
            holder.progressText.setText("下载完成");
            holder.progressText.setTextColor(context.getResources().getColor(android.R.color.holo_green_light)); // 成功时颜色为绿色
            holder.itemView.setOnClickListener(v -> {
                // 启动安装程序并传递 APK 路径
                Intent intent = new Intent(context, InstallActivity.class);
                intent.putExtra("APK_PATH", task.getFilePath()); // 传递文件路径
                context.startActivity(intent);
            });
            break;

        case DownloadTask.STATUS_ERROR:
            holder.progressBar.setVisibility(View.GONE);
            holder.progressText.setText("下载失败");
            holder.progressText.setTextColor(context.getResources().getColor(android.R.color.holo_red_light)); // 失败时颜色为红色
            break;

        default:
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressText.setText("下载中...");
            break;
    }

    // 检测文件是否存在
    File file = new File(task.getFilePath());
    if (file.exists()) {
        holder.progressText.setText("文件已下载，点击安装");
        holder.progressText.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
    }

    // 处理重试按钮的点击事件
    holder.retryButton.setOnClickListener(v -> {
        removeDownloadTask(position);
        downloadFragment.addDownloadTask(task.getAppName(), task.getUrl(), task.getTotalSize()); // 重试时重新添加任务
    });

    // 处理删除按钮的点击事件
    holder.deleteButton.setOnClickListener(v -> removeDownloadTask(position));

    // 长按删除下载记录
    holder.itemView.setOnLongClickListener(v -> {
        removeDownloadTask(position);
        return true;
    });
}

    @Override
    public int getItemCount() {
        return downloadTasks.size();
    }

    // 删除下载记录
    private void removeDownloadTask(int position) {
        DownloadTask task = downloadTasks.get(position);
        downloadTasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, downloadTasks.size());

        // 删除对应的下载文件
        File file = new File(task.getFilePath());
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                Toast.makeText(context, "无法删除下载文件: " + task.getAppName(), Toast.LENGTH_SHORT).show();
            }
        }

        // 更新SharedPreferences，移除任务
        downloadFragment.saveDownloadTasks(); // 保存任务记录
    }

    public void updateDownloadStatus(int position) {
        // 更新下载状态为完成
        DownloadTask task = downloadTasks.get(position);
        task.setStatus(DownloadTask.STATUS_COMPLETED);
        notifyItemChanged(position);
    }

    public void markDownloadFailed(int position) {
        // 标记下载失败
        DownloadTask task = downloadTasks.get(position);
        task.setStatus(DownloadTask.STATUS_ERROR);
        notifyItemChanged(position);
    }

    static class DownloadViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        ProgressBar progressBar;
        TextView progressText;
        ImageButton retryButton, deleteButton;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            progressBar = itemView.findViewById(R.id.download_progress);
            progressText = itemView.findViewById(R.id.download_progress_text);
            retryButton = itemView.findViewById(R.id.retry_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
