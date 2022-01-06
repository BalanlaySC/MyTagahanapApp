package com.example.mytagahanap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private ArrayList<SubjectModel> mClassSched;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

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

            cvimgDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public SubjectAdapter(ArrayList<SubjectModel> classSched) {
        mClassSched = classSched;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_cardview, parent, false);
        SubjectViewHolder svh = new SubjectViewHolder(v, mListener);
        return svh;
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectModel currentSubject = mClassSched.get(position);

        holder.cvtxtDescription.setText(currentSubject.getmDescription());
        if(holder.cvtxtDescription.getText().toString().compareTo("Unable to get subjects") == 0) {
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
    public int getItemCount() {
        return mClassSched.size();
    }
}