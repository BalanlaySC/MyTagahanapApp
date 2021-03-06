package com.example.mytagahanap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytagahanap.R;
import com.example.mytagahanap.models.SubjectModel;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    public static final int SUBJECT = 1;
    public static final int SUGGESTION = 2;
    private ArrayList<SubjectModel> mClassSched;
    private int mAdapterType;
    private OnItemClickListener mListener;

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        public TextView cvtxtDescription, cvtxtSubjectCode,
                cvtxtClassID, cvtxtRoom, cvtxtTime, cvtxtDay;
        private ImageButton cvimgDirections;

        public SubjectViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            cvtxtDescription = itemView.findViewById(R.id.cvtxtDescription);
            cvtxtSubjectCode = itemView.findViewById(R.id.cvtxtSubjectCode);
            cvtxtClassID = itemView.findViewById(R.id.cvtxtClassID);
            cvtxtRoom = itemView.findViewById(R.id.cvtxtRoom);
            cvtxtTime = itemView.findViewById(R.id.cvtxtTime);
            cvtxtDay = itemView.findViewById(R.id.cvtxtDay);
            cvimgDirections = itemView.findViewById(R.id.cvimgDirections);

            cvimgDirections.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public SubjectAdapter(ArrayList<SubjectModel> classSched, int adapterType) {
        mClassSched = classSched;
        mAdapterType = adapterType;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_subject, parent, false);
        return new SubjectViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectModel currentSubject = mClassSched.get(position);

        holder.cvtxtDescription.setText(currentSubject.getmDescription());
        if(holder.cvtxtDescription.getText().toString().contains("Unable")) {
            holder.cvtxtSubjectCode.setVisibility(View.GONE);
            holder.cvtxtClassID.setVisibility(View.GONE);
            holder.cvtxtRoom.setVisibility(View.GONE);
            holder.cvtxtTime.setVisibility(View.GONE);
            holder.cvtxtDay.setVisibility(View.GONE);
            holder.cvimgDirections.setVisibility(View.GONE);
        }
        holder.cvtxtSubjectCode.setText(currentSubject.getmSubjectCode());
        holder.cvtxtClassID.setText(currentSubject.getmClassID());
        holder.cvtxtRoom.setText(currentSubject.getmRoom());
        holder.cvtxtTime.setText(currentSubject.getmTime());
        holder.cvtxtDay.setText(currentSubject.getmDay());
    }

    @Override
    public int getItemCount() { return mClassSched.size(); }
}