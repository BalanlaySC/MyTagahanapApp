package com.example.mytagahanap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class SearchFragment extends Fragment {
    private EditText edtTxtLocation;
    private Button btnSearch;
    private DatabaseAccess databaseAccess;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initViews(view);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
                databaseAccess.openDatabase();

                String location = edtTxtLocation.getText().toString();
                String coordinates = databaseAccess.getCoordinates(location);
                Toast.makeText(getActivity(), coordinates, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void initViews(View view) {
        edtTxtLocation = view.findViewById(R.id.edtTxtLocation);
        btnSearch = view.findViewById(R.id.btnSearch);
    }
}
