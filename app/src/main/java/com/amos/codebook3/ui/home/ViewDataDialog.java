package com.amos.codebook3.ui.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.amos.codebook3.R;
import com.amos.codebook3.domain.DataObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;

/**
 * 查看数据对话框
 * 用于显示密码条目的详细信息，并提供复制和编辑功能
 */
public class ViewDataDialog extends DialogFragment {
    private static final String ARG_DATA = "data";
    
    // 界面元素
    private TextInputEditText urlText;
    private TextInputEditText usernameText;
    private TextInputEditText passwordText;
    private TextInputEditText descriptionText;
    private MaterialButton copyUrl;
    private MaterialButton copyUsername;
    private MaterialButton copyPassword;
    private MaterialButton copyDescription;
    private MaterialButton editButton;
    private MaterialButton closeButton;
    
    // 数据对象
    private DataObject data;


    /**
     * 创建对话框实例
     * @param data 要显示的数据对象
     * @return ViewDataDialog实例
     */
    public static ViewDataDialog newInstance(DataObject data) {
        ViewDataDialog dialog = new ViewDataDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATA, data);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_MaterialComponents_Light_Dialog);
        
        // 从参数中获取数据对象
        if (getArguments() != null) {
            data = (DataObject) getArguments().getSerializable(ARG_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_view_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        initViews(view);
        // 设置数据
        setData();
        // 设置点击事件
        setupClickListeners();
    }

    /**
     * 初始化视图组件
     */
    private void initViews(View view) {
        urlText = view.findViewById(R.id.urlText);
        usernameText = view.findViewById(R.id.usernameText);
        passwordText = view.findViewById(R.id.passwordText);
        descriptionText = view.findViewById(R.id.descriptionText);
        copyUrl=view.findViewById(R.id.copyUrl);
        copyUsername = view.findViewById(R.id.copyUsername);
        copyPassword = view.findViewById(R.id.copyPassword);
        copyDescription = view.findViewById(R.id.copyDescription);
        editButton = view.findViewById(R.id.editButton);
        closeButton = view.findViewById(R.id.closeButton);
    }

    /**
     * 设置数据到视图
     */
    private void setData() {
        if (data != null) {
            urlText.setText(data.url);
            usernameText.setText(data.username);
            passwordText.setText(data.password);
            descriptionText.setText(data.description);
        }
    }

    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        //复制url
        copyUrl.setOnClickListener(v->copyToClipboard("url",data.url));
        // 复制用户名
        copyUsername.setOnClickListener(v -> copyToClipboard("用户名", data.username));
        
        // 复制密码
        copyPassword.setOnClickListener(v -> copyToClipboard("密码", data.password));
        
        // 复制描述
        copyDescription.setOnClickListener(v -> copyToClipboard("描述", data.description));
        
        // 编辑按钮
        editButton.setOnClickListener(v -> {
            setEditClickListener(data);
        });
        
        // 关闭按钮
        closeButton.setOnClickListener(v -> dismiss());
    }

    /**
     * 复制文本到剪贴板
     * @param label 标签
     * @param text 要复制的文本
     */
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), label + "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置编辑按钮点击监听器
     */
    private void setEditClickListener(DataObject data){
        //TODO 在查看界面跳转到编辑界面
        Bundle args = new Bundle();
        args.putSerializable("data", (Serializable) data);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_home_to_edit_data, args);
        dismiss();
    }

}
