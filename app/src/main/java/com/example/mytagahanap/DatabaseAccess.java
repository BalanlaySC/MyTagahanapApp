package com.example.mytagahanap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAccess {
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
        cursor = mySQLiteDB.rawQuery();
    }
}
