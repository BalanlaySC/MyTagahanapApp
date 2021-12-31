package com.example.mytagahanap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class SharedPrefManager {
    private static volatile SharedPrefManager mInstance;
    private static Context mCtx;

    private static final String PREFERENCES = "preferences";
    private static final String KEY_ID_NUMBER = "userIdnumber";
    private static final String KEY_USER_FNAME = "userfName";
    private static final String KEY_USER_LNAME = "userlName";
    private static final String KEEP_ME_SIGNED_IN = "keepMeSignedIn";
    private static final String KEY_USER_TOKEN = "userToken";

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static SharedPrefManager getInstance(Context context) {
        if(mInstance == null) {
            synchronized (SharedPrefManager.class) {
                if(mInstance == null) {
                    mInstance = new SharedPrefManager(context);
                }
            }
        }
        return mInstance;
    }

    public boolean userLogin(int idnumber, String fName, String lName, boolean kmsi, String token) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_ID_NUMBER, idnumber);
        editor.putString(KEY_USER_FNAME, fName);
        editor.putString(KEY_USER_LNAME, lName);
        editor.putBoolean(KEEP_ME_SIGNED_IN, kmsi);
        editor.putString(KEY_USER_TOKEN, token);
        editor.apply();

        return true;
    }

    public boolean isLoggedIn() {
        boolean result = false;
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if(sharedPreferences.getInt(KEY_ID_NUMBER, 0) != 0) {
            result = true;
        }
        return result;
    }

    public boolean logOut() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        return true;
    }

    public int getIdnumber() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ID_NUMBER, 0);
    }

    public String getfName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_FNAME, "");
    }

    public String getlName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_LNAME, "");
    }

    public boolean getKeepMeSignedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEEP_ME_SIGNED_IN, false);
    }

    public String getToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_TOKEN, "");
    }

    public String getFullName() {
        return getlName() + ", " + getfName();
    }

    public String getAllSharedPref() {
        return getIdnumber() + " " + getfName() + " " +
                getlName() + " " + getKeepMeSignedIn() + " " + getToken();
    }

//    public long getExpiryDate() { }
}
