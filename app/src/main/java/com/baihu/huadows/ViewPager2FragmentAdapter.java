// ViewPager2FragmentAdapter.java
package com.baihu.huadows;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPager2FragmentAdapter extends FragmentStateAdapter {

    private final Fragment[] fragments;

    public ViewPager2FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new Fragment[]{
            new HomeFragment(),  // 第一个 Fragment
            new AppListFragment(),       // 第二个 Fragment（主界面）
            new DownloadFragment()       // 第三个 Fragment（下载界面）
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length; // 页面数量
    }

    // 提供获取指定位置的 Fragment 实例方法
    public Fragment getFragment(int position) {
        return fragments[position];
    }
}
