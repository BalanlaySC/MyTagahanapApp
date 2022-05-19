package com.example.mytagahanap.fragments;

import android.content.Context;
import android.graphics.Color;
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
import com.example.mytagahanap.globals.DatabaseAccess;
import com.example.mytagahanap.globals.SharedPrefManager;
import com.example.mytagahanap.interfaces.VolleyCallbackInterface;
import com.example.mytagahanap.models.LocationModel;
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
    private ArrayList<LocationModel> locations;

    private int idnumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dashboardFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        idnumber = SharedPrefManager.getInstance(dashboardFragmentContext).getIdnumber();

        // Fetch the for analytics
        Log.d(TAG, "Fetching analytics");
        fetchAnalytics(Constants.URL_ANALYTICS + idnumber, idnumber);

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPagerAnalyticSlider);
        sliderModelList = new ArrayList<>();

        sliderAdapter = new SliderAdapter(sliderModelList, viewPager2);
        viewPager2.setAdapter(sliderAdapter);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(dashboardFragmentContext);
        locations = databaseAccess.getAllLocations();

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

    public void fetchAnalytics(String url, int idnumber) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> onSuccessRequest(dashboardFragmentContext, response, idnumber),
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
    public void onSuccessRequest(Context context, String response, int idnumber) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.getBoolean("error")) {
                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                return;
            }

            if(idnumber > 5) {
                initRetrievedAnalytics(obj.getJSONArray("recently_searched_locs"),
                        "Recently searched locations", 1);
                initRetrievedAnalytics(obj.getJSONArray("recently_visited_locs"),
                        "Recently visited locations", 2);
            } else {
                initRetrievedAnalytics(obj.getJSONArray("most_searched_today"),
                        "Most searched locations today", 3);
                initRetrievedAnalytics(obj.getJSONArray("most_visited_today"),
                        "Most visited locations today", 4);
                initRetrievedAnalytics(obj.getJSONArray("most_searched_week"),
                        "Most searched locations this week", 5);
                initRetrievedAnalytics(obj.getJSONArray("most_visited_week"),
                        "Most visited locations this week", 6);
//                initRetrievedAnalytics(obj.getJSONArray("most_searched_month"),
//                        "Most searched locations this month", 7);
//                initRetrievedAnalytics(obj.getJSONArray("most_visited_month"),
//                        "Most visited locations this month", 8);
            }
            initRetrievedAnalytics(obj.getJSONArray("user_reviews"),
                    "User feedback", 9);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Formatting received response of
     * @param jsonArray
     * @param title
     * @param type
     */
    private void initRetrievedAnalytics(JSONArray jsonArray, String title, int type) {
        SliderModel sliderModel = null;
        ArrayList<LocationModel> recentLocs = new ArrayList<>();
        ArrayList<PieEntry> reviews = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<BarEntry> timelyLocs = new ArrayList<>();
        ArrayList<LocationModel> tempLocs = new ArrayList<>();

        try {
            switch (type){
                case 1:
                case 2:
                    for (int i = 0; i < jsonArray.length(); i++) {
                        recentLocs.add(new LocationModel(jsonArray.getString(i),
                                0f, 0f));
                    }
                    sliderModel = new SliderModel(recentLocs, title);
                    break;
                case 9:
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String review = jsonArray.getJSONArray(i).getString(0);
                        int review_count = jsonArray.getJSONArray(i).getInt(1);
                        if(review.equals("Great!"))
                            colors.add(Color.parseColor("#e52a8c")); // pink
                        if(review.equals("Happy"))
                            colors.add(Color.parseColor("#0094ca")); // light-blue
                        if(review.equals("Wow!"))
                            colors.add(Color.parseColor("#0067a7")); // blue
                        if(review.equals("Angry!"))
                            colors.add(Color.parseColor("#e0000f")); // red
                        if(review.equals("Sad."))
                            colors.add(Color.parseColor("#4f5153")); // gray

                        reviews.add(new PieEntry((float) review_count , review));
                    }
                    sliderModel = new SliderModel(reviews, colors, title);
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String currentLoc = jsonArray.getJSONArray(i).getString(0);
                        int locCount = jsonArray.getJSONArray(i).getInt(1);
                        timelyLocs.add(new BarEntry(i, locCount));
                        tempLocs.add(new LocationModel(currentLoc,0f, 0f));
                    }
                    sliderModel = new SliderModel(timelyLocs, tempLocs, title, "Updated");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int position = sliderModelList.isEmpty() ? 0 : sliderModelList.size();
        sliderModelList.add(position, sliderModel);
        sliderAdapter.notifyItemInserted(position);
    }

    private void setDashboardFragments(LinearLayout linearLayout, String actionBarTitle, Fragment fragment) {
        linearLayout.setOnClickListener(view -> {
            if (actionBarTitle.equals("Map")) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("Locations", locations);
                fragment.setArguments(bundle);
            }
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(actionBarTitle);
            ((MainActivity) requireActivity()).getNavigationView().setCheckedItem(R.id.nav_map);
        });
    }
}