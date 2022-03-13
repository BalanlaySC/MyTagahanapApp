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

public class ScheduleFragment extends Fragment {
    private static final String TAG = "ScheduleFragment";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private ArrayList<SubjectModel> classSchedule;
    private ArrayList<RoomModel> rooms;
    private ArrayList<LocationModel> locations;

    private Context scheduleFragmentContext;
    private TextView schedFragTitle;
    private MapFragment mapFragment;
    private MapInterface mapInterface;

    private SubjectAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scheduleFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        schedFragTitle = view.findViewById(R.id.schedFragTitle);
        schedFragTitle.setVisibility(View.VISIBLE);

        initAccess();
        buildRecyclerView(view);
        return view;
    }

    public void initAccess() {
        mapFragment = new MapFragment();
        setMapInterface(mapFragment);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(scheduleFragmentContext);
        rooms = databaseAccess.getAllRooms();
        locations = databaseAccess.getAllLocations();

        classSchedule = new ArrayList<>();
        fetchClassSchedule();
    }

    private void fetchClassSchedule() {
        Log.d(TAG, "fetchUserClassSchedule: Fetching data");
        classSchedule = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.URL_CLASS_SCHED + SharedPrefManager.getInstance(scheduleFragmentContext).getIdnumber(),
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
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
                            SharedPrefManager.getInstance(scheduleFragmentContext).setFetchedData(true);
                            classSchedule.sort(Comparator.comparing(SubjectModel::getmDescription));
                            mAdapter.notifyDataSetChanged();
                            schedFragTitle.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(scheduleFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(scheduleFragmentContext, "Server is down.", Toast.LENGTH_SHORT).show()) {
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

    public void buildRecyclerView(View viewFragSched) {
        RecyclerView mRecyclerView = viewFragSched.findViewById(R.id.recvClassSched);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(scheduleFragmentContext);
        mAdapter = new SubjectAdapter(classSchedule, SubjectAdapter.SUBJECT);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(position -> {
            String currentRoom = classSchedule.get(position).getmRoom();
            LocationModel destinationLM = getLocationObj(getRoomObj(currentRoom).getLocationName());

            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            setMapInterface(mapFragment);

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("Locations", locations);
            mapFragment.setArguments(bundle);

            handler.postDelayed(() -> mapInterface.openBottomSheetDialog(destinationLM, currentRoom,
                    mapInterface.getMapFragView().getContext()), 1000);
        });
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
}
