/*
 * This view class provides to show a screen, where will be executed the login or the redirection to the
 *  Login activity.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 15th December, 2021
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.User;

import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;

public class SplashActivity extends AppCompatActivity {
    private static final int TIME_LOGIN_TIMEOUT = 5000;
    private static final int MESSAGE_LOGIN_TIMEOUT = 1;

    private FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        fileManager = new FileManager(SplashActivity.this);
        user = (User) fileManager.readObject(User.NAMEFILE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(SplashActivity.this, DatabaseService.class);
        startService(intentDatabaseService);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);

        /* Checking if there is a previous login and, if not exists, will be launched the Login activity. */
        if (user == null) {
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
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            /* Executing the login and waiting the result within the time defined on TIME_LOGIN_TIMEOUT. */
            databaseService.login(user, SplashActivity.class.getSimpleName());

            /* Preparing the message for the previously purpose. */
            Message messageLoginTimeout = new Message();
            messageLoginTimeout.what = MESSAGE_LOGIN_TIMEOUT;
            loginTimeout.sendMessageAtTime(messageLoginTimeout, SystemClock.uptimeMillis() + TIME_LOGIN_TIMEOUT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * @brief This method provides to manage a message like the launch of the Login Activity.
     */
    private Handler loginTimeout = new Handler() {
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
            if (intentFrom != null) {
                if (intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY) && intentFrom.hasExtra(SERVICE_STATUS_CODE)) {
                    if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SplashActivity.class.getSimpleName()) == 0) {
                        Intent intentTo = null;
                        loginTimeout.removeMessages(MESSAGE_LOGIN_TIMEOUT);

                        /* Checking the result about login by the HTTP status code, provided by the Database Service. */
                        int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                        switch (statusCode) {
                            case 200:
                                intentTo = new Intent(SplashActivity.this, MainActivity.class);
                                user = (User) intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                                fileManager.saveObject(user, User.NAMEFILE);

                                break;
                            case 204:
                                intentTo = new Intent(SplashActivity.this, LoginActivity.class);
                                intentTo.putExtra(INTENT_TOAST_MESSAGE, getString(R.string.toastUserNotValidated));

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
                        Intent finalIntentTo = intentTo;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (finalIntentTo != null) {
                                    startActivity(finalIntentTo);
                                    finish();
                                }
                            }
                        }, 2000);
                    }
                }
            }
        }
    };
}