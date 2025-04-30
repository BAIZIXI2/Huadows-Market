package com.baihu.huadows;

import android.graphics.drawable.Drawable;

public class AppOperationInfo {

    private String name;
    private Drawable icon;
    private String packageName;
    private boolean isSystemApp;

    public AppOperationInfo(String name, Drawable icon, String packageName, boolean isSystemApp) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.isSystemApp = isSystemApp;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }
}
