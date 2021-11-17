package com.example.mytagahanap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    public static final String USER_TABLE = "Users";
    public static final String COLUMN_IDNUMBER = "IDNumber";
    public static final String COLUMN_PASSWORD = "Password";

    public DBHelper(Context context) {
        super(context, "MyTagahanap.db", null, 1);
    }

    // this is called the first time a database is accessed
    // there should be code here to create a new database
    @Override
    public void onCreate(SQLiteDatabase mySQLiteDB) {
        String createTableStatement = "CREATE TABLE " + USER_TABLE + " (" + COLUMN_IDNUMBER + " TEXT PRIMARY KEY, " + COLUMN_PASSWORD + " TEXT)";
        mySQLiteDB.execSQL(createTableStatement);
    }

    // this is called if the database version number changes
    // it prevents previous users apps from breaking when you change the database design
    @Override
    public void onUpgrade(SQLiteDatabase mySQLiteDB, int oldVersion, int newVersion) {
        String dropTableStatement = "DROP TABLE IF EXISTS Users";
        mySQLiteDB.execSQL(dropTableStatement);
    }

    public Boolean checkIDNumberPassword(String idnumber, String password) {
        String[] idAndPass = new  String[] {idnumber, password};
        SQLiteDatabase mySQLiteDB = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_IDNUMBER + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = mySQLiteDB.rawQuery(queryString, idAndPass);
        if (cursor.getCount() > 0) {
            return true;
        }
        else {
            return false;
        }
    }
}
