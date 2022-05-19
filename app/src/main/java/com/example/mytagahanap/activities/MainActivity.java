package com.example.mytagahanap.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.mytagahanap.globals.DatabaseAccess;
import com.example.mytagahanap.R;
import com.example.mytagahanap.fragments.AboutAppFragment;
import com.example.mytagahanap.fragments.DashboardFragment;
import com.example.mytagahanap.fragments.MapFragment;
import com.example.mytagahanap.fragments.ScheduleFragment;
import com.example.mytagahanap.fragments.SchoolInfoFragment;
import com.example.mytagahanap.fragments.SubmissionFragment;
import com.example.mytagahanap.globals.Constants;
import com.example.mytagahanap.globals.SharedPrefManager;
import com.example.mytagahanap.interfaces.MainActivityInterface;
import com.example.mytagahanap.interfaces.MapInterface;
import com.example.mytagahanap.interfaces.VolleyCallbackInterface;
import com.example.mytagahanap.models.LocationModel;
import com.example.mytagahanap.network.RequestHandler;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MainActivityInterface, VolleyCallbackInterface {
    private static final String TAG = "MainActivity";
    final Handler handler = new Handler(Looper.getMainLooper());

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Menu menu;
    private MenuItem mapMenuItem, dashboardMenuItem, currentMenuItem;
    private Dialog profileDialog, feedbackDialog;

    private MapFragment mapFragment;
    private ScheduleFragment scheduleFragment;
    private DashboardFragment dashboardFragment;
    private MapInterface mapInterface;

    private ArrayList<LocationModel> locations;
    private String fullName, token;
    private int idnumber;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUserData();

        // check if session is still valid
        if (SharedPrefManager.getInstance(this).getKeepMeSignedIn()) {
            if (isSessionTimedOut(SharedPrefManager.getInstance(this).getTimeOutSession()) < 0) {
                logoutUser("Your session has timed out.\nPlease log in again.", Toast.LENGTH_LONG);
                return;
            }
            try {
                SharedPrefManager.getInstance(this).setKeepMeSignedIn(getIntent().getExtras().getBoolean("KeepMeSignedIn"));
            } catch (NullPointerException ignored) {
            }
        } else {
            logoutUser("Logout on exit", -1);
            return;
        }
        initViews();

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        locations = new ArrayList<>();
        saveLocationsToJSON();
//        locations = databaseAccess.getAllLocations();
        String parcelableTag = "Locations";
        mapFragment = new MapFragment();
        setMapInterface(mapFragment);
        scheduleFragment = new ScheduleFragment();
        dashboardFragment = new DashboardFragment();

        if (savedInstanceState == null) {
            initFragment("Dashboard", dashboardFragment);
            String title = String.format("Hello, <b>%s</b>", SharedPrefManager.getInstance(this).getfName());
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
            }
            navigationView.setCheckedItem(R.id.nav_dashboard);

            parcelDataToFragment(parcelableTag, locations, dashboardFragment);

            // reversed so that when location is disabled (= false) turns to true
            if (isLocationEnabled(this)) {
                enableLoc();
            }
        }

        profileDialog = new Dialog(this);
        feedbackDialog = new Dialog(this);
    }

    /**
     *
     * @param userTimeMillis time in milliseconds on which the session will expire
     * @return -1 if session is expired, 0 >= is session is valid
     */
    private int isSessionTimedOut(long userTimeMillis) {
        Date userDate = new Date(userTimeMillis);
        Calendar cal = Calendar.getInstance();              // creates calendar
        cal.setTime(new Date(System.currentTimeMillis()));  // sets calendar time/date
        int result = compareDates(userDate, cal.getTime());
        if (result > 0) {
            Log.d(TAG, "isSessionTimedOut: Session is active " + userDate);
        } else if (result < 0) {
            Log.d(TAG, "isSessionTimedOut: Session invalid " + userDate);
        } else {
            Log.d(TAG, "isSessionTimedOut: Date is equal" + userDate);
        }
        return result;
    }

    /**
     * Logs out the user and redirected to Login section
     *
     * @param strToast msg to be displayed for the user
     * @param len duration of Toast
     */
    private void logoutUser(String strToast, int len) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.putExtra("previous User", SharedPrefManager.getInstance(this).getIdnumber());
        SharedPrefManager.getInstance(this).logOut();
        finish();
        if (len >= 0) {
            Toast.makeText(this, strToast, len).show();
        } else {
            Log.d(TAG, strToast);
        }
        startActivity(intent);
    }

    // Load info of currently logged in user upon opening the app
    private void loadUserData() {
        fullName = SharedPrefManager.getInstance(this).printFullName();
        idnumber = SharedPrefManager.getInstance(this).getIdnumber();
        token = SharedPrefManager.getInstance(this).getToken();


    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.other_menu); // Other menu, top-right
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        if (idnumber != 1) {
            Menu navMenu = navigationView.getMenu();
            navMenu.findItem(R.id.nav_submitted_spots).setVisible(false);
        }
        View headerView = navigationView.getHeaderView(0);
        TextView navheaderName = headerView.findViewById(R.id.navheaderName);
        RelativeLayout navheaderLayout = headerView.findViewById(R.id.navheaderLayout);
        LinearLayout navLogout = headerView.findViewById(R.id.navLogout);

        navheaderName.setText(fullName);
        navigationView.setNavigationItemSelectedListener(this);
        navheaderLayout.setOnClickListener(view -> openProfileDialog());
        navLogout.setOnClickListener(view -> logoutUser("Logout Successfully", Toast.LENGTH_SHORT));

        // Side drawer
        drawer = findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (mapFragment.getDirectionsDialog() != null || mapFragment.getLocationsDialog() != null
                        || mapFragment.getPathToRoomDialog() != null) {
                    if (mapFragment.getDirectionsDialog().isShowing()) {
                        mapFragment.getDirectionsDialog().dismiss();
                    }
                    if (mapFragment.getLocationsDialog().isShowing()) {
                        mapFragment.getLocationsDialog().dismiss();
                    }
                    if (mapFragment.getPathToRoomDialog().isShowing()) {
                        mapFragment.getLocationsDialog().dismiss();
                    }
                }
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Initializing the option menu, also the search function
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MainActivity.this.menu = menu;
        // Inflate the search menu action bar.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.other_menu, menu);
        mapMenuItem = menu.findItem(R.id.nav_map);
        currentMenuItem = navigationView.getCheckedItem();

        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.actionSearch);

        // Get SearchView object.
        SearchView searchView = (SearchView) searchMenu.getActionView();

        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.color.divider);
        searchAutoComplete.setThreshold(1);

        // Create a new ArrayAdapter and add data (locations) to search auto complete object.
        ArrayAdapter<LocationModel> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locations);
        searchAutoComplete.setAdapter(arrayAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener((adapterView, view, itemIndex, id) -> {
            searchView.clearFocus();
            LocationModel clickedLocation = (LocationModel) adapterView.getItemAtPosition(itemIndex);
            searchAutoComplete.setText(clickedLocation.getLocationName());
            if (mapMenuItem != currentMenuItem) {
                initFragment("Map", mapFragment);
                parcelDataToFragment("Locations", locations, mapFragment);
                navigationView.setCheckedItem(R.id.nav_map);
            }
            handler.postDelayed(() -> mapInterface.openBottomSheetDialog(clickedLocation, "", MainActivity.this), 500);
        });

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (containsLocation(query)) {
                    LocationModel clickedLocation = getLocationObj(query);
                    if (mapMenuItem != currentMenuItem) {
                        initFragment("Map", mapFragment);
                        parcelDataToFragment("Locations", locations, mapFragment);
                        navigationView.setCheckedItem(R.id.nav_map);
                    }
                    handler.postDelayed(() -> mapInterface.openBottomSheetDialog(clickedLocation, "", MainActivity.this), 500);
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

        // User guide inside the app
        MenuItem actionHelp = menu.findItem(R.id.actionHelp);
        actionHelp.setOnMenuItemClickListener(menuItem -> {
            Uri uri = Uri.parse("http://mytagahanap.000webhostapp.com/First-time%20user%20guide.pdf");
            Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent1);
            return false;
        });

        return true;
    }

    // Selecting something on the menu will switch into that fragment
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String[] actionBarTitle = {"Dashboard", "Map", "Class Schedule", "School Information",
//                "College Information",
                "About App", "List of Submitted Spots"};
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                initFragment(actionBarTitle[0], dashboardFragment);
                break;
            case R.id.nav_map:
                initFragment(actionBarTitle[1], mapFragment);
                parcelDataToFragment("Locations", locations, mapFragment);

                if (isLocationEnabled(MainActivity.this)) {
                    enableLoc();
                }
                break;
            case R.id.nav_subjects:
                initFragment(actionBarTitle[2], scheduleFragment);
                break;
            case R.id.nav_schoolinfo:
                initFragment(actionBarTitle[3], new SchoolInfoFragment());
                break;
