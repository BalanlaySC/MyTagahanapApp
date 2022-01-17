package com.example.mytagahanap;

import android.content.Context;
import android.view.View;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public interface MapInterface {
    LocationModel getDevCurrentLocation();
    void markMapboxMap(MapboxMap mbM, Point p1);
    void markMapboxMapOffset(MapboxMap mbM, LocationModel locationModel);
    void setMapboxMap(MapboxMap mbM);
    void setMapFragView(View v);
    void initDirectionDialog(LocationModel lm1);
    void openBottomSheetDialog(LocationModel lm1, Context context);
    void setClickedLocation(LatLng p);
    MapboxMap getMapboxMap();
    void getRoute(MapboxMap mbM, Point p1, Point p2);
    View getMapFragView();
    Point getClickedLocation();
    void clearLayers();
}
