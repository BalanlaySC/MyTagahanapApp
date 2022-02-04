package com.example.mytagahanap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SchoolInfoFragment extends Fragment {

    @SuppressLint("InlinedApi")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context schInfoFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_school_info, container, false);
        View view1 = inflater.inflate(R.layout.cardview_school_info, container, false);

        TextView tvSchoolInfo = view.findViewById(R.id.tvSchoolInfo);
        tvSchoolInfo.setText(createIndentedText((String) tvSchoolInfo.getText(), 100, 0));
        tvSchoolInfo.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);


        ArrayList<UEPAddInfoModel> addInfos = new ArrayList<>();
        addInfos.add(new UEPAddInfoModel("The UEP Seal", getString(R.string.uepSealText), R.drawable.ueplogo));
        addInfos.add(new UEPAddInfoModel("UEP's Vision", getString(R.string.uepVisionText), 0));
        addInfos.add(new UEPAddInfoModel("UEP's Mission", getString(R.string.uepMissionText), 0));
        addInfos.add(new UEPAddInfoModel("Core Values", getString(R.string.coreValuesText), 0));
        addInfos.add(new UEPAddInfoModel("UEP Hymn", getString(R.string.uepHymnText), 0));

        RecyclerView uepaddInfoRecView = view.findViewById(R.id.recvSchoolInfo);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(schInfoFragmentContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        UEPAddInfoAdapter mAdapter = new UEPAddInfoAdapter(addInfos);

        uepaddInfoRecView.setLayoutManager(mLayoutManager);
        uepaddInfoRecView.setAdapter(mAdapter);
        uepaddInfoRecView.smoothScrollToPosition(0);

        mAdapter.setOnItemClickListener(position -> {
            addInfos.get(position).setExpanded(!addInfos.get(position).isExpanded());
            mAdapter.notifyItemChanged(position);
        });
        return view;
    }

    public static SpannableString createIndentedText(String text, int marginFirstLine, int marginNextLines) {
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0,text.length(),0);
        return result;
    }
}