//            case R.id.nav_collegeinfo:
//                initFragment(actionBarTitle[4], new CollegeInfoFragment());
//                break;
            case R.id.nav_aboutapp:
                initFragment(actionBarTitle[4], new AboutAppFragment());
                break;
            case R.id.nav_submitted_spots:
                initFragment(actionBarTitle[5], new SubmissionFragment());
                break;
            case R.id.nav_feedback:
                openFeedbackDialog();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Handles moving from one fragment to another
     * @param actionBarTitle title to be shown in the Toolbar
     * @param fragment the fragment to move into
     */
    private void initFragment(String actionBarTitle, Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(actionBarTitle);
        }
    }

    // Display a window pop-up showing the user's profile
    private void openProfileDialog() {
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
        profileDefaultLoc.setText(SharedPrefManager.getInstance(this).getDefLoc());
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
            if (profileSaveBtn.getVisibility() == View.GONE) {
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
            changePassword(getApplicationContext(), String.valueOf(idnumber),
                    String.valueOf(profilePassword.getText()).trim());
        });

        profileImgBtnClose.setOnClickListener(view -> profileDialog.dismiss());

        profileDialog.show();
    }

    // Display a window pop-up showing user's feedback
    private void openFeedbackDialog() {
        drawer.closeDrawer(GravityCompat.START);
        feedbackDialog.setContentView(R.layout.layout_dialog_react);
        Window window = feedbackDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView reactGreat = feedbackDialog.findViewById(R.id.reactGreat);
        TextView reactHappy = feedbackDialog.findViewById(R.id.reactHappy);
        TextView reactWow = feedbackDialog.findViewById(R.id.reactWow);
        TextView reactSad = feedbackDialog.findViewById(R.id.reactSad);
        TextView reactAngry = feedbackDialog.findViewById(R.id.reactAngry);
        ImageButton reactImgBtnClose = feedbackDialog.findViewById(R.id.reactImgBtnClose);

        sendFeedback(getApplicationContext(), feedbackDialog,
                reactGreat, String.valueOf(idnumber));
        sendFeedback(getApplicationContext(), feedbackDialog,
                reactHappy, String.valueOf(idnumber));
        sendFeedback(getApplicationContext(), feedbackDialog,
                reactWow, String.valueOf(idnumber));
        sendFeedback(getApplicationContext(), feedbackDialog,
                reactSad, String.valueOf(idnumber));
        sendFeedback(getApplicationContext(), feedbackDialog,
                reactAngry, String.valueOf(idnumber));

        reactImgBtnClose.setOnClickListener(view -> feedbackDialog.dismiss());
        feedbackDialog.show();
    }

    // This will minimize the drawer instead of closing the app
    @Override
    public void onBackPressed() {
        if (mapFragment.getDirectionsDialog() != null || mapFragment.getLocationsDialog() != null
                || mapFragment.getPathToRoomDialog() != null) {
            if (mapFragment.getDirectionsDialog().isShowing()) {
                mapFragment.getDirectionsDialog().dismiss();
            }
            if (mapFragment.getLocationsDialog().isShowing()) {
                mapFragment.getLocationsDialog().dismiss();
            }
            if (mapFragment.getPathToRoomDialog().isShowing()) {
                mapFragment.getLocationsDialog().dismiss();
            }
        }
        dashboardMenuItem = getMenu().findItem(R.id.nav_dashboard);
        currentMenuItem = getNavigationView().getCheckedItem();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (dashboardMenuItem != currentMenuItem) {
            initFragment("Map", dashboardFragment);
            parcelDataToFragment("Locations", locations, dashboardFragment);
            navigationView.setCheckedItem(R.id.nav_map);
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

    /**
     * Checks if device's GPS/Location is enabled
     *
     * @param context current state of the app
     * @return !isLocationEnabled return true if enabled and false if disabled
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

    /**
     * Initializing interface for MapFragment in order to use functions
     * from the said class
     *
     * @param mapInterface Fragment's instance
     */
    public void setMapInterface(MapInterface mapInterface) {
        this.mapInterface = mapInterface;
    }

    /**
     * Compare two Date objects
     *
     * @param date1 1st Date object
     * @param date2 2nd Date object
     * @return -1 is past, 0 is equal, 1 is future
     */
    public int compareDates(Date date1, Date date2) {
        return date1.compareTo(date2);
    }

    /**
     * Sends request to change current user's password
     *
     * @param userIdNumber current user's id number
     * @param password new password
     */
    public void changePassword(Context context, String userIdNumber, String password) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_CHANGE_PASSWORD,
                response -> onSuccessRequest(context, response, Constants.KEY_CHANGE_PASS),
                error -> Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
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

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }

    /**
     * Sends request to submit a feedback on "What do you feel about the app?"
     *
     * @param tv the chosen feedback of the user
     * @param userIdNumber current user's id number
     */
    public void sendFeedback(Context context, Dialog feedbackDialog, TextView tv, String userIdNumber) {
        tv.setOnClickListener(view -> {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST, Constants.URL_SEND_FEEDBACK,
                    response -> onSuccessRequest(context, response, Constants.KEY_SEND_FEEDBACK),
                    error -> Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            ) {
                @NonNull
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_idnumber", userIdNumber);
                    params.put("reaction", (String) tv.getText());
                    return params;
                }
            };

            RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
            feedbackDialog.dismiss();
        });
    }

    // Fetch the class schedule of the current user
    public void saveLocationsToJSON() {
        Log.d(TAG, "fetchLocations: Fetching data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_RETRIEVE_LOCATIONS + SharedPrefManager.getInstance(this).getIdnumber(),
                response -> onSuccessRequest(this, response, Constants.KEY_FETCH_LOCATIONS),
                error -> Toast.makeText(this, "Server is down.", Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(MainActivity.this).getIdnumber()));
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onSuccessRequest(Context context, String response, int request) {
        String message = "";
        try {
            JSONObject obj = new JSONObject(response);
            message = obj.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (request) {
            case Constants.KEY_CHANGE_PASS:
            case Constants.KEY_SEND_FEEDBACK:
                Log.d(TAG, "onSuccessRequest: " + message);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                break;
            case Constants.KEY_FETCH_LOCATIONS:
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                File file = getFileStreamPath("locations-" + dateFormat.format(date) + ".json");
                if (!file.exists()) {
                    Log.d(TAG, "writing locations.json");
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                openFileOutput("locations-" + dateFormat.format(date) + ".json",
                                        Context.MODE_PRIVATE));
                        outputStreamWriter.write(response);
                        outputStreamWriter.close();
                        Log.d(TAG, "locations.json saved.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * This allows us to pass on data to another fragment
     *
     * @param parcelableTag unique name that will be used to access parcel data
     * @param arrayList a list to be passed on
     * @param fragment to where the data is going
     */
    private void parcelDataToFragment(String parcelableTag, ArrayList<? extends Parcelable> arrayList, Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(parcelableTag, arrayList);
        fragment.setArguments(bundle);
    }

    /** @return Returns the instance of the Navigation View of the toolbar */
    @Override
    public NavigationView getNavigationView() {
        return navigationView;
    }

    /** @return Returns the instance of the Menu of the toolbar */
    @Override
    public Menu getMenu() {
        return menu;
    }
}