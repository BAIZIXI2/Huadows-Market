package com.baihu.huadows;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private static final String JSON_URL = "https://huadows.cn/json/config.json";
    private static final String ANNOUNCEMENTS_URL = "https://huadows.cn/json/announcements/announcements.json"; // 公告 JSON URL

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageButton imageButtonLeft = view.findViewById(R.id.imageButtonLeft);
        ImageButton imageButtonRight = view.findViewById(R.id.imageButtonRight);
        
        // 设置默认图片
        setRoundedImage(imageButtonLeft, R.drawable.sample_image);
        setRoundedImage(imageButtonRight, R.drawable.sample_image);

        LinearLayout extraButton1 = view.findViewById(R.id.extraButton1);
        LinearLayout extraButton2 = view.findViewById(R.id.extraButton2);

        extraButton1.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), Appmanage.class);
    startActivity(intent);
           
           
            
                
                });
           extraButton2.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), AccessibilityPermissionManagerActivity.class);
    startActivity(intent);
           
           
            
                
                });

        GridLayout squareContainer = view.findViewById(R.id.squareContainer);
        loadSquareData(squareContainer);

        LinearLayout announcementLayout = view.findViewById(R.id.announcementLayout);
        loadAnnouncementData(announcementLayout);

        return view;
    }

    private void loadSquareData(GridLayout parentLayout) {
        new Thread(() -> {
            try {
                URL url = new URL(JSON_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                urlConnection.disconnect();

                parseJsonData(result.toString(), parentLayout);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("加载数据时发生错误: " + e.getMessage());
            }
        }).start();
    }

    private void parseJsonData(String jsonData, GridLayout parentLayout) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray largeButtons = jsonObject.getJSONArray("large_buttons");

            if (largeButtons.length() > 0) {
                JSONObject button1 = largeButtons.getJSONObject(0);
                String imageUrl1 = button1.getString("image");
                String extraValue1 = button1.optString("extra", "");
                addLargeButton(parentLayout, imageUrl1, extraValue1, R.id.imageButtonLeft);

                if (largeButtons.length() > 1) {
                    JSONObject button2 = largeButtons.getJSONObject(1);
                    String imageUrl2 = button2.getString("image");
                    String extraValue2 = button2.optString("extra", "");
                    addLargeButton(parentLayout, imageUrl2, extraValue2, R.id.imageButtonRight);
                }
            }

            JSONArray buttons = jsonObject.getJSONArray("buttons");
            for (int i = 0; i < buttons.length(); i++) {
                JSONObject button = buttons.getJSONObject(i);
                String text = button.getString("text");
                String imageUrl = button.getString("image");
                String extraValue = button.optString("extra", "");
                addSquareWithText(parentLayout, text, imageUrl, extraValue);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("解析 JSON 数据时发生错误: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Error Message", message);
            clipboard.setPrimaryClip(clip);
        });
    }

    private void setRoundedImage(ImageButton button, int imageResId) {
        Glide.with(this)
                .load(imageResId)
                .transform(new RoundedCorners(30))
                .into(button);
    }

    private void addLargeButton(GridLayout parentLayout, String imageUrl, String extraValue, int buttonId) {
        requireActivity().runOnUiThread(() -> {
            ImageButton button = getView().findViewById(buttonId);
            Glide.with(this)
                    .load(imageUrl)
                    .transform(new RoundedCorners(30))
                    .into(button);

            button.setOnClickListener(v -> {
                if (!extraValue.isEmpty()) {
                    Intent intent = new Intent(getContext(), Classify.class);
                    intent.putExtra("extra_data", extraValue);
                    startActivity(intent);
                } else {
                    showToast("附加值为空，无法打开新界面");
                }
            });
        });
    }

    private void addSquareWithText(GridLayout parentLayout, String text, String imageUrl, String extraValue) {
        requireActivity().runOnUiThread(() -> {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(0, 0, 0, 0);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

            LinearLayout squareLayout = new LinearLayout(getContext());
            squareLayout.setLayoutParams(params);
            squareLayout.setOrientation(LinearLayout.VERTICAL);
            squareLayout.setGravity(Gravity.CENTER);

            ImageView squareImage = new ImageView(getContext());
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(350, 350);
            imageParams.gravity = Gravity.CENTER;
            squareImage.setLayoutParams(imageParams);
            squareImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(this)
                    .load(imageUrl)
                    .transform(new RoundedCorners(30))
                    .into(squareImage);

            squareLayout.setOnClickListener(v -> {
                if (!extraValue.isEmpty()) {
                    Intent intent = new Intent(getContext(), Classify.class);
                    intent.putExtra("extra_data", extraValue);
                    startActivity(intent);
                } else {
                    showToast("附加值为空，无法打开新界面");
                }
            });

            TextView squareText = new TextView(getContext());
            squareText.setText(text);
            squareText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            squareText.setPadding(0, 8, 0, 0);

            squareLayout.addView(squareImage);
            squareLayout.addView(squareText);
            parentLayout.addView(squareLayout);
        });
    }

    private void loadAnnouncementData(LinearLayout announcementLayout) {
        new Thread(() -> {
            try {
                URL url = new URL(ANNOUNCEMENTS_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                urlConnection.disconnect();

                parseAnnouncementData(result.toString(), announcementLayout);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("加载公告时发生错误: " + e.getMessage());
            }
        }).start();
    }
private void parseAnnouncementData(String jsonData, LinearLayout announcementLayout) {
    try {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray announcements = jsonObject.getJSONArray("announcements");

        if (announcements.length() > 0) {
            JSONObject announcement = announcements.getJSONObject(0);
            String imageUrl = "https://huadows.cn/json/announcements/" + announcement.getString("image");
            String title = announcement.getString("title");
            String content = announcement.getString("content");

            requireActivity().runOnUiThread(() -> {
                // 创建 ImageView
                ImageView imageView = new ImageView(getContext());
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(imageParams);

                // 使用 Glide 加载图片并添加占位图和错误图
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.fixed_image) // 占位图
                        .error(R.drawable.fixed_image) // 错误图
                        .into(imageView);

                announcementLayout.addView(imageView);

                TextView titleView = new TextView(getContext());
                titleView.setText(title);
                titleView.setTextSize(18);
                titleView.setGravity(Gravity.CENTER);
                announcementLayout.addView(titleView);

                announcementLayout.setOnClickListener(v -> {
    Intent intent = new Intent(getContext(), AnnouncementDetailActivity.class);
    intent.putExtra("content", content);
    intent.putExtra("title", title);
    intent.putExtra("imageUrl", imageUrl); // 添加图片链接
    startActivity(intent);
});

                announcementLayout.setVisibility(View.VISIBLE);
            });
        }
    } catch (JSONException e) {
        e.printStackTrace();
        showToast("解析公告数据时发生错误: " + e.getMessage());
    }
}


}
