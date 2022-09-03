/*
 * This view class provides to show a screen, where will be executed the login or the redirection
 *  to the Login activity.
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.APIService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.User;

import static it.davidepalladino.airanalyzer.controller.APIService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private final int MESSAGE_LOGIN_TIMEOUT = 1;

    private User user;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(SplashActivity.this, APIService.class);
        startService(intentDatabaseService);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);

        /*
         * Checking if exists a previous login and, if doesn't exist, will be launched the Login activity.
         * This condition is valid when the application is launched for the first time, or if the data has been wiped.
         */
        FileManager fileManager = new FileManager(SplashActivity.this);
        user = (User) fileManager.readObject(User.NAMEFILE);
        if (user != null) {
            User.setInstance((User) fileManager.readObject(User.NAMEFILE));
        } else {
            user = User.getInstance();
            goToLogin();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            apiService = localBinder.getService();

            /* Executing the login and waiting the result within the time defined on TIME_LOGIN_TIMEOUT. */
            apiService.login(user, SplashActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);

            /* Preparing the message for the previously purpose. */
            Message messageLoginTimeout = new Message();
            messageLoginTimeout.what = MESSAGE_LOGIN_TIMEOUT;
            loginTimeout.sendMessageAtTime(messageLoginTimeout, SystemClock.uptimeMillis() + TIME_LOGIN_TIMEOUT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    /**
     * @brief This method provides to manage a message like the launch of the Login Activity.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    private final Handler loginTimeout = new Handler() {
        /* If has been passed 5 seconds, will be launched the Login activity. */
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_LOGIN_TIMEOUT:
                    goToLogin();
            }
        }
    };

    /**
     * @brief This method provides to launch the Login Activity, used in several cases.
     */
    private void goToLogin() {
        Intent intentTo = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intentTo);
        finish();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
            if (
                intentFrom != null &&
                intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY) &&
                intentFrom.hasExtra(SERVICE_STATUS_CODE)
            ) {
                Intent intentTo = null;
                loginTimeout.removeMessages(MESSAGE_LOGIN_TIMEOUT);

                /* Checking the result about login by the HTTP status code, provided by the Database Service. */
                int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                switch (statusCode) {
                    case 200:
                        if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SplashActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                            apiService.getMe(SplashActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ME);
                        } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SplashActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ME) == 0) {
                            String passwordStored = User.getInstance().password;
                            User.setInstance(intentFrom.getParcelableExtra(SERVICE_BODY));
                            User.getInstance().password = passwordStored;

                            intentTo = new Intent(SplashActivity.this, MainActivity.class);
                        } else {
                            intentTo = new Intent(SplashActivity.this, LoginActivity.class);
                        }
                        break;
                    case 401:
                        intentTo = new Intent(SplashActivity.this, LoginActivity.class);
                        intentTo.putExtra(INTENT_TOAST_MESSAGE, getString(R.string.toastIncorrectUsernamePassword));

                        break;
                    case 404:
                    case 500:
                        intentTo = new Intent(SplashActivity.this, LoginActivity.class);
                        intentTo.putExtra(INTENT_TOAST_MESSAGE, getString(R.string.toastServerOffline));

                        break;
                    default:
                        intentTo = new Intent(SplashActivity.this, LoginActivity.class);
                        break;
                }

                /* Launch the right Activity based the previous checks. */
                if (intentTo != null) {
                    Intent finalIntentTo = intentTo;
                    new Handler().postDelayed(() -> {
                        startActivity(finalIntentTo);
                        finish();
                    }, 2000);
                }
            }
        }
    };
}