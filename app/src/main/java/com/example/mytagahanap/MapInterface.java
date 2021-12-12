package com.example.mytagahanap;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public interface MapInterface {
    void getRoute(MapboxMap mbM, Point p1, Point p2);
    void setMapboxMap(MapboxMap mbM);
    MapboxMap getMapboxMap();
    void removeLayer();
}
