package com.example.mytagahanap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class EnlargeImageActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarge_image);
        Context enlargeImageContext = getApplicationContext();

        viewPager2 = findViewById(R.id.viewPagerImageSlider);

        List<SliderItem> sliderItemList = this.getIntent().getParcelableArrayListExtra("enlargedImage");
        SliderAdapter sliderAdapter = new SliderAdapter(sliderItemList, viewPager2, enlargeImageContext);

        sliderAdapter.notifyDataSetChanged();
        viewPager2.setAdapter(sliderAdapter);
//        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        sliderAdapter.setOnItemClickListener(position -> onBackPressed());
    }
}