package com.example.mytagahanap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseAccess {
    private static final String TAG = "DatabaseAccess";

    private final SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase mySQLiteDB;
    private static DatabaseAccess dbInstance;
    Cursor cursor = null;

    // to avoid object creation outside the class
    private DatabaseAccess(Context context) {
        this.sqLiteOpenHelper = new DatabaseOpenHelper(context);
    }

    // return a single instance of the class
    public static DatabaseAccess getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DatabaseAccess(context);
        }
        return dbInstance;
    }

    public void openDatabase() {
        this.mySQLiteDB = sqLiteOpenHelper.getReadableDatabase();
    }

    public void closeDatabase() {
        if (mySQLiteDB != null) {
            this.mySQLiteDB.close();
        }
    }

    // Query to the database and return the coordinates
    public String getCoordinates(String locationName) {
        String queryCoordinates = "SELECT x, y FROM Coordinates WHERE Title = '" + locationName + "'";
        cursor = mySQLiteDB.rawQuery(queryCoordinates, new String[]{});
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            String xCoordinates = cursor.getString(0);
            String yCoordinates = cursor.getString(1);
            buffer.append(xCoordinates + ", " + yCoordinates);
        }
        return buffer.toString();
    }

    // Query to the database and return all locations
    public ArrayList<LocationModel> getAllLocations() {
        openDatabase();
        String queryLocations = "SELECT * FROM Coordinates";
        ArrayList<LocationModel> locations = new ArrayList<LocationModel>();

        cursor = mySQLiteDB.rawQuery(queryLocations, null);
        if (cursor.moveToFirst()) {
            do {
                String locationName = cursor.getString(0);
                float locationLng = cursor.getFloat(2);
                float locationLat = cursor.getFloat(1);

                LocationModel newLocation = new LocationModel(locationName, locationLat, locationLng);
                locations.add(newLocation);
            } while (cursor.moveToNext());
        }
        closeDatabase();
        return locations;
    }
}
