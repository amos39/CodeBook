package com.amos.codebook3.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amos.codebook3.R;
import com.amos.codebook3.domain.DataObject;

import java.util.List;

/**
 * RecyclerView 适配器，用于显示主页的数据列表
 */
public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.ViewHolder> {
    // 数据列表
    private List<DataObject> dataList;
    // 默认显示字段
    private String displayField = "url";
    // 项点击监听器
    private OnItemClickListener listener;

    /**
     * 项点击事件的接口
     */
    public interface OnItemClickListener {
        void onViewClick(DataObject data); // 查看按钮点击事件
        void onEditClick(DataObject data); // 编辑按钮点击事件
    }

    /**
     * 构造方法，初始化适配器
     *
     * @param dataList 数据列表
     * @param listener 项点击监听器
     */
    public DataListAdapter(List<DataObject> dataList, OnItemClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    /**
     * 设置显示字段
     *
     * @param field 字段名称
     */
    public void setDisplayField(String field) {
        this.displayField = field;
        notifyDataSetChanged(); // 通知数据已更改
    }

    /**
     * 更新数据列表
     *
     * @param newData 新数据列表
     */
    public void updateData(List<DataObject> newData) {
        if(newData == null){
            return;
        }
        this.dataList = newData;
        notifyDataSetChanged(); // 通知数据已更改
    }

    /**
     * 创建新的 ViewHolder
     *
     * @param parent ViewGroup 父视图
     * @param viewType 视图类型
     * @return 新的 ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据到 ViewHolder
     *
     * @param holder ViewHolder
     * @param position 数据位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataObject data = dataList.get(position);
        String displayText = "";
        switch (displayField) {
            case "url":
                displayText = data.url;
                break;
            case "username":
                displayText = data.username;
                break;
            case "description":
                displayText = data.description;
                break;
        }
        holder.textView.setText(displayText);

        holder.btnView.setOnClickListener(v -> listener.onViewClick(data));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(data));
    }

    /**
     * 获取数据列表项数
     *
     * @return 数据列表项数
     */
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * ViewHolder 类，用于缓存视图
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView; // 显示文本的 TextView
        ImageButton btnView; // 查看按钮
        ImageButton btnEdit; // 编辑按钮

        /**
         * 构造方法，初始化 ViewHolder
         *
         * @param view 视图
         */
        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text_item);
            btnView = view.findViewById(R.id.btn_view);
            btnEdit = view.findViewById(R.id.btn_edit);
        }
    }
}