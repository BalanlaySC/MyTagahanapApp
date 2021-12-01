package com.example.mytagahanap;

public class LocationModel {
    private final String locationName;
    private final float locationLat;
    private final float locationLng;

    public LocationModel(String locationName, float locationLat, float locationLng) {
        this.locationName = locationName;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
    }

    @Override
    public String toString() {
        return locationName;
    }

    public String getLocationName() {
        return locationName;
    }

    public float getLocationLat() {
        return locationLat;
    }

    public float getLocationLng() {
        return locationLng;
    }
}
