package com.amos.codebook3.ui.manage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.amos.codebook3.MyUtil.MyUtil;
import com.amos.codebook3.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 管理界面Fragment
 * 用于管理应用设置，如密码保护等功能
 */
public class ManageFragment extends Fragment {
    private static final String PREF_NAME = "settings";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_PASSWORD_ENABLED = "password_enabled";

    //启用密码开关
    private SwitchMaterial switchPassword;
    //设置密码按钮
    private MaterialButton btnOperateKey;
    private SharedPreferences preferences;

    //打开浏览器进行远程连接
    private MaterialButton btnConnectToServer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化SharedPreferences
        preferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // 初始化视图
        switchPassword = view.findViewById(R.id.switchPassword);
        btnOperateKey = view.findViewById(R.id.btnChangePassword);
        btnConnectToServer=view.findViewById(R.id.connect_server);
        
        // 设置密码开关状态
        boolean isPasswordEnabled = preferences.getBoolean(KEY_PASSWORD_ENABLED, false);
        switchPassword.setChecked(isPasswordEnabled);
        btnOperateKey.setVisibility(isPasswordEnabled ? View.VISIBLE : View.GONE);
        
        // 监听开关变化
        switchPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showSetPasswordDialog();
            } else {
                preferences.edit()
                        .putBoolean(KEY_PASSWORD_ENABLED, false)
                        .putString(KEY_PASSWORD_HASH, null)
                        .apply();
                btnOperateKey.setVisibility(View.GONE);
            }
        });
        
        // 修改密码按钮点击事件
        btnOperateKey.setOnClickListener(v -> showChangePasswordDialog());

        // 设置导入按钮监听器
        view.findViewById(R.id.inport_data).setOnClickListener(v -> {
            Navigation.findNavController(v)
                .navigate(R.id.importFragment);
        });

        // 设置导出按钮监听器
        view.findViewById(R.id.output_data).setOnClickListener(v -> {
            Navigation.findNavController(v)
                .navigate(R.id.exportFragment);
        });

        // 更改密钥
        view.findViewById(R.id.renew_database).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.navigation_change_key)
        );


        //设置连接远程服务器访问键监听器
        btnConnectToServer.setOnClickListener(v->{
            // TODO
//            // 创建一个包含 URL 的 Intent
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xxx.com"));
//            // 启动浏览器
//            startActivity(intent);
            Toast.makeText(requireContext(),"敬请期待...",Toast.LENGTH_SHORT).show();

        });
    }


    /**
     * 显示设置密码对话框
     */
    private void showSetPasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_password, null);
        
        TextInputLayout passwordLayout = dialogView.findViewById(R.id.passwordLayout);
        TextInputLayout confirmLayout = dialogView.findViewById(R.id.confirmPasswordLayout);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        TextInputEditText confirmInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("设置密码")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    String confirm = confirmInput.getText().toString();

                    if(password.isEmpty()){
                        Toast.makeText(getContext(), "密码为空，设置失败", Toast.LENGTH_LONG).show();
                        switchPassword.setChecked(false);
                        return;
                    }
                    if (!password.equals(confirm)) {
                        Toast.makeText(getContext(), "两次输入不一致，设置失败", Toast.LENGTH_LONG).show();
                        switchPassword.setChecked(false);
                        return;
                    }
                    
                    String passwordHash = MyUtil.hashPassword(password);
                    preferences.edit()
                            .putString(KEY_PASSWORD_HASH, passwordHash)
                            .putBoolean(KEY_PASSWORD_ENABLED, true)
                            .apply();
                    btnOperateKey.setVisibility(View.VISIBLE);
                    btnOperateKey.setText("修改密码");
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    switchPassword.setChecked(false);
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 显示更改密码对话框
     */
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_password, null);

        TextInputLayout passwordLayout = dialogView.findViewById(R.id.passwordLayout);
        TextInputLayout confirmLayout = dialogView.findViewById(R.id.confirmPasswordLayout);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        TextInputEditText confirmInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("修改密码")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    String confirm = confirmInput.getText().toString();

                    if(password.isEmpty()){
                        Toast.makeText(getContext(),"密码为空，修改失败",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!password.equals(confirm)) {
                        Toast.makeText(getContext(),"两次输入不一致，修改失败",Toast.LENGTH_LONG).show();
                        return;
                    }

                    String passwordHash = MyUtil.hashPassword(password);
                    preferences.edit()
                            .putString(KEY_PASSWORD_HASH, passwordHash)
                            .putBoolean(KEY_PASSWORD_ENABLED, true)
                            .apply();
                    btnOperateKey.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .setCancelable(false)
                .show();
    }


}
