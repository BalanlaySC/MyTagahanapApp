package com.example.mytagahanap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AboutDevsFragment extends Fragment {
    private static final String TAG = "AboutDevsFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context abtdevsFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_aboutdevs, container, false);
        View view1 = inflater.inflate(R.layout.cardview_proponents, container, false);

        ArrayList<ProponentModel> proponents = new ArrayList<>();
        proponents.add(new ProponentModel("Samuel", "C.", "Balanlay",
                "UEP, Catarman N. Samar", "BSIT-4A", 22, R.drawable.prop1));
        proponents.add(new ProponentModel("Joyce", "M.", "DeGuzman",
                "Cawayan, Catarman N. Samar", "BSIT-4A", 21, R.drawable.prop2));

        RecyclerView proponentsRecView = view.findViewById(R.id.recvProponents);
        proponentsRecView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(abtdevsFragmentContext);
        ProponentAdapter mAdapter = new ProponentAdapter(proponents);

        proponentsRecView.setLayoutManager(mLayoutManager);
        proponentsRecView.setAdapter(mAdapter);
        RelativeLayout cvrlPropInfo = view1.findViewById(R.id.cvrlPropInfo);
        mAdapter.setOnItemClickListener(position -> {
            if(cvrlPropInfo.getVisibility() == View.GONE) {
                proponents.get(position).setExpanded(!proponents.get(position).isExpanded());
                mAdapter.notifyItemChanged(position);
            } else {
                proponents.get(position).setExpanded(!proponents.get(position).isExpanded());
                mAdapter.notifyItemChanged(position);
            }
        });

        return view;
    }
}
