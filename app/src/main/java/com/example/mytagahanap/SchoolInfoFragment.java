package com.example.mytagahanap;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SchoolInfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_info, container, false);
        Context schInfoFragmentContext = requireContext().getApplicationContext();

        TextView tvSchoolInfo = view.findViewById(R.id.tvSchoolInfo);
        tvSchoolInfo.setText(createIndentedText((String) tvSchoolInfo.getText(), 100, 0));
//        tvSchoolInfo.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        return view;
    }

    static SpannableString createIndentedText(String text, int marginFirstLine, int marginNextLines) {
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0,text.length(),0);
        return result;
    }
}
