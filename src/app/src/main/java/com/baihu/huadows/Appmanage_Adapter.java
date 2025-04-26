package com.baihu.huadows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.util.List;

public class Appmanage_Adapter extends BaseAdapter {

    private Context context;
    private List<AppOperationInfo> appList;

    public Appmanage_Adapter(Context context, List<AppOperationInfo> appList) {
        this.context = context;
        this.appList = appList;
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 使用ViewHolder模式来提高性能，避免重复调用findViewById
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_appmanage, parent, false);
            
            // 初始化ViewHolder
            viewHolder = new ViewHolder();
            viewHolder.appIcon = convertView.findViewById(R.id.app_icon);
            viewHolder.appName = convertView.findViewById(R.id.app_name);
            viewHolder.appPackageName = convertView.findViewById(R.id.app_package_name);
            viewHolder.appIsSystem = convertView.findViewById(R.id.app_is_system);
            
            convertView.setTag(viewHolder);  // 设置标签，以便下次复用
        } else {
            // 复用ViewHolder
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 获取当前的应用信息
        AppOperationInfo appInfo = appList.get(position);

        // 确保控件不为 null 后再设置文本
        if (viewHolder.appName != null && appInfo.getName() != null) {
            viewHolder.appName.setText(appInfo.getName());
        }

        if (viewHolder.appPackageName != null && appInfo.getPackageName() != null) {
            viewHolder.appPackageName.setText(appInfo.getPackageName());
        }

        if (viewHolder.appIsSystem != null) {
            String appType = appInfo.isSystemApp() ? "系统应用" : "用户应用";
            viewHolder.appIsSystem.setText(appType);
        }

        if (viewHolder.appIcon != null && appInfo.getIcon() != null) {
            viewHolder.appIcon.setImageDrawable(appInfo.getIcon());
        }

        return convertView;
    }

    // ViewHolder类用于缓存视图，提高性能
    static class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appPackageName;
        TextView appIsSystem;
    }
}
