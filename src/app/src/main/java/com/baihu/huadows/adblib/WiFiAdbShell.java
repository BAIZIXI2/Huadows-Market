package com.baihu.huadows.adblib;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.baihu.huadows.CustomToast;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class WiFiAdbShell {

    private static WiFiAdbShell instance;
    private AdbConnection connection;
    private AdbCell socket;
    private Context context;
    private boolean isConnected = false;
    private static final String PRIVATE_KEY_FILE = "private_key.pem";
    private static final String PUBLIC_KEY_FILE = "public_key.pem";

    private WiFiAdbShell(Context context) {
        this.context = context;
    }

    // 单例模式获取实例
    public static WiFiAdbShell getInstance(Context context) {
        if (instance == null) {
            instance = new WiFiAdbShell(context);
        }
        return instance;
    }

    // 提交并执行shell命令
    public void executeShellCommand(String command) {
        new AdbShellTask().execute(command);
    }

    // 任务类，用于异步执行adb shell命令
    private class AdbShellTask extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... commands) {
            try {
                if (!isConnected) {
                    // 检查并连接设备
                    connectDevice();
                }

                // 设备已连接，执行shell命令
                AdbStream stream = connection.open("shell:" + commands[0]);
                stream.write(commands[0] + "\n");

                // 读取返回的数据
                byte[] data = stream.read();
                publishProgress(new String(data));

                // 关闭流
                stream.close();

            } catch (IOException | InterruptedException e) {
                String errorMessage = "命令执行失败: " + e.getMessage();
                publishProgress(errorMessage);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // 显示执行结果
            String message = values[0];
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            copyToClipboard(message);  // 复制到剪贴板
        }

        // 连接设备
        private void connectDevice() throws IOException, InterruptedException {
            try {
                socket = new SocketCell(new java.net.Socket("127.0.0.1", 5555));  // 本设备的本地调试端口
                AdbCrypto crypto = loadOrCreateKeyPair();

                connection = AdbConnection.create(socket, crypto);
                connection.connect();
                isConnected = true;

                publishProgress("设备连接成功！");

            } catch (IOException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                isConnected = false;
                throw new IOException("连接设备失败: " + e.getMessage());
            }
        }

        // 加载或创建RSA密钥对
        private AdbCrypto loadOrCreateKeyPair() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
            File privateKeyFile = new File(context.getFilesDir(), PRIVATE_KEY_FILE);
            File publicKeyFile = new File(context.getFilesDir(), PUBLIC_KEY_FILE);

            if (privateKeyFile.exists() && publicKeyFile.exists()) {
                // 如果密钥文件存在，则加载密钥对
                return AdbCrypto.loadAdbKeyPair(new Base64Implementation(), privateKeyFile, publicKeyFile);
            } else {
                // 如果密钥文件不存在，则生成新的密钥对并保存
                AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new Base64Implementation());
                crypto.saveAdbKeyPair(privateKeyFile, publicKeyFile);
                return crypto;
            }
        }

        // 将错误信息复制到剪贴板
        private void copyToClipboard(String message) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ADB Error", message);
            clipboard.setPrimaryClip(clip);
        }
    }

    // 内部类，用于Base64编码
    private static class Base64Implementation implements AdbBase64 {
        @Override
        public String encodeToString(byte[] data) {
            return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
        }
    }
}
