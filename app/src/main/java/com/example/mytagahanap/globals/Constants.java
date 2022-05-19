package com.example.mytagahanap.globals;

import com.example.mytagahanap.R;
import com.example.mytagahanap.models.ProponentModel;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    // constant urls to server
//    public static final String ROOT_URL = "http://192.168.1.195/mytagahanap/v1/"; // local server
    public static final String ROOT_URL = "http://mytagahanap.000webhostapp.com/mytagahanap/v1/";
    public static final String URL_CLASS_SCHED = ROOT_URL + "classSchedule.php?idnumber=";
    public static final String URL_LOGIN = ROOT_URL + "userLogin.php";
    public static final String URL_SEARCH_LOG = ROOT_URL + "recordAccessedLocation.php";
    public static final String URL_VISIT_LOG = ROOT_URL + "recordVisitedLocation.php";
    public static final String URL_SUBMIT_SUGGESTION = ROOT_URL + "submitSuggestion.php";
    public static final String URL_SEND_FEEDBACK = ROOT_URL + "sendFeedback.php";
    public static final String URL_CHANGE_PASSWORD = ROOT_URL + "changePassword.php";
    public static final String URL_CHANGE_DEF_LOC = ROOT_URL + "changeDefLoc.php";
    public static final String URL_RESET_PASSWORD = ROOT_URL + "resetPassword.php";
    public static final String URL_SUBMITTED_LOC = ROOT_URL + "submittedLocations.php?idnumber=";
    public static final String URL_ANALYTICS = ROOT_URL + "retrieveAnalytics.php?idnumber=";
    public static final String URL_RETRIEVE_LOCATIONS = ROOT_URL + "retrieveAllLocations.php?idnumber=";
    public static final String ROOT_API_URL = "http://mytagahanap.pythonanywhere.com/";
