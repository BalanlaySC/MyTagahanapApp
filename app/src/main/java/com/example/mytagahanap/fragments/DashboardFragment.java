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
import com.example.mytagahanap.interfaces.VolleyCallbackInterface;
import com.example.mytagahanap.models.SliderModel;
import com.example.mytagahanap.network.RequestHandler;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment implements VolleyCallbackInterface {
    private static final String TAG = "DashboardFragment";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private Context dashboardFragmentContext;

    private SliderAdapter sliderAdapter;
    private List<SliderModel> sliderModelList;

    private int idnumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dashboardFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        idnumber = SharedPrefManager.getInstance(dashboardFragmentContext).getIdnumber();

        // Fetch the searched locations for analytics
        Log.d(TAG, "Fetching searched locations");
        fetchAnalytics(Constants.URL_SEARCHED_LOCS + idnumber, Constants.KEY_FETCH_SEARCHED);

        // Fetch the visited locations for analytics
        handler.postDelayed(() -> {
            Log.d(TAG, "Fetching visited locations");
            fetchAnalytics(Constants.URL_VISITED_LOCS + idnumber, Constants.KEY_FETCH_VISITED);
        }, 1000);

        // Fetch user reviews for analytics
        handler.postDelayed(() -> {
            Log.d(TAG, "Fetching user reviews");
            fetchAnalytics(Constants.URL_USER_REVIEWS + idnumber, Constants.KEY_FETCH_REVIEWS);
        }, 1500);

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPagerAnalyticSlider);
        sliderModelList = new ArrayList<>();

        sliderAdapter = new SliderAdapter(sliderModelList, viewPager2);
        viewPager2.setAdapter(sliderAdapter);

        LinearLayout dashboardLayoutMap = view.findViewById(R.id.dashboardLayoutMap);
        LinearLayout dashboardLayoutSched = view.findViewById(R.id.dashboardLayoutSched);
        LinearLayout dashboardLayoutInfo = view.findViewById(R.id.dashboardLayoutInfo);
        LinearLayout dashboardLayoutAbtApp = view.findViewById(R.id.dashboardLayoutAbtApp);

        setDashboardFragments(dashboardLayoutMap, "Map", new MapFragment());
        setDashboardFragments(dashboardLayoutSched, "Class Schedule", new ScheduleFragment());
        setDashboardFragments(dashboardLayoutInfo, "School Information", new SchoolInfoFragment());
        setDashboardFragments(dashboardLayoutAbtApp, "About App", new AboutAppFragment());
        return view;
    }

    public void fetchAnalytics(String url, int request) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> onSuccessRequest(dashboardFragmentContext, response, request),
                error -> Toast.makeText(dashboardFragmentContext, "Server is down.", Toast.LENGTH_SHORT).show()
        ) {
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
    public void onSuccessRequest(Context context, String response, int request) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.getBoolean("error")) {
                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                return;
            }

            switch (request) {
                case Constants.KEY_FETCH_SEARCHED:
                    Log.d(TAG, "onSuccessRequest: Fetched searched locations");
                    initRetrievedAnalytics(obj.getJSONArray("searched_locations"),
                            "Most searched locations", "bar");
                    break;
                case Constants.KEY_FETCH_VISITED:
                    Log.d(TAG, "onSuccessRequest: Fetched visited locations");
                    initRetrievedAnalytics(obj.getJSONArray("visited_locations"),
                            "Most visited locations", "bar");
                    break;
                case Constants.KEY_FETCH_REVIEWS:
                    Log.d(TAG, "onSuccessRequest: Fetched user reviews");
                    initRetrievedAnalytics(obj.getJSONArray("user_reviews"),
                            "User reviews", "pie");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRetrievedAnalytics(JSONArray jsonArray, String title, String type) {
        SliderModel sliderModel = null;
        Map<String, Integer> map = Utils.countFreq(jsonArray, jsonArray.length());
        ArrayList<String> keys = new ArrayList<>(map.keySet());
        ArrayList<String> locations = new ArrayList<>();
        ArrayList<PieEntry> pieEntryArrayList = new ArrayList<>();
        ArrayList<BarEntry> barEntryArrayList = new ArrayList<>();

        if (type.equals("bar")) {
            for (int i = 0; i < map.size(); i++) {
                String currentLoc = keys.get(i);
                barEntryArrayList.add(new BarEntry(i, map.get(currentLoc)));
                locations.add(currentLoc);
            }
            sliderModel = new SliderModel(barEntryArrayList, locations, title);
        } else if (type.equals("pie")) {
            for (int i = map.size() - 1; i >= 0; i--) {
                String currentReact = keys.get(i);
                pieEntryArrayList.add(new PieEntry(map.get(currentReact), currentReact));
            }
            sliderModel = new SliderModel(pieEntryArrayList);
        }

        int position = sliderModelList.isEmpty() ? 0 : sliderModelList.size();
        sliderModelList.add(position, sliderModel);
        sliderAdapter.notifyItemInserted(position);
    }

    private void setDashboardFragments(LinearLayout linearLayout, String actionBarTitle, Fragment fragment) {
        linearLayout.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(actionBarTitle);
            ((MainActivity) requireActivity()).getNavigationView().setCheckedItem(R.id.nav_map);
        });
    }
}