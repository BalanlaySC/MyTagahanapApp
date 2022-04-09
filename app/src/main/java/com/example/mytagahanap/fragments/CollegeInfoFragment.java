package com.example.mytagahanap.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytagahanap.R;
import com.example.mytagahanap.adapters.UEPAddInfoAdapter;
import com.example.mytagahanap.models.UEPAddInfoModel;

import java.util.ArrayList;

public class CollegeInfoFragment extends Fragment {

    @SuppressLint("InlinedApi")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context collegeInfoFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_college_info, container, false);

        ArrayList<UEPAddInfoModel> addInfos = new ArrayList<>();
        addInfos.add(new UEPAddInfoModel("College of Science",
                getString(R.string.collegeInfoCS), R.drawable.image_logo_cs));

        RecyclerView recvCollegeInfo = view.findViewById(R.id.recvCollegeInfo);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(collegeInfoFragmentContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        UEPAddInfoAdapter mAdapter = new UEPAddInfoAdapter(addInfos, getResources());

        recvCollegeInfo.setLayoutManager(mLayoutManager);
        recvCollegeInfo.setAdapter(mAdapter);
        recvCollegeInfo.smoothScrollToPosition(0);

        mAdapter.setOnItemClickListener(position -> {
            addInfos.get(position).setExpanded(!addInfos.get(position).isExpanded());
            mAdapter.notifyItemChanged(position);
        });
        return view;
    }
}
