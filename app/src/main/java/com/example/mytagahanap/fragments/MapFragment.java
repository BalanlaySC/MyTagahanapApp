package com.example.mytagahanap.fragments;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.mytagahanap.globals.Constants;
import com.example.mytagahanap.globals.DatabaseAccess;
import com.example.mytagahanap.activities.EnlargeImageActivity;
import com.example.mytagahanap.models.EnlargedImageModel;
import com.example.mytagahanap.activities.MainActivity;
import com.example.mytagahanap.interfaces.MapInterface;
import com.example.mytagahanap.R;
import com.example.mytagahanap.network.RequestHandler;
import com.example.mytagahanap.globals.SharedPrefManager;
import com.example.mytagahanap.interfaces.VolleyCallbackInterface;
import com.example.mytagahanap.adapters.LocationAdapter;
import com.example.mytagahanap.models.LocationModel;
import com.example.mytagahanap.models.RoomModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.TurfJoins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements PermissionsListener, MapInterface, VolleyCallbackInterface {
    private static final String TAG = "MapFragment";
    final Handler handler = new Handler(Looper.getMainLooper());

    private View view;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng latLng;
    private Point origin, destination;
    private DirectionsRoute currentRoute;
    private GeoJsonSource routeGJS, iconGJSOrigin, iconGJSDestination, iconGJSLongClick;
    private BottomSheetDialog bottomSheetDialog;
    private Dialog directionsDialog, suggestionDialog, locationsDialog,
            pathToRoomDialog, bldgLvlDialog;
    private ExtendedFloatingActionButton mapfragmentFab;
    private FloatingActionButton mapfragFabStyle;

    private Context mapFragmentContext;

    private String symbolLayerId;
    private ArrayList<LocationModel> locations;
    private List<String> imageURLList;
    private List<RasterLayer> rasterLayerList;
    private RasterLayer currentRasterLayer;
    private float mapRotation;

    public MapFragment() {
    }

    @SuppressLint({"WrongConstant", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mapFragmentContext = requireContext().getApplicationContext();
        Mapbox.getInstance(mapFragmentContext, getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setMapFragView(view);
        String styleUrl = SharedPrefManager.getInstance(mapFragmentContext).getCurrentStyleUrl();

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            setMapboxMap(mapboxMap);
            mapboxMap.setStyle(styleUrl, style -> {
                enableLocationComponent(style);
                initLayers(style);

                mapboxMap.addOnMapLongClickListener(point -> {
                    @SuppressLint("DefaultLocale") String pointName = String.format("%1$.7f, %2$.7f", point.getLongitude(), point.getLatitude());
                    openBottomSheetDialog(new LocationModel(pointName, (float) point.getLatitude(), (float) point.getLongitude()), "",
                            requireContext());
                    return false;
                });
            });
        });

        mapfragmentFab = view.findViewById(R.id.mapfragmentFab);
        mapfragmentFab.shrink();
        mapfragmentFab.setOnClickListener(view1 -> {
            openLocationsDialog();
            mapfragmentFab.hide();
            mapfragFabStyle.hide();
        });
        mapfragmentFab.setOnLongClickListener(view12 -> {
            if (!mapfragmentFab.isExtended()) {
                mapfragmentFab.extend();
            } else {
                mapfragmentFab.shrink();
            }
            return true;
        });

        mapfragFabStyle = view.findViewById(R.id.mapfragFabStyle);
        mapfragFabStyle.setOnClickListener(view13 -> {
            if (SharedPrefManager.getInstance(mapFragmentContext).getCurrentStyleUrl().equals(Constants.STYLE_URL)) {
                SharedPrefManager.getInstance(mapFragmentContext).setMapboxStyleUrl(Constants.STREET_STYLE_URL);
            } else {
                SharedPrefManager.getInstance(mapFragmentContext).setMapboxStyleUrl(Constants.STYLE_URL);
            }

            MapFragment mapfrag = new MapFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapfrag).commit();
        });

        suggestionDialog = new Dialog(getActivity());
        directionsDialog = new Dialog(getActivity());
        locationsDialog = new Dialog(getActivity());
        pathToRoomDialog = new Dialog(getActivity());
        bldgLvlDialog = new Dialog(getActivity());

        locations = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            locations = getArguments().getParcelableArrayList("Locations");
            locations.sort(Comparator.comparing(LocationModel::getLocationName));
        } else {
            locations.add(new LocationModel("Unable to retrieve data", (float) 0.0, (float) 0.0));
        }
        return view;
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(mapFragmentContext)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(mapFragmentContext, loadedMapStyle).build());

            // Enable to make component visible
            if (ActivityCompat.checkSelfPermission(mapFragmentContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mapFragmentContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setMaxAnimationFps(30);
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    /* Return the current location of the device.
    If outside of UEP the assigned Default Location will be used
    0 is long, 1 is lat */
    @SuppressLint("MissingPermission")
    public LocationModel getDevCurrentLocation() {
        String defaultLocation = SharedPrefManager.getInstance(mapFragmentContext).getDefLoc();
        Log.d(TAG, "getDevCurrentLocation: defaultlocation " + getLocationObj(defaultLocation));
        LocationModel currentLM;
        if (defaultLocation.contains(", ")){
            String[] parts = defaultLocation.split(", ");
            currentLM = new LocationModel(defaultLocation, Float.parseFloat(parts[1]), Float.parseFloat(parts[0]));
        } else {
            currentLM = getLocationObj(defaultLocation);
        }
        Log.d(TAG, "getDevCurrentLocation: currentLM " + currentLM.toString());
        LocationModel origin;

        if (ActivityCompat.checkSelfPermission(mapFragmentContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mapFragmentContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "getDevCurrentLocation: L218-User location is on " + true);
            if (location != null) {
                origin = new LocationModel("Current Location",
                        (float) location.getLatitude(), (float) location.getLongitude());
            } else {
                Log.d(TAG, "getDevCurrentLocation: L234-Location is null");
                origin = currentLM;
            }
        } else {
            Log.d(TAG, "getDevCurrentLocation: L348-User location is on " + false);
            origin = currentLM;
        }

        return origin;
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param mapboxMap   the Mapbox map object that the route will be drawn on
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    public void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        String defaultLocation = SharedPrefManager.getInstance(mapFragmentContext).getDefLoc();
        LocationModel currentLM;
        if (defaultLocation.contains(", ")){
            String[] parts = defaultLocation.split(", ");
            currentLM = new LocationModel(defaultLocation, Float.parseFloat(parts[1]), Float.parseFloat(parts[0]));
        } else {
            currentLM = getLocationObj(defaultLocation);
        }
        Log.d(TAG, "getRoute: currentLM " + SharedPrefManager.getInstance(mapFragmentContext).getDefLoc());
        Point currentLMPoint = Point.fromLngLat(currentLM.getLocationLng(), currentLM.getLocationLat());
//        Polygon polygon = Polygon.fromLngLats(Constants.POINTS_UEP_BOUNDARY);
        Polygon polygon = Polygon.fromLngLats(Constants.POINTS_WAYPOINT1_BOUNDARY);
        MapboxDirections mapboxDirections;
//        Toast.makeText(mapFragmentContext, "Currently outside of UEP\n" +
//                "Generating path from " + currentLM.getLocationName(), Toast.LENGTH_LONG).show();

//        boolean isLocInsideUEP = TurfJoins.inside(origin, polygon);
        boolean isUserInsideWB = TurfJoins.inside(origin, polygon); // if origin is inside waypoint1_boundary
        boolean isLocInsideWB = TurfJoins.inside(destination, polygon); // if destination is inside waypoint1_boundary
        if (Mapbox.getAccessToken() != null) {
            if (isLocInsideWB) {
                if (!isUserInsideWB && (origin.longitude() > 124.66425)) {
                    currentLMPoint = Point.fromLngLat(124.66549, 12.50925);
                    mapboxDirections = MapboxDirections.builder()
                            .addWaypoint(currentLMPoint)
                            .origin(origin)
                            .destination(destination)
                            .overview(DirectionsCriteria.OVERVIEW_FULL)
                            .profile(DirectionsCriteria.PROFILE_DRIVING)
                            .accessToken(Mapbox.getAccessToken())
                            .build();
                } else if (!isUserInsideWB && (origin.longitude() < 124.66425)) {
                    currentLMPoint = Point.fromLngLat(124.66279, 12.50923);
                    mapboxDirections = MapboxDirections.builder()
                            .addWaypoint(currentLMPoint)
                            .origin(origin)
                            .destination(destination)
                            .overview(DirectionsCriteria.OVERVIEW_FULL)
                            .profile(DirectionsCriteria.PROFILE_DRIVING)
                            .accessToken(Mapbox.getAccessToken())
                            .build();
                } else {
                    mapboxDirections = MapboxDirections.builder()
                            .origin(origin)
                            .destination(destination)
                            .overview(DirectionsCriteria.OVERVIEW_FULL)
                            .profile(DirectionsCriteria.PROFILE_WALKING)
                            .accessToken(Mapbox.getAccessToken())
                            .build();
                }
            } else {
//                Log.d(TAG, "getRoute: origin is inside UEP");
                mapboxDirections = MapboxDirections.builder()
                        .origin(origin)
                        .destination(destination)
                        .overview(DirectionsCriteria.OVERVIEW_FULL)
                        .profile(DirectionsCriteria.PROFILE_WALKING)
                        .accessToken(Mapbox.getAccessToken())
                        .build();
            }
        } else {
            Log.d(TAG, "getRoute: null access token");
            return;
        }

        mapboxDirections.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.d(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.d(TAG, "No routes found");
                    return;
                }

                // Get the directions route
                currentRoute = response.body().routes().get(0);

                // Make a toast which displays the route's distance
                Toast.makeText(mapFragmentContext, String.format(
                        "Route is %1$f meters long.",
                        currentRoute.distance()), Toast.LENGTH_SHORT).show();

                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        // Retrieve and update the source designated for showing the directions route
                        if (routeGJS != null) {
                            routeGJS.setGeoJson(FeatureCollection.fromJson(""));
                            iconGJSOrigin.setGeoJson(FeatureCollection.fromJson(""));
                            iconGJSDestination.setGeoJson(FeatureCollection.fromJson(""));
                        }
                        if (iconGJSLongClick != null) {
                            iconGJSLongClick.setGeoJson(FeatureCollection.fromJson(""));
                        }
                        routeGJS = style.getSourceAs(Constants.ROUTE_SOURCE_ID);
                        iconGJSOrigin = style.getSourceAs(Constants.ICON_SOURCE_ID_O);
                        iconGJSDestination = style.getSourceAs(Constants.ICON_SOURCE_ID_D);

                        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                                .include(new LatLng(origin.latitude(), origin.longitude()))
                                .include(new LatLng(destination.latitude(), destination.longitude()))
                                .build(), 100), 1000);
                        // Create a LineString with the directions route's
                        // geometry and
                        // reset the GeoJSON routeGeoJsonSource for the route LineLayer source
                        // Also generate markers for origin and destination
                        if (routeGJS != null) {
                            routeGJS.setGeoJson(LineString.fromPolyline(Objects.requireNonNull(currentRoute.geometry()), PRECISION_6));
                        }
                        if (iconGJSOrigin != null) {
                            iconGJSOrigin.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{
                                    Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude()))}));
                        }
                        if (iconGJSDestination != null) {
                            iconGJSDestination.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{
                                    Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
                Log.d(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(mapFragmentContext, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Show dialog with option to show Path to Room in the map or as image
    public void initPathToRoomDialog() {
        pathToRoomDialog.setContentView(R.layout.layout_dialog_pathtoroom);
        pathToRoomDialog.setOnDismissListener(dialogInterface -> {
            if (mapfragmentFab != null)
                mapfragmentFab.hide();
            if (mapfragFabStyle != null)
                mapfragFabStyle.hide();
        });
        Window window = pathToRoomDialog.getWindow();
        window.setGravity(Gravity.CENTER_VERTICAL);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // todo add direction and suggestion
        Button btnPathRoomMap = pathToRoomDialog.findViewById(R.id.ptrbtnPathInTheMap);
        Button btnPathRoomImage = pathToRoomDialog.findViewById(R.id.ptrbtnPathAsImages);
        btnPathRoomMap.setOnClickListener(view -> {
            pathToRoomDialog.dismiss();
            initBldgLvlDialog();
            currentRasterLayer = rasterLayerList.get(0);
            getMapboxMap().easeCamera(CameraUpdateFactory.bearingTo(mapRotation));
            getMapboxMap().easeCamera(CameraUpdateFactory.zoomTo(18.0));
            getMapboxMap().getStyle(style -> style.addLayer(currentRasterLayer));
        });
        btnPathRoomImage.setOnClickListener(view -> {
            try {
                pathToRoomDialog.dismiss();
                Intent intent = new Intent(mapFragmentContext, EnlargeImageActivity.class);
                ArrayList<EnlargedImageModel> imageSet = new ArrayList<>();
                for (String url : imageURLList) {
                    imageSet.add(new EnlargedImageModel(url, ""));
                }
                Log.d(TAG, "imageset " + imageSet);
                intent.putParcelableArrayListExtra("enlargedImage", imageSet);
                startActivity(intent);
            } catch (NullPointerException e) {
                Toast.makeText(mapFragmentContext, "Try again.", Toast.LENGTH_SHORT).show();
            }
        });
        ImageButton btnPathRoomClose = pathToRoomDialog.findViewById(R.id.ptrbtnClose);
        pathToRoomDialog.show();
        btnPathRoomClose.setOnClickListener(view -> {
            clearLayers();
            pathToRoomDialog.dismiss();
            if (mapfragmentFab != null)
                mapfragmentFab.hide();
            if (mapfragFabStyle != null)
                mapfragFabStyle.hide();
        });
    }

    // Display Path to Room in the map and able to change floor level
    public void initBldgLvlDialog() {
        bldgLvlDialog.setContentView(R.layout.layout_dialog_buildinglevel);
        Window window = bldgLvlDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getDisplay().getMetrics(displayMetrics);
        Button exitBtn = bldgLvlDialog.findViewById(R.id.bldgLvlExitBtn);
        exitBtn.setOnClickListener(view -> {
            bldgLvlDialog.dismiss();
            Objects.requireNonNull(getMapboxMap().getStyle()).removeLayer(currentRasterLayer);
        });
        Button bldgLvl1Btn = bldgLvlDialog.findViewById(R.id.bldgLvl1Btn);
        Button bldgLvl2Btn = bldgLvlDialog.findViewById(R.id.bldgLvl2Btn);
        Button bldgLvl3Btn = bldgLvlDialog.findViewById(R.id.bldgLvl3Btn);
        bldgLvl1Btn.setMinimumWidth(displayMetrics.widthPixels / 8);
        bldgLvl1Btn.setOnClickListener(view -> {
            Objects.requireNonNull(getMapboxMap().getStyle()).removeLayer(currentRasterLayer);
            currentRasterLayer = rasterLayerList.get(0);
            getMapboxMap().getStyle(style -> style.addLayer(currentRasterLayer));
            getMapboxMap().easeCamera(CameraUpdateFactory.bearingTo(mapRotation));
        });
        bldgLvl2Btn.setVisibility(View.GONE);
        bldgLvl3Btn.setVisibility(View.GONE);
        if (imageURLList.size() > 1) {
            bldgLvl2Btn.setVisibility(View.VISIBLE);
            bldgLvl2Btn.setOnClickListener(view -> {
                Objects.requireNonNull(getMapboxMap().getStyle()).removeLayer(currentRasterLayer);
                currentRasterLayer = rasterLayerList.get(1);
                getMapboxMap().getStyle(style -> style.addLayer(currentRasterLayer));
                getMapboxMap().easeCamera(CameraUpdateFactory.bearingTo(mapRotation));
            });
            if (imageURLList.size() > 2) {
                bldgLvl3Btn.setVisibility(View.VISIBLE);
                bldgLvl3Btn.setOnClickListener(view -> {
                    Objects.requireNonNull(getMapboxMap().getStyle()).removeLayer(currentRasterLayer);
                    currentRasterLayer = rasterLayerList.get(2);
                    getMapboxMap().getStyle(style -> style.addLayer(currentRasterLayer));
                    getMapboxMap().easeCamera(CameraUpdateFactory.bearingTo(mapRotation));
                });
            }
        }
        bldgLvlDialog.show();
    }

    /**
     * Marks the map on the clicked location
     *
     * @param mapboxMap Current instance of Mapbox.mapboxMap
     * @param clickedLoc Point object where it holds the coordinates
     *                   of the clicked location
     */
    public void markMapboxMap(MapboxMap mapboxMap, Point clickedLoc) {
        mapboxMap.getStyle(style -> {
            Objects.requireNonNull(style.getLayer(symbolLayerId)).setProperties(
                    textField(""),
                    textFont(new String[]{"Roboto Regular", "Arial Unicode MS Regular"}),
                    textOffset(new Float[]{0f, -1.25f}));
            iconGJSLongClick = style.getSourceAs(Constants.ICON_SOURCE_ID_LC);
            if (iconGJSLongClick != null) {
                iconGJSLongClick.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{
                        Feature.fromGeometry(clickedLoc)}));
            }

            mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(clickedLoc.latitude(), clickedLoc.longitude())), 1000);
        });
    }

    /**
     * Marks the map on the clicked location but with an offset
     *
     * @param mapboxMap Current instance of mapboxMap
     * @param clickedLocation Point object where it holds the coordinates
     *                        of the clicked location
     */
    public void markMapboxMapOffset(MapboxMap mapboxMap, LocationModel clickedLocation) {
        float cLLng = clickedLocation.getLocationLng();
        float cLLat = clickedLocation.getLocationLat();
        mapboxMap.getStyle(style -> {
            Objects.requireNonNull(style.getLayer(symbolLayerId)).setProperties(
                    textField(clickedLocation.getLocationName()),
                    textFont(new String[]{"Roboto Black", "Arial Unicode MS Bold"}),
                    textOffset(new Float[]{0f, -1.25f}));
            iconGJSLongClick = style.getSourceAs(Constants.ICON_SOURCE_ID_LC);
            if (iconGJSLongClick != null) {
                iconGJSLongClick.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{
                        Feature.fromGeometry(Point.fromLngLat(cLLng, cLLat))}));
            }

            mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(cLLat - 0.001, cLLng)), 1000);
            mapboxMap.easeCamera(CameraUpdateFactory.zoomTo(16.0));
        });
    }

    /**
     * Add the route and marker icon layers to the map
     *
     * @param loadedMapStyle Current instance of Mapbox.style
     */
    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(Constants.ROUTE_LAYER_ID, Constants.ROUTE_SOURCE_ID);

        //Add the route and marker sources to the map
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ROUTE_SOURCE_ID));
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ICON_SOURCE_ID_O));
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ICON_SOURCE_ID_D));
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ICON_SOURCE_ID_LC));

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#2F7AC6"))
        );
        loadedMapStyle.addLayer(routeLayer);

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(Constants.RED_PIN_ICON_ID,
                Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.image_red_marker, null))));
        loadedMapStyle.addImage(Constants.GREEN_PIN_ICON_ID,
                Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.image_green_marker, null))));
        loadedMapStyle.addImage(Constants.BLUE_PIN_ICON_ID,
                Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.image_blue_marker, null))));

        // Add the marker icon SymbolLayers to the map
        loadedMapStyle.addLayer(new SymbolLayer(Constants.ICON_LAYER_ID_O, Constants.ICON_SOURCE_ID_O).withProperties(
                iconImage(Constants.GREEN_PIN_ICON_ID),
                iconSize((float) 0.25),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f}),
                textField("You are here"),
                textFont(new String[]{"Roboto Regular", "Arial Unicode MS Regular"}),
                textOffset(new Float[]{0f, -1.25f})));

        loadedMapStyle.addLayer(new SymbolLayer(Constants.ICON_LAYER_ID_D, Constants.ICON_SOURCE_ID_D).withProperties(
                iconImage(Constants.RED_PIN_ICON_ID),
                iconSize((float) 0.25),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f}),
                textField("Destination"),
                textFont(new String[]{"Roboto Regular", "Arial Unicode MS Regular"}),
                textOffset(new Float[]{0f, -1.25f})));

        SymbolLayer symbolLayer = new SymbolLayer(Constants.ICON_LAYER_ID_LC, Constants.ICON_SOURCE_ID_LC).withProperties(
                iconImage(Constants.BLUE_PIN_ICON_ID),
                iconSize((float) 0.2),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f}));
        symbolLayerId = symbolLayer.getId();
        loadedMapStyle.addLayer(symbolLayer);
    }

    /**
     * Display bottomsheet dialog to show information of the location
     *
     * @param clickedLocation LocationModel object holds the name and coordinates
     *                        of the clicked location or building
     * @param room Name of the room (from class schedule)
     * @param context Enables the ability to allows access to application-specific
     *                resources and classes
     */
    public void openBottomSheetDialog(LocationModel clickedLocation, String room, Context context) {
        handler.postDelayed(() -> markMapboxMapOffset(getMapboxMap(), clickedLocation), 250);
        Log.d(TAG, "openBottomSheetDialog: ->" + clickedLocation);
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setOnCancelListener(dialogInterface -> clearLayers());
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet);

        // Set bottomsheet title to clicked location
        TextView btsTxtLocation = bottomSheetDialog.findViewById(R.id.btsTxtLocation);
        if (btsTxtLocation != null) {
            btsTxtLocation.setText(clickedLocation.getLocationName());
            if (!room.equals("")) {
                btsTxtLocation.setText(clickedLocation.getLocationName() + " -> " + room);
            }
        }

        // OnClickListener for Directions Button
        TextView btnDirections = bottomSheetDialog.findViewById(R.id.btnDirections);
        if (btnDirections != null) {
            btnDirections.setOnClickListener(view -> {
                Log.d(TAG, "initViews: Directions started");
                bottomSheetDialog.dismiss();

                // Popup dialog when pressing directions/starting navigation
                initDirectionDialog(clickedLocation, room);
            });
        }

        // Display images of a known location
        LinearLayout bsImageLayout = bottomSheetDialog.findViewById(R.id.bsImageLayout);
        if (bsImageLayout != null) {
            initImageLayout(clickedLocation, room, bsImageLayout);
        }

        // More option menu
        ImageButton imgbtnShowMore = bottomSheetDialog.findViewById(R.id.imgbtnShowMore);
        LinearLayout layoutShowMore = bottomSheetDialog.findViewById(R.id.layoutShowMore);
        assert layoutShowMore != null;
        TextView smbtnViewRooms = bottomSheetDialog.findViewById(R.id.smbtnViewRooms);
        TextView smbtnSetDefault = bottomSheetDialog.findViewById(R.id.smbtnSetDefault);
        TextView smbtnSuggestLocation = bottomSheetDialog.findViewById(R.id.smbtnSuggestLocation);

        if (imgbtnShowMore != null) {
            // click ... opens/Visible more option and closes/Gone more option
            imgbtnShowMore.setOnClickListener(view -> {
                if (layoutShowMore.getVisibility() == View.GONE) {
                    layoutShowMore.setVisibility(View.VISIBLE);
                } else {
                    layoutShowMore.setVisibility(View.GONE);
                }
            });
        }
        if (smbtnSetDefault != null) {
            // set def location feature is only available for existing locations
            smbtnSetDefault.setVisibility(View.VISIBLE);

            // change def location
            smbtnSetDefault.setOnClickListener(view -> {
                changeDefaultLocation(String.valueOf(
                        SharedPrefManager.getInstance(context).getIdnumber()),
                        clickedLocation.getLocationName());
                SharedPrefManager.getInstance(context).updateDefLocation(clickedLocation.getLocationName());
            });
        }
        if (smbtnViewRooms != null) {
            if (getStreamRM(clickedLocation)){
                smbtnViewRooms.setVisibility(View.VISIBLE);
            } else {
                smbtnViewRooms.setVisibility(View.GONE);
            }
            String imgUrl = (Constants.ROOT_URL + "img/" +
                    clickedLocation.getLocationName() + "/" + clickedLocation.getLocationName())
                    .replace(" ", "%20");
            // check if location is college building, else feature disabled
            // .filter(o -> o.getName().equals(name)).findFirst().isPresent()
            Stream<RoomModel> stream =  DatabaseAccess.getInstance(mapFragmentContext)
                    .getAllRooms()
                    .stream()
                    .filter(o -> o.getLocationName().equals(clickedLocation.getLocationName()));
            List<RoomModel> clickedLocationRooms = stream.collect(Collectors.toList());
            ArrayList<EnlargedImageModel> imageSet = new ArrayList<>();
            for (RoomModel roomModel : clickedLocationRooms) {
                imageSet.add(new EnlargedImageModel(imgUrl +
                        roomModel.getRoomName().replace(" ", "%20") + "Room.jpg", ""));
            }

            smbtnViewRooms.setOnClickListener(view -> {
                Intent intent = new Intent(mapFragmentContext, EnlargeImageActivity.class);
                Log.d(TAG, "openBottomSheetDialog: -> Enlarged Image " + imageSet);
                intent.putParcelableArrayListExtra("enlargedImage", imageSet);
                startActivity(intent);
            });
        }
        if (smbtnSuggestLocation != null) {
            // feature viable/Visible if clickedLocation is unique, else feature disabled/Gone
            if (containsLocation(clickedLocation.getLocationName())) {
                smbtnSuggestLocation.setVisibility(View.GONE);
            } else {
                smbtnSuggestLocation.setVisibility(View.VISIBLE);
            }

            // add request to the database to add
            smbtnSuggestLocation.setOnClickListener(view -> {
                layoutShowMore.setVisibility(View.GONE);
                bottomSheetDialog.dismiss();
                int counter = SharedPrefManager.getInstance(mapFragmentContext).getContributionCounter();
                if (counter < 5) {
                    handler.postDelayed(() -> openSuggestionDialog(new LatLng(clickedLocation.getLocationLat(),
                            clickedLocation.getLocationLng())), 250);
                } else {
                    clearLayers();
                    Toast.makeText(mapFragmentContext, "You have run out of suggestion query", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (MainActivity.isLocationEnabled(context)) {
            Toast.makeText(context, "To start at your current location " +
                    "you must enable GPS/Location Access", Toast.LENGTH_SHORT).show();
        }
        bottomSheetDialog.show();
    }

    /**
     * Fast way to verify if location's name is in the location database
     *
     * @param clickedLocation A LocationModel object containing the name
     *                        and coordinates of a clicked location
     * @return True if location's name is in the location database, otherwise False
     */
    private boolean getStreamRM(LocationModel clickedLocation) {
        return DatabaseAccess.getInstance(mapFragmentContext)
                .getAllRooms()
                .stream()
                .anyMatch(o -> o.getLocationName().equals(clickedLocation.getLocationName()));
    }

    /**
     * Display images for a clicked location in bottom sheet
     *
     * @param clickedLocation A LocationModel object containing the name
     *                        and coordinates of a clicked location
     * @param room Name of the room (from class schedule)
     * @param bsImageLayout LinearLayout where the it contains ImageViews
     *                      where images of location will be displayed
     */
    private void initImageLayout(LocationModel clickedLocation, String room, LinearLayout bsImageLayout) {
        String holder = (Constants.ROOT_URL + "img/" +
                clickedLocation.getLocationName().replace(".", "")
                + "/" + clickedLocation.getLocationName())
                .replace(" ", "%20");
        String roomPreviewUrl = holder + room.replace(" ", "%20") + ".jpg";
        String roomUrl = holder + room.replace(" ", "%20") + "Room.jpg";
        String roomThumbnailUrl = holder +  room.replace(" ", "%20")  + "Thumb.jpg";

        ShapeableImageView locationimg1 = bottomSheetDialog.findViewById(R.id.locationimg1);
        ShapeableImageView locationimg2 = bottomSheetDialog.findViewById(R.id.locationimg2);

        // thumbnails in bottom sheet
        if (containsLocation(clickedLocation.getLocationName())) {
            bsImageLayout.setVisibility(View.VISIBLE);
            if (locationimg1 != null && locationimg2 != null) {
                Glide.with(mapFragmentContext)
                        .asBitmap()
                        .load(Uri.parse(holder + "Preview.jpg"))
                        .thumbnail(Glide.with(mapFragmentContext)
                                .asBitmap()
                                .load(Uri.parse(holder + "Thumb.jpg")))
                        .into(locationimg1);
                if (!room.equals("")) {
                    locationimg2.setVisibility(View.VISIBLE);
                    Glide.with(mapFragmentContext)
                            .asBitmap()
                            .load(Uri.parse(roomPreviewUrl))
                            .thumbnail(Glide.with(mapFragmentContext)
                                    .asBitmap()
                                    .load(Uri.parse(roomThumbnailUrl)))
                            .into(locationimg2);
                } else {
                    locationimg2.setVisibility(View.GONE);
                }
            }
        }

        // listener for preview image
        if (locationimg1 != null) {
            locationimg1.setOnClickListener(view -> {
                Intent intent = new Intent(mapFragmentContext, EnlargeImageActivity.class);
                intent.putExtra("enlargedImage",
                        new EnlargedImageModel(holder + "Preview.jpg", holder + "Thumb.jpg"));
                startActivity(intent);

            });
        }
        if (locationimg2 != null) {
            locationimg2.setOnClickListener(view -> {
                Intent intent = new Intent(mapFragmentContext, EnlargeImageActivity.class);
                ArrayList<EnlargedImageModel> imageSet = new ArrayList<>();
                imageSet.add(new EnlargedImageModel(roomPreviewUrl, roomThumbnailUrl));
                imageSet.add(new EnlargedImageModel(roomUrl, ""));
                intent.putParcelableArrayListExtra("enlargedImage", imageSet);
                startActivity(intent);
            });
        }
    }

    /**
     * Display dialog for sending suggestions
     *
     * @param point LatLng object where it holds the coordinates
     *              where the user long clicked in the map
     */
    private void openSuggestionDialog(LatLng point) {
        suggestionDialog.setContentView(R.layout.layout_dialog_suggestion);
        suggestionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView suggestionLong = suggestionDialog.findViewById(R.id.suggestionLong);
        suggestionLong.setText(String.valueOf(point.getLongitude()));
        TextView suggestionLat = suggestionDialog.findViewById(R.id.suggestionLat);
        suggestionLat.setText(String.valueOf(point.getLatitude()));
        EditText suggestionLocName = suggestionDialog.findViewById(R.id.suggestionLocName);
        EditText suggestionDescription = suggestionDialog.findViewById(R.id.suggestionDescription);
        Button suggestionSendBtn = suggestionDialog.findViewById(R.id.suggestionSendBtn);
        ImageButton suggestionImgBtnClose = suggestionDialog.findViewById(R.id.suggestionImgBtnClose);

        // submit a suggestion
        suggestionSendBtn.setOnClickListener(view -> {
            SharedPrefManager.getInstance(mapFragmentContext).incrementContribution();
            String toastMsg = String.format("You have %s suggestion query left.",
                    (5 - SharedPrefManager.getInstance(mapFragmentContext).getContributionCounter()));
            Toast.makeText(mapFragmentContext, toastMsg, Toast.LENGTH_SHORT).show();
            // get user input in location name and description
            String mLocName = suggestionLocName.getText().toString();
            String mLocDesc = suggestionDescription.getText().toString();
            String mLocLong = String.valueOf(point.getLongitude());
            String mLocLat = String.valueOf(suggestionLat.getText());
            suggestLocation(String.valueOf(SharedPrefManager.getInstance(mapFragmentContext).getIdnumber()),
                    mLocName, mLocLong, mLocLat, mLocDesc);
            suggestionDialog.dismiss();
        });
        suggestionImgBtnClose.setOnClickListener(view -> suggestionDialog.dismiss());

        suggestionDialog.show();
    }

    /**
     * Display dialog for a current location and destination
     *
     * @param destinationLM LocationModel object holds the name and coordinates
     *                      of the destination
     * @param room Name of the room (from class schedule)
     */
    public void initDirectionDialog(LocationModel destinationLM, String room) {
        handler.postDelayed(() -> {
            if (mapfragmentFab != null)
                mapfragmentFab.hide();
            if (mapfragFabStyle != null)
                mapfragFabStyle.hide();
        }, 250);
        directionsDialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                directionsDialog.dismiss();
                clearLayers();
                Log.d(TAG, "initDirectionDialog: directDialog " + directionsDialog.isShowing());
                if (mapfragmentFab != null)
                    mapfragmentFab.show();
            }
            return true;
        });
        directionsDialog.setContentView(R.layout.layout_dialog_directions);

        // Configurations to the position of the dialog and make the map still clickable
        Window window = directionsDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.y = 30;
        window.setAttributes(wlp);

        TextView directionsStartLoc = directionsDialog.findViewById(R.id.directionsStartLoc);
        TextView directionsDestination = directionsDialog.findViewById(R.id.directionsDestination);
        ImageButton directionsImgBtnClose = directionsDialog.findViewById(R.id.directionsImgBtnClose);

        // Setting up the Point object: origin & destination
        LocationModel originLM = getDevCurrentLocation();
        origin = Point.fromLngLat(originLM.getLocationLng(),
                originLM.getLocationLat());
        destination = Point.fromLngLat(destinationLM.getLocationLng(),
                destinationLM.getLocationLat());

        // Setting the area for the destination
        Polygon polygon = getArrivalArea(destinationLM);
        // Checks if the device is within the perimeter of the destination
        if (TurfJoins.inside(origin, polygon)) {
            searchLog(String.valueOf(SharedPrefManager.getInstance(mapFragmentContext).getIdnumber()),
                    originLM.getLocationName(), destinationLM.getLocationName());
            Toast.makeText(mapFragmentContext, "You are already here!", Toast.LENGTH_LONG).show();
            clearLayers();
            if (!room.equals("")) {
                requestPathToRoom(destinationLM.getLocationName(), room);
            }
            visitLog(String.valueOf(SharedPrefManager.getInstance(mapFragmentContext).getIdnumber()),
                    destinationLM.getLocationName());
            directionsDialog.dismiss();
            if (mapfragmentFab != null)
                mapfragmentFab.show();
            if (mapfragFabStyle != null)
                mapfragFabStyle.show();
            return;
        }

        // Request route from device location to where the user want to go
        getRoute(getMapboxMap(), origin, destination);

        directionsStartLoc.setText(originLM.getLocationName());
        directionsDestination.setText(destinationLM.getLocationName());
        directionsDialog.show();

        directionsImgBtnClose.setOnClickListener(view -> {
            clearLayers();
            directionsDialog.dismiss();
            if (mapfragmentFab != null)
                mapfragmentFab.show();
            if (mapfragFabStyle != null)
                mapfragFabStyle.show();
        });
        searchLog(String.valueOf(SharedPrefManager.getInstance(mapFragmentContext).getIdnumber()),
                originLM.getLocationName(), destinationLM.getLocationName());

        handler.postDelayed(() -> {
            if (!directionsDialog.isShowing()){
                clearLayers();
            }
        }, 2500);
        //
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "initDirectionDialog: directDialog " + directionsDialog.isShowing());
                        if (directionsDialog.isShowing()) {
                            if (TurfJoins.inside(origin, polygon)) {
                                Toast.makeText(mapFragmentContext, "You have arrived!", Toast.LENGTH_LONG).show();
                                timer.cancel();
                                clearLayers();
                                if (!room.equals("")) {
                                    requestPathToRoom(destinationLM.getLocationName(), room);
                                }
                                visitLog(String.valueOf(SharedPrefManager.getInstance(mapFragmentContext).getIdnumber()),
                                        destinationLM.getLocationName());
                                directionsDialog.dismiss();
                                return;
                            }
                            Log.d(TAG, "initViews: 15 secs passed executing reroute");
                            LocationModel currentLM = getDevCurrentLocation();
                            origin = Point.fromLngLat(currentLM.getLocationLng(),
                                    currentLM.getLocationLat());
                            getRoute(getMapboxMap(), origin, destination);
                        } else {
                            timer.cancel();
                            clearLayers();
                        }
                    });
                }
            }
        }, 10000, 10000);
    }

    // Display dialog for list of locations
    private void openLocationsDialog() {
        locationsDialog.setContentView(R.layout.layout_dialog_locations);
        locationsDialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                locationsDialog.dismiss();
                mapfragmentFab.show();
                mapfragFabStyle.show();
            }
            return true;
        });
        // Configurations to the position of the dialog and make the map still clickable
        Window window = locationsDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.y = 30;
        window.setAttributes(wlp);

        ImageButton recvImgBtnClose = locationsDialog.findViewById(R.id.recvImgBtnClose);
        RecyclerView mRecyclerView = locationsDialog.findViewById(R.id.recvLocations);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mapFragmentContext);
        LocationAdapter mAdapter = new LocationAdapter(locations);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(position -> {
            mapfragmentFab.show();

            openBottomSheetDialog(locations.get(position), "", requireContext());
            locationsDialog.dismiss();
        });
        recvImgBtnClose.setOnClickListener(view -> {
            locationsDialog.dismiss();
            mapfragmentFab.show();
        });

        locationsDialog.show();
    }

    // Remove generated icons or path
    @Override
    public void clearLayers() {
        getMapboxMap().getStyle(style -> {
            // If a route layer exists in the style, remove the layer
            if (routeGJS != null) {
                routeGJS.setGeoJson(FeatureCollection.fromJson(""));
                iconGJSOrigin.setGeoJson(FeatureCollection.fromJson(""));
                iconGJSDestination.setGeoJson(FeatureCollection.fromJson(""));
                handler.postDelayed(() -> enableLocationComponent(style), 500);
            }
            if (iconGJSLongClick != null) {
                iconGJSLongClick.setGeoJson(FeatureCollection.fromJson(""));
                handler.postDelayed(() -> enableLocationComponent(style), 500);
            }
        });
        getMapboxMap().easeCamera(CameraUpdateFactory.zoomTo(16.0));
    }

    /**
     * Called upon arriving in the building
     *
     * @param building_name The building's name where the room is located
     * @param room_name The room of the selected subject
     */
    public void requestPathToRoom(String building_name, String room_name) {
        Log.d(TAG, "requestPathToRoom: " + building_name + room_name);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.ROOT_API_URL,
                response -> onSuccessRequest(mapFragmentContext, response, Constants.KEY_REQUEST_PATH_TO_ROOM),
                error -> Toast.makeText(mapFragmentContext, error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("building", building_name);
                params.put("room", room_name);
                params.put("dpi", String.valueOf(300));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(mapFragmentContext).addToRequestQueue(stringRequest);
    }

    /**
     * Sends request to log the user's searched location
     *
     * @param userIdNumber ID number of the current user
     * @param origin Location (or coordinates) of the current user
     * @param destination Name of the building/destination
     */
    private void searchLog(String userIdNumber, String origin, String destination) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_SEARCH_LOG,
                response -> onSuccessRequest(mapFragmentContext, response, Constants.KEY_SEARCH_LOG),
                error -> Log.d(TAG, "Search not recorded")
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", userIdNumber);
                params.put("origin", origin);
                params.put("destination", destination);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(mapFragmentContext).addToRequestQueue(stringRequest);
    }

    /**
     * Sends request to log user's visited location
     *
     * @param userIdNumber ID number of the current user
     * @param destination Name of the building/destination
     */
    private void visitLog(String userIdNumber, String destination) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_VISIT_LOG,
                response -> onSuccessRequest(mapFragmentContext, response, Constants.KEY_VISIT_LOG),
                error -> Log.d(TAG, "Visit not recorded.")
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", userIdNumber);
                params.put("destination", destination);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(mapFragmentContext).addToRequestQueue(stringRequest);
    }

    /**
     * Sends to request to submit a location to be added in the map
     *
     * @param userIdNumber ID number of the current user
     * @param locName Name of the suggested location
     * @param longitude Suggested location's longitude
     * @param latitude Suggested location's latitude
     * @param locDescription Short description of the suggested location
     */
    private void suggestLocation(String userIdNumber, String locName,
                                 String longitude, String latitude, String locDescription) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_SUBMIT_SUGGESTION,
                response -> onSuccessRequest(mapFragmentContext, response, Constants.KEY_SUGGEST_LOCATION),
                error -> Toast.makeText(mapFragmentContext, "Suggestion not sent", Toast.LENGTH_SHORT).show()
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", userIdNumber);
                params.put("loc_name", locName);
                params.put("longitude", longitude);
                params.put("latitude", latitude);
                params.put("loc_description", locDescription);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(mapFragmentContext).addToRequestQueue(stringRequest);
    }

    /**
     * Change the default location of the current user
     * The default location is used when the user is currently outside of UEP
     *
     * @param userIdNumber ID number of the current user
     * @param newDefLoc A building or coordinates
     */
    private void changeDefaultLocation(String userIdNumber, String newDefLoc) {
        Log.d(TAG, "changeDefaultLocation: " + userIdNumber + ", " + newDefLoc);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_CHANGE_DEF_LOC,
                response -> onSuccessRequest(mapFragmentContext, response, Constants.KEY_CHANGE_DEFAULT_LOCATION),
                error -> Toast.makeText(mapFragmentContext, "Default location not changed.", Toast.LENGTH_SHORT).show()
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", userIdNumber);
                params.put("user_def_loc", newDefLoc);
                return params;
            }
        };

        RequestHandler.getInstance(mapFragmentContext).addToRequestQueue(stringRequest);
    }

    @Override
    public void onSuccessRequest(Context context, String response, int request) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.getBoolean("error")) {
                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                return;
            }

            switch (request) {
                // Set up the images to be displayed on the map by layer
                case Constants.KEY_REQUEST_PATH_TO_ROOM:
                    Log.d(TAG, "onSuccessRequest: Path to room");
                    JSONArray array = obj.getJSONArray("building_boundary");
                    List<LatLng> building_boundary = new ArrayList<>();
                    List<String> imageURLs = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONArray currentJSONArray = array.getJSONArray(i);
                        building_boundary.add(new LatLng((double) currentJSONArray.get(0), (double) currentJSONArray.get(1)));
                    }
                    LatLngQuad quad = new LatLngQuad(
                            building_boundary.get(0), building_boundary.get(1),
                            building_boundary.get(2), building_boundary.get(3)
                    );
                    for (int n = 0; n < obj.getInt("floors"); n++) {
                        String img_url = Constants.ROOT_API_URL +
                                obj.getString("image_url" + (n + 1)).replace(" ", "%20");
                        imageURLs.add(img_url);
                        String id_source = Constants.ID_IMAGE_SOURCE + (n + 1);
                        String id_layer = Constants.ID_IMAGE_LAYER + (n + 1);
                        getMapboxMap().getStyle(style -> {
                            try {
                                style.addSource(new ImageSource(id_source, quad, new URI(img_url)));
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    // a float from 0 to 360 where the screen will face
                    // 0 or 360 North, 90 East, 180 South and 270 West
                    mapRotation = (float) obj.getDouble("building_rotation");
                    // URLs of images (floor plan) to be displayed in the map
                    imageURLList = imageURLs;
                    if (rasterLayerList == null) {
                        rasterLayerList = new ArrayList<>();
                    } else {
                        rasterLayerList.clear();
                    }
                    for (int x = 0; x < imageURLList.size(); x++) {
                        String id_source = Constants.ID_IMAGE_SOURCE + (x + 1);
                        String id_layer = Constants.ID_IMAGE_LAYER + (x + 1);
                        rasterLayerList.add(new RasterLayer(id_layer, id_source));
                    }
                    initPathToRoomDialog();
                    break;
                case Constants.KEY_SEARCH_LOG:
                case Constants.KEY_VISIT_LOG:
                    Log.d(TAG, "onSuccessRequest: " + obj.getString("message"));
                    break;
                case Constants.KEY_SUGGEST_LOCATION:
                case Constants.KEY_CHANGE_DEFAULT_LOCATION:
                    Log.d(TAG, "onSuccessRequest: " + obj.getString("message"));
                    Toast.makeText(mapFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        SharedPrefManager.getInstance(mapFragmentContext).setFetchedData(false);
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * Create a square around the location
     *
     * @param loc A LocationModel object containing the name
     *            and coordinates of a clicked location
     * @return Polygon from a set of points
     */
    private Polygon getArrivalArea(LocationModel loc) {
        float areaConstant = 0.00015F;
        float destLng = loc.getLocationLng();
        float lngIncrease = destLng + areaConstant;
        float lngDecrease = destLng - areaConstant;
        float destLat = loc.getLocationLat();
        float latIncrease = destLat + areaConstant;
        float latDecrease = destLat - areaConstant;

        List<List<Point>> area = new ArrayList<>();
        List<Point> points = new ArrayList<>();

        points.add(Point.fromLngLat(lngDecrease,latIncrease)); //top-left
        points.add(Point.fromLngLat(lngDecrease,latDecrease)); //top-right
        points.add(Point.fromLngLat(lngIncrease,latDecrease)); //bottom-right
        points.add(Point.fromLngLat(lngIncrease,latIncrease)); //bottom-left
        points.add(Point.fromLngLat(lngDecrease,latIncrease)); //top-left & closing pt
        area.add(points);
        return Polygon.fromLngLats(area);
    }

    @Override
    public void setMapboxMap(MapboxMap mapboxMap) {
        MapFragment.this.mapboxMap = mapboxMap;
    }

    @Override
    public Dialog getLocationsDialog() {
        return MapFragment.this.locationsDialog;
    }

    @Override
    public Dialog getDirectionsDialog() {
        return MapFragment.this.directionsDialog;
    }

    @Override
    public Dialog getPathToRoomDialog() {
        return MapFragment.this.pathToRoomDialog;
    }

    @Override
    public Dialog getBldgLvlDialog() {
        return MapFragment.this.bldgLvlDialog;
    }

    @Override
    public void setMapFragView(View v) {
        MapFragment.this.view = v;
    }

    @Override
    public void setClickedLocation(LatLng clickedLocation) {
        MapFragment.this.latLng = clickedLocation;
    }

    @Override
    public MapboxMap getMapboxMap() {
        return MapFragment.this.mapboxMap;
    }

    @Override
    public View getMapFragView() {
        return MapFragment.this.view;
    }

    @Override
    public Point getClickedLocation() {
        return Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
    }

    // Return LocationModel object with locationName loc
    public LocationModel getLocationObj(String loc) {
        for (LocationModel locationModel : locations) {
            if (locationModel.getLocationName().equals(loc)) {
                return locationModel;
            }
        }
        return null;
    }

    // Check if the string loc is in the Arraylist location
    public boolean containsLocation(String loc) {
        for (LocationModel locationModel : locations) {
            if (locationModel.getLocationName().equals(loc)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onExplanationNeeded(List<String> list) {

    }

    @Override
    public void onPermissionResult(boolean b) {

    }
}
