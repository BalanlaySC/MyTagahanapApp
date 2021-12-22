package com.example.mytagahanap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class Login extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    TextInputEditText textInputEditTextIDNumber, textInputEditTextPassword;
    Button buttonLogin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextIDNumber = findViewById(R.id.textInputLayoutIDNumber);
        textInputEditTextPassword = findViewById(R.id.textInputLayoutPassword);
        buttonLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progress);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String idnumber, password;
                idnumber = String.valueOf(textInputEditTextIDNumber.getText());
                password = String.valueOf(textInputEditTextPassword.getText());

                if(!idnumber.equals("") && !password.equals("")) {
                    progressBar.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String[] field = new String[2];
                            field[0] = "idnumber";
                            field[1] = "password";
                            String[] data = new String[2];
                            data[0] = idnumber;
                            data[1] = password;
                            // https://9bb6-175-176-71-31.ngrok.io/TagahanapLogin/login.php local access
                            PutData putData = new PutData("http://mytagahanap.000webhostapp.com/TagahanapLogin/login.php", "POST", field, data);
                            if(putData.startPut()) {
                                if(putData.onComplete()) {
                                    progressBar.setVisibility(View.GONE);
                                    String result = putData.getResult();
                                    if(result.equals("Login Success")) {
                                        Log.d(TAG, result);
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Log.d(TAG, result);
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}