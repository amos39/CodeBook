package com.amos.codebook3.ui.edit;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.amos.codebook3.R;
import com.amos.codebook3.data.DataBaseService;
import com.amos.codebook3.domain.DataObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 新增数据的 Fragment
 */
public class AddDataFragment extends Fragment {
    private TextInputEditText urlEdit, usernameEdit, passwordEdit, descriptionEdit;
    private boolean hasChanges = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);


        // 初始化视图
        Toolbar toolbar = view.findViewById(R.id.topbar);
        urlEdit = view.findViewById(R.id.edit_url);
        usernameEdit = view.findViewById(R.id.edit_username);
        passwordEdit = view.findViewById(R.id.edit_password);
        descriptionEdit = view.findViewById(R.id.edit_description);
        ExtendedFloatingActionButton saveButton = view.findViewById(R.id.fab_save);

        // 设置工具栏导航
        toolbar.setNavigationOnClickListener(v -> handleBackPress());

        // 设置文本变化监听
        setupTextChangeListeners();

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> handleSave());

        return view;
    }

    private void setupTextChangeListeners() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                hasChanges = true;
            }
        };

        urlEdit.addTextChangedListener(watcher);
        usernameEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);
        descriptionEdit.addTextChangedListener(watcher);
    }

    private void handleSave() {
        String url = urlEdit.getText().toString().trim();
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password)||TextUtils.isEmpty(description)) {
            //Snackbar.make(urlEdit, "URL、用户名和密码不能为空", Snackbar.LENGTH_SHORT).show();
            Toast.makeText(getContext(),"URL、用户名和密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

        DataObject newData = new DataObject();
        newData.url = url;
        newData.username = username;
        newData.password = password;
        newData.description = description;
        if (hasChanges) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("确认")
                    .setMessage("确定要添加内容吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        //保存数据
                        String mes=DataBaseService.insert(requireContext(),newData);
                        //打印结果
                        Toast.makeText(getContext(),mes,Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    })
                    .setNegativeButton("取消", null)
                    .show();

        } else {

           // Navigation.findNavController(requireView()).navigateUp();
        }
        //保存后会退出，无法显示snackbar
       // Snackbar.make(urlEdit, "保存成功", Snackbar.LENGTH_SHORT).show();
        //Navigation.findNavController(requireView()).navigateUp();
    }
    private void handleBackPress() {
        if (hasChanges) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("确认")
                    .setMessage("您有未保存的更改，确定要放弃吗？")
                    .setPositiveButton("确定", (dialog, which) -> 
                        Navigation.findNavController(requireView()).navigateUp())
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
