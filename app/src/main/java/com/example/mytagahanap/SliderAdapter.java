package com.example.mytagahanap;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<SliderItem> sliderItemList;
    ViewPager2 viewPager2;
    private Context context;
    private SubjectAdapter.OnItemClickListener mListener;
    final Handler handler = new Handler(Looper.getMainLooper());

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener(SubjectAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public SliderAdapter(List<SliderItem> sliderItemList, ViewPager2 viewPager2, Context context) {
        this.sliderItemList = sliderItemList;
        this.viewPager2 = viewPager2;
        this.context = context;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.slide_item_container,
                        parent,
                        false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.setImage(sliderItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return sliderItemList.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        private TouchImageView imageView;
        private ImageButton viewPagerCloseBtn;

        public SliderViewHolder(@NonNull View itemView, SubjectAdapter.OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
            viewPagerCloseBtn = itemView.findViewById(R.id.viewPagerCloseBtn);
            Log.d("SliderViewHolder Constructor", "SliderViewHolder: " + itemView.findViewById(R.id.imageSlide));
            Log.d("SliderViewHolder Constructor", "SliderViewHolder: " + itemView.findViewById(R.id.viewPagerCloseBtn));

            if (viewPagerCloseBtn != null){
                viewPagerCloseBtn.setOnClickListener(view -> {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                });
            }
        }

        void setImage(SliderItem sliderItem) {
            Glide.with(context)
                    .asBitmap()
                    .placeholder(R.drawable.image_placeholder)
                    .load(Uri.parse(sliderItem.getImgUrl()))
                    .into(imageView);
            handler.postDelayed(() -> Glide.with(context)
                    .asBitmap()
                    .placeholder(R.drawable.image_placeholder)
                    .load(Uri.parse(sliderItem.getImgUrl()))
                    .into(imageView), 5000);
        }
    }
}
