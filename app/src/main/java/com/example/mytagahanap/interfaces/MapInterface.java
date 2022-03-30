package com.example.mytagahanap.interfaces;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.example.mytagahanap.models.LocationModel;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public interface MapInterface {
    LocationModel getDevCurrentLocation();
    void markMapboxMap(MapboxMap mbM, Point p1);
    void markMapboxMapOffset(MapboxMap mbM, LocationModel locationModel);
    void setMapboxMap(MapboxMap mbM);
    Dialog getLocationsDialog();
    Dialog getDirectionsDialog();
    Dialog getPathToRoomDialog();
    Dialog getBldgLvlDialog();
    void setMapFragView(View v);
    void initDirectionDialog(LocationModel lm1, String r);
    void openBottomSheetDialog(LocationModel lm1, String r, Context context);
    void setClickedLocation(LatLng p);
    MapboxMap getMapboxMap();
    void getRoute(MapboxMap mbM, Point p1, Point p2);
    View getMapFragView();
    Point getClickedLocation();
    void clearLayers();
}
