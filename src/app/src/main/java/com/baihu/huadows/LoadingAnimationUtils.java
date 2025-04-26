package com.baihu.huadows;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class LoadingAnimationUtils {

    private static FrameLayout layoutContainer;
    private static ImageView imageView;
    private static View blackBackgroundView;

    public static void startLoadingAnimation(Context context) {
        // 创建容器布局
        if (layoutContainer == null) {
            layoutContainer = new FrameLayout(context);
            layoutContainer.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            // 创建黑幕背景 View
            blackBackgroundView = new View(context);
            blackBackgroundView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            blackBackgroundView.setBackgroundColor(Color.BLACK);

            // 创建 ImageView
            imageView = new ImageView(context);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);

            // 将黑幕背景和 ImageView 添加到布局容器中
            layoutContainer.addView(blackBackgroundView);
            layoutContainer.addView(imageView);

            // 将布局容器添加到 Activity 的根布局中
            if (context instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) context;
                ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
                rootView.addView(layoutContainer);
            }
        }

        // 开始动画
        imageView.setBackgroundResource(R.drawable.loading_animation);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        imageView.setVisibility(View.VISIBLE);
        blackBackgroundView.setVisibility(View.VISIBLE);
        animationDrawable.start();
    }

    public static void stopLoadingAnimation(Context context) {
        if (imageView != null && imageView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
            animationDrawable.stop();
        }

        // 隐藏动画和黑幕背景
        if (blackBackgroundView != null) {
            blackBackgroundView.setVisibility(View.GONE);
            if (layoutContainer != null) {
                layoutContainer.removeView(blackBackgroundView);
                layoutContainer.removeView(imageView);
                if (context instanceof android.app.Activity) {
                    android.app.Activity activity = (android.app.Activity) context;
                    ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
                    rootView.removeView(layoutContainer);
                }
                layoutContainer = null;
                imageView = null;
                blackBackgroundView = null;
            }
        }
    }
}
