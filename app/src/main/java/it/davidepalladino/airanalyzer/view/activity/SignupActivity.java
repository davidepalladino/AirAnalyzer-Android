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
import it.davidepalladino.airanalyzer.controller.Setting;
import it.davidepalladino.airanalyzer.view.widget.TextWatcherField;
import it.davidepalladino.airanalyzer.model.Signup;
import it.davidepalladino.airanalyzer.view.dialog.SignupDialog;
import it.davidepalladino.airanalyzer.view.widget.Toast;

import static it.davidepalladino.airanalyzer.controller.CheckField.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.REQUEST_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.STATUS_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_BROADCAST;

public class SignupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TextWatcherField.AuthTextWatcherCallback {
    private static final String BROADCAST_REQUEST_CODE_MASTER = "SignupActivity";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_CHECK_USERNAME = "CheckUsername";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_CHECK_EMAIL = "CheckEmail";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP = "Signup";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextAnswer1;
    private EditText editTextAnswer2;
    private EditText editTextAnswer3;

    private TextView textViewUsername;
    private TextView textViewPassword;
    private TextView textViewEmail;
    private TextView textViewName;
    private TextView textViewSurname;
    private TextView textViewAnswer1;
    private TextView textViewAnswer2;
    private TextView textViewAnswer3;

    private Spinner spinnerQuestions1;
    private Spinner spinnerQuestions2;
    private Spinner spinnerQuestions3;

    private Button buttonContinue;

    private String questionSelected1;
    private String questionSelected2;
    private String questionSelected3;

    private Toast toast;
    private Setting setting;
    private Signup signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextSurname = (EditText) findViewById(R.id.editTextSurname);
        editTextAnswer1 = (EditText) findViewById(R.id.editTextAnswer1);
        editTextAnswer2 = (EditText) findViewById(R.id.editTextAnswer2);
        editTextAnswer3 = (EditText) findViewById(R.id.editTextAnswer3);

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewPassword = (TextView) findViewById(R.id.textViewPassword);
        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewSurname = (TextView) findViewById(R.id.textViewSurname);
        textViewAnswer1 = (TextView) findViewById(R.id.textViewAnswer1);
        textViewAnswer2 = (TextView) findViewById(R.id.textViewAnswer2);
        textViewAnswer3 = (TextView) findViewById(R.id.textViewAnswer3);

        spinnerQuestions1 = (Spinner) findViewById(R.id.spinnerQuestions1);
        spinnerQuestions2 = (Spinner) findViewById(R.id.spinnerQuestions2);
        spinnerQuestions3 = (Spinner) findViewById(R.id.spinnerQuestions3);

        buttonContinue = (Button) findViewById(R.id.buttonContinue);

        editTextUsername.addTextChangedListener(new TextWatcherField(this, editTextUsername));
        editTextPassword.addTextChangedListener(new TextWatcherField(this, editTextPassword));
        editTextEmail.addTextChangedListener(new TextWatcherField(this, editTextEmail));
        editTextName.addTextChangedListener(new TextWatcherField(this, editTextName));
        editTextSurname.addTextChangedListener(new TextWatcherField(this, editTextSurname));
        editTextAnswer1.addTextChangedListener(new TextWatcherField(this, editTextAnswer1));
        editTextAnswer2.addTextChangedListener(new TextWatcherField(this, editTextAnswer2));
        editTextAnswer3.addTextChangedListener(new TextWatcherField(this, editTextAnswer3));

        ArrayAdapter<String> adapterQuestions1 = new ArrayAdapter<String>(this, R.layout.item_spinner, getResources().getStringArray(R.array.Questions1));
        adapterQuestions1.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerQuestions1.setAdapter(adapterQuestions1);

        ArrayAdapter<String> adapterQuestions2 = new ArrayAdapter<String>(this, R.layout.item_spinner, getResources().getStringArray(R.array.Questions2));
        adapterQuestions2.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerQuestions2.setAdapter(adapterQuestions2);

        ArrayAdapter<String> adapterQuestions3 = new ArrayAdapter<String>(this, R.layout.item_spinner, getResources().getStringArray(R.array.Questions3));
        adapterQuestions3.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerQuestions3.setAdapter(adapterQuestions3);

        spinnerQuestions1.setOnItemSelectedListener(this);
        spinnerQuestions2.setOnItemSelectedListener(this);
        spinnerQuestions3.setOnItemSelectedListener(this);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean errorField = false;

