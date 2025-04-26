package com.baihu.huadows;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private DownloadFragment downloadFragment;
    private LinearLayout indicatorLayout; // 页面指示器布局
    private int numPages = 3; // 假设有三个页面（控制指示器数量）
    private Handler handler = new Handler();
    private Runnable hideIndicatorRunnable; // 隐藏指示器的任务
    private boolean isIndicatorVisible = true; // 指示器是否可见

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定 ViewPager2
        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout); // 绑定指示器布局

        // 创建适配器并设置到 ViewPager2
        ViewPager2FragmentAdapter adapter = new ViewPager2FragmentAdapter(this);
        viewPager.setAdapter(adapter);

        // 设置默认显示页面
        viewPager.setCurrentItem(0); // 默认显示第一个Fragment
// 延迟切换到第二个页面，模拟虚假的主界面
        viewPager.postDelayed(() -> viewPager.setCurrentItem(1), 0); // 切换到 AppListFragment
        // 添加指示器点
        setupIndicator();

        // 添加页面变化监听器
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicator(position); // 更新指示器状态
                showIndicator(); // 显示指示器
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    resetHideIndicatorTimer(); // 重置隐藏指示器的定时器
                }
            }
        });

        // 通过适配器获取 DownloadFragment 实例
        downloadFragment = (DownloadFragment) adapter.getFragment(2); // 假设下载页面在第三个位置

        // 注册广播接收器来监听下载任务
        IntentFilter filter = new IntentFilter("com.baihu.huadows.ADD_DOWNLOAD_TASK");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String appName = intent.getStringExtra("appName");
                String url = intent.getStringExtra("url");
                String size = intent.getStringExtra("size");  // 获取传递的应用大小

                // 将大小转换为字节数 (假设是以 MB 表示)
                long totalSizeInBytes = convertSizeToBytes(size);

                if (downloadFragment != null) {
                    // 传递应用名称、URL 以及总大小给 DownloadFragment
                    downloadFragment.addDownloadTask(appName, url, totalSizeInBytes);
                }
            }
        }, filter);
    }

    private void setupIndicator() {
        for (int i = 0; i < numPages; i++) {
            TextView indicator = new TextView(this);
            indicator.setText("•"); // 使用点作为指示器
            indicator.setTextSize(80); // 默认大小
            indicator.setTextColor(getResources().getColor(android.R.color.white)); // 默认颜色
            indicatorLayout.addView(indicator);
        }
        updateIndicator(0); // 初始化时设置第一个点的状态
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            TextView indicator = (TextView) indicatorLayout.getChildAt(i);
            if (i == position) {
                animateIndicator(indicator, 100); // 当前选中的点变大
            } else {
                animateIndicator(indicator, 80); // 其他点恢复正常大小
            }
        }
    }

    private void animateIndicator(TextView indicator, float targetSize) {
        ValueAnimator animator = ValueAnimator.ofFloat(indicator.getTextSize(), targetSize);
        animator.addUpdateListener(valueAnimator -> {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            indicator.setTextSize(animatedValue / getResources().getDisplayMetrics().scaledDensity); // 设置文本大小
        });
        animator.setDuration(280); // 动画持续时间
        animator.start();
    }

    private void showIndicator() {
        if (!isIndicatorVisible) {
            isIndicatorVisible = true;
            indicatorLayout.setVisibility(View.VISIBLE);
            indicatorLayout.setTranslationY(indicatorLayout.getHeight()); // 初始位置在下方
            ValueAnimator animator = ValueAnimator.ofFloat(indicatorLayout.getHeight(), 0);
            animator.addUpdateListener(valueAnimator -> {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                indicatorLayout.setTranslationY(animatedValue);
            });
            animator.setDuration(280); // 动画持续时间
            animator.start();
            resetHideIndicatorTimer(); // 重置隐藏指示器的定时器
        }
    }

    private void hideIndicator() {
        if (isIndicatorVisible) {
            isIndicatorVisible = false;
            ValueAnimator animator = ValueAnimator.ofFloat(0, indicatorLayout.getHeight());
            animator.addUpdateListener(valueAnimator -> {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                indicatorLayout.setTranslationY(animatedValue);
            });
            animator.setDuration(280); // 动画持续时间
            animator.start();
        }
    }

    private void resetHideIndicatorTimer() {
        handler.removeCallbacks(hideIndicatorRunnable); // 移除之前的回调
        hideIndicatorRunnable = () -> hideIndicator(); // 创建新的隐藏任务
        handler.postDelayed(hideIndicatorRunnable, 3000); // 3秒后执行隐藏任务
    }

    // 将大小（字符串）转换为字节数的方法
    private long convertSizeToBytes(String size) {
        if (size.toLowerCase().contains("mb")) {
            return (long) (Double.parseDouble(size.replace("MB", "").trim()) * 1024 * 1024);
        } else if (size.toLowerCase().contains("kb")) {
            return (long) (Double.parseDouble(size.replace("KB", "").trim()) * 1024);
        }
        return 0;
    }

    // 提供一个方法从其他Activity访问DownloadFragment
    public DownloadFragment getDownloadFragment() {
        return downloadFragment;
    }
}
