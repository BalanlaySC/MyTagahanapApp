package com.example.mytagahanap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private ArrayList<SubjectModel> mClassSched;

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        public TextView cvtxtDescription, cvtxtSubjectCode,
                cvtxtClassID, cvtxtRoom, cvtxtTime, cvtxtDay;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            cvtxtDescription = itemView.findViewById(R.id.cvtxtDescription);
            cvtxtSubjectCode = itemView.findViewById(R.id.cvtxtSubjectCode);
            cvtxtClassID = itemView.findViewById(R.id.cvtxtClassID);
            cvtxtRoom = itemView.findViewById(R.id.cvtxtRoom);
            cvtxtTime = itemView.findViewById(R.id.cvtxtTime);
            cvtxtDay = itemView.findViewById(R.id.cvtxtDay);
        }
    }

    public SubjectAdapter(ArrayList<SubjectModel> classSched) {
        mClassSched = classSched;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_cardview, parent, false);
        SubjectViewHolder svh = new SubjectViewHolder(v);
        return svh;
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectModel currentSubject = mClassSched.get(position);

        holder.cvtxtDescription.setText(currentSubject.getmDescription());
        holder.cvtxtSubjectCode.setText(currentSubject.getmSubjectCode());
        holder.cvtxtClassID.setText(currentSubject.getmClassID());
        holder.cvtxtRoom.setText(currentSubject.getmRoom());
        holder.cvtxtTime.setText(currentSubject.getmTime());
        holder.cvtxtDay.setText(currentSubject.getmDay());
    }

    @Override
    public int getItemCount() {
        return mClassSched.size();
    }
}
