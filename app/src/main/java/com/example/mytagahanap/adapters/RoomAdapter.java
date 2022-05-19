package com.example.mytagahanap.adapters;

import android.graphics.Color;
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
import com.example.mytagahanap.models.RoomModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> implements Filterable {
    private ArrayList<RoomModel> itemList;
    private ArrayList<RoomModel> origList;
    private OnItemClickListener listener;

    public RoomAdapter(ArrayList<RoomModel> itemList) {
        this.itemList = itemList;
        this.origList = new ArrayList<>(itemList);
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<RoomModel> filteredList = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(origList);
            } else {
                filteredList.addAll(itemList.stream()
                        .filter(obj -> obj.getLocationName().toLowerCase()
                                .contains(charSequence.toString().toLowerCase()) ||
                                obj.getRoomName().toLowerCase().contains(
                                        charSequence.toString().toLowerCase()))
                        .collect(Collectors.toList()));
                filteredList.sort(Comparator.comparing(RoomModel::getRoomName));
            }

            FilterResults filteredResults = new FilterResults();
            filteredResults.values = filteredList;
            return filteredResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            itemList.clear();
            itemList.addAll((Collection<? extends RoomModel>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void resetList() {
        itemList.clear();
        itemList.addAll(origList);
        notifyDataSetChanged();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        public TextView cvtxtLocationName;
        public TextView cvtxtBldgName;
        public RelativeLayout cvrlLocations;

        public RoomViewHolder(@NonNull View itemView, OnItemClickListener listener) {
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

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_location, parent, false);
        return new RoomViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomModel currentLocation = itemList.get(position);

        holder.cvtxtLocationName.setText(currentLocation.getRoomName());
        holder.cvtxtBldgName.setVisibility(View.VISIBLE);
        holder.cvtxtBldgName.setText(currentLocation.getLocationName());
        if(position % 2 == 1) {
            holder.cvrlLocations.setBackgroundColor(Color.parseColor("#aeaaaa"));
        } else {
            holder.cvrlLocations.setBackgroundColor(Color.parseColor("#cccccc"));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
