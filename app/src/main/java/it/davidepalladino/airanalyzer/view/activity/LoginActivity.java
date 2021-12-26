/*
 * This view class provides to show a screen, where the user can access to the Air Analyzer service,
 *  or to request to signup.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 24th November, 2021
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
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.view.widget.TextWatcherField;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;

import static it.davidepalladino.airanalyzer.controller.CheckField.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;

public class LoginActivity extends AppCompatActivity implements TextWatcherField.AuthTextWatcherCallback, View.OnClickListener {
    private EditText editTextUsername;
    private EditText editTextPassword;

    private TextView textViewUsernameMessage;
    private TextView textViewPasswordMessage;

    private GeneralToast generalToast;
    private FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        textViewUsernameMessage = findViewById(R.id.textViewUsernameMessage);
        textViewPasswordMessage = findViewById(R.id.textViewPasswordMessage);

        editTextUsername.addTextChangedListener(new TextWatcherField(this, editTextUsername));
        editTextPassword.addTextChangedListener(new TextWatcherField(this, editTextPassword));

        TextView textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(this);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        generalToast = new GeneralToast(LoginActivity.this, getLayoutInflater());
        fileManager = new FileManager(LoginActivity.this);

        /*
         * Checking if there is a User stored into the internal memory of the phone. In this case,
         *  where will be set the EditText with the username; else, will be created a new Ususerer object.
         */
        user = (User) fileManager.readObject(User.NAMEFILE);
        if (user != null) {
            editTextUsername.setText(user.username);
        } else {
            user = new User();
        }

        /* Getting the Intent object and check if there is some Toast message, to show if exists. */
        Intent intentFrom = getIntent();
        if (intentFrom != null && intentFrom.hasExtra(INTENT_TOAST_MESSAGE)) {
            generalToast.make(R.drawable.ic_error, intentFrom.getStringExtra(INTENT_TOAST_MESSAGE));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(LoginActivity.this, DatabaseService.class);
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
                boolean errorField = false;

                if (!checkSyntaxEditText(editTextUsername)) {
                    errorField = true;
                }

                if (!checkSyntaxEditText(editTextPassword)) {
                    errorField = true;
                }

                if (!errorField) {
                    user.setLoginCredentials(editTextUsername.getText().toString(), editTextPassword.getText().toString());
                    fileManager.saveObject(user, User.NAMEFILE);

                    databaseService.login(user, LoginActivity.class.getSimpleName());
                } else {
                    generalToast.make(R.drawable.ic_error, getString(R.string.toastIncorrectUsernamePassword));
                }

                break;
            case R.id.textViewSignUp:
                Intent intentToSigin = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intentToSigin);

                break;
        }
    }

    @Override
    public boolean checkSyntaxEditText(EditText editText) {
        TextView textViewMessage = null;
        String message = "";

        boolean wrongSyntax = false;

        switch (editText.getId()) {
            case R.id.editTextUsername:
                textViewMessage = textViewUsernameMessage;
                message = getString(R.string.emptyFieldUsername);

                /* Checking the username syntax and will be reported to the user if is wrong, in the next and final check. */
                if (!checkUsername(editText) && !editText.getText().toString().isEmpty()) {
                    wrongSyntax = true;
                    message = getString(R.string.wrongSyntaxUsername);

                    break;
                }

                break;

            case R.id.editTextPassword:
                textViewMessage = textViewPasswordMessage;
                message = getString(R.string.emptyFieldPassword);

                /* Checking the password syntax and will be reported to the user if is wrong, in the next and final check. */
                if (!checkPassword(editText) && !editText.getText().toString().isEmpty()) {
                    wrongSyntax = true;
                    message = getString(R.string.wrongSyntaxPassword);

                    break;
                }

                break;
        }

        /* Checking the actual field and will be reported to the user if is empty or if there is an error of syntax. */
        if (editText.getText().length() == 0 || wrongSyntax) {
            textViewMessage.setVisibility(View.VISIBLE);
            textViewMessage.setText(message);

            return false;
        } else {
            textViewMessage.setVisibility(View.GONE);

            return true;
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();
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
                if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(LoginActivity.class.getSimpleName()) == 0) {
                    int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                    switch (statusCode) {
                        case 200:
                            user = (User) intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                            fileManager.saveObject(user, User.NAMEFILE);

                            Intent intentTo = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intentTo);
                            finish();

                            break;
                        case 204:
                            generalToast.make(R.drawable.ic_error, getString(R.string.toastUserNotValidated));
                            break;
                        case 401:
                            generalToast.make(R.drawable.ic_error, getString(R.string.toastIncorrectUsernamePassword));
                            break;
                        case 422:
                            generalToast.make(R.drawable.ic_error, getString(R.string.toastErrorField));
                            break;
                        case 404:
                        case 500:
                            generalToast.make(R.drawable.ic_error, getString(R.string.toastServerOffline));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        }
    };
}