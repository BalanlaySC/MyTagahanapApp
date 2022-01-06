package com.example.mytagahanap;

public class RoomModel {
    private final String roomName;
    private final String locationName;

    public RoomModel(String roomName, String locationName) {
        this.roomName = roomName;
        this.locationName = locationName;
    }

    @Override
    public String toString() {
        return roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getLocationName() {
        return locationName;
    }
}
