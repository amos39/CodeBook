package com.amos.codebook3.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amos.codebook3.R;
import com.amos.codebook3.data.DataBaseService;
import com.amos.codebook3.domain.DataObject;
import com.amos.codebook3.domain.Result;
import com.amos.codebook3.ui.adapter.DataListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements DataListAdapter.OnItemClickListener {

    private DataListAdapter adapter;
    private EditText searchEdit;
    private Spinner sortSpinner;
    private String currentSortField = "url";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);



        // Setup RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DataListAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Setup search
        searchEdit = root.findViewById(R.id.edit_search);
        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // Setup spinner
        sortSpinner = root.findViewById(R.id.spinner_sort);
        String[] sortOptions = {"URL", "用户名", "描述"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, sortOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentSortField = "url";
                        break;
                    case 1:
                        currentSortField = "username";
                        break;
                    case 2:
                        currentSortField = "description";
                        break;
                }
                adapter.setDisplayField(currentSortField);
                performSearch();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Setup FAB
        FloatingActionButton addButton = root.findViewById(R.id.fab_add);
        addButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_add_data);
        });
//TODO Rewrite
        // Setup list item click event
//        adapter.setOnItemClickListener(data -> {
//            Bundle args = new Bundle();
//            args.putSerializable("data", data);
//            Navigation.findNavController(requireView())
//                    .navigate(R.id.action_home_to_edit_data, args);
//        });

        // Initial data load
        loadData();

        return root;
    }

    private void loadData() {
        Result<List<DataObject>> result=DataBaseService.getAllData(requireContext());
        if(result.message!=null){
            Toast.makeText(getContext(),result.message,Toast.LENGTH_SHORT).show();
        }
        List<DataObject> dataList = result.data;
        adapter.updateData(dataList);

    }

    private void performSearch() {

        String query = searchEdit.getText().toString();
        Result<List<DataObject>> result= DataBaseService.searchData(requireContext(),query, currentSortField);
        if(result.message!=null){
            Toast.makeText(getContext(),result.message,Toast.LENGTH_SHORT).show();
        }
        List<DataObject> list = result.data;
        adapter.updateData(list);
    }

    @Override
    public void onViewClick(DataObject data) {
        ViewDataDialog dialog = ViewDataDialog.newInstance(data);
        dialog.show(getChildFragmentManager(), "ViewData");
    }

    @Override
    public void onEditClick(DataObject data) {
        Bundle args = new Bundle();
        args.putSerializable("data", (Serializable) data);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_home_to_edit_data, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