                if (!checkAuthEditText(editTextUsername)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextPassword)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextEmail)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextName)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextSurname)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextAnswer1)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextAnswer2)) {
                    errorField = true;
                }

                if (!checkAuthEditText(editTextAnswer3)) {
                    errorField = true;
                }

                if (!errorField) {
                    signup = new Signup(
                            "",
                            editTextUsername.getText().toString(),
                            editTextPassword.getText().toString(),
                            editTextEmail.getText().toString(),
                            editTextName.getText().toString(),
                            editTextSurname.getText().toString(),
                            questionSelected1,
                            questionSelected2,
                            questionSelected3,
                            editTextAnswer1.getText().toString().toLowerCase(),
                            editTextAnswer2.getText().toString().toLowerCase(),
                            editTextAnswer3.getText().toString().toLowerCase()
                    );

                    //databaseService.checkUsername(signup.getUsername(), REQUEST_CODE_MASTER + REQUEST_CODE_EXTENSION_CHECK_USERNAME);
                    //databaseService.checkEmail(signup.getEmail(), REQUEST_CODE_MASTER + REQUEST_CODE_EXTENSION_CHECK_EMAIL);

                    databaseService.signup(signup, BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP);

                } else {
                    toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorField));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        toast = new Toast(SignupActivity.this, getLayoutInflater());
        setting = new Setting(SignupActivity.this);
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
    public boolean checkAuthEditText(EditText editText) {
        TextView errorTextView = null;
        String errorMessage = "";

        boolean errorSyntax = false;

        switch (editText.getId()) {
            case R.id.editTextUsername:
                errorTextView = textViewUsername;
                errorMessage = getString(R.string.errorUsername);

                databaseService.checkUsername(editText.getText().toString(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_USERNAME);

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
            case R.id.editTextEmail:
                errorTextView = textViewEmail;
                errorMessage = getString(R.string.errorEmail);

                databaseService.checkEmail(editText.getText().toString(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_EMAIL);

                if (!checkEmail(editText) && !editText.getText().toString().isEmpty()) {
                    errorSyntax = true;
                    errorMessage = getString(R.string.noticeEmail);
                }

                break;
            case R.id.editTextName:
                errorTextView = textViewName;
                errorMessage = getString(R.string.errorName);
                break;
            case R.id.editTextSurname:
                errorTextView = textViewSurname;
                errorMessage = getString(R.string.errorSurname);
                break;
            case R.id.editTextAnswer1:
                errorTextView = textViewAnswer1;
                errorMessage = getString(R.string.errorAnswer);
                break;
            case R.id.editTextAnswer2:
                errorTextView = textViewAnswer2;
                errorMessage = getString(R.string.errorAnswer);
                break;
            case R.id.editTextAnswer3:
                errorTextView = textViewAnswer3;
                errorMessage = getString(R.string.errorAnswer);
                break;
        }

        if (!editText.getText().toString().isEmpty() && !errorSyntax) {
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
                Intent intentTo = null;

                int statusCode = intentFrom.getIntExtra(STATUS_CODE_SERVICE, 0);
                switch (statusCode) {
                    case 201:
                        // CHECK USERNAME BROADCAST
                        if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_USERNAME) == 0) {
                            textViewUsername.setVisibility(View.VISIBLE);
                            textViewUsername.setText(getString(R.string.existsUsername));

                        // CHECK EMAIL BROADCAST
                        } else if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_CHECK_EMAIL) == 0) {
                            textViewEmail.setVisibility(View.VISIBLE);
                            textViewEmail.setText(getString(R.string.existsEmail));

                        // SIGN UP
                        } else if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP) == 0) {
                            textViewUsername.setVisibility(View.GONE);
                            textViewEmail.setVisibility(View.GONE);

                            //setting.saveLogin(new Login(signup.getUsername(), signup.getPassword()));

                            SignupDialog signupDialog = new SignupDialog();
                            signupDialog.setActivity(SignupActivity.this);
                            signupDialog.show(getSupportFragmentManager(), "");
                        }

                        break;
                    case 403:
                        toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorService));
                        break;
                    case 404:
                    case 500:
                        toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastServerOffline));
                        break;
                    case 422:
                        toast.makeToastBlack(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorField));
                        break;
                }
            }
        }
        }
    };
}