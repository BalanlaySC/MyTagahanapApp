package com.example.mytagahanap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.mapbox.geojson.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private static final String TAG = "ScheduleFragment";
    private static final String ROOT_URL = "http://192.168.1.195/mytagahanap/v1/";
    public static final String URL_CLASS_SCHED = ROOT_URL + "classSchedule.php";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private ArrayList<SubjectModel> classSchedule;
    private ArrayList<RoomModel> rooms;
    private ArrayList<LocationModel> locations;

    private Context scheduleFragmentContext;
    private ProgressBar pbSchedFrag;
    private MapFragment mapFragment;
    private MapInterface mapInterface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scheduleFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        initAccess(view);
        getClassSchedule(view);

        return view;
    }

    public void initAccess(View view) {
        mapFragment = new MapFragment();

        pbSchedFrag = view.findViewById(R.id.pbSchedFrag);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(scheduleFragmentContext);
        rooms = new ArrayList<>();
        locations = new ArrayList<>();

        rooms = databaseAccess.getAllRooms();
        locations = databaseAccess.getAllLocations();
    }

    public void buildRecyclerView(View viewFragSched) {
        RecyclerView mRecyclerView = viewFragSched.findViewById(R.id.recvClassSched);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(scheduleFragmentContext);
        SubjectAdapter mAdapter = new SubjectAdapter(classSchedule);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(position -> {
            String currentRoom = classSchedule.get(position).getmRoom();
            LocationModel origin = getLocationObj(SharedPrefManager.getInstance(scheduleFragmentContext).getDefLoc());
            LocationModel destination = getLocationObj(getRoomObj(currentRoom).getLocationName());
            // TODO optimization, have a popup confirmation to generate path

            getParentFragmentManager().popBackStack();
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            setMapInterface(mapFragment);

            handler.postDelayed(() -> startRoute(origin, destination), 1000);
        });
    }

    public void getClassSchedule(View view1) {
        classSchedule = new ArrayList<>();
        pbSchedFrag.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ScheduleFragment.URL_CLASS_SCHED,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            pbSchedFrag.setVisibility(View.GONE);
                            JSONArray allSubs = obj.getJSONArray("class_schedule");
                            for (int i = 0; i < allSubs.length(); i++) {
                                JSONObject arrayJObj = allSubs.getJSONObject(i);
                                classSchedule.add(new SubjectModel(arrayJObj.getString("class_id"),
                                        arrayJObj.getString("subj_code"),
                                        arrayJObj.getString("description"),
                                        arrayJObj.getString("time"),
                                        arrayJObj.getString("day"),
                                        arrayJObj.getString("room")));
                            }
                            classSchedule.sort(Comparator.comparing(SubjectModel::getmDescription));
                            buildRecyclerView(view1);
                        } else {
                            Toast.makeText(scheduleFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            pbSchedFrag.setVisibility(View.GONE);
            classSchedule.add(new SubjectModel("", "", "Unable to get subjects",
                    "", "", ""));
            buildRecyclerView(view1);
            Toast.makeText(scheduleFragmentContext, "L110" + error.getMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(scheduleFragmentContext).getIdnumber()));
                return params;
            }
        };

        RequestHandler.getInstance(scheduleFragmentContext).addToRequestQueue(stringRequest);
    }

    public void setMapInterface(MapInterface mapInterface) { this.mapInterface = mapInterface; }

    // Return LocationModel object with locationName loc
    public LocationModel getLocationObj(String loc) {
        for (LocationModel locationModel : locations) {
            if (locationModel.getLocationName().equals(loc)) {
                return locationModel;
            }
        }
        return null;
    }

    // Return LocationModel object with locationName loc
    public RoomModel getRoomObj(String rm) {
        for (RoomModel roomModel : rooms) {
            if (roomModel.getRoomName().equals(rm)) {
                return roomModel;
            }
        }
        return null;
    }

    public void startRoute(LocationModel origin, LocationModel destination) {
        mapInterface.getRoute(mapInterface.getMapboxMap(),
                Point.fromLngLat(origin.getLocationLng(), origin.getLocationLat()),
                Point.fromLngLat(destination.getLocationLng(), destination.getLocationLat()));

        RelativeLayout layoutDirections = mapInterface.getMapFragView().findViewById(R.id.layoutDirections);
        layoutDirections.setVisibility(View.VISIBLE);
        TextView editTxtStartLoc = mapInterface.getMapFragView().findViewById(R.id.txtViewStartLoc);
        TextView editTxtDestination = mapInterface.getMapFragView().findViewById(R.id.txtViewDestination);
        ImageButton btnCloseDirections = mapInterface.getMapFragView().findViewById(R.id.btnCloseDirections);

        editTxtStartLoc.setText(origin.getLocationName());
        editTxtDestination.setText(destination.getLocationName());
        btnCloseDirections.setOnClickListener(view1 -> {
            layoutDirections.setVisibility(View.GONE);
            mapInterface.removeLayer();
        });

//        NavigationView navigationView = viewMain.findViewById(R.id.nav_view);
//        navigationView.setCheckedItem(R.id.nav_map);
    }
}
