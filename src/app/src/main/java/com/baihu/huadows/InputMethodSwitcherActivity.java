package com.baihu.huadows;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class InputMethodSwitcherActivity extends AppCompatActivity {

    private List<InputMethodInfo> inputMethodList;
    private InputMethodManager inputMethodManager;
    private ListView inputMethodListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_method_switcher);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 加载输入法列表
        inputMethodList = inputMethodManager.getInputMethodList();

        inputMethodListView = findViewById(R.id.inputMethodListView);
        Button switchButton = findViewById(R.id.switchButton);

        // 显示输入法列表
        displayInputMethodList();

        // 切换到用户选择的输入法
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = inputMethodListView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    InputMethodInfo selectedInputMethod = inputMethodList.get(selectedPosition);
                    switchInputMethod(selectedInputMethod.getId());
                } else {
                    Toast.makeText(InputMethodSwitcherActivity.this, "请选择一个输入法", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 显示输入法列表
    private void displayInputMethodList() {
        List<String> inputMethodNames = new ArrayList<>();
        for (InputMethodInfo info : inputMethodList) {
            inputMethodNames.add(info.loadLabel(getPackageManager()).toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, inputMethodNames);
        inputMethodListView.setAdapter(adapter);
    }

    // 切换输入法
    private void switchInputMethod(String inputMethodId) {
        try {
            // 通过WRITE_SECURE_SETTINGS权限修改默认输入法
            Settings.Secure.putString(
                    getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD,
                    inputMethodId);
            Toast.makeText(this, "输入法已切换", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "没有权限切换输入法", Toast.LENGTH_SHORT).show();
        }
    }
}
