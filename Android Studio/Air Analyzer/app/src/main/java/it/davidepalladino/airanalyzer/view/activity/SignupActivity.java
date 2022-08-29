/*
 * This view class provides to show a screen, where the user will be able to sign up.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.1
 * @date 3rd January, 2022
 *
 * This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
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
import it.davidepalladino.airanalyzer.view.dialog.SignupDialog;

import static it.davidepalladino.airanalyzer.controller.APIService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;

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

            apiService.signup(user, SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP);
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
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
        if (intentFrom != null) {
            if (intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY) && intentFrom.hasExtra(SERVICE_STATUS_CODE)) {
                int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                switch (statusCode) {
                    case 201:
                        // CHECK USERNAME BROADCAST
                        if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_USERNAME) == 0) {
                            textViewUsernameMessage.setVisibility(View.VISIBLE);
                            textViewUsernameMessage.setText(getString(R.string.existsUsername));

                        // CHECK EMAIL BROADCAST
                        } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_EMAIL) == 0) {
                            textViewEmailMessage.setVisibility(View.VISIBLE);
                            textViewEmailMessage.setText(getString(R.string.existsEmail));

                        // SIGN UP BROADCAST
                        } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP) == 0) {
                            textViewUsernameMessage.setVisibility(View.GONE);
                            textViewEmailMessage.setVisibility(View.GONE);

                            /* Removing the password form the User object and saving it on internal memory of the phone. */
                            user.password = "";
//                            fileManager.saveObject(user, User.NAMEFILE);

                            SignupDialog signupDialog = new SignupDialog();
                            signupDialog.setActivity(SignupActivity.this);
                            signupDialog.show(getSupportFragmentManager(), "");
                        }

                        break;
                    case 403:
                        genericToast.make(R.drawable.ic_error, getString(R.string.toastErrorService));
                        break;
                    case 404:
                    case 500:
                        genericToast.make(R.drawable.ic_error, getString(R.string.toastServerOffline));
                        break;
                    case 422:
                        genericToast.make(R.drawable.ic_error, getString(R.string.toastErrorField));
                        break;
                    default:
                        break;
                }
            }
        }
        }
    };
}