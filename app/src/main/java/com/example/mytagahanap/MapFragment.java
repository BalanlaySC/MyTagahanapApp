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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
import com.mapbox.turf.TurfJoins;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements PermissionsListener, MapInterface {
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
    private Dialog suggestionDialog, locationsDialog;
    private ExtendedFloatingActionButton mapfragmentFab;

    private Context mapFragmentContext;

    private String symbolLayerId;
    private ArrayList<LocationModel> locations;

    public MapFragment() {
    }

    @SuppressLint({"WrongConstant", "ResourceType"})
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

                mapboxMap.addOnMapLongClickListener(point -> {
                    @SuppressLint("DefaultLocale") String pointName = String.format("%1$.7f, %2$.7f", point.getLongitude(), point.getLatitude());
                    openBottomSheetDialog(new LocationModel(pointName, (float) point.getLatitude(), (float) point.getLongitude()),
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
        });
        mapfragmentFab.setOnLongClickListener(view12 -> {
            if (!mapfragmentFab.isExtended()) {
                mapfragmentFab.extend();
            } else {
                mapfragmentFab.shrink();
            }
            return true;
        });

        suggestionDialog = new Dialog(getActivity());
        locationsDialog = new Dialog(getActivity());

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
            Log.d(TAG, "enableLocationComponent: getcameraposition" + mapboxMap.getCameraPosition());
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
            handler.postDelayed(() -> {
                if (ActivityCompat.checkSelfPermission(mapFragmentContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(mapFragmentContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    enableLocationComponent(loadedMapStyle);
                }
            }, 10000);
        }
    }

    /* Return the current location of the device.
    If outside of UEP the assigned Default Location will be used
    0 is long, 1 is lat */
    @SuppressLint("MissingPermission")
    public LocationModel getDevCurrentLocation() {
        String defaultLocation = SharedPrefManager.getInstance(mapFragmentContext).getDefLoc();
        Log.d(TAG, "getDevCurrentLocation: defaultlocation " + defaultLocation);
        LocationModel currentLM = getLocationObj(defaultLocation);
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
        LocationModel currentLM = getLocationObj(SharedPrefManager.getInstance(mapFragmentContext).getDefLoc());
        Log.d(TAG, "getRoute: currentLM " + SharedPrefManager.getInstance(mapFragmentContext).getDefLoc());
        Point currentLMPoint = Point.fromLngLat(currentLM.getLocationLng(), currentLM.getLocationLat());
        Polygon polygon = Polygon.fromLngLats(com.example.mytagahanap.Constants.POINTS);
        MapboxDirections mapboxDirections;

        boolean isLocInsideUEP = TurfJoins.inside(origin, polygon);
        if (Mapbox.getAccessToken() != null) {
            if (isLocInsideUEP) {
                Log.d(TAG, "getRoute: origin is inside UEP");
                mapboxDirections = MapboxDirections.builder()
                        .origin(origin)
                        .destination(destination)
                        .overview(DirectionsCriteria.OVERVIEW_FULL)
                        .profile(DirectionsCriteria.PROFILE_WALKING)
                        .accessToken(Mapbox.getAccessToken())
                        .build();
            } else {
                Log.d(TAG, "getRoute: origin is outside of UEP");
                Toast.makeText(mapFragmentContext, "Currently outside of UEP\n" +
                        "Generating path from " + currentLM.getLocationName(), Toast.LENGTH_LONG).show();
                mapboxDirections = MapboxDirections.builder()
                        .addWaypoint(currentLMPoint)
                        .origin(origin)
                        .destination(destination)
                        .overview(DirectionsCriteria.OVERVIEW_FULL)
                        .profile(DirectionsCriteria.PROFILE_DRIVING)
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

    // Marks the map on the clicked location
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

    // Marks the map on the clicked location but with a offset
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

        // Add the red marker icon SymbolLayer to the map
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

    // Display bottomsheet dialog to show information of the location
    public void openBottomSheetDialog(LocationModel clickedLocation, Context context) {
        handler.postDelayed(() -> markMapboxMapOffset(getMapboxMap(), clickedLocation), 250);
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setOnCancelListener(dialogInterface -> clearLayers());
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet);

        // Display clicked location
        TextView btsTxtLocation = bottomSheetDialog.findViewById(R.id.btsTxtLocation);
        if (btsTxtLocation != null) {
            btsTxtLocation.setText(clickedLocation.getLocationName());
        }

        // OnClickListener for Directions Button
        TextView btnDirections = bottomSheetDialog.findViewById(R.id.btnDirections);
        if (btnDirections != null) {
            btnDirections.setOnClickListener(view -> {
                Log.d(TAG, "initViews: Directions started");
                bottomSheetDialog.dismiss();

                // Popup dialog when pressing directions/starting navigation
                initDirectionDialog(clickedLocation);
            });
        }

        // Display images of a known location
        LinearLayout bsImageLayout = bottomSheetDialog.findViewById(R.id.bsImageLayout);
        if (bsImageLayout != null) {
            initImageLayout(clickedLocation, bsImageLayout);
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
            if (containsLocation(clickedLocation.getLocationName())) {
                smbtnSetDefault.setVisibility(View.VISIBLE);
            } else {
                smbtnSetDefault.setVisibility(View.GONE);
            }

            // change def location
            smbtnSetDefault.setOnClickListener(view -> {
                changeDefaultLocation(String.valueOf(
                        SharedPrefManager.getInstance(context).getIdnumber()),
                        clickedLocation.getLocationName());
                SharedPrefManager.getInstance(context).updateDefLocation(clickedLocation.getLocationName());
            });
        }
        if (smbtnViewRooms != null) {
            String imgUrl = (Constants.ROOT_URL + "img/" +
                    clickedLocation.getLocationName() + "/" + clickedLocation.getLocationName())
                    .replace(" ", "%20");
            // check if location is college building, else feature disabled
            if (clickedLocation.getLocationName().startsWith("College")) {
                smbtnViewRooms.setVisibility(View.VISIBLE);
                smbtnViewRooms.setOnClickListener(view -> {
                    ArrayList<SliderItem> sliderItems = new ArrayList<>();
                    sliderItems.add(new SliderItem(imgUrl + "FloorPlan.jpg"));
                    Intent intent = new Intent(mapFragmentContext, EnlargeImageActivity.class);
                    intent.putParcelableArrayListExtra("enlargedImage", sliderItems);
                    startActivity(intent);
                });
            }
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

    // Displays images for a clicked location in bottom sheet
    private void initImageLayout(LocationModel clickedLocation, LinearLayout bsImageLayout) {
        String holder = (Constants.ROOT_URL + "img/" +
                clickedLocation.getLocationName().replace(".", "")
                + "/" + clickedLocation.getLocationName())
                .replace(" ", "%20");

        bsImageLayout.setOnClickListener(view -> {
            ArrayList<SliderItem> sliderItems = new ArrayList<>();
            sliderItems.add(new SliderItem(holder + "Preview1.jpg"));
            sliderItems.add(new SliderItem(holder + "Preview2.jpg"));
            Intent intent = new Intent(mapFragmentContext, EnlargeImageActivity.class);
            intent.putParcelableArrayListExtra("enlargedImage", sliderItems);
            startActivity(intent);
        });

        ShapeableImageView locationimg1 = bottomSheetDialog.findViewById(R.id.locationimg1);
        ShapeableImageView locationimg2 = bottomSheetDialog.findViewById(R.id.locationimg2);
        // glide allows us to display images from the internet, via url/uri
        if (containsLocation(clickedLocation.getLocationName())) {
            bsImageLayout.setVisibility(View.VISIBLE);
            if (locationimg1 != null && locationimg2 != null) {
                Glide.with(mapFragmentContext)
                        .asBitmap()
                        .placeholder(R.drawable.image_placeholder)
                        .load(Uri.parse(holder + "Thumb1.jpg"))
                        .into(locationimg1);
                Glide.with(mapFragmentContext)
                        .asBitmap()
                        .placeholder(R.drawable.image_placeholder)
                        .load(Uri.parse(holder + "Thumb2.jpg"))
                        .into(locationimg2);
                handler.postDelayed(() -> {
                    Glide.with(mapFragmentContext)
                            .asBitmap()
                            .placeholder(R.drawable.image_placeholder)
                            .load(Uri.parse(holder + "Thumb1.jpg"))
                            .into(locationimg1);
                    Glide.with(mapFragmentContext)
                            .asBitmap()
                            .placeholder(R.drawable.image_placeholder)
                            .load(Uri.parse(holder + "Thumb2.jpg"))
                            .into(locationimg2);
                }, 5000);
            }
        }
    }

    // Display dialog for sending suggestions
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
            sendSuggestion(String.valueOf(SharedPrefManager.getInstance(mapFragmentContext).getIdnumber()),
                    mLocName, mLocLong, mLocLat, mLocDesc);
            suggestionDialog.dismiss();
        });
        suggestionImgBtnClose.setOnClickListener(view -> suggestionDialog.dismiss());

        suggestionDialog.show();
    }

    // Display dialog for a current location and destination
    public void initDirectionDialog(LocationModel destinationLM) {
        handler.postDelayed(() -> {
            if (mapfragmentFab != null)
                mapfragmentFab.hide();
        }, 250);
        Dialog directionsDialog = new Dialog(getActivity());
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

        LocationModel originLM = getDevCurrentLocation();
        origin = Point.fromLngLat(originLM.getLocationLng(),
                originLM.getLocationLat());
        float destLng = destinationLM.getLocationLng();
        float destLat = destinationLM.getLocationLat();
        destination = Point.fromLngLat(destLng, destLat);
        getRoute(getMapboxMap(), origin, destination);

        directionsStartLoc.setText(originLM.getLocationName());
        directionsDestination.setText(destinationLM.getLocationName());
        directionsDialog.show();

        directionsImgBtnClose.setOnClickListener(view -> {
            clearLayers();
            directionsDialog.dismiss();
            if (mapfragmentFab != null)
                mapfragmentFab.show();
        });

        List<List<Point>> area = new ArrayList<>();
        List<Point> points = new ArrayList<>();

        points.add(Point.fromLngLat(destLng - 0.00015,destLat + 0.00015));
        points.add(Point.fromLngLat(destLng - 0.00015,destLat - 0.00015));
        points.add(Point.fromLngLat(destLng + 0.00015,destLat - 0.00015));
        points.add(Point.fromLngLat(destLng + 0.00015,destLat + 0.00015));
        points.add(Point.fromLngLat(destLng - 0.00015,destLat + 0.00015));
        area.add(points);

        Polygon polygon = Polygon.fromLngLats(area);

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
            }
            return true;
        });
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

            openBottomSheetDialog(locations.get(position), requireContext());
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
        // Where LAYER_ID is a valid id of a layer that already
        // exists in the style
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
    }

    private void sendSuggestion(String userIdNumber, String locName,
                                String longitude, String latitude, String locDescription) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_SUBMIT_SUGGESTION,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(mapFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(mapFragmentContext, "No connection to server.", Toast.LENGTH_SHORT).show()
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

        RequestHandler.getInstance(mapFragmentContext).addToRequestQueue(stringRequest);
    }

    private void changeDefaultLocation(String userIdNumber, String newDefLoc) {
        Log.d(TAG, "changeDefaultLocation: " + userIdNumber + ", " + newDefLoc);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_CHANGE_DEF_LOC,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(mapFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(mapFragmentContext, "No connection to server.", Toast.LENGTH_SHORT).show()
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

    @Override
    public void setMapboxMap(MapboxMap mapboxMap) {
        MapFragment.this.mapboxMap = mapboxMap;
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
