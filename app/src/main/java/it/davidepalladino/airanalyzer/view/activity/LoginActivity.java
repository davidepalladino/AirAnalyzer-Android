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
import it.davidepalladino.airanalyzer.controller.Setting;
import it.davidepalladino.airanalyzer.view.widget.TextWatcherField;
import it.davidepalladino.airanalyzer.model.Login;
import it.davidepalladino.airanalyzer.view.widget.Toast;

import static it.davidepalladino.airanalyzer.controller.CheckField.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.REQUEST_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.STATUS_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.Setting.NAMEPREFERENCE_TOKEN;
import static it.davidepalladino.airanalyzer.controller.IntentConst.*;

public class LoginActivity extends AppCompatActivity implements TextWatcherField.AuthTextWatcherCallback, View.OnClickListener {
    private static final String BROADCAST_REQUEST_CODE_MASTER = "LoginActivity";

    private EditText editTextUsername;
    private EditText editTextPassword;

    private TextView textViewUsername;
    private TextView textViewPassword;

    private Toast toast;
    private Setting setting;
    private Login login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewPassword = (TextView) findViewById(R.id.textViewPassword);

        editTextUsername.addTextChangedListener(new TextWatcherField(this, editTextUsername));
        editTextPassword.addTextChangedListener(new TextWatcherField(this, editTextPassword));

        TextView textViewSignUp = (TextView) findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(this);

        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        toast = new Toast(LoginActivity.this, getLayoutInflater());

        setting = new Setting(LoginActivity.this);

        login = setting.readLogin();
        if (login != null) {
            editTextUsername.setText(login.getUsername());
        }

        Intent intentFrom = getIntent();
        if (intentFrom != null && intentFrom.hasExtra(INTENT_MESSAGE_TOAST)) {
            toast.makeToastBlack(R.drawable.ic_baseline_error_24, intentFrom.getStringExtra(INTENT_MESSAGE_TOAST));
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

                if (!checkAuthEditText(editTextUsername)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextPassword)) {
                    errorField = true;
                }

                if (!errorField) {
                    login = new Login(
                            editTextUsername.getText().toString(),
                            editTextPassword.getText().toString()
                    );

                    setting.saveLogin(login);
                    databaseService.login(login, BROADCAST_REQUEST_CODE_MASTER);
                } else {
                    toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastIncorrectUsernamePassword));
                }

                break;
            case R.id.textViewSignUp:
                Intent intentToSigin = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intentToSigin);

                break;
        }
    }

    @Override
    public boolean checkAuthEditText(EditText editText) {
        TextView errorTextView = null;
        String errorMessage = "";

        boolean errorSyntax = false;

        switch (editText.getId()) {
            case R.id.editTextUsername:
                errorTextView = textViewUsername;
                errorMessage = getString(R.string.errorUsername);

                if (!checkUsername(editText) && !editText.getText().toString().isEmpty()) {
                    errorSyntax = true;
                    errorMessage = getString(R.string.noticeUsername);
                }

                break;

            case R.id.editTextPassword:
                errorTextView = textViewPassword;
                errorMessage = getString(R.string.errorPassowrd);

                if (!checkPassword(editText) && !editText.getText().toString().isEmpty()) {
                    errorSyntax = true;
                    errorMessage = getString(R.string.noticePassword);
                }

                break;
        }

        if (editText.getText().toString().length() != 0 && !errorSyntax) {
            errorTextView.setVisibility(View.GONE);

            return true;
        } else {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(errorMessage);

            return false;
        }
    }

    public DatabaseService databaseService;
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
        if (intentFrom != null) {
            if (intentFrom.hasExtra(REQUEST_CODE_SERVICE) && intentFrom.hasExtra(STATUS_CODE_SERVICE)) {
                if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER) == 0) {
                    int statusCode = intentFrom.getIntExtra(STATUS_CODE_SERVICE, 0);
                    switch (statusCode) {
                        case 200:
                            setting.saveToken(intentFrom.getStringExtra(NAMEPREFERENCE_TOKEN));

                            Intent intentTo = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intentTo);
                            finish();

                            break;
                        case 204:
                            toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastUserNotValidated));
                            break;
                        case 401:
                            toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastIncorrectUsernamePassword));
                            break;
                        case 422:
                            toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorField));
                            break;
                        case 404:
                        case 500:
                            toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastServerOffline));
                            break;
                    }
                }
            }
        }
        }
    };
}