package com.example.mytagahanap;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static volatile SharedPrefManager mInstance;
    private static Context mCtx;

    private static final String PREFERENCES = "preferences";
    private static final String KEY_ID_NUMBER = "userIdnumber";
    private static final String KEY_PASSWORD = "userPassword";
    private static final String KEY_USER_FNAME = "userfName";
    private static final String KEY_USER_LNAME = "userlName";
    private static final String KEY_DEF_LOC = "userDefLoc";
    private static final String KEEP_ME_SIGNED_IN = "keepMeSignedIn";
    private static final String KEY_USER_TOKEN = "userToken";
    private static final String KEY_CONT_COUNTER = "contributionCounter";
    private static final String KEY_TIME_OUT_SESSION = "timeOutSession";
    private static final String FETCHED_DATA = "fetchedData";

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

    public boolean userLogin(
            int idnumber,
            String password,
            String fName,
            String lName,
            String defloc,
            boolean kmsi,
            String token,
            long timeOutSession) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_ID_NUMBER, idnumber);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_USER_FNAME, fName);
        editor.putString(KEY_USER_LNAME, lName);
        editor.putString(KEY_DEF_LOC, defloc);
        editor.putBoolean(KEEP_ME_SIGNED_IN, kmsi);
        editor.putString(KEY_USER_TOKEN, token);
        editor.putInt(KEY_CONT_COUNTER, 0);
        editor.putLong(KEY_TIME_OUT_SESSION, timeOutSession);
        editor.putBoolean(FETCHED_DATA, false);
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

    public void incrementContribution() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(getContributionCounter() < 5) {
            editor.putInt(KEY_CONT_COUNTER, getContributionCounter() + 1);
        }
        editor.apply();
    }

    public void updatePassword(String newPass) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_PASSWORD, newPass);
        editor.apply();
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

    public String getDefLoc() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_DEF_LOC, "");
    }

    public boolean getKeepMeSignedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEEP_ME_SIGNED_IN, false);
    }

    public String getToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_TOKEN, "");
    }

    public long getTimeOutSession() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(KEY_TIME_OUT_SESSION, 0);
    }

    public String getPassword() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }

    public void setFetchedData(boolean b) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(FETCHED_DATA, b);
        editor.apply();
    }

    public boolean isFetchedData() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(FETCHED_DATA, false);
    }

    public String getFullName() {
        return getlName() + ", " + getfName();
    }

    public String getAllSharedPref() {
        return getIdnumber() + " " + getfName() + " " +
                getlName() + " " + getKeepMeSignedIn() + " " + getToken();
    }

    public int getContributionCounter() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_CONT_COUNTER, 0);
    }
}
