/*
 * This view class provides to show a screen, where the user will be able to sign up.
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.widget.TextWatcherField;
import it.davidepalladino.airanalyzer.view.dialog.SignupDialog;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;

import static it.davidepalladino.airanalyzer.controller.CheckField.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;

public class SignupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TextWatcherField.AuthTextWatcherCallback {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextAnswer1;
    private EditText editTextAnswer2;
    private EditText editTextAnswer3;

    private TextView textViewUsernameMessage;
    private TextView textViewPasswordMessage;
    private TextView textViewEmailMessage;
    private TextView textViewNameMessage;
    private TextView textViewSurnameMessage;
    private TextView textViewAnswer1Message;
    private TextView textViewAnswer2Message;
    private TextView textViewAnswer3Message;

    private Spinner spinnerQuestions1;
    private Spinner spinnerQuestions2;
    private Spinner spinnerQuestions3;

    private Button buttonContinue;

    private String questionSelected1;
    private String questionSelected2;
    private String questionSelected3;

    private GeneralToast generalToast;
    private FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextAnswer1 = findViewById(R.id.editTextAnswer1);
        editTextAnswer2 = findViewById(R.id.editTextAnswer2);
        editTextAnswer3 = findViewById(R.id.editTextAnswer3);

        textViewUsernameMessage = findViewById(R.id.textViewUsernameMessage);
        textViewPasswordMessage = findViewById(R.id.textViewPasswordMessage);
        textViewEmailMessage = findViewById(R.id.textViewEmailMessage);
        textViewNameMessage = findViewById(R.id.textViewNameMessage);
        textViewSurnameMessage = findViewById(R.id.textViewSurnameMessage);
        textViewAnswer1Message = findViewById(R.id.textViewAnswer1Message);
        textViewAnswer2Message = findViewById(R.id.textViewAnswer2Message);
        textViewAnswer3Message = findViewById(R.id.textViewAnswer3Message);

        spinnerQuestions1 = findViewById(R.id.spinnerQuestions1);
        spinnerQuestions2 = findViewById(R.id.spinnerQuestions2);
        spinnerQuestions3 = findViewById(R.id.spinnerQuestions3);

        buttonContinue = findViewById(R.id.buttonContinue);

        editTextUsername.addTextChangedListener(new TextWatcherField(this, editTextUsername));
        editTextPassword.addTextChangedListener(new TextWatcherField(this, editTextPassword));
        editTextEmail.addTextChangedListener(new TextWatcherField(this, editTextEmail));
        editTextName.addTextChangedListener(new TextWatcherField(this, editTextName));
        editTextSurname.addTextChangedListener(new TextWatcherField(this, editTextSurname));
        editTextAnswer1.addTextChangedListener(new TextWatcherField(this, editTextAnswer1));
        editTextAnswer2.addTextChangedListener(new TextWatcherField(this, editTextAnswer2));
        editTextAnswer3.addTextChangedListener(new TextWatcherField(this, editTextAnswer3));

        ArrayAdapter<String> adapterQuestions1 = new ArrayAdapter<String>(this, R.layout.item_spinner, getResources().getStringArray(R.array.spinnerSignupQuestions1));
        adapterQuestions1.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerQuestions1.setAdapter(adapterQuestions1);

        ArrayAdapter<String> adapterQuestions2 = new ArrayAdapter<String>(this, R.layout.item_spinner, getResources().getStringArray(R.array.spinnerSignupQuestions2));
        adapterQuestions2.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerQuestions2.setAdapter(adapterQuestions2);

        ArrayAdapter<String> adapterQuestions3 = new ArrayAdapter<String>(this, R.layout.item_spinner, getResources().getStringArray(R.array.spinnerSignupQuestions3));
        adapterQuestions3.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerQuestions3.setAdapter(adapterQuestions3);

        spinnerQuestions1.setOnItemSelectedListener(this);
        spinnerQuestions2.setOnItemSelectedListener(this);
        spinnerQuestions3.setOnItemSelectedListener(this);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean errorField = false;

                /* Checking the text for every fields. */
                if (
                        !checkSyntaxEditText(editTextUsername) ||
                        !checkSyntaxEditText(editTextPassword) ||
                        !checkSyntaxEditText(editTextPassword) ||
                        !checkSyntaxEditText(editTextEmail) ||
                        !checkSyntaxEditText(editTextName) ||
                        !checkSyntaxEditText(editTextSurname) ||
                        !checkSyntaxEditText(editTextAnswer1) ||
                        !checkSyntaxEditText(editTextAnswer2) ||
                        !checkSyntaxEditText(editTextAnswer3)
                ) {
                    errorField = true;
                }

                /*
                 * If there is not any error, will be created an user object to send to server for the sign up;
                 *  else, will be shown a Toast message to indicate that there is some error.
                 */
                if (!errorField) {
                    user = new User();
                    user.username = editTextUsername.getText().toString();
                    user.password = editTextPassword.getText().toString();
                    user.email = editTextEmail.getText().toString();
                    user.name = editTextName.getText().toString();
                    user.surname = editTextSurname.getText().toString();
                    user.question1 = questionSelected1;
                    user.question2 = questionSelected2;
                    user.question3 = questionSelected3;
                    user.answer1 = editTextAnswer1.getText().toString().toLowerCase();
                    user.answer2 = editTextAnswer2.getText().toString().toLowerCase();
                    user.answer3 = editTextAnswer3.getText().toString().toLowerCase();

                    databaseService.signup(user, SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP);
                } else {
                    generalToast.make(R.drawable.ic_error, getString(R.string.toastErrorField));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        generalToast = new GeneralToast(SignupActivity.this, getLayoutInflater());
        fileManager = new FileManager(SignupActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(SignupActivity.this, DatabaseService.class);
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
        int spinnerID = parent.getId();

        switch (spinnerID) {
            case R.id.spinnerQuestions1:
                questionSelected1 = parent.getItemAtPosition(position).toString();
                break;
            case R.id.spinnerQuestions2:
                questionSelected2 = parent.getItemAtPosition(position).toString();
                break;
            case R.id.spinnerQuestions3:
                questionSelected3 = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean checkSyntaxEditText(EditText editText) {
        TextView textViewMessage = null;
        String message = "";

        boolean wrongSyntax = false;

        switch (editText.getId()) {
            case R.id.editTextUsername:
                databaseService.checkUsername(editText.getText().toString(), SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_USERNAME);

                textViewMessage = textViewUsernameMessage;
                message = getString(R.string.emptyFieldUsername);

                /* Checking the username syntax and will be reported to the user if is wrong, in the next and final check. */
                if (!checkUsername(editText)) {
                    wrongSyntax = true;
                    message = getString(R.string.wrongSyntaxUsername);
                }

                break;
            case R.id.editTextPassword:
                textViewMessage = textViewPasswordMessage;
                message = getString(R.string.emptyFieldPassword);

                /* Checking the password syntax and will be reported to the user if is wrong, in the next and final check. */
                if (!checkPassword(editText)) {
                    wrongSyntax = true;
                    message = getString(R.string.wrongSyntaxPassword);
                }

                break;
            case R.id.editTextEmail:
                databaseService.checkEmail(editText.getText().toString(), SignupActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_EMAIL);

                textViewMessage = textViewEmailMessage;
                message = getString(R.string.errorEmail);

                /* Checking the email syntax and will be reported to the user if is wrong, in the next and final check. */
                if (!checkEmail(editText)) {
                    wrongSyntax = true;
                    message = getString(R.string.noticeEmail);
                }

                break;
            case R.id.editTextName:
                textViewMessage = textViewNameMessage;
                message = getString(R.string.errorName);
                break;
            case R.id.editTextSurname:
                textViewMessage = textViewSurnameMessage;
                message = getString(R.string.errorSurname);
                break;
            case R.id.editTextAnswer1:
                textViewMessage = textViewAnswer1Message;
                message = getString(R.string.errorAnswer);
                break;
            case R.id.editTextAnswer2:
                textViewMessage = textViewAnswer2Message;
                message = getString(R.string.errorAnswer);
                break;
            case R.id.editTextAnswer3:
                textViewMessage = textViewAnswer3Message;
                message = getString(R.string.errorAnswer);
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
                Intent intentTo = null;

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
                            fileManager.saveObject(user, User.NAMEFILE);

                            SignupDialog signupDialog = new SignupDialog();
                            signupDialog.setActivity(SignupActivity.this);
                            signupDialog.show(getSupportFragmentManager(), "");
                        }

                        break;
                    case 403:
                        generalToast.make(R.drawable.ic_error, getString(R.string.toastErrorService));
                        break;
                    case 404:
                    case 500:
                        generalToast.make(R.drawable.ic_error, getString(R.string.toastServerOffline));
                        break;
                    case 422:
                        generalToast.make(R.drawable.ic_error, getString(R.string.toastErrorField));
                        break;
                    default:
                        break;
                }
            }
        }
        }
    };
}