/*
 * This view class provides to show a screen, where the user can access to the Air Analyzer service,
 *  or to request to signup.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 3rd September, 2022
 *
 */

package it.davidepalladino.airanalyzer.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.APIService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.widget.GenericToast;

import static it.davidepalladino.airanalyzer.controller.APIService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressLint("NonConstantResourceId")
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextUsername;
    private EditText editTextPassword;

    private TextView textViewUsernameMessage;
    private TextView textViewPasswordMessage;

    private GenericToast genericToast;

    private User user;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        textViewUsernameMessage = findViewById(R.id.textViewUsernameMessage);
        textViewPasswordMessage = findViewById(R.id.textViewPasswordMessage);

        TextView buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(this);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        genericToast = new GenericToast(LoginActivity.this, getLayoutInflater());

        user = User.getInstance();
        editTextUsername.setText(user.username);

        /* Getting the Intent object and check if there is some Toast message, to show if exists. */
        Intent intentFrom = getIntent();
        if (intentFrom != null && intentFrom.hasExtra(INTENT_TOAST_MESSAGE)) {
            genericToast.make(R.drawable.ic_error, intentFrom.getStringExtra(INTENT_TOAST_MESSAGE));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(LoginActivity.this, APIService.class);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                user.username = editTextUsername.getText().toString();
                user.password = editTextPassword.getText().toString();

                /* Saving the information for next time that the application is launched. */
                FileManager fileManager = new FileManager(LoginActivity.this);
                fileManager.saveObject(User.getInstance(), User.NAMEFILE);

                apiService.login(user, LoginActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);

                break;
            case R.id.buttonSignUp:
                Intent intentToSigin = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intentToSigin);

                break;
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            apiService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
            if (
                intentFrom != null &&
                intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY) &&
                intentFrom.hasExtra(SERVICE_STATUS_CODE)
            ) {
                int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                switch (statusCode) {
                    case 200:
                        if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(LoginActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                            apiService.getMe(LoginActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ME);
                        } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(LoginActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ME) == 0) {
                            String passwordStored = User.getInstance().password;
                            User.setInstance(intentFrom.getParcelableExtra(SERVICE_BODY));
                            User.getInstance().password = passwordStored;


                            Intent intentTo = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intentTo);
                            finish();
                        }
                        break;
                    case 401:
                        genericToast.make(R.drawable.ic_error,getString(R.string.toastIncorrectUsernamePassword));

                        textViewUsernameMessage.setVisibility(View.GONE);
                        textViewUsernameMessage.setText("");

                        textViewPasswordMessage.setVisibility(View.GONE);
                        textViewPasswordMessage.setText("");

                        break;
                    case 409:
                        String response = intentFrom.getStringExtra(SERVICE_BODY);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            jsonObject = (JSONObject) jsonObject.getJSONObject("reason");

                            JSONArray jsonArrayUsername = jsonObject.has("username") ? jsonObject.getJSONArray("username") : null;
                            updateTextViewMessage(jsonArrayUsername, textViewUsernameMessage);

                            JSONArray jsonArrayPassword = jsonObject.has("password") ? jsonObject.getJSONArray("password") : null;
                            updateTextViewMessage(jsonArrayPassword, textViewPasswordMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 404:
                    case 500:
                        genericToast.make(R.drawable.ic_error, getString(R.string.toastServerOffline));
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * @brief Update the hide TextView with specific messages contained on a JSONArray.
     * @param jsonArray JSON array whit the specific messages.
     * @param textViewMessage TextView to update.
     */
    private void updateTextViewMessage(JSONArray jsonArray, TextView textViewMessage) throws JSONException {
        if (jsonArray != null) {
            ArrayList<String> errors = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                errors.add(jsonArray.getString(i));
            }

            textViewMessage.setVisibility(View.VISIBLE);
            textViewMessage.setText(String.join("\n", errors));
        } else {
            textViewMessage.setVisibility(View.GONE);
            textViewMessage.setText("");
        }
    }
}