package com.example.mytagahanap.fragments;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytagahanap.globals.Constants;
import com.example.mytagahanap.R;
import com.example.mytagahanap.adapters.ProponentAdapter;
import com.example.mytagahanap.models.ProponentModel;

import java.util.ArrayList;

public class AboutAppFragment extends Fragment {
    private static final String TAG = "AboutAppFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context abtdevsFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_aboutapp, container, false);
        View view1 = inflater.inflate(R.layout.cardview_proponents, container, false);

        TextView aboutappIntro = view.findViewById(R.id.aboutappIntro);
        aboutappIntro.setText(SchoolInfoFragment.createIndentedText((String) aboutappIntro.getText(), 100, 0));
        aboutappIntro.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        ArrayList<ProponentModel> proponents = new ArrayList<>();
        proponents.add(Constants.proponent1);
        proponents.add(Constants.proponent2);

        RecyclerView proponentsRecView = view.findViewById(R.id.recvProponents);
        proponentsRecView.setHasFixedSize(false);
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
