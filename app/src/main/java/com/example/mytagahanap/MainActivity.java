package com.example.mytagahanap;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ProgressBar pbMainActivity;
    private Dialog profileDialog;

    private MapFragment mapFragment;
    private ScheduleFragment scheduleFragment;
    private MapInterface mapInterface;

    private ArrayList<LocationModel> locations;
    private ArrayList<SubjectModel> classSchedule;
    private String fullName, token, defaultLocation;
    private int idnumber;

    public MainActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSharedPreference();

          // check if token is still valid
        if(isSessionTimedOut(SharedPrefManager.getInstance(this).getTimeOutSession()) < 0) {
            logoutUser("Your session has timed out.\nPlease log in again.", Toast.LENGTH_LONG);
            return;
        }
        initViews();

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        locations = new ArrayList<>();
        locations = databaseAccess.getAllLocations();

        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            scheduleFragment = new ScheduleFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            setMapInterface(mapFragment);
            navigationView.setCheckedItem(R.id.nav_map);

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("Locations", locations);
            mapFragment.setArguments(bundle);

            // reversed so that when location is disabled (= false) turns to true
            if (isLocationEnabled(this)) {
                enableLoc();
            }
        }

        handler.postDelayed(this::parseUserClassSchedule, 500);
        profileDialog = new Dialog(this);
    }

    private int isSessionTimedOut(long userTimeMillis) {
        Date userDate = new Date(userTimeMillis);
        Calendar cal = Calendar.getInstance();              // creates calendar
        cal.setTime(new Date(System.currentTimeMillis()));  // sets calendar time/date
        int result = compareDates(userDate, cal.getTime());
        if(result > 0) {
            Log.d(TAG, "isSessionTimedOut: Session is active " + userDate);
        }
        else if (result < 0) {
            Log.d(TAG, "isSessionTimedOut: Session invalid " + userDate);
        }
        else {
            Log.d(TAG, "isSessionTimedOut: Date is equal" + userDate);
        }
        return result;
    }

    private void logoutUser(String strToast, int len) {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        intent.putExtra("previous User", SharedPrefManager.getInstance(this).getIdnumber());
        SharedPrefManager.getInstance(this).logOut();
        finish();
        Toast.makeText(this, strToast, len).show();
        startActivity(intent);
    }

    private void loadSharedPreference() {
        fullName = SharedPrefManager.getInstance(this).getFullName();
        idnumber = SharedPrefManager.getInstance(this).getIdnumber();
        defaultLocation = SharedPrefManager.getInstance(this).getDefLoc();
        token = SharedPrefManager.getInstance(this).getToken();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.other_menu);

        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.main_layout);

        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navheaderName = headerView.findViewById(R.id.navheaderName);
        TextView navheaderidNumber = headerView.findViewById(R.id.navheaderidNumber);
        LinearLayout navheaderLayout = headerView.findViewById(R.id.navheaderLayout);

        navheaderName.setText(fullName);
        navheaderidNumber.setText(String.valueOf(idnumber));
        navigationView.setNavigationItemSelectedListener(this);
        navheaderLayout.setOnClickListener(view -> openProfileDialog());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Initializing the option menu, also the search function
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the search menu action bar.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.other_menu, menu);
        MenuItem currentMenuItem = navigationView.getCheckedItem();
        MenuItem mapMenuItem = menu.findItem(R.id.nav_map);

        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.actionSearch);

        // Get SearchView object.
        SearchView searchView = (SearchView) searchMenu.getActionView();

        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.color.divider);

        // Create a new ArrayAdapter and add data (locations) to search auto complete object.
        ArrayAdapter<LocationModel> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        searchAutoComplete.setAdapter(arrayAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener((adapterView, view, itemIndex, id) -> {
            searchView.clearFocus();
            LocationModel clickedLocation = (LocationModel) adapterView.getItemAtPosition(itemIndex);
            searchAutoComplete.setText(clickedLocation.getLocationName());
            mapInterface.openBottomSheetDialog(clickedLocation, MainActivity.this);
            if (mapMenuItem != currentMenuItem) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        mapFragment).commit();
                navigationView.setCheckedItem(R.id.nav_map);
            }
        });

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (containsLocation(query)) {
                    LocationModel clickedLocation = getLocationObj(query);
                    mapInterface.openBottomSheetDialog(clickedLocation, MainActivity.this);
                    if (mapMenuItem != currentMenuItem) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                mapFragment).commit();
                        navigationView.setCheckedItem(R.id.nav_map);
                    }
                } else {
                    Toast.makeText(MainActivity.this, query + "\nnot found, please try again", Toast.LENGTH_LONG).show();
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
                        mapFragment).commit();

                Bundle bundleLoc = new Bundle();
                bundleLoc.putParcelableArrayList("Locations", locations);
                mapFragment.setArguments(bundleLoc);

                if (isLocationEnabled(MainActivity.this)) {
                    enableLoc();
                }
                break;
            case R.id.nav_subjects:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        scheduleFragment).commit();

                Bundle bundleSched = new Bundle();
                bundleSched.putParcelableArrayList("Class Schedule", classSchedule);
                scheduleFragment.setArguments(bundleSched);
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

    private void openProfileDialog()  {
        drawer.closeDrawer(GravityCompat.START);
        profileDialog.setContentView(R.layout.layout_dialog_profile);
        Window window = profileDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView profileName = profileDialog.findViewById(R.id.profileName);
        TextView profileIdnumber = profileDialog.findViewById(R.id.profileIdnumber);
        TextView profileToken = profileDialog.findViewById(R.id.profileToken);
        TextView profileDefaultLoc = profileDialog.findViewById(R.id.profileDefaultLoc);
        TextView profileSaveBtn = profileDialog.findViewById(R.id.profileSaveBtn);
        EditText profilePassword = profileDialog.findViewById(R.id.profilePassword);
        Button profileLogout = profileDialog.findViewById(R.id.profileLogout);
        ImageButton profileImgBtnClose = profileDialog.findViewById(R.id.profileImgBtnClose);
        ImageButton profileEditBtn = profileDialog.findViewById(R.id.profileEditBtn);

        profileName.setText(fullName);
        profileIdnumber.setText(String.valueOf(idnumber));
        profileToken.setText(token);
        profileDefaultLoc.setText(defaultLocation);
        profilePassword.setText(SharedPrefManager.getInstance(this).getPassword());

        profileToken.setOnClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Token", profileToken.getText());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "Token copied", Toast.LENGTH_SHORT).show();
        });

        profilePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        profileLogout.setOnClickListener(view -> {
            logoutUser("Logout Successfully", Toast.LENGTH_SHORT);
            profileDialog.dismiss();
        });

        profileEditBtn.setOnClickListener(view -> {
            if(profileSaveBtn.getVisibility() == View.GONE) {
                profilePassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                profilePassword.setClickable(true);
                profilePassword.setFocusable(true);
                profilePassword.setFocusableInTouchMode(true);
                profileSaveBtn.setVisibility(View.VISIBLE);
            } else {
                profilePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                profilePassword.setClickable(false);
                profilePassword.setFocusable(false);
                profilePassword.setFocusableInTouchMode(false);
                profileSaveBtn.setVisibility(View.GONE);
            }
        });

        profileSaveBtn.setOnClickListener(view -> {
            SharedPrefManager.getInstance(MainActivity.this)
                    .updatePassword(String.valueOf(profilePassword.getText()).trim());
            profilePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            profilePassword.setClickable(false);
            profilePassword.setFocusable(false);
            profilePassword.setFocusableInTouchMode(false);
            profileSaveBtn.setVisibility(View.GONE);
            changePassword(String.valueOf(idnumber), String.valueOf(profilePassword.getText()).trim());
        });

        profileImgBtnClose.setOnClickListener(view -> profileDialog.dismiss());

        profileDialog.show();
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
                Toast.makeText(this, String.valueOf(response), Toast.LENGTH_SHORT).show();
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

    // Check if the string loc is in the Arraylist location
    public boolean containsLocation(String loc) {
        for (LocationModel locationModel : locations) {
            if (locationModel.getLocationName().equals(loc)) { return true; }
        }
        return false;
    }

    // Return LocationModel object with locationName loc
    public LocationModel getLocationObj(String loc) {
        for (LocationModel locationModel : locations) {
            if (locationModel.getLocationName().equals(loc)) { return locationModel; }
        }
        return null;
    }

    // Parse the class schedule of the current user
    public void parseUserClassSchedule() {
        Log.d(TAG, "parseUserClassSchedule: Parsing data");
        classSchedule = new ArrayList<>();
        pbMainActivity = mapInterface.getMapFragView().findViewById(R.id.pbMapFragment);
        pbMainActivity.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_CLASS_SCHED + idnumber,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            pbMainActivity.setVisibility(View.GONE);
                            JSONArray allSubj = obj.getJSONArray("class_schedule");
                            for (int i = 0; i < allSubj.length(); i++) {
                                JSONObject arrayJObj = allSubj.getJSONObject(i);
                                classSchedule.add(new SubjectModel(arrayJObj.getString("class_id"),
                                        arrayJObj.getString("subj_code"),
                                        arrayJObj.getString("description"),
                                        arrayJObj.getString("time"),
                                        arrayJObj.getString("day"),
                                        arrayJObj.getString("room")));
                            }
                            classSchedule.sort(Comparator.comparing(SubjectModel::getmDescription));
                        } else {
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            pbMainActivity.setVisibility(View.GONE);
            classSchedule.add(new SubjectModel("", "", "Unable to get subjects",
                    "", "", ""));
            Toast.makeText(this, "Server is down.", Toast.LENGTH_SHORT).show();
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(getApplicationContext()).getIdnumber()));
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void setMapInterface(MapInterface mapInterface) { this.mapInterface = mapInterface; }
    
    /*  Compare two Date objects
        -1 is past, 0 is equal, 1 is future */
    public int compareDates(Date date1, Date date2) { return date1.compareTo(date2); }

    private void changePassword(String userIdNumber, String password) {
        Log.d(TAG, "changePassword: " + userIdNumber + ", " + password);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_CHANGE_PASSWORD,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(MainActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(MainActivity.this, "No connection to server.", Toast.LENGTH_SHORT).show()
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", userIdNumber);
                params.put("user_password", password);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }
}