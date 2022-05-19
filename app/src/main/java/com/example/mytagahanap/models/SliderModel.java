package com.example.mytagahanap.models;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class SliderModel {
    private int numActiveUsers;
    private String analyticType;
    private ArrayList<BarEntry> barEntries;
    private ArrayList<LocationModel> locations;
    private ArrayList<Integer> colors;
    private ArrayList<PieEntry> pieEntries;

    public SliderModel(int numActiveUsers) {
        this.numActiveUsers = numActiveUsers;
    }

    public SliderModel(ArrayList<PieEntry> pieData, ArrayList<Integer> colors, String type) {
        this.pieEntries = pieData;
        this.colors = colors;
        this.analyticType = type;
    }

    public SliderModel(ArrayList<LocationModel> locations, String type) {
        this.locations = locations;
        this.analyticType = type;
    }

    public SliderModel(ArrayList<BarEntry> barData, ArrayList<LocationModel> locations, String type, String period) {
        this.barEntries = barData;
        this.locations = locations;
        this.analyticType = type;
    }

    public String getNumActiveUsers() {
        if (numActiveUsers == 0)
            return "";
        else
            return String.format("CURRENT ACTIVE USERS: <b>%s</b>", numActiveUsers);
    }

    public ArrayList<BarEntry> getBarEntries() {
        return barEntries;
    }

    public ArrayList<PieEntry> getPieEntries() {
        return pieEntries;
    }

    public ArrayList<Integer> getColors() {
        return colors;
    }

    public ArrayList<LocationModel> getLocations() {
        return locations;
    }

    public String getAnalyticType() {
        return analyticType;
    }

    @Override
    public String toString() {
        return "SliderModel{" +
                "numActiveUsers=" + numActiveUsers +
                ", mostVisit=" + barEntries +
                ", reviews=" + pieEntries +
                '}';
    }
}
