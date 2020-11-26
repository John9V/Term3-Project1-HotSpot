package com.example.hotspot.ui.risks;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hotspot.R;
import com.example.hotspot.ui.home.HomeFragment;

public class RisksFragment extends Fragment {

    private RecyclerView riskList;

    public RisksFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_risks, container, false);
        riskList = view.findViewById(R.id.risk_list);
        RisksListAdapter adapter = new RisksListAdapter(HomeFragment.risks);
        riskList.setAdapter(adapter);
        riskList.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}