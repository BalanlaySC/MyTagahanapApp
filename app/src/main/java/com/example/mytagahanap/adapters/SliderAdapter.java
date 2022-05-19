package com.example.mytagahanap.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mytagahanap.R;
import com.example.mytagahanap.globals.Utils;
import com.example.mytagahanap.models.SliderModel;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private static final String TAG = "SliderAdapter";
    private List<SliderModel> sliderModelList;
    private ViewPager2 viewPager2;

    public SliderAdapter(List<SliderModel> sliderModelList, ViewPager2 viewPager2) {
        this.sliderModelList = sliderModelList;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_slide_item_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        SliderModel currentSliderModel = sliderModelList.get(position);
        if (!currentSliderModel.getNumActiveUsers().equals("")) {
            holder.setActiveUsersText(currentSliderModel);
        } else if (currentSliderModel.getBarEntries() != null) {
            holder.setHorizontalBarChart(currentSliderModel);
        } else if (currentSliderModel.getPieEntries() != null) {
            holder.setPieChart(currentSliderModel);
        } else if (currentSliderModel.getLocations() != null &&
                !currentSliderModel.getAnalyticType().equals("")) {
            holder.setListChart(currentSliderModel);
        }
    }

    @Override
    public int getItemCount() {
        return sliderModelList.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        final int WHITE = itemView.getContext().getColor(R.color.white);
        private LinearLayout linearLayout, linearLayout2;
        private TextView textView, textView2;
        private HorizontalBarChart horizontalBarChart;
        private PieChart pieChart;
        private RecyclerView recyclerView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.analyticLinLayout);
            linearLayout2 = itemView.findViewById(R.id.analyticLinLayout2);
            textView = itemView.findViewById(R.id.analyticActiveUsersText);
            textView2 = itemView.findViewById(R.id.analyticListTitle);
            horizontalBarChart = itemView.findViewById(R.id.analyticHorBarChart);
            pieChart = itemView.findViewById(R.id.analyticPieChart);
            recyclerView = itemView.findViewById(R.id.analyticList);
        }

        void setActiveUsersText(SliderModel sliderModel) {
            if (sliderModel.getNumActiveUsers() != null) {
                textView.setText(Html.fromHtml(sliderModel.getNumActiveUsers(), Html.FROM_HTML_MODE_LEGACY));
                linearLayout.setVisibility(View.VISIBLE);
            }
        }

        void setHorizontalBarChart(SliderModel sliderModel) {
            if (sliderModel.getBarEntries() != null) {
                horizontalBarChart.setDrawBarShadow(false);
                horizontalBarChart.getDescription().setText(sliderModel.getAnalyticType());
                horizontalBarChart.getDescription().setTextColor(WHITE);
                horizontalBarChart.getDescription().setTypeface(Typeface.DEFAULT_BOLD);
                horizontalBarChart.getDescription().setTextSize(16f);
                horizontalBarChart.getDescription().setYOffset(-15);
                horizontalBarChart.getLegend().setEnabled(false);
                horizontalBarChart.setPinchZoom(false);
                horizontalBarChart.setDrawValueAboveBar(true);
                horizontalBarChart.setFitBars(true);
                horizontalBarChart.animateY(1000);
                horizontalBarChart.setScaleEnabled(false);
                horizontalBarChart.setDoubleTapToZoomEnabled(false);
                horizontalBarChart.setDragEnabled(false);

                XAxis xAxis = horizontalBarChart.getXAxis();
                xAxis.setDrawGridLines(false);
                xAxis.setEnabled(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawAxisLine(false);
                xAxis.setLabelCount(sliderModel.getLocations().size());
                xAxis.setTextColor(WHITE);
                xAxis.setTextSize(16f);
                xAxis.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return Utils.ellipsize(sliderModel.getLocations().get((int) value).getLocationName(), 10);
                    }
                });

                YAxis yAxisLeft = horizontalBarChart.getAxisLeft();
                yAxisLeft.setAxisMinimum(0f);
                yAxisLeft.setDrawGridLines(false);
                yAxisLeft.setEnabled(false);

                YAxis yAxisRight = horizontalBarChart.getAxisRight();
                yAxisRight.setDrawGridLines(false);
                yAxisRight.setEnabled(false);

                BarDataSet barDataSet = new BarDataSet(sliderModel.getBarEntries(), "Locations");
                barDataSet.setColor(WHITE);
                barDataSet.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value);
                    }
                });
                barDataSet.setValueTextColor(Color.WHITE);
                barDataSet.setValueTextSize(16f);
                barDataSet.setBarShadowColor(Color.argb(40, 150, 150, 150));

                BarData barData = new BarData(barDataSet);
                barData.setBarWidth(0.9f);
                horizontalBarChart.setData(barData);
                horizontalBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        Toast.makeText(itemView.getContext(), sliderModel.getLocations().get((int) e.getX())
                                + " count: " + (int) e.getY(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected() {

                    }
                });
                horizontalBarChart.setExtraOffsets(-10,-20,-10,-5);
                horizontalBarChart.invalidate();
                horizontalBarChart.setVisibility(View.VISIBLE);
            }
        }

        void setPieChart(SliderModel sliderModel) {
            Log.d(TAG, "setPieChart: started");
            if (sliderModel.getPieEntries() != null) {
                PieDataSet pieDataSet = new PieDataSet(sliderModel.getPieEntries(), "");
                pieDataSet.setColors(sliderModel.getColors());
                pieDataSet.setValueTextColor(Color.WHITE);
                pieDataSet.setValueTextSize(16f);

                PieData pieData = new PieData(pieDataSet);

                pieChart.setData(pieData);
                pieChart.getDescription().setEnabled(false);
                pieChart.getLegend().setTextColor(WHITE);
                pieChart.getLegend().setTextSize(16f);
                pieChart.getDescription().setText("");
                pieChart.setCenterText(sliderModel.getAnalyticType());
                pieChart.animateY(2000);
                pieChart.setVisibility(View.VISIBLE);
            }
        }

        void setListChart(SliderModel sliderModel) {
            Log.d(TAG, "setListChart: started");
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(itemView.getContext());
            LocationAdapter locationAdapter = new LocationAdapter(sliderModel.getLocations());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(locationAdapter);

            locationAdapter.setOnItemClickListener(position -> Toast.makeText(itemView.getContext(),
                    sliderModel.getLocations().get(position).getLocationName(), Toast.LENGTH_SHORT).show());

            textView2.setText(sliderModel.getAnalyticType());
            if (sliderModel.getLocations().isEmpty()) {
                textView2.setText(sliderModel.getAnalyticType() + "\n\n" + "No visited locations yet.");
            }
            linearLayout2.setVisibility(View.VISIBLE);
        }
    }
}
