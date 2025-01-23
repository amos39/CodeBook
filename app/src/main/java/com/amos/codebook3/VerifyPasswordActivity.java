package com.amos.codebook3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.amos.codebook3.MainActivity;
import com.amos.codebook3.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VerifyPasswordActivity extends AppCompatActivity {
    private static final String PREF_NAME = "settings";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_PASSWORD_ENABLED = "password_enabled";

    private TextInputEditText passwordInput;
    private MaterialButton btnConfirm;
    private MaterialButton btnExit;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isPasswordEnabled = preferences.getBoolean(KEY_PASSWORD_ENABLED, false);
        
        if (!isPasswordEnabled) {
            // 如果没有启用密码保护，直接启动主界面
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_verify_password);
        
        // 初始化视图
        passwordInput = findViewById(R.id.passwordInput);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnExit = findViewById(R.id.btnExit);

        // 设置点击事件
        btnConfirm.setOnClickListener(v -> verifyPassword());
        btnExit.setOnClickListener(v -> finish());
    }

    private void verifyPassword() {
        String inputPassword = passwordInput.getText().toString();
        if (inputPassword.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        String storedHash = preferences.getString(KEY_PASSWORD_HASH, "");
        String inputHash = hashPassword(inputPassword);

        if (storedHash.equals(inputHash)) {
            startMainActivity();
        } else {
            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
