package com.example.mytagahanap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class ProponentAdapter extends RecyclerView.Adapter<ProponentAdapter.ProponentViewHolder> {
    private ArrayList<ProponentModel> mProponents;
    private OnItemClickListener mListener;

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    public ProponentAdapter(ArrayList<ProponentModel> proponents) { mProponents = proponents; }

    public static class ProponentViewHolder extends RecyclerView.ViewHolder {
        public TextView cvPropDisplayName, cvPropName, cvPropAge,
                cvPropCYS, cvPropAdrs;
        public RelativeLayout cvrlPropInfo;
        public ShapeableImageView cvimgPicture;

        public ProponentViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            cvPropDisplayName = itemView.findViewById(R.id.cvPropDisplayName);
            cvrlPropInfo = itemView.findViewById(R.id.cvrlPropInfo);
            cvPropName = itemView.findViewById(R.id.cvPropName);
            cvPropAge = itemView.findViewById(R.id.cvPropAge);
            cvPropCYS = itemView.findViewById(R.id.cvPropCYS);
            cvPropAdrs = itemView.findViewById(R.id.cvPropAdrs);
            cvimgPicture = itemView.findViewById(R.id.cvimgPicture);

            itemView.setOnClickListener(view -> {
                if(listener != null) {
                    int position = getBindingAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ProponentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_proponents, parent, false);
        return new ProponentViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProponentViewHolder holder, int position) {
        ProponentModel currentProponent = mProponents.get(position);

        holder.cvPropDisplayName.setText(currentProponent.getFullNameLF());
        holder.cvPropName.setText(currentProponent.getFullNameFL());
        holder.cvPropAge.setText(String.valueOf(currentProponent.getAge()));
        holder.cvPropCYS.setText(currentProponent.getCourse());
        holder.cvPropAdrs.setText(currentProponent.getAddress());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(10, 10, 10, 0);
        params.height = 300;
        params.width = 300;

        if(currentProponent.isExpanded()) {
            holder.cvrlPropInfo.setVisibility(View.VISIBLE);
            holder.cvPropDisplayName.setVisibility(View.GONE);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.height = 250;
            params.width = 250;
            holder.cvimgPicture.setLayoutParams(params);
        } else {
            holder.cvrlPropInfo.setVisibility(View.GONE);
            holder.cvPropDisplayName.setVisibility(View.VISIBLE);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            holder.cvimgPicture.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() { return mProponents.size(); }
}