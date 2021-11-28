package com.example.mytagahanap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseOpenHelper extends SQLiteAssetHelper {
    private static final String TAG = "DatabaseOpenHelper";
    private static final String DATABASE_NAME = "MyTagahanap.sqlite";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        Log.d(TAG, "Upgrading database from version " + oldVersion  + " to "
//                + newVersion + ", which will destroy all old data");
//        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
//        onCreate(db);
//
//    }
}
