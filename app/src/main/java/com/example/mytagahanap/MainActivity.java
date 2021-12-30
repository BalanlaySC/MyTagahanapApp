package com.example.mytagahanap;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.geojson.Point;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ArrayList<LocationModel> locations;
    private BottomSheetDialog bottomSheetDialog;
    private TextView btsTxtLocation;

    private MapFragment mapFragment;
    private MapInterface mapInterface;

    private Point origin, destination;
    private final Point defOrigWhiteBeach = Point.fromLngLat(124.6779, 12.50784);
    private final Point defOrigUEPWelcome = Point.fromLngLat(124.659079, 12.51298);
    private final Point defOrigAdminBldg = Point.fromLngLat(124.666905, 12.509913);
    private double[] coords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          // check if token is still valid
//        if(!SharedPrefManager.getInstance(this).isLoggedIn()) {
//            SharedPrefManager.getInstance(this).logOut();
//            Toast.makeText(this, "Token expired. Login Again", Toast.LENGTH_LONG).show();
//            finish();
//            Intent intent = new Intent(getApplicationContext(), Login.class);
//            startActivity(intent);
//            return;
//        }

        initViews();

        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            setMapInterface(mapFragment);
            navigationView.setCheckedItem(R.id.nav_map);

            // reversed so that when location is disabled (= false) turns to true
            if (isLocationEnabled(this)) {
                enableLoc();
            }
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.other_menu);

        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.main_layout);

        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navheaderName = (TextView) headerView.findViewById(R.id.navheaderName);
        TextView navheaderidNumber = (TextView) headerView.findViewById(R.id.navheaderidNumber);
        LinearLayout navheaderLayout = (LinearLayout) headerView.findViewById(R.id.navheaderLayout);

        navheaderName.setText(SharedPrefManager.getInstance(this).getFullName());
        navheaderidNumber.setText(String.valueOf(SharedPrefManager.getInstance(this).getIdnumber()));
        navigationView.setNavigationItemSelectedListener(this);
        navheaderLayout.setOnClickListener(view -> {
            SharedPrefManager.getInstance(this).logOut();
            // uncomment if need to view user info
//            Log.d(TAG, SharedPrefManager.getInstance(this).getAllSharedPref());
            Toast.makeText(this, "Logout Successfully", Toast.LENGTH_LONG).show();
            finish();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        locations = new ArrayList<>();
        locations = databaseAccess.getAllLocations();

        bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_bottom_sheet, findViewById(R.id.bottomSheetContainer));

        // OnClickListener for Directions Button
        bottomSheetView.findViewById(R.id.btnDirections).setOnClickListener(view -> {
            Log.d(TAG, "Directions clicked");
//            Toast.makeText(MainActivity.this, "Generating path to " + btsTxtLocation.getText(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            LocationModel clickedLocation = getLocationObj((String) btsTxtLocation.getText());

            origin = getDevCurrentLocation();
            destination = Point.fromLngLat(clickedLocation.getLocationLng(), clickedLocation.getLocationLat());
            mapInterface.getRoute(mapInterface.getMapboxMap(), origin, destination);

            // Popup when pressing directions/starting navigation
            RelativeLayout layoutDirections = findViewById(R.id.layoutDirections);
            TextView editTxtStartLoc = findViewById(R.id.editTxtStartLoc);
            TextView editTxtDestination = findViewById(R.id.editTxtDestination);

            editTxtDestination.setText(btsTxtLocation.getText());
            layoutDirections.setVisibility(View.VISIBLE);

            ImageButton btnCloseDirections = findViewById(R.id.btnCloseDirections);
            btnCloseDirections.setOnClickListener(view1 -> {
                layoutDirections.setVisibility(View.GONE);
                mapInterface.removeLayer();
            });
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        btsTxtLocation = bottomSheetView.findViewById(R.id.btsTxtLocation);
    }

    // Initializing the option menu, specifically the search function
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the search menu action bar.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.other_menu, menu);

        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.actionSearch);

        // Get SearchView object.
        SearchView searchView = (SearchView) searchMenu.getActionView();

        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.holo_blue_light);

        // Create a new ArrayAdapter and add data (locations) to search auto complete object.
        ArrayAdapter<LocationModel> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        searchAutoComplete.setAdapter(arrayAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener((adapterView, view, itemIndex, id) -> {
            LocationModel clickedLocation = (LocationModel) adapterView.getItemAtPosition(itemIndex);
            String queryString = clickedLocation.getLocationName();

            searchView.clearFocus();
            searchAutoComplete.setText(queryString);
            initBottomSheet(queryString);
        });

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (containsLocation(query)) {
                    initBottomSheet(query);
                } else {
                    Toast.makeText(MainActivity.this, query + " not found, please try again", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    // Selecting something on the menu will switch into that fragment
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapFragment()).commit();

                if (isLocationEnabled(MainActivity.this)) {
                    enableLoc();
                }
                break;
            case R.id.nav_subjects:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ScheduleFragment()).commit();
                break;
            case R.id.nav_bldginfo:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BldgInfoFragment()).commit();
                break;
            case R.id.nav_aboutdevs:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AboutDevsFragment()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // This will minimize the drawer instead of closing the app
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Remove focus from a edit text and minimize keyboard
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    // Ask user to turn on GPS/Location
    public void enableLoc() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000); // 30 secs
        locationRequest.setFastestInterval(5 * 1000);   // 5 secs

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(this, 1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    /* Check if GPS/Location is enabled
     * !isLocationEnabled return true if enabled and false if disabled
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return true;
            }
            return locationMode == Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    // Return the current location of the device.
    // 0 is long, 1 is lat
    @SuppressLint("MissingPermission")
    public Point getDevCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (!isLocationEnabled(MainActivity.this)) {
            Log.d(TAG, "L336-User location is on " + !isLocationEnabled(MainActivity.this));
            if (location != null) {
                origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            } else {
                Log.d(TAG, "L340-:Location is null");
                origin = defOrigAdminBldg;
            }
        } else {
            Log.d(TAG, "L344-User location is off " + !isLocationEnabled(MainActivity.this));
            origin = defOrigAdminBldg;
        }

        return origin;
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

    // Return LocationModel object with locationName loc
    public LocationModel getLocationObj(String loc) {
        for (LocationModel locationModel : locations) {
            if (locationModel.getLocationName().equals(loc)) {
                return locationModel;
            }
        }
        return null;
    }

    // Initialize bottom sheet
    public void initBottomSheet(String query) {
        bottomSheetDialog.show();
        btsTxtLocation.setText(query);
        if (isLocationEnabled(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "To start at your current location " +
                    "you must enable GPS/Location Access", Toast.LENGTH_SHORT).show();
        }
    }

    public void setMapInterface(MapInterface mapInterface) {
        this.mapInterface = mapInterface;
    }
}