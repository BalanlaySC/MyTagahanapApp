package com.example.mytagahanap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private ArrayList<LocationModel> mLocations;
    private OnItemClickListener mListener;

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    public LocationAdapter(ArrayList<LocationModel> locations) { mLocations = locations; }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        public TextView cvtxtLocationName;
        public RelativeLayout cvrlLocations;

        public LocationViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            cvtxtLocationName = itemView.findViewById(R.id.cvtxtLocationName);
            cvrlLocations = itemView.findViewById(R.id.cvrlLocations);

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
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_location, parent, false);
        return new LocationViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationModel currentLocation = mLocations.get(position);

        holder.cvtxtLocationName.setText(currentLocation.getLocationName());
        if(position % 2 == 1) {
            holder.cvrlLocations.setBackgroundColor(Color.parseColor("#aeaaaa"));
        } else {
            holder.cvrlLocations.setBackgroundColor(Color.parseColor("#cccccc"));
        }
    }

    @Override
    public int getItemCount() { return mLocations.size(); }
}