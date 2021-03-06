package com.example.mytagahanap.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytagahanap.R;
import com.example.mytagahanap.models.LocationModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> implements Filterable {
    private final ArrayList<LocationModel> mLocations;
    private final ArrayList<LocationModel> mLocationsAll;
    private OnItemClickListener mListener;

    public LocationAdapter(ArrayList<LocationModel> locations) {
        mLocationsAll = new ArrayList<>(locations);
        mLocations = locations;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        // run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<LocationModel> filteredList = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(mLocationsAll);
            } else {
                filteredList.addAll(mLocations.stream()
                        .filter(obj -> obj.getLocationName().toLowerCase()
                                .contains(charSequence.toString().toLowerCase()))
                        .collect(Collectors.toList()));
            }

            FilterResults filteredResults = new FilterResults();
            filteredResults.values = filteredList;
            return filteredResults;
        }

        // run on a ui thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mLocations.clear();
            Log.d("LocationAdapter", "publishResults: " + mLocations.toString());
            mLocations.addAll((Collection<? extends LocationModel>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    public void resetList() {
        mLocations.clear();
        mLocations.addAll(mLocationsAll);
        notifyDataSetChanged();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        public TextView cvtxtLocationName;
        public TextView cvtxtBldgName;
        public RelativeLayout cvrlLocations;

        public LocationViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            cvtxtLocationName = itemView.findViewById(R.id.cvtxtLocationName);
            cvtxtBldgName = itemView.findViewById(R.id.cvtxtBldgName);
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