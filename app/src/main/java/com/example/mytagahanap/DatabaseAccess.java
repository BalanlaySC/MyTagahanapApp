package com.example.mytagahanap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseAccess {
    private static final String TAG = "DatabaseAccess";

    private SQLiteOpenHelper sqLiteOpenHelper;
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

    // query to the database and return the result
    // TODO change this into arraylist, so that coordinates can be used in navigation
    public String getCoordinates(String locationName) {
        String queryCoordinates = "SELECT x, y FROM Coordinates WHERE Location = '" + locationName + "'";
        cursor = mySQLiteDB.rawQuery(queryCoordinates, new String[]{});
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            String xCoordinates = cursor.getString(0);
            String yCoordinates = cursor.getString(1);
            buffer.append(xCoordinates + ", " + yCoordinates);
            Log.d(TAG, buffer.toString());
        }
        return buffer.toString();
    }

    public ArrayList<String> getAllLocations() {
        String queryLocations = "SELECT Location FROM Coordinates";
        ArrayList<String> locations = new ArrayList<String>();

        cursor = mySQLiteDB.rawQuery(queryLocations, null);
        if (cursor.moveToFirst()) {
            do {
                String location = cursor.getString(0);
                locations.add(location);
            } while (cursor.moveToNext());
        }
        return locations;
    }
}
