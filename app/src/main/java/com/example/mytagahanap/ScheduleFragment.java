package com.example.mytagahanap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {
    private static final String TAG = "ScheduleFragment";
    public final Handler handler = new Handler(Looper.getMainLooper());

    private ArrayList<SubjectModel> classSchedule;
    private ArrayList<RoomModel> rooms;
    private ArrayList<LocationModel> locations;

    private Context scheduleFragmentContext;
    private MapFragment mapFragment;
    private MapInterface mapInterface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scheduleFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        initAccess();
        buildRecyclerView(view);
        return view;
    }

    public void initAccess() {
        mapFragment = new MapFragment();
        setMapInterface(mapFragment);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(scheduleFragmentContext);
        rooms = new ArrayList<>();
        locations = new ArrayList<>();

        rooms = databaseAccess.getAllRooms();
        locations = databaseAccess.getAllLocations();

        classSchedule = new ArrayList<>();
        Bundle bundle = getArguments();
        if(bundle != null) {
            classSchedule = getArguments().getParcelableArrayList("Class Schedule");
        } else {
            classSchedule.add(new SubjectModel("", "", "Unable to get subjects",
                    "", "", ""));
        }
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
            LocationModel destinationLM = getLocationObj(getRoomObj(currentRoom).getLocationName());

            getParentFragmentManager().popBackStack();
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            setMapInterface(mapFragment);

            handler.postDelayed(() -> mapInterface.openBottomSheetDialog(destinationLM,
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
