package com.amos.codebook3.ui.import_export;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amos.codebook3.R;
import com.amos.codebook3.data.DataBaseService;
import com.amos.codebook3.domain.DataObject;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImportFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView tvPath;
    private TextView tvPreview;
    private MaterialButton btnImport;
    private BackupFileAdapter adapter;
    private File selectedFile;
    private File backupDir;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_import, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        recyclerView = view.findViewById(R.id.recyclerView);
        tvPath = view.findViewById(R.id.tvPath);
        tvPreview = view.findViewById(R.id.tvPreview);
        btnImport = view.findViewById(R.id.btnImport);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp()
        );

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BackupFileAdapter(this::onFileSelected, this::onDeleteFile);
        recyclerView.setAdapter(adapter);

        // 获取备份目录
        backupDir = new File(requireContext().getExternalFilesDir(null), "backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        tvPath.setText("备份文件目录：" + backupDir.getAbsolutePath());

        // 加载文件列表
        loadBackupFiles();

        // 设置导入按钮点击事件
        btnImport.setOnClickListener(v -> importSelectedFile());
    }

    private void loadBackupFiles() {
        File[] files = backupDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0) {
            tvPreview.setText("没有找到备份文件");
            return;
        }

        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        adapter.setFiles(Arrays.asList(files));
    }

    private void onDeleteFile(File file) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除文件 " + file.getName() + " 吗？")
            .setPositiveButton("删除", (d, which) -> {
                if (file.delete()) {
                    Toast.makeText(requireContext(), "文件已删除", Toast.LENGTH_SHORT).show();
                    if (file.equals(selectedFile)) {
                        selectedFile = null;
                        btnImport.setEnabled(false);
                        tvPreview.setText("");
                    }
                    loadBackupFiles();
                } else {
                    Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("取消", null)
            .create();

        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(android.R.color.black));
    }

    private void onFileSelected(File file) {
        selectedFile = file;
        btnImport.setEnabled(true);
        
        try {
            // 读取文件内容
            FileReader reader = new FileReader(file);
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
            reader.close();

            // 尝试格式化JSON
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type listType = new TypeToken<List<DataObject>>(){}.getType();
                List<DataObject> dataObjects = gson.fromJson(content.toString(), listType);
                String prettyJson = gson.toJson(dataObjects);
                tvPreview.setText(prettyJson);
            } catch (JsonSyntaxException e) {
                // 如果JSON格式化失败，显示原始内容
                tvPreview.setText(content.toString());
            }
        } catch (IOException e) {
            tvPreview.setText("无法读取文件内容：" + e.getMessage());
        }
    }

    private void importSelectedFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            Toast.makeText(requireContext(), "请选择要导入的文件", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            FileReader reader = new FileReader(selectedFile);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<DataObject>>(){}.getType();
            List<DataObject> dataObjects = gson.fromJson(reader, listType);
            reader.close();

            if (dataObjects == null || dataObjects.isEmpty()) {
                Toast.makeText(requireContext(), "无效的数据格式", Toast.LENGTH_SHORT).show();
                return;
            }

            // 验证数据格式
            boolean isValid = dataObjects.stream().allMatch(data -> 
                data.url != null && !data.url.isEmpty() &&
                data.username != null && !data.username.isEmpty() &&
                data.password != null && !data.password.isEmpty()
            );

            if (!isValid) {
                Toast.makeText(requireContext(), "数据格式不正确", Toast.LENGTH_SHORT).show();
                return;
            }

            // 导入数据
            int successCount = 0;
            for (DataObject data : dataObjects) {
                String result = DataBaseService.insert(requireContext(), data);
                if (result.equals("添加成功")) {
                    successCount++;
                }
            }

            Toast.makeText(requireContext(), 
                String.format("成功导入 %d/%d 条数据", successCount, dataObjects.size()),
                Toast.LENGTH_SHORT).show();

            Navigation.findNavController(requireView()).navigateUp();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "导入失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

class BackupFileAdapter extends RecyclerView.Adapter<BackupFileAdapter.ViewHolder> {
    private List<File> files = new ArrayList<>();
    private int selectedPosition = -1;
    private final OnFileSelectedListener listener;
    private final OnFileDeleteListener deleteListener;

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    public interface OnFileDeleteListener {
        void onDeleteFile(File file);
    }

    public BackupFileAdapter(OnFileSelectedListener listener, OnFileDeleteListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setFiles(List<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_backup_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        File file = files.get(position);
        holder.tvFileName.setText(file.getName());
        
        // 格式化文件大小
        String fileSize = String.format(Locale.getDefault(), "%.2f KB", file.length() / 1024.0);
        
        // 格式化修改时间
        String modifiedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(file.lastModified()));
        
        holder.tvFileSize.setText(String.format("%s  •  %s", fileSize, modifiedTime));
        holder.radioButton.setChecked(position == selectedPosition);

        View.OnClickListener clickListener = v -> {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onFileSelected(file);
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.radioButton.setOnClickListener(clickListener);
        
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteFile(file));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        final RadioButton radioButton;
        final TextView tvFileName;
        final TextView tvFileSize;
        final View btnDelete;

        ViewHolder(View view) {
            super(view);
            radioButton = view.findViewById(R.id.radioButton);
            tvFileName = view.findViewById(R.id.tvFileName);
            tvFileSize = view.findViewById(R.id.tvFileSize);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
