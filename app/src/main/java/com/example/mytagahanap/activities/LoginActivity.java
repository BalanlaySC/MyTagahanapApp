package com.example.mytagahanap.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.mytagahanap.BuildConfig;
import com.example.mytagahanap.globals.Constants;
import com.example.mytagahanap.R;
import com.example.mytagahanap.interfaces.VolleyCallbackInterface;
import com.example.mytagahanap.network.RequestHandler;
import com.example.mytagahanap.globals.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements VolleyCallbackInterface {
    private static final String TAG = "Login";

    private String idnumber, password;

    private TextView tvMessage;
    private TextInputEditText tietIDNumber, tietPassword;
    private EditText editTxtToken;
    private CheckBox checkboxKMSI;
    private ProgressBar progressBar;
    private LinearLayout tokenLayout;
    private String curVersion;

    Context loginContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginContext = getApplicationContext();
        try {
            curVersion = loginContext.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "current version " + curVersion);

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
        tietIDNumber.setInputType(InputType.TYPE_CLASS_TEXT);
        tietPassword = findViewById(R.id.tietPassword);
        tietPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        checkboxKMSI = findViewById(R.id.checkboxKMSI);
        CheckBox checkboxShowPass = findViewById(R.id.checkboxShowPass);
        progressBar = findViewById(R.id.progress);
        TextView tvForgotPass = findViewById(R.id.tvForgotPass);
        tvForgotPass.setPaintFlags(tvForgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        TextView tvUserGuide = findViewById(R.id.tvUserGuide);
        tvUserGuide.setPaintFlags(tvForgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvMessage = findViewById(R.id.tvMessage);
        Button buttonLogin = findViewById(R.id.btnLogin);
        tokenLayout = findViewById(R.id.tokenLayout);
        editTxtToken = findViewById(R.id.editTxtToken);
        Button buttonTokenSend = findViewById(R.id.buttonTokenSend);

        Intent intent = getIntent();
        int userIdnumber = intent.getIntExtra("previous User", 0);
        if (userIdnumber != 0)
            tietIDNumber.setText(String.valueOf(userIdnumber));

        tvForgotPass.setOnClickListener(view -> {
            tokenLayout.setVisibility(View.VISIBLE);
            Toast.makeText(loginContext, "Enter token and send to " +
                    "reset to default password", Toast.LENGTH_LONG).show();
        });

        tvUserGuide.setOnClickListener(view -> {
            Uri uri = Uri.parse("http://mytagahanap.000webhostapp.com/First-time%20user%20guide.pdf");
            Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent1);
        });

        buttonTokenSend.setOnClickListener(view -> resetPassword(String.valueOf(tietIDNumber.getText()).trim(),
                String.valueOf(editTxtToken.getText()).trim()));

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

        checkboxShowPass.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                tietPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                tietPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    /**
     * Sends request to the server in order to verify if credentials are valid
     * and if any account match with user's ID number and password
     *
     * @param idNumber user's ID number
     * @param password user's password
     */
    private void logIn(String idNumber, String password) {
        progressBar.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_LOGIN,
                response -> onSuccessRequest(loginContext, response, Constants.KEY_LOGIN),
                error -> {
                    progressBar.setVisibility(View.GONE);
                    tietPassword.setText("");
                    Toast.makeText(loginContext, "No connection to server.", Toast.LENGTH_SHORT).show();
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idnumber", idNumber);
                params.put("password", password);
                params.put("appver", curVersion);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    /**
     * Sends request to server to reset the current user's password
     *
     * @param idNumber user's ID number
     * @param token    a random string of characters generated during logging in
     */
    private void resetPassword(String idNumber, String token) {
        Log.d(TAG, "resetPassword: " + idNumber + ", " + token);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Constants.URL_RESET_PASSWORD,
                response -> onSuccessRequest(loginContext, response, Constants.KEY_RESET_PASS),
                error -> Toast.makeText(loginContext, "No connection to server.", Toast.LENGTH_SHORT).show()
        ) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_idnumber", idNumber);
                params.put("user_token", token);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onSuccessRequest(Context context, String response, int request) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.getBoolean("error")) {
//                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                tvMessage.setText(obj.getString("message"));
                tvMessage.setVisibility(View.VISIBLE);
                tietPassword.setText("");
                return;
            }
            Log.d(TAG, "onSuccessRequest: " + obj.getString("message"));

            switch (request) {
                case Constants.KEY_LOGIN:
                    SharedPrefManager.getInstance(loginContext).userLogin(
                            obj.getInt("idnumber"),
                            password,
                            obj.getString("f_name"),
                            obj.getString("l_name"),
                            obj.getString("def_loc"),
                            true,
                            obj.getString("token"),
                            initTimeSession());
                    // uncomment if need to view user info
                    // Log.d(TAG, SharedPrefManager.getInstance(loginContext).getAllSharedPref());
                    Toast.makeText(loginContext, "Login Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(loginContext, MainActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putBoolean("KeepMeSignedIn", checkboxKMSI.isChecked());
                    intent.putExtras(mBundle);
                    startActivity(intent);
                    finish();
                    break;
                case Constants.KEY_RESET_PASS:
                    Toast.makeText(loginContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    tokenLayout.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return time 4 hours after logging in
     */
    private long initTimeSession() {
        Calendar cal = Calendar.getInstance();              // creates calendar
        cal.setTime(new Date(System.currentTimeMillis()));  // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 4);                // adds 4 hrs - .HOUR_OF_DAY for hours
        return cal.getTime().getTime();
    }
}