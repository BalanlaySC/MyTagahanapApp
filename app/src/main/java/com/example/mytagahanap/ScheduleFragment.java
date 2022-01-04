package com.example.mytagahanap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private static final String TAG = "ScheduleFragment";
    private static final String ROOT_URL = "http://192.168.1.222/mytagahanap/v1/";
    public static final String URL_CLASS_SCHED = ROOT_URL + "classSchedule.php";

    private ArrayList<SubjectModel> classSchedule;

    private Context scheduleFragmentContext;
    ProgressBar pbSchedFrag;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scheduleFragmentContext = requireContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        pbSchedFrag = view.findViewById(R.id.pbSchedFrag);
        getClassSchedule(view);

        return view;
    }

    public void buildRecyclerView(View view) {
        RecyclerView mRecyclerView = view.findViewById(R.id.recvClassSched);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(scheduleFragmentContext);
        SubjectAdapter mAdapter = new SubjectAdapter(classSchedule);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(position -> {
            String currentRoom = classSchedule.get(position).getmRoom();

            // TODO generate path to building of that room
            Toast.makeText(scheduleFragmentContext, "Current room " + currentRoom, Toast.LENGTH_SHORT).show();
        });
    }

    public void getClassSchedule(View view) {
        pbSchedFrag.setVisibility(View.VISIBLE);
        classSchedule = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CLASS_SCHED,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        pbSchedFrag.setVisibility(View.GONE);
                        JSONArray allSubs = obj.getJSONArray("class_schedule");
                        for(int i = 0;i < allSubs.length() ;i++) {
                            JSONObject arrayJObj = allSubs.getJSONObject(i);
                            classSchedule.add(new SubjectModel(arrayJObj.getString("class_id").toString(),
                                    arrayJObj.getString("subj_code").toString(),
                                    arrayJObj.getString("description").toString(),
                                    arrayJObj.getString("time").toString(),
                                    arrayJObj.getString("day").toString(),
                                    arrayJObj.getString("room").toString()));
                        }
                        classSchedule.sort(Comparator.comparing(SubjectModel::getmDescription));
                        buildRecyclerView(view);
                    } else {
                        Toast.makeText(scheduleFragmentContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pbSchedFrag.setVisibility(View.GONE);
                Toast.makeText(scheduleFragmentContext, "L110" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", String.valueOf(SharedPrefManager.getInstance(scheduleFragmentContext).getIdnumber()));
                return params;
            }
        };

        RequestHandler.getInstance(scheduleFragmentContext).addToRequestQueue(stringRequest);
    }
}
