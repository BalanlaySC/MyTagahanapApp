package com.example.mytagahanap;

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
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements PermissionsListener, MapInterface {
    private static final String TAG = "MapFragment";
    final Handler handler = new Handler(Looper.getMainLooper());

    private MapView mapView;
    private View view;
    private MapboxMap mapboxMap;
    private DirectionsRoute currentRoute;
    private GeoJsonSource routeGeoJsonSource, iconGeoJsonSourceOrigin, iconGeoJsonSourceDestination;

    private Context mapFragmentContext;

    public MapFragment() { }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mapFragmentContext = requireContext().getApplicationContext();
        Mapbox.getInstance(mapFragmentContext, getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setMapFragView(view);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            setMapboxMap(mapboxMap);
            mapboxMap.setStyle(Constants.STYLE_URL, style -> {
                enableLocationComponent(style);
                initLayers(style);
            });
        });
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
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
            handler.postDelayed(() -> enableLocationComponent(loadedMapStyle), 10000);
        }
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     * @param mapboxMap the Mapbox map object that the route will be drawn on
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    public void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        MapboxDirections mapboxDirections = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .accessToken(Mapbox.getAccessToken())
                .build();

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

                Log.d(TAG, "mapboxMap is " + mapboxMap);
                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        // Retrieve and update the source designated for showing the directions route
                        routeGeoJsonSource = style.getSourceAs(Constants.ROUTE_SOURCE_ID);
                        iconGeoJsonSourceOrigin = style.getSourceAs(Constants.ICON_SOURCE_ID_O);
                        iconGeoJsonSourceDestination = style.getSourceAs(Constants.ICON_SOURCE_ID_D);

                        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
                                new LatLngBounds.Builder()
                                        .include(new LatLng(origin.latitude(), origin.longitude()))
                                        .include(new LatLng(destination.latitude(), destination.longitude()))
                                        .build(), 50), 3000);

                        // Create a LineString with the directions route's geometry and
                        // reset the GeoJSON routeGeoJsonSource for the route LineLayer source
                        // Also generate markers for origin and destination
                        if (routeGeoJsonSource != null) {
                            routeGeoJsonSource.setGeoJson(LineString.fromPolyline(Objects.requireNonNull(currentRoute.geometry()), PRECISION_6));
                        }
                        if (iconGeoJsonSourceOrigin != null) {
                            iconGeoJsonSourceOrigin.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {
                                    Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude()))}));
                        }
                        if (iconGeoJsonSourceDestination != null) {
                            iconGeoJsonSourceDestination.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {
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

    /**
     * Add the route and marker icon layers to the map
     */
    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(Constants.ROUTE_LAYER_ID, Constants.ROUTE_SOURCE_ID);

        //Add the route and marker sources to the map
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ROUTE_SOURCE_ID));
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ICON_SOURCE_ID_O));
        loadedMapStyle.addSource(new GeoJsonSource(Constants.ICON_SOURCE_ID_D));

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
                Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.red_marker))));
        loadedMapStyle.addImage(Constants.GREEN_PIN_ICON_ID,
                Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.green_marker))));

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(Constants.ICON_LAYER_ID_O, Constants.ICON_SOURCE_ID_O).withProperties(
                iconImage(Constants.GREEN_PIN_ICON_ID),
                iconSize((float) 0.25),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f}),
                textField("You are here"),
                textFont(new String[] {"Roboto Regular","Arial Unicode MS Regular"}),
                textOffset(new Float[] {0f, -1.25f})));

        loadedMapStyle.addLayer(new SymbolLayer(Constants.ICON_LAYER_ID_D, Constants.ICON_SOURCE_ID_D).withProperties(
                iconImage(Constants.RED_PIN_ICON_ID),
                iconSize((float) 0.25),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f}),
                textField("Destination"),
                textFont(new String[] {"Roboto Regular","Arial Unicode MS Regular"}),
                textOffset(new Float[] {0f, -1.25f})));
    }

    @Override
    public void removeLayer() {
        // Where LAYER_ID is a valid id of a layer that already
        // exists in the style
        getMapboxMap().getStyle(style -> {
            // If a route layer exists in the style, remove the layer
            if (routeGeoJsonSource != null ) {
                routeGeoJsonSource.setGeoJson(FeatureCollection.fromJson(""));
                iconGeoJsonSourceOrigin.setGeoJson(FeatureCollection.fromJson(""));
                iconGeoJsonSourceDestination.setGeoJson(FeatureCollection.fromJson(""));
                handler.postDelayed(() -> enableLocationComponent(style), 1000);
            }
        });
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
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Toast.makeText(mapFragmentContext, "You didn't grand permission.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
//        if (granted) {
//            Toast.makeText(mapFragmentContext, "onPermissionResult", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(mapFragmentContext, "onPermissionResult", Toast.LENGTH_SHORT).show();
//            getActivity().finish();
//        }
    }

    @Override
    public void setMapboxMap(MapboxMap mapboxMap) { MapFragment.this.mapboxMap = mapboxMap; }

    @Override
    public void setMapFragView(View v) { MapFragment.this.view = v; }

    @Override
    public MapboxMap getMapboxMap() { return MapFragment.this.mapboxMap; }

    @Override
    public View getMapFragView() { return MapFragment.this.view; }

//    public void
//    destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
//    MarkerOptions markerOptions2 = new MarkerOptions();
//                            markerOptions2.position(point);
//                            markerOptions2.title("destination");
//                            mapboxMap.addMarker(markerOptions2);
//    reverseGeocodeFunc(point,c);
//    getRoute(mapboxMap, origin, destination);
}
