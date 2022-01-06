package com.example.mytagahanap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private static final String ROOT_URL = "http://192.168.1.195/mytagahanap/v1/";
    public static final String URL_LOGIN = ROOT_URL + "userLogin.php";

    private String token, idnumber, password;

    TextInputEditText tietIDNumber, tietPassword;
    TextView tvForgotPass;
    Button buttonLogin;
    CheckBox checkboxKMSI;
    ProgressBar progressBar;

    Context loginContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginContext = getApplicationContext();

        // check if user enabled keep me signed in
        // if not then log out and clear shared pref
        if (SharedPrefManager.getInstance(loginContext).getKeepMeSignedIn()) {
            // uncomment if need to view user info
//            Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
            finish();
            Intent intent = new Intent(loginContext, MainActivity.class);
            startActivity(intent);
            return;
        } else {
            SharedPrefManager.getInstance(loginContext).logOut();
            // uncomment if need to view user info
//            Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
        }

        tietIDNumber = findViewById(R.id.tietIDNumber);
        tietPassword = findViewById(R.id.tietPassword);
        tvForgotPass = findViewById(R.id.tvForgotPass);
        buttonLogin = findViewById(R.id.btnLogin);
        checkboxKMSI = findViewById(R.id.checkboxKMSI);
        progressBar = findViewById(R.id.progress);

//        checkToken(token);

        buttonLogin.setOnClickListener(view -> {
            idnumber = String.valueOf(tietIDNumber.getText()).trim();
            password = String.valueOf(tietPassword.getText()).trim();
            logIn(idnumber, password);
        });
    }

    private void logIn(String idnumber, String password) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            token = generateToken();
                            if (checkboxKMSI.isChecked()) {
                                Calendar cal = Calendar.getInstance(); // creates calendar
                                cal.setTime(new Date());               // sets calendar time/date
                                cal.add(Calendar.MINUTE, 10);       // adds 10 minute .HOUR_OF_DAY for hours
                                Date expiryDate = cal.getTime();
                            }
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                SharedPrefManager.getInstance(loginContext)
                                        .userLogin(
                                                obj.getInt("idnumber"),
                                                obj.getString("f_name"),
                                                obj.getString("l_name"),
                                                obj.getString("def_loc"),
                                                checkboxKMSI.isChecked(),
                                                token
                                        );
                                // uncomment if need to view user info
//                                Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
                                Toast.makeText(loginContext, "Login Success", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(loginContext, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(loginContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(loginContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", idnumber);
                params.put("password", password);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    // Generate token with SecureRandom
    public String generateToken() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        StringBuilder result = new StringBuilder();
        for (byte temp : bytes) {
            result.append(String.format("%02x", temp));
        }
        return result.toString();
    }

    private void checkToken(String token) {
        if (!token.equals("")) {
            Log.d(TAG, "Login Success");
            Toast.makeText(loginContext, "Login Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(loginContext, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /*  Compare two Date objects
        -1 is past, 0 is equal, 1 is future */
    public int compareDates(Date date1, Date date2) {
        return date1.compareTo(date2);
    }
}