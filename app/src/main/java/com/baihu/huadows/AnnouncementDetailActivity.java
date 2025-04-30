package com.baihu.huadows;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class AnnouncementDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

        ImageView announcementImage = findViewById(R.id.announcementImage);
        TextView announcementTitle = findViewById(R.id.announcementTitle);
        TextView announcementContent = findViewById(R.id.announcementContent);

        // 获取传递的数据
        String content = getIntent().getStringExtra("content");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String title = getIntent().getStringExtra("title");

        // 设置标题和内容
        announcementTitle.setText(title);
        announcementContent.setText(content);

        // 加载公告图片
        Glide.with(this)
                .load(imageUrl)
                .transform(new RoundedCorners(30))
                .into(announcementImage);
    }
}
