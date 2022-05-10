package com.example.mytagahanap.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mytagahanap.models.EnlargedImageModel;
import com.example.mytagahanap.R;
import com.example.mytagahanap.models.TouchImageView;

import java.util.ArrayList;

public class EnlargeImageActivity extends AppCompatActivity {
    private static final String TAG = "EnlargeImageActivity";
    private TouchImageView tivEnlarged;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarge_image);
        Context enlargeImageContext = getApplicationContext();

        EnlargedImageModel s = this.getIntent().getParcelableExtra("enlargedImage");
        ArrayList<EnlargedImageModel> imageset = this.getIntent().getParcelableArrayListExtra("enlargedImage");
        tivEnlarged = findViewById(R.id.tivEnlarged);
        ImageButton enlargedCloseBtn = findViewById(R.id.enlargedCloseBtn);
        final int[] currentPosition = {0};
        if (s == null) {
            Glide.with(enlargeImageContext)
                    .asBitmap()
                    .placeholder(R.drawable.image_placeholder)
                    .load(Uri.parse(imageset.get(currentPosition[0]).getImgUrl()))
                    .into(tivEnlarged);
        } else {
            Glide.with(enlargeImageContext)
                    .asBitmap()
                    .load(Uri.parse(s.getImgUrl()))
                    .placeholder(R.drawable.image_placeholder)
                    .thumbnail(Glide.with(enlargeImageContext)
                            .asBitmap()
                            .load(s.getImgThumbUrl()))
                    .into(tivEnlarged);
        }

        enlargedCloseBtn.setOnClickListener(view -> onBackPressed());

        Button enlargeNextBtn = findViewById(R.id.enlargeNextBtn);
        enlargeNextBtn.setOnClickListener(view -> {
            currentPosition[0] += 1;
            if (currentPosition[0] > (imageset.size() - 1)) {
                currentPosition[0] = 0;
            }
            Log.d(TAG, "currentPosition = " + currentPosition[0]);
            try {
                Glide.with(enlargeImageContext)
                        .asBitmap()
                        .placeholder(R.drawable.image_placeholder)
                        .load(Uri.parse(imageset.get(currentPosition[0]).getImgUrl()))
                        .into(tivEnlarged);
            } catch (IndexOutOfBoundsException ignored) {
                Log.d(TAG, currentPosition[0] + " is out bounds");
            }
        });

        Button enlargePreviousBtn = findViewById(R.id.enlargePreviousBtn);
        if (imageset != null) {
            enlargePreviousBtn.setVisibility(View.VISIBLE);
            enlargeNextBtn.setVisibility(View.VISIBLE);
        } else {
            enlargePreviousBtn.setVisibility(View.GONE);
            enlargeNextBtn.setVisibility(View.GONE);
        }
        enlargePreviousBtn.setOnClickListener(view -> {
            currentPosition[0] -= 1;
            if (currentPosition[0] < 0) {
                currentPosition[0] = (imageset.size() - 1);
            }
            Log.d(TAG, "currentPosition = " + currentPosition[0]);
            try {
                Glide.with(enlargeImageContext)
                        .asBitmap()
                        .placeholder(R.drawable.image_placeholder)
                        .load(Uri.parse(imageset.get(currentPosition[0]).getImgUrl()))
                        .into(tivEnlarged);
            } catch (IndexOutOfBoundsException ignored) {
                Log.d(TAG, "currentPosition = " + currentPosition[0]);
            }
        });
    }
}