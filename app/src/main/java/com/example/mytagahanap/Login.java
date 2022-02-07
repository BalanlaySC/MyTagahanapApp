package com.example.mytagahanap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";

    private String idnumber, password;

    private TextView tvMessage;
    private TextInputEditText tietIDNumber, tietPassword;
    private EditText editTxtToken;
    private CheckBox checkboxKMSI;
    private ProgressBar progressBar;
    private LinearLayout tokenLayout;

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
            // Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
            finish();
            Intent intent = new Intent(loginContext, MainActivity.class);
            startActivity(intent);
            return;
        } else {
            SharedPrefManager.getInstance(loginContext).logOut();
            // uncomment if need to view user info
            // Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
        }

        initViews();

    }

    private void initViews() {
        tietIDNumber = findViewById(R.id.tietIDNumber);
        tietPassword = findViewById(R.id.tietPassword);
        checkboxKMSI = findViewById(R.id.checkboxKMSI);
        progressBar = findViewById(R.id.progress);
        TextView tvForgotPass = findViewById(R.id.tvForgotPass);
        tvMessage = findViewById(R.id.tvMessage);
        Button buttonLogin = findViewById(R.id.btnLogin);
        tokenLayout = findViewById(R.id.tokenLayout);
        editTxtToken = findViewById(R.id.editTxtToken);
        Button buttonTokenSend = findViewById(R.id.buttonTokenSend);

        Intent intent = getIntent();
        int userIdnumber = intent.getIntExtra("previous User", 0);
        if (userIdnumber != 0)
            tietIDNumber.setText(String.valueOf(userIdnumber));

        // todo utilize the new textview to display messages, substitute toasts
        tvForgotPass.setPaintFlags(tvForgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvForgotPass.setOnClickListener(view -> {
            tokenLayout.setVisibility(View.VISIBLE);
            Toast.makeText(loginContext, "Enter token and send to " +
                    "reset to default password", Toast.LENGTH_LONG).show();
        });
        buttonTokenSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword(String.valueOf(tietIDNumber.getText()).trim(),
                        String.valueOf(editTxtToken.getText()).trim());
            }
        });

        buttonLogin.setOnClickListener(view -> {
            if (!String.valueOf(tietIDNumber.getText()).equals("")
                    && !String.valueOf(tietPassword.getText()).equals("")) {
                idnumber = String.valueOf(tietIDNumber.getText()).trim();
                password = String.valueOf(tietPassword.getText()).trim();
                logIn(idnumber, password);
            } else {
                tvMessage.setText("Invalid login, please try again.");
                tvMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void logIn(String idnumber, String password) {
        progressBar.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_LOGIN,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (checkboxKMSI.isChecked()) {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                SharedPrefManager.getInstance(loginContext)
                                        .userLogin(obj.getInt("idnumber"),
                                                password,
                                                obj.getString("f_name"),
                                                obj.getString("l_name"),
                                                obj.getString("def_loc"),
                                                checkboxKMSI.isChecked(),
                                                obj.getString("token"),
                                                initTimeSession());
                                // uncomment if need to view user info
                                // Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
                                Toast.makeText(loginContext, "Login Success", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(loginContext, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                tvMessage.setText(obj.getString("message"));
                                tvMessage.setVisibility(View.VISIBLE);
                                tietIDNumber.setText("");
                                tietPassword.setText("");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            progressBar.setVisibility(View.GONE);
            tietPassword.setText("");
            Toast.makeText(loginContext, "No connection to server.", Toast.LENGTH_SHORT).show();
        }
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", idnumber);
                params.put("password", password);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void resetPassword(String userIdNumber, String token) {
        Log.d(TAG, "resetPassword: " + userIdNumber + ", " + token);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_RESET_PASSWORD,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(loginContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(loginContext, "No connection to server.", Toast.LENGTH_SHORT).show()
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", userIdNumber);
                params.put("user_token", token);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private long initTimeSession() {
        Calendar cal = Calendar.getInstance();              // creates calendar
        cal.setTime(new Date(System.currentTimeMillis()));  // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 2);                // adds 2 hrs .HOUR_OF_DAY for hours
        return cal.getTime().getTime();
    }
}