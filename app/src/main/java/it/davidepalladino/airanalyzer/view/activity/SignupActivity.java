/*
 * This view class provides to show a screen, where the user will be able to sign up.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 16th September, 2022
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
public class SignupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextSurname;

    private TextView textViewUsernameMessage;
    private TextView textViewPasswordMessage;
    private TextView textViewEmailMessage;
    private TextView textViewNameMessage;
    private TextView textViewSurnameMessage;

    private String timezoneSelected;

    private GenericToast genericToast;

    private User user;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);

        textViewUsernameMessage = findViewById(R.id.textViewUsernameMessage);
        textViewPasswordMessage = findViewById(R.id.textViewPasswordMessage);
        textViewEmailMessage = findViewById(R.id.textViewEmailMessage);
        textViewNameMessage = findViewById(R.id.textViewNameMessage);
        textViewSurnameMessage = findViewById(R.id.textViewSurnameMessage);

        Spinner spinnerTimezone = findViewById(R.id.spinnerTimezone);

        Button buttonContinue = findViewById(R.id.buttonContinue);

        ArrayAdapter<String> adapterTimezone = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.spinnerTimezone));
        adapterTimezone.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerTimezone.setAdapter(adapterTimezone);

        spinnerTimezone.setOnItemSelectedListener(this);

        buttonContinue.setOnClickListener(v -> {
            user = User.getInstance();
            user.username = editTextUsername.getText().toString();
            user.password = editTextPassword.getText().toString();
            user.email = editTextEmail.getText().toString();
            user.name = editTextName.getText().toString();
            user.surname = editTextSurname.getText().toString();
            user.timezone = timezoneSelected;

            apiService.signup(user, SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_SIGNUP);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        genericToast = new GenericToast(SignupActivity.this, getLayoutInflater());
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(SignupActivity.this, APIService.class);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        timezoneSelected = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

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
                        if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN) == 0) {
                            Intent intentTo = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intentTo);
                            finish();
                        }

                        break;
                    case 201:
                        if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_SIGNUP) == 0) {
                            FileManager fileManager = new FileManager(SignupActivity.this);
                            fileManager.saveObject(user, User.NAMEFILE);

                            apiService.login(user, SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN);
                        }

                        break;
                    case 409:
                        if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_SIGNUP) == 0) {
                            String response = intentFrom.getStringExtra(SERVICE_BODY);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                jsonObject = (JSONObject) jsonObject.getJSONObject("reason");

                                JSONArray jsonArrayUsername = jsonObject.has("username") ? jsonObject.getJSONArray("username") : null;
                                updateTextViewMessage(jsonArrayUsername, textViewUsernameMessage);

                                JSONArray jsonArrayPassword = jsonObject.has("password") ? jsonObject.getJSONArray("password") : null;
                                updateTextViewMessage(jsonArrayPassword, textViewPasswordMessage);

                                JSONArray jsonArrayName = jsonObject.has("name") ? jsonObject.getJSONArray("name") : null;
                                updateTextViewMessage(jsonArrayName, textViewNameMessage);

                                JSONArray jsonArraySurname = jsonObject.has("surname") ? jsonObject.getJSONArray("surname") : null;
                                updateTextViewMessage(jsonArraySurname, textViewSurnameMessage);

                                JSONArray jsonArrayEmail = jsonObject.has("email") ? jsonObject.getJSONArray("email") : null;
                                updateTextViewMessage(jsonArrayEmail, textViewEmailMessage);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    case 404:
                    case 500:
                    case 501:
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