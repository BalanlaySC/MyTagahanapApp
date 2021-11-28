package com.example.mytagahanap;

public class LocationModel {
    private final String locationName;
    private final int xCord;
    private final int yCord;

    public LocationModel(String locationName, int xCord, int yCord) {
        this.locationName = locationName;
        this.xCord = xCord;
        this.yCord = yCord;
    }

    public String getLocationName() {
        return locationName;
    }

    public int getxCord() {
        return xCord;
    }

    public int getyCord() {
        return yCord;
    }
}
