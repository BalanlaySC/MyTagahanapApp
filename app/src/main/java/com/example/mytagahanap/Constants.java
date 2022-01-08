package com.example.mytagahanap;

import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final String ROOT_URL = "http://192.168.1.195/mytagahanap/v1/";
    public static final String URL_CLASS_SCHED = ROOT_URL + "classSchedule.php";
    public static final String URL_LOGIN = ROOT_URL + "userLogin.php";

    public static final String STYLE_URL = "mapbox://styles/balanlaysc/ckwj3ml7b28rh15qafm4xzg2u";
    public static final String ROUTE_LAYER_ID = "route-layer-id";
    public static final String ROUTE_SOURCE_ID = "route-source-id";
    public static final String ICON_LAYER_ID_O = "icon-layer-id-origin";
    public static final String ICON_SOURCE_ID_O = "icon-source-id-origin";
    public static final String ICON_LAYER_ID_D = "icon-layer-id-destination";
    public static final String ICON_SOURCE_ID_D = "icon-source-id-destination";
    public static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    public static final String GREEN_PIN_ICON_ID = "green-pin-icon-id";

    public static final List<List<Point>> POINTS = new ArrayList<>();
    private static final List<Point> OUTER_POINTS = new ArrayList<>();

    static {
        OUTER_POINTS.add(Point.fromLngLat(124.65854,12.512988));
        OUTER_POINTS.add(Point.fromLngLat(124.659617,12.514332));
        OUTER_POINTS.add(Point.fromLngLat(124.660615,12.513849));
        OUTER_POINTS.add(Point.fromLngLat(124.661363,12.515713));
        OUTER_POINTS.add(Point.fromLngLat(124.662862,12.514925));
        OUTER_POINTS.add(Point.fromLngLat(124.661959,12.513138));
        OUTER_POINTS.add(Point.fromLngLat(124.664085,12.512475));
        OUTER_POINTS.add(Point.fromLngLat(124.66973,12.509786));
        OUTER_POINTS.add(Point.fromLngLat(124.671533,12.508833));
        OUTER_POINTS.add(Point.fromLngLat(124.67346,12.509029));
        OUTER_POINTS.add(Point.fromLngLat(124.677333,12.508275));
        OUTER_POINTS.add(Point.fromLngLat(124.679003,12.509004));
        OUTER_POINTS.add(Point.fromLngLat(124.679812,12.508557));
        OUTER_POINTS.add(Point.fromLngLat(124.679619,12.508076));
        OUTER_POINTS.add(Point.fromLngLat(124.678013,12.507381));
        OUTER_POINTS.add(Point.fromLngLat(124.674987,12.50788));
        OUTER_POINTS.add(Point.fromLngLat(124.670707,12.50766));
        OUTER_POINTS.add(Point.fromLngLat(124.667938,12.508194));
        OUTER_POINTS.add(Point.fromLngLat(124.667377,12.506585));
        OUTER_POINTS.add(Point.fromLngLat(124.665077,12.50538));
        OUTER_POINTS.add(Point.fromLngLat(124.662295,12.504996));
        OUTER_POINTS.add(Point.fromLngLat(124.661694,12.507611));
        OUTER_POINTS.add(Point.fromLngLat(124.66237,12.508222));
        OUTER_POINTS.add(Point.fromLngLat(124.660793,12.508027));
        OUTER_POINTS.add(Point.fromLngLat(124.658615,12.508906));
        OUTER_POINTS.add(Point.fromLngLat(124.660092,12.51223));
        OUTER_POINTS.add(Point.fromLngLat(124.65854,12.512988));
        POINTS.add(OUTER_POINTS);
    }
}
