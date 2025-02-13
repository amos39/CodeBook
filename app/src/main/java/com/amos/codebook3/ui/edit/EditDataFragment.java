/*
   ______    ___   ______   ________  ______      ___      ___   ___  ____
 .' ___  | .'   `.|_   _ `.|_   __  ||_   _ \   .'   `.  .'   `.|_  ||_  _|
/ .'   \_|/  .-.  \ | | `. \ | |_ \_|  | |_) | /  .-.  \/  .-.  \ | |_/ /
| |       | |   | | | |  | | |  _| _   |  __'. | |   | || |   | | |  __'.
\ `.___.'\\  `-'  /_| |_.' /_| |__/ | _| |__) |\  `-'  /\  `-'  /_| |  \ \_
 `.____ .' `.___.'|______.'|________||_______/  `.___.'  `.___.'|____||____|

    https://github.com/amos39/CodeBook

*/
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 编辑数据的 Fragment
 */
public class EditDataFragment extends Fragment {

    private DataObject data;
    private TextInputEditText urlEdit, usernameEdit, passwordEdit, descriptionEdit;
    private boolean hasChanges = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_data, container, false);


        // 初始化视图
        Toolbar toolbar = view.findViewById(R.id.topbar);
        urlEdit = view.findViewById(R.id.edit_url);
        usernameEdit = view.findViewById(R.id.edit_username);
        passwordEdit = view.findViewById(R.id.edit_password);
        descriptionEdit = view.findViewById(R.id.edit_description);
        ExtendedFloatingActionButton saveButton = view.findViewById(R.id.fab_save);
        ExtendedFloatingActionButton deleteButton = view.findViewById(R.id.fab_delete);

        // 获取传入的数据
        if (getArguments() != null && getArguments().containsKey("data")) {
            data = (DataObject) getArguments().getSerializable("data");
            urlEdit.setText(data.url);
            usernameEdit.setText(data.username);
            passwordEdit.setText(data.password);
            descriptionEdit.setText(data.description);
        }

        // 设置工具栏导航
        toolbar.setNavigationOnClickListener(v -> handleBackPress());

        // 设置文本变化监听
        setupTextChangeListeners();

        // 设置按钮点击事件
        saveButton.setOnClickListener(v -> handleSave());
        deleteButton.setOnClickListener(v -> handleDelete());

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
                TextUtils.isEmpty(password) || TextUtils.isEmpty(description)) {
            Snackbar.make(urlEdit, "URL、用户名和密码不能为空", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (hasChanges) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("保存修改")
                    .setMessage("是否保存修改？")
                    .setPositiveButton("保存", (dialog, which) -> {
                        saveData(url, username, password, description);
                        //Toast.makeText(getContext(),"保存成功",Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    private void saveData(String url, String username, String password, String description) {
        data.url = url;
        data.username = username;
        data.password = password;
        data.description = description;

        String mes=DataBaseService.update(requireContext(),data);
        Toast.makeText(getContext(),mes,Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void handleDelete() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除确认")
                .setMessage("确定要删除这条记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    String mes=DataBaseService.delete(requireContext(),data);
                   // Snackbar.make(urlEdit, "删除成功", Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(getContext(),mes,Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("取消", null)
                .show();
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
        //repository.close();
    }
}