//    public static final String ROOT_API_URL = "http://192.168.1.195:5000/"; // local server

    // mapbox constants
    public static final String STYLE_URL = "mapbox://styles/balanlaysc/ckwj3ml7b28rh15qafm4xzg2u";
    public static final String STREET_STYLE_URL = "mapbox://styles/balanlaysc/ckztgctxd00m914nybcatj974";
    public static final String ROUTE_LAYER_ID = "route-layer-id";
    public static final String ROUTE_SOURCE_ID = "route-source-id";
    public static final String ICON_LAYER_ID_O = "icon-layer-id-origin";
    public static final String ICON_SOURCE_ID_O = "icon-source-id-origin";
    public static final String ICON_LAYER_ID_D = "icon-layer-id-destination";
    public static final String ICON_SOURCE_ID_D = "icon-source-id-destination";
    public static final String ICON_LAYER_ID_LC = "icon-layer-id-long-click";
    public static final String ICON_SOURCE_ID_LC = "icon-source-id-long-click";
    public static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    public static final String GREEN_PIN_ICON_ID = "green-pin-icon-id";
    public static final String BLUE_PIN_ICON_ID = "blue-pin-icon-id";
    public static final String ID_IMAGE_SOURCE = "image_source-id";
    public static final String ID_IMAGE_LAYER = "image_layer-id";

    public static final List<List<Point>> POINTS_UEP_BOUNDARY = new ArrayList<>();
    private static final List<Point> OUTER_POINTS1 = new ArrayList<>();

    static {
        OUTER_POINTS1.add(Point.fromLngLat(124.65854,12.512988));
        OUTER_POINTS1.add(Point.fromLngLat(124.659617,12.514332));
        OUTER_POINTS1.add(Point.fromLngLat(124.660615,12.513849));
        OUTER_POINTS1.add(Point.fromLngLat(124.661363,12.515713));
        OUTER_POINTS1.add(Point.fromLngLat(124.662862,12.514925));
        OUTER_POINTS1.add(Point.fromLngLat(124.661959,12.513138));
        OUTER_POINTS1.add(Point.fromLngLat(124.664085,12.512475));
        OUTER_POINTS1.add(Point.fromLngLat(124.66973,12.509786));
        OUTER_POINTS1.add(Point.fromLngLat(124.671533,12.508833));
        OUTER_POINTS1.add(Point.fromLngLat(124.67346,12.509029));
        OUTER_POINTS1.add(Point.fromLngLat(124.677333,12.508275));
        OUTER_POINTS1.add(Point.fromLngLat(124.679003,12.509004));
        OUTER_POINTS1.add(Point.fromLngLat(124.679812,12.508557));
        OUTER_POINTS1.add(Point.fromLngLat(124.679619,12.508076));
        OUTER_POINTS1.add(Point.fromLngLat(124.678013,12.507381));
        OUTER_POINTS1.add(Point.fromLngLat(124.674987,12.50788));
        OUTER_POINTS1.add(Point.fromLngLat(124.670707,12.50766));
        OUTER_POINTS1.add(Point.fromLngLat(124.667938,12.508194));
        OUTER_POINTS1.add(Point.fromLngLat(124.667377,12.506585));
        OUTER_POINTS1.add(Point.fromLngLat(124.665077,12.50538));
        OUTER_POINTS1.add(Point.fromLngLat(124.662295,12.504996));
        OUTER_POINTS1.add(Point.fromLngLat(124.661694,12.507611));
        OUTER_POINTS1.add(Point.fromLngLat(124.66237,12.508222));
        OUTER_POINTS1.add(Point.fromLngLat(124.660793,12.508027));
        OUTER_POINTS1.add(Point.fromLngLat(124.658615,12.508906));
        OUTER_POINTS1.add(Point.fromLngLat(124.660092,12.51223));
        OUTER_POINTS1.add(Point.fromLngLat(124.65854,12.512988));
        POINTS_UEP_BOUNDARY.add(OUTER_POINTS1);
    }

    public static final List<List<Point>> POINTS_WAYPOINT1_BOUNDARY = new ArrayList<>();
    private static final List<Point> OUTER_POINTS2 = new ArrayList<>();

    static {
        OUTER_POINTS2.add(Point.fromLngLat(124.66283, 12.50916));
        OUTER_POINTS2.add(Point.fromLngLat(124.6632, 12.50849));
        OUTER_POINTS2.add(Point.fromLngLat(124.6643, 12.50812));
        OUTER_POINTS2.add(Point.fromLngLat(124.6652, 12.5082));
        OUTER_POINTS2.add(Point.fromLngLat(124.66551, 12.50924));
        OUTER_POINTS2.add(Point.fromLngLat(124.66565, 12.51009));
        OUTER_POINTS2.add(Point.fromLngLat(124.6635, 12.51032));
        OUTER_POINTS2.add(Point.fromLngLat(124.66283, 12.50916));
        POINTS_WAYPOINT1_BOUNDARY.add(OUTER_POINTS2);
    }

    public static final ProponentModel proponent1 = new ProponentModel("Samuel", "C.",
            "Balanlay", "UEP, Catarman N. Samar", "BSIT-4A", 22, R.drawable.image_prop1);
    public static final ProponentModel proponent2 = new ProponentModel("Joyce", "M.", "DeGuzman",
            "Cawayan, Catarman N. Samar", "BSIT-4A", 22, R.drawable.image_prop2);

    // keys for requests
    public static final int KEY_LOGIN = 10;
    public static final int KEY_RESET_PASS = 20;
    public static final int KEY_CHANGE_PASS = 60;
    public static final int KEY_SEND_FEEDBACK = 70;
    public static final int KEY_FETCH_LOCATIONS = 100;
    public static final int KEY_REQUEST_PATH_TO_ROOM = 110;
    public static final int KEY_SEARCH_LOG = 120;
    public static final int KEY_VISIT_LOG = 130;
    public static final int KEY_SUGGEST_LOCATION = 140;
    public static final int KEY_CHANGE_DEFAULT_LOCATION = 150;
    public static final int KEY_FETCH_SEARCHED = 210;
    public static final int KEY_FETCH_VISITED = 220;
    public static final int KEY_FETCH_REVIEWS = 230;
}
