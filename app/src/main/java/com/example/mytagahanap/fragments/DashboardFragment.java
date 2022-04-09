package com.example.mytagahanap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.mytagahanap.R;
import com.example.mytagahanap.activities.MainActivity;
import com.example.mytagahanap.adapters.SliderAdapter;
import com.example.mytagahanap.globals.Constants;
import com.example.mytagahanap.globals.SharedPrefManager;
import com.example.mytagahanap.globals.Utils;
import com.example.mytagahanap.interfaces.VolleyCallbackInterface2;
import com.example.mytagahanap.models.LocationModel;
import com.example.mytagahanap.models.SliderModel;
import com.example.mytagahanap.network.RequestHandler;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment implements VolleyCallbackInterface2 {
    private static final String TAG = "DashboardFragment";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private Context dashboardFragmentContext;

    private SliderAdapter sliderAdapter;
    private List<SliderModel> sliderModelList;
    private ArrayList<BarEntry> mostSearched, mostVisited;
    private ArrayList<PieEntry> userReviews;

    private int idnumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dashboardFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        idnumber = SharedPrefManager.getInstance(dashboardFragmentContext).getIdnumber();

        fetchSearchedLocations();
        fetchVisitedLocations();
        fetchUserReviews();

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPagerAnalyticSlider);
        sliderModelList = new ArrayList<>();
        mostSearched = new ArrayList<>();
        mostVisited = new ArrayList<>();
        userReviews = new ArrayList<>();

        sliderAdapter = new SliderAdapter(sliderModelList, viewPager2);
        viewPager2.setAdapter(sliderAdapter);

        LinearLayout dashboardLayoutMap = view.findViewById(R.id.dashboardLayoutMap);
        dashboardLayoutMap.setOnClickListener(view1 -> {
            initFragment("Map", new MapFragment());
            ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_map);
        });
        LinearLayout dashboardLayoutSched = view.findViewById(R.id.dashboardLayoutSched);
        dashboardLayoutSched.setOnClickListener(view1 -> {
            initFragment("Class Schedule", new ScheduleFragment());
            ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_subjects);
        });
        LinearLayout dashboardLayoutInfo = view.findViewById(R.id.dashboardLayoutInfo);
        dashboardLayoutInfo.setOnClickListener(view1 -> {
            initFragment("School Information", new SchoolInfoFragment());
            ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_schoolinfo);
        });
        LinearLayout dashboardLayoutAbtApp = view.findViewById(R.id.dashboardLayoutAbtApp);
        dashboardLayoutAbtApp.setOnClickListener(view1 -> {
            initFragment("About App", new AboutAppFragment());
            ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_aboutapp);
        });
        return view;
    }

    // Fetch the searched locations for analytics
    public void fetchSearchedLocations() {
        Log.d(TAG, "fetchSearchedLocations: Fetching data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_SEARCHED_LOCS + idnumber,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            JSONArray allSearchedLocs = obj.getJSONArray("searched_locations");
                            handler.post(() -> onSuccessRequest(allSearchedLocs, "mostSearched"));
                        } else {
                            Toast.makeText(dashboardFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(dashboardFragmentContext, "Server is down.", Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(dashboardFragmentContext).getIdnumber()));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(dashboardFragmentContext).addToRequestQueue(stringRequest);
    }

    // Fetch the visited locations for analytics
    public void fetchVisitedLocations() {
        Log.d(TAG, "fetchVisitedLocations: Fetching data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_VISITED_LOCS + idnumber,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            JSONArray allVisitedLocs = obj.getJSONArray("visited_locations");
                            handler.postDelayed(() -> onSuccessRequest(allVisitedLocs, "mostVisited"), 1000);
                        } else {
                            Toast.makeText(dashboardFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(dashboardFragmentContext, "Server is down.", Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(dashboardFragmentContext).getIdnumber()));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(dashboardFragmentContext).addToRequestQueue(stringRequest);
    }

    // Fetch user reviews for analytics
    public void fetchUserReviews() {
        Log.d(TAG, "fetchVisitedLocations: Fetching data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_USER_REVIEWS + idnumber,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            JSONArray allUserReviews = obj.getJSONArray("user_reviews");
                            handler.postDelayed(() -> onSuccessRequest(allUserReviews, "userReviews"), 2000);
                        } else {
                            Toast.makeText(dashboardFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(dashboardFragmentContext, "Server is down.", Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(dashboardFragmentContext).getIdnumber()));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(dashboardFragmentContext).addToRequestQueue(stringRequest);
    }

    @Override
    public void onSuccessRequest(JSONArray jsonArray, String type) {
        Log.d(TAG, "onSuccessRequest: " + type);
        ArrayList<String> locations = new ArrayList<>();
        if (type.equals("mostSearched")) {
            Map<String, Integer> map = Utils.countFreq(jsonArray, jsonArray.length());
            map.values().removeAll(Collections.singleton(1));
            ArrayList<String> keys = new ArrayList<>(map.keySet());

            for (int i = 0; i < map.size(); i++) {
                String currentLoc = keys.get(i);
                mostSearched.add(new BarEntry(i, map.get(currentLoc)));
                locations.add(currentLoc);
            }
            if (sliderModelList.isEmpty()) {
                sliderModelList.add(0, new SliderModel(mostSearched, locations, "Most searched locations"));
                sliderAdapter.notifyItemInserted(0);
            } else {
                int size = sliderModelList.size();
                sliderModelList.add(size, new SliderModel(mostSearched, locations, "Most searched locations"));
                sliderAdapter.notifyItemInserted(size);
            }
        } else if (type.equals("mostVisited")) {
            Map<String, Integer> map = Utils.countFreq(jsonArray, jsonArray.length());
            ArrayList<String> keys = new ArrayList<>(map.keySet());

            for (int i = 0; i < map.size(); i++) {
                String currentLoc = keys.get(i);
                mostVisited.add(new BarEntry(i, map.get(currentLoc)));
                locations.add(currentLoc);
            }
            if (sliderModelList.isEmpty()) {
                sliderModelList.add(0, new SliderModel(mostVisited, locations, "Most visited locations"));
                sliderAdapter.notifyItemInserted(0);
            } else {
                int size = sliderModelList.size();
                sliderModelList.add(size, new SliderModel(mostVisited, locations, "Most visited locations"));
                sliderAdapter.notifyItemInserted(size);
            }
        } else if (type.equals("userReviews")) {
            Map<String, Integer> map = Utils.countFreq(jsonArray, jsonArray.length());
            ArrayList<String> keys = new ArrayList<>(map.keySet());

            for (int i = map.size() - 1; i >= 0; i--) {
                String currentReact = keys.get(i);
                userReviews.add(new PieEntry(map.get(currentReact), currentReact));
            }

            if (sliderModelList.isEmpty()) {
                sliderModelList.add(0, new SliderModel(userReviews));
                sliderAdapter.notifyItemInserted(0);
            } else {
                int size = sliderModelList.size();
                sliderModelList.add(size, new SliderModel(userReviews));
                sliderAdapter.notifyItemInserted(size);
            }
        }
    }

    private void initFragment(String actionBarTitle, Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setTitle(actionBarTitle);
    }
}