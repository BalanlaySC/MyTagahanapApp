package com.example.mytagahanap.models;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationModel implements Parcelable {
    private final String locationName;
    private final float locationLat;
    private final float locationLng;

    public LocationModel(String locationName, float locationLat, float locationLng) {
        this.locationName = locationName;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
    }

    protected LocationModel(Parcel in) {
        locationName = in.readString();
        locationLat = in.readFloat();
        locationLng = in.readFloat();
    }

    public static final Creator<LocationModel> CREATOR = new Creator<LocationModel>() {
        @Override
        public LocationModel createFromParcel(Parcel in) {
            return new LocationModel(in);
        }

        @Override
        public LocationModel[] newArray(int size) {
            return new LocationModel[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(locationName);
        parcel.writeFloat(locationLat);
        parcel.writeFloat(locationLng);
    }
}
