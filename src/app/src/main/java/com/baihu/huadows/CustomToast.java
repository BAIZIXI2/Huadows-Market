package com.baihu.huadows;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;

public class CustomToast {

    private static final int TOAST_DURATION = 2000;  // 每条消息显示的时间，单位：毫秒
    private static final int SPACING = 150;  // 消息之间的间距，单位：像素
    private static Queue<Toast> toastQueue = new LinkedList<>();  // 管理消息队列
    private static Handler handler = new Handler();

    // 在activity中的使用方法 CustomToast.showCustomToast(this, "提示消息！");
    //在Fragment中 CustomToast.showCustomToast(getContext(), "提示消息！");
    //在View CustomToast.showCustomToast(getContext(), "提示消息！");
    // 显示自定义的 Toast 消息
    public static void showCustomToast(Context context, String message) {
        // 获取 LayoutInflater 服务
        LayoutInflater inflater = LayoutInflater.from(context);

        // 加载自定义布局
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);

        // 设置图标（应用图标）
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.mipmap.ic_launcher);  // 你可以在这里换成其他图标

        // 设置消息文本
        TextView text = layout.findViewById(R.id.toast_message);
        text.setText(message);

        // 创建 Toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);  // 设置为短时间显示
        toast.setView(layout);  // 设置自定义布局

        // 添加到队列并设置显示
        toastQueue.add(toast);
        manageToastStack(context);
    }

    private static void manageToastStack(Context context) {
        // 如果队列为空，直接返回
        if (toastQueue.isEmpty()) return;

        // 获取队列中的所有 Toast
        Toast currentToast = toastQueue.peek();

        // 如果当前消息还未显示，则显示
        if (currentToast != null) {
            showStackedToast(currentToast, context, toastQueue.size() - 1);

            // 延迟隐藏当前消息并移除队列，显示下一条消息
            handler.postDelayed(() -> {
                currentToast.cancel();
                toastQueue.poll();  // 移除队列中的头部
                manageToastStack(context);  // 显示下一条消息
            }, TOAST_DURATION);
        }
    }

    private static void showStackedToast(Toast toast, Context context, int index) {
        // 设置 Toast 显示位置（堆叠效果）
        int yOffset = SPACING * index;  // 每条消息与前面的消息间隔一定的高度
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, yOffset + 100);  // 屏幕底部 100px 处开始显示
        toast.show();  // 显示 Toast
    }
}
