package com.example.mytagahanap;

import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapbox.geojson.Point;

public interface MainInterface {
    Point getDevCurrentLocation();
    void setLayoutDirections(RelativeLayout rl);
    void setTxtViewStartLoc(TextView tv1);
    void setTxtViewDestination(TextView tv2);
    void setBtnCloseDirections(ImageButton im);
    RelativeLayout getLayoutDirections();
    TextView getTxtViewStartLoc();
    TextView getTxtViewDestination();
    ImageButton getBtnCloseDirections();
}
