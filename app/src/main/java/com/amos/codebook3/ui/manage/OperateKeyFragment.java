package com.amos.codebook3.ui.manage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.amos.codebook3.R;
import com.amos.codebook3.data.DataBaseService;
import com.amos.codebook3.data.SPCollection;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;


public class OperateKeyFragment extends Fragment {
    private TextView tvMessage;
    private TextView tvName;
    private TextView tvVersion;
    private TextView tvKey;
    private TextInputEditText etNewKey;
    private MaterialButton btnChangeKey;
    private String currentKey;
    private MaterialButton btnDeleteKey;
    private  TextInputEditText etInputKey;
    private  MaterialButton btnInputKey;
    private MaterialButton btnResetDataBase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_key, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        tvMessage = view.findViewById(R.id.tvMessageOnChangeKey);
        tvName = view.findViewById(R.id.tvName);
        tvVersion = view.findViewById(R.id.tvVersion);
        tvKey = view.findViewById(R.id.tvKey);
        etNewKey = view.findViewById(R.id.etNewKey);
        btnChangeKey = view.findViewById(R.id.btnChangeKey);
        btnDeleteKey=view.findViewById(R.id.btnDeleteKey);
        etInputKey=view.findViewById(R.id.etInputKey);
        btnInputKey=view.findViewById(R.id.btnInputKey);
        btnResetDataBase=view.findViewById(R.id.btnResetDatabase);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp()
        );

        // 加载配置信息
        loadDatabaseInfo();

        // 设置输入按钮点击事件
        btnInputKey.setOnClickListener(v -> {
            String Key = etInputKey.getText().toString().trim();
            if (TextUtils.isEmpty(Key)) {
                Toast.makeText(requireContext(), "请输入密钥", Toast.LENGTH_SHORT).show();
                return;
            }

            //处理逻辑
           showConfirmDialogInput(Key);


        });

        // 设置更改按钮点击事件
        btnChangeKey.setOnClickListener(v -> {
            String newKey = etNewKey.getText().toString().trim();
            if (TextUtils.isEmpty(newKey)) {
                Toast.makeText(requireContext(), "请输入新的密钥", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newKey.equals(currentKey)) {
                Toast.makeText(requireContext(), "新密钥不能与当前密钥相同", Toast.LENGTH_SHORT).show();
                return;
            }
            //处理逻辑
            showConfirmDialogChange(newKey);
        });

        // 设置删除按钮点击事件
        btnDeleteKey.setOnClickListener(v -> {
            //处理逻辑
            showConfirmDialogDelete();
        });
        //设置重置数据库
        btnResetDataBase.setOnClickListener(v->{
            //
            showConfirmDialogResetDataBase();
        });

    }



    private void loadDatabaseInfo() {
        try {
            SharedPreferences preferences= getContext().getSharedPreferences(SPCollection.FN_DATABASE, Context.MODE_PRIVATE);
            String name = preferences.getString(SPCollection.KN_DB_NAME,null);
            Integer version = preferences.getInt(SPCollection.KN_DB_VERSION,-1);
            String key = preferences.getString(SPCollection.KN_DB_KEY,null);
            Integer keyIsSaved=preferences.getInt(SPCollection.KN_DB_KEY_IS_SAVED,-1);
            if (name != null) {
                tvName.setText("数据库名称：" + name);
            }

            if (version != -1) {
                tvVersion.setText("数据库版本：" + version);
            }

            if (keyIsSaved>0 && key != null ) {
                tvKey.setText("密钥：" + key);
                currentKey = key;
            }else{
                tvKey.setText("无密钥" );
                currentKey =null;
            }

        } catch (Exception e) {
            tvMessage.setText("加载配置信息失败：" + e.getMessage());
        }
    }

    private void showConfirmDialogChange(String newKey) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("确认更改")

            .setMessage("确定要更改数据库密钥吗？\n注意：更改密钥后需要使用新密钥才能访问数据。")
            .setPositiveButton("确定", (d, which) -> {
                if(currentKey == null){
                    Toast.makeText(requireContext(), "需要当前数据库密钥！" , Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String result= DataBaseService.renewDataBase(getContext(),newKey).message;
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();
                    loadDatabaseInfo(); // 刷新显示
                    etNewKey.setText(""); // 清空输入框
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "密钥更改失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }


            })
            .setNegativeButton("取消", null)
            .create();

        dialog.show();

        // 设置按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(android.R.color.black));
    }

    private void showConfirmDialogDelete() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除密钥吗？")
                .setPositiveButton("确定", (d, which) -> {
                    try {
                      SharedPreferences preferences= getContext().getSharedPreferences(SPCollection.FN_DATABASE,Context.MODE_PRIVATE);
                      SharedPreferences.Editor editor=preferences.edit();
                      editor.remove(SPCollection.KN_DB_KEY);
                      editor.putInt(SPCollection.KN_DB_KEY_IS_SAVED,0);
                      editor.apply();
                        Toast.makeText(requireContext(), "密钥删成功", Toast.LENGTH_SHORT).show();
                        loadDatabaseInfo();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "密钥删除失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();

        // 设置按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(android.R.color.holo_red_light));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(android.R.color.black));
    }


    private void showConfirmDialogInput(String inputKey) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("确认输入")
                .setMessage("确定要输入密钥吗？")
                .setPositiveButton("确定", (d, which) -> {
                    //将获取的密钥写入共享参数文件中
                    SharedPreferences preferences=getContext().getSharedPreferences(SPCollection.FN_DATABASE,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    try {
                        editor.putString(SPCollection.KN_DB_KEY, inputKey);
                        editor.putInt(SPCollection.KN_DB_KEY_IS_SAVED,1);
                        editor.apply();
                        Toast.makeText(requireContext(), "写入成功", Toast.LENGTH_SHORT).show();
                        loadDatabaseInfo();
                    }catch (Exception e){
                        Toast.makeText(requireContext(), "写入失败", Toast.LENGTH_SHORT).show();
                    }
                    //AppRestartHelper.restartApp(getContext(),2);
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();

        // 设置按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(android.R.color.holo_red_light));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(android.R.color.black));
    }




    //重置数据库对话框
    private void showConfirmDialogResetDataBase() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("确认重置")
                .setMessage("确定要重置数据库吗？")
                .setPositiveButton("确定", (d, which) -> {
                    SharedPreferences preferences= requireContext().getSharedPreferences(SPCollection.FN_DATABASE,Context.MODE_PRIVATE);
                    //重置数据库
                    String mes=DataBaseService.deleteDataBase(requireContext(),preferences.getString(SPCollection.KN_DB_NAME,null));
                    Toast.makeText(requireContext(),mes,Toast.LENGTH_SHORT).show();
                    //如果数据库删除失败则中止
                    if(!mes.equals("删除陈功")) return;

                    //删除共享参数
                    SharedPreferences.Editor editor= preferences.edit();
                    editor.clear();
                    editor.apply();
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();

        // 设置按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(android.R.color.holo_red_light));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(android.R.color.black));
    }
}
