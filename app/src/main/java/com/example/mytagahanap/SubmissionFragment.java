package com.example.mytagahanap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SubmissionFragment extends Fragment {
    private static final String TAG = "ScheduleFragment";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private ArrayList<SubjectModel> suggestedSpots; // reuse SubjectModel
    private ArrayList<LocationModel> locations;

    private Context spotsFragmentContext;
    private TextView spotFragTitle;
    private MapFragment mapFragment;
    private MapInterface mapInterface;

    private SubjectAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        spotsFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_submissions, container, false);

        spotFragTitle = view.findViewById(R.id.spotFragTitle);
        spotFragTitle.setVisibility(View.VISIBLE);

        initAccess();
        buildRecyclerView(view);
        return view;
    }

    public void initAccess() {
        mapFragment = new MapFragment();
        setMapInterface(mapFragment);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(spotsFragmentContext);
        locations = databaseAccess.getAllLocations();

        suggestedSpots = new ArrayList<>();
        fetchSubmittedLocations();
    }

    public void buildRecyclerView(View viewFragSched) {
        RecyclerView mRecyclerView = viewFragSched.findViewById(R.id.recvSuggestedSpots);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(spotsFragmentContext);
        mAdapter = new SubjectAdapter(suggestedSpots, SubjectAdapter.SUGGESTION);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(position -> {
            String locName = suggestedSpots.get(position).getmDescription();
            float locLat = Float.parseFloat(suggestedSpots.get(position).getmRoom());
            float locLong = Float.parseFloat(suggestedSpots.get(position).getmClassID());
            LocationModel destinationLM = new LocationModel(locName, locLat, locLong);

            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            setMapInterface(mapFragment);

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("Locations", locations);
            mapFragment.setArguments(bundle);

            handler.postDelayed(() -> mapInterface.openBottomSheetDialog(destinationLM, "",
                    mapInterface.getMapFragView().getContext()), 1000);
        });
    }

    public void setMapInterface(MapInterface mapInterface) {
        this.mapInterface = mapInterface;
    }

    // Fetch the class schedule of the current user
    public void fetchSubmittedLocations() {
        Log.d(TAG, "fetchSubmittedLocations: Fetching data");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_SUBMITTED_LOC + 1,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            suggestedSpots.clear();
                            JSONArray allSubj = obj.getJSONArray("location_suggestions");
                            for (int i = 0; i < allSubj.length(); i++) {
                                JSONObject arrayJObj = allSubj.getJSONObject(i);
                                suggestedSpots.add(new SubjectModel(arrayJObj.getString("latitude"),
                                        arrayJObj.getString("user_idnumber"),
                                        arrayJObj.getString("loc_name"),
                                        arrayJObj.getString("loc_description"),
                                        "",
                                        arrayJObj.getString("longitude")));
                            }
                            suggestedSpots.sort(Comparator.comparing(SubjectModel::getmDescription));
                            spotFragTitle.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(spotsFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(spotsFragmentContext, "Server is down.", Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(spotsFragmentContext).getIdnumber()));
                return params;
            }
        };

        RequestHandler.getInstance(spotsFragmentContext).addToRequestQueue(stringRequest);
    }
}
