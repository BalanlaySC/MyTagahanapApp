package com.example.mytagahanap;

import android.view.View;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public interface MapInterface {
    void getRoute(MapboxMap mbM, Point p1, Point p2);
    void setMapboxMap(MapboxMap mbM);
    void setMapFragView(View v);
    MapboxMap getMapboxMap();
    View getMapFragView();
    void removeLayer();
}
