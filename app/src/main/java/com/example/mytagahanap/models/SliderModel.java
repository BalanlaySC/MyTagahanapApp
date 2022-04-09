package com.example.mytagahanap.models;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class SliderModel {
    private int numActiveUsers;
    private String analyticType;
    private ArrayList<BarEntry> mostVisit;
    private ArrayList<String> locations;
    private ArrayList<PieEntry> reviews;

    public SliderModel(int numActiveUsers) {
        this.numActiveUsers = numActiveUsers;
    }

    public SliderModel(ArrayList<PieEntry> pieData) {
        this.reviews = pieData;
    }

    public SliderModel(ArrayList<BarEntry> barData, ArrayList<String> locations, String type) {
        this.mostVisit = barData;
        this.locations = locations;
        this.analyticType = type;
    }

    public String getNumActiveUsers() {
        if (numActiveUsers == 0)
            return "";
        else
            return String.format("CURRENT ACTIVE USERS: <b>%s</b>", numActiveUsers);
    }

    public ArrayList<BarEntry> getMostVisit() {
        return mostVisit;
    }

    public ArrayList<PieEntry> getReviews() {
        return reviews;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public String getAnalyticType() {
        return analyticType;
    }

    @Override
    public String toString() {
        return "SliderModel{" +
                "numActiveUsers=" + numActiveUsers +
                ", mostVisit=" + mostVisit +
                ", reviews=" + reviews +
                '}';
    }
}
