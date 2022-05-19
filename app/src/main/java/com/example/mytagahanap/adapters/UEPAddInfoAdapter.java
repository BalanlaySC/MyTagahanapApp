package com.example.mytagahanap.adapters;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.text.LineBreaker;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytagahanap.R;
import com.example.mytagahanap.fragments.SchoolInfoFragment;
import com.example.mytagahanap.models.UEPAddInfoModel;

import java.util.ArrayList;

public class UEPAddInfoAdapter extends RecyclerView.Adapter<UEPAddInfoAdapter.UEPAddInfoViewHolder> {
    private static final String TAG = "UEPAddInfoAdapter";
    private ArrayList<UEPAddInfoModel> mUEPAddInfo;
    private Resources mResources;
    private OnItemClickListener mListener;

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }

    public UEPAddInfoAdapter(ArrayList<UEPAddInfoModel> uepAddInfo, Resources resources) {
        mUEPAddInfo = uepAddInfo;
        mResources = resources;
    }

    public static class UEPAddInfoViewHolder extends RecyclerView.ViewHolder {
        public TextView cvAddInfoTitle, tvAddInfo;
        public ImageButton imgBtnExpand;
        public ImageView ivAddInfo;

        public UEPAddInfoViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            cvAddInfoTitle = itemView.findViewById(R.id.cvAddInfoTitle);
            tvAddInfo = itemView.findViewById(R.id.tvAddInfo);
            imgBtnExpand = itemView.findViewById(R.id.imgBtnExpand);
            ivAddInfo = itemView.findViewById(R.id.ivAddInfo);

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
    public UEPAddInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_school_info, parent, false);
        return new UEPAddInfoViewHolder(v, mListener);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onBindViewHolder(@NonNull UEPAddInfoViewHolder holder, int position) {
        UEPAddInfoModel currentUEPAddInfo = mUEPAddInfo.get(position);

        holder.cvAddInfoTitle.setText(currentUEPAddInfo.getTitleInfo());
        if(currentUEPAddInfo.getTitleInfo().equals("UEP's Vision") ||
                currentUEPAddInfo.getTitleInfo().equals("UEP's Mission")) {
            holder.tvAddInfo.setText(SchoolInfoFragment.createIndentedText(
                    currentUEPAddInfo.getExpandedText(), 100, 0));
            holder.tvAddInfo.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        } else {
            holder.tvAddInfo.setText(SchoolInfoFragment.createIndentedText(
                    currentUEPAddInfo.getExpandedText(), 0, 100));
            holder.tvAddInfo.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        boolean cardviewStatus = currentUEPAddInfo.isExpanded();
        if(cardviewStatus) {
            holder.tvAddInfo.setVisibility(View.VISIBLE);
            if (currentUEPAddInfo.getImgId() > 0) {
                holder.ivAddInfo.setImageResource(currentUEPAddInfo.getImgId());
                holder.ivAddInfo.setVisibility(View.VISIBLE);
            } else {
                holder.ivAddInfo.setVisibility(View.GONE);
            }
            holder.imgBtnExpand.setRotation(180F);
        } else {
            holder.tvAddInfo.setVisibility(View.GONE);
            holder.ivAddInfo.setVisibility(View.GONE);
            holder.imgBtnExpand.setRotation(0F);
        }
    }

    @Override
    public int getItemCount() { return mUEPAddInfo.size(); }
}