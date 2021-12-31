package com.example.mytagahanap;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {
    private Context scheduleFragmentContext;
    private RecyclerView mRecyclerView;
    private SubjectAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<SubjectModel> classSchedule;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scheduleFragmentContext = getContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        createClassSchedule();
        buildRecyclerView(view);

        return view;
    }

    public void createClassSchedule() {
        classSchedule = new ArrayList<>();
        classSchedule.add(new SubjectModel("0011660",
                "Integrative Programming and Technologies 2", "IPT102",
                "L1", "1PM -> 2PM", "MonWedFri"));
        classSchedule.add(new SubjectModel("0011660",
                "Integrative Programming and Technologies 2", "IPT102",
                "CSFLD9", "8AM -> 9AM", "MonWed"));
        classSchedule.add(new SubjectModel("0011730",
                "Information Assurance and Security 2", "IAS102",
                "L3", "1030AM -> 12PM", "MonWed"));
        classSchedule.add(new SubjectModel("0011730",
                "Information Assurance and Security 2", "IAS102",
                "CCS OFC", "5PM -> 7PM", "Mon"));
        classSchedule.add(new SubjectModel("0011850",
                "The Entrepreneurial Mind", "GE ELEC 3",
                "CS204", "830AM -> 10AM", "TueThu"));
    }

    public void buildRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recvClassSched);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(scheduleFragmentContext);
        mAdapter = new SubjectAdapter(classSchedule);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new SubjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String currentRoom = classSchedule.get(position).getmRoom();

                // TODO generate path to building of that room
                Toast.makeText(scheduleFragmentContext, "Current room " + currentRoom, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
