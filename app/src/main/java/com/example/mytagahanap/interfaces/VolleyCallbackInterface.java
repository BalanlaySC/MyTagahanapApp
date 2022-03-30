package com.example.mytagahanap.interfaces;

import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

import java.util.List;

public interface VolleyCallbackInterface {
    void onSuccessRequest(List<String> stringList, float f);
}
