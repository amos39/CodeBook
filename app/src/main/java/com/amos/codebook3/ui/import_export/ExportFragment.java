/*
   ______    ___   ______   ________  ______      ___      ___   ___  ____
 .' ___  | .'   `.|_   _ `.|_   __  ||_   _ \   .'   `.  .'   `.|_  ||_  _|
/ .'   \_|/  .-.  \ | | `. \ | |_ \_|  | |_) | /  .-.  \/  .-.  \ | |_/ /
| |       | |   | | | |  | | |  _| _   |  __'. | |   | || |   | | |  __'.
\ `.___.'\\  `-'  /_| |_.' /_| |__/ | _| |__) |\  `-'  /\  `-'  /_| |  \ \_
 `.____ .' `.___.'|______.'|________||_______/  `.___.'  `.___.'|____||____|

    https://github.com/amos39/CodeBook

*/
package com.amos.codebook3.ui.import_export;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.amos.codebook3.domain.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//导出界面
public class ExportFragment extends Fragment {

    private ExportAdapter adapter;
    private List<DataObject> allData=null;
    private CheckBox checkboxSelectAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_export, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp()
        );

        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (adapter != null) {
                adapter.selectAll(isChecked);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 获取所有数据
//        allData = DataBaseService.getAllData(requireContext()).data;
        Result<List<DataObject>> result= DataBaseService.getAllData(requireContext());
        allData=result.data;
        if(result.message!=null){
            //发生错误则打印错误信息
            Toast.makeText(requireContext(),result.message,Toast.LENGTH_SHORT).show();
        }else{
            adapter = new ExportAdapter(allData);
            recyclerView.setAdapter(adapter);
            view.findViewById(R.id.btnExport).setOnClickListener(v -> exportSelectedData());
        }


    }

    private void exportSelectedData() {
        List<DataObject> selectedData = adapter.getSelectedData();
        if (selectedData.isEmpty()) {
            Toast.makeText(requireContext(), "请选择要导出的数据", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 创建备份目录
            File backupDir = new File(requireContext().getExternalFilesDir(null), "backup");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 创建导出文件
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File exportFile = new File(backupDir, "backup" + timestamp + ".json");

            // 使用Gson导出数据
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(selectedData);

            // 写入文件
            FileWriter writer = new FileWriter(exportFile);
            writer.write(jsonContent);
            writer.close();

            String message = String.format("已导出 %d 条数据到：\n%s", 
                selectedData.size(), exportFile.getAbsolutePath());
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

            Navigation.findNavController(requireView()).navigateUp();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

class ExportAdapter extends RecyclerView.Adapter<ExportAdapter.ViewHolder> {

    private final List<DataObject> dataList;
    private final List<Boolean> selectionStates;

    public ExportAdapter(List<DataObject> dataList) {
        this.dataList = dataList;
        this.selectionStates = new ArrayList<>(dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            selectionStates.add(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_export, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataObject data = dataList.get(position);
        holder.tvUrl.setText(data.url);
        holder.checkbox.setChecked(selectionStates.get(position));

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !selectionStates.get(position);
            selectionStates.set(position, newState);
            holder.checkbox.setChecked(newState);
        });

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> 
            selectionStates.set(position, isChecked)
        );
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void selectAll(boolean selected) {
        for (int i = 0; i < selectionStates.size(); i++) {
            selectionStates.set(i, selected);
        }
        notifyDataSetChanged();
    }

    public List<DataObject> getSelectedData() {
        List<DataObject> selectedData = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            if (selectionStates.get(i)) {
                selectedData.add(dataList.get(i));
            }
        }
        return selectedData;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        android.widget.TextView tvUrl;

        ViewHolder(View view) {
            super(view);
            checkbox = view.findViewById(R.id.checkbox);
            tvUrl = view.findViewById(R.id.tvUrl);
        }
    }
}
