/*
 * This view class provides to show a screen, where the user can see the several measures for room.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 25th December, 2021
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

import static androidx.work.ExistingPeriodicWorkPolicy.KEEP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import it.davidepalladino.airanalyzer.AirAnalyzerApplication;
import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.NotificationErrorWorker;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.fragment.HomeFragment;
import it.davidepalladino.airanalyzer.view.fragment.NotificationFragment;
import it.davidepalladino.airanalyzer.view.fragment.SettingFragment;
import it.davidepalladino.airanalyzer.view.fragment.RoomFragment;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;
import it.davidepalladino.airanalyzer.controller.FileManager;

import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;
import static it.davidepalladino.airanalyzer.model.Notification.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private AirAnalyzerApplication airAnalyzerApplication;
    private BottomNavigationView bottomNavigationView;
    private BadgeDrawable badgeDrawableNotifications;

    private FragmentManager fragmentManager;
    private HomeFragment fragmentHome;
    private RoomFragment fragmentRoom;
    private NotificationFragment fragmentNotification;
    private SettingFragment fragmentSetting;
    private Fragment fragmentActive;

    private GeneralToast generalToast;

    private FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    private byte attemptsLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        airAnalyzerApplication = (AirAnalyzerApplication) getApplicationContext();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // HOME
                if (id == R.id.menuItemHome) {

                    fragmentManager.beginTransaction().hide((Fragment) fragmentActive).show(fragmentHome).commit();
                    fragmentActive = fragmentHome;
                    ((HomeFragment) fragmentActive).updateMeasures();

                // ROOM
                } else if (id == R.id.menuItemRoom) {
                    fragmentManager.beginTransaction().hide((Fragment) fragmentActive).show(fragmentRoom).commit();
                    fragmentActive = fragmentRoom;
                    ((RoomFragment) fragmentActive).updateMeasures();

                // NOTIFICATIONS
                } else if (id == R.id.menuItemNotifications) {
                    fragmentManager.beginTransaction().hide((Fragment) fragmentActive).show(fragmentNotification).commit();
                    fragmentActive = fragmentNotification;

                    /*
                     * If the user decided to click the Notification item, will be updated the number considering the latest ID previously
                     *  saved with the dedicated methods.
                     */
                    saveLatestNotificationID(fragmentNotification.arrayListNotificationsLatest);
                    updateBadge(fragmentNotification.arrayListNotificationsLatest);

                // SETTING
                } else if (id == R.id.menuItemSetting) {
                    fragmentManager.beginTransaction().hide((Fragment) fragmentActive).show(fragmentSetting).commit();
                    fragmentActive = fragmentSetting;
                }

                return true;
            }
        });
        badgeDrawableNotifications = bottomNavigationView.getOrCreateBadge(R.id.menuItemNotifications);
        badgeDrawableNotifications.setHorizontalOffset(10);
        badgeDrawableNotifications.setVerticalOffset(10);
        badgeDrawableNotifications.setVisible(false);

        /* Creating the instance for Fragment Manager and every Fragment needed for the application. */
        fragmentManager = getSupportFragmentManager();
        fragmentSetting = SettingFragment.newInstance();
        fragmentNotification = NotificationFragment.newInstance();
        fragmentRoom = RoomFragment.newInstance();
        fragmentHome = HomeFragment.newInstance();
        fragmentActive = fragmentHome;

        /* Adding every Fragment to stack of FragmentManager. */
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentSetting).hide(fragmentSetting).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentNotification).hide(fragmentNotification).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentRoom).hide(fragmentRoom).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentHome).commit();

        /* Forcing the management of menus, calling the home item on Bottom Navigation View. */
        bottomNavigationView.setSelectedItemId(R.id.menuItemHome);
        bottomNavigationView.setSelected(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        generalToast = new GeneralToast(MainActivity.this, getLayoutInflater());

        fileManager = new FileManager(MainActivity.this);
        user = (User) fileManager.readObject(User.NAMEFILE);

        WorkManager workManager = WorkManager.getInstance(MainActivity.this);
        PeriodicWorkRequest notificationRequest = new PeriodicWorkRequest.Builder(NotificationErrorWorker.class, fileManager.readNotificationTime(PREFERENCE_NOTIFICATION_ERROR_TIME), TimeUnit.MINUTES)
                .build();
        workManager.enqueueUniquePeriodicWork(NotificationErrorWorker.tag, KEEP, notificationRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();

        airAnalyzerApplication.setCurrentActivity(this);

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(MainActivity.this, DatabaseService.class);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (airAnalyzerApplication.getCurrentActivity() instanceof MainActivity) {
            airAnalyzerApplication.setCurrentActivity(null);
        }

        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * @brief This method provides to update the list of notifications if the actual fragment is NotificationFragment.
     * @param arrayListNotificationsLatest ArrayList of notifications that is necessary for the fragment.
     */
    public void updateListNotificationFragment(ArrayList<Notification> arrayListNotificationsLatest) {
        if (fragmentActive instanceof NotificationFragment) {
            saveLatestNotificationID(arrayListNotificationsLatest);
        }

        fragmentNotification.updateList(arrayListNotificationsLatest);
    }

    /**
     * @brief This method provides to update the badge with the number of unread notifications.
     * @param arrayListNotificationsLatest ArrayList of notifications where will be searched the unseen notification.
     */
    public void updateBadge(ArrayList<Notification> arrayListNotificationsLatest) {
        int totalNotifications = 0;
        int latestNotificationID = fileManager.readNotificationID(PREFERENCE_NOTIFICATION_LATEST_ID_BADGE);

        /* Counting the unseen notifications for the badge. */
        if (arrayListNotificationsLatest != null) {
            for (Notification notification : arrayListNotificationsLatest) {
                if (notification.isSeen == 0 && notification.id > latestNotificationID) {
                    totalNotifications++;
                }
            }
        }

        /* Setting the badge if the fragment active is not an instance of NotificationFragment. */
        if (totalNotifications > 0 && !(fragmentActive instanceof NotificationFragment)) {
            badgeDrawableNotifications.setVisible(true);
            badgeDrawableNotifications.setNumber(totalNotifications);
        } else {
            badgeDrawableNotifications.setVisible(false);
            badgeDrawableNotifications.clearNumber();
        }
    }

    /**
     * @brief This method provides to save the latest ID of unread notification both for badge and Notification.
     * @param arrayListNotificationsLatest ArrayList of notifications where will be searched the first ID of unseen notification.
     */
    public void saveLatestNotificationID(ArrayList<Notification> arrayListNotificationsLatest) {
        int latestNotificationID = fileManager.readNotificationID(PREFERENCE_NOTIFICATION_LATEST_ID_BADGE);

        /* Searching and save the ID of the first unseen from the list. */
        if (arrayListNotificationsLatest != null) {
            for (Notification notification : arrayListNotificationsLatest) {
                if (notification.isSeen == 0 && notification.id > latestNotificationID) {
                    fileManager.saveNotitificationID(PREFERENCE_NOTIFICATION_LATEST_ID_BADGE, notification.id);
                    fileManager.saveNotitificationID(PREFERENCE_NOTIFICATION_LATEST_ID_WORKER, notification.id);
                    break;
                }
            }
        }
    }

    /**
     * @brief This method provides to launch the Login Activity, if the credentials are bad or if
     *  there is a logout request.
     * @param toastMessage A message for Toast. The value can be "null", if you wish; in this case
     *  there will be not any Toast.
     */
    private void goToLogin(String toastMessage) {
        /* Deleting the information for the login. */
        user.password = "";
        user.token = "";
        fileManager.saveObject(user, User.NAMEFILE);

        Intent intentTo = new Intent(MainActivity.this, LoginActivity.class);

        if (toastMessage != null) {
            intentTo.putExtra(INTENT_TOAST_MESSAGE, getString(R.string.toastUserError));
        }

        startActivity(intentTo);
        finish();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getRooms(user.getAuthorization(), true, MainActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS);
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
                        case 200:
                            // LOGIN BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(MainActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                user = intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                                fileManager.saveObject(user, User.NAMEFILE);

                                attemptsLogin = 1;
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            /* Checking the attempts for executing another login, or for launching the Login Activity. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        databaseService.login(user, MainActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                        attemptsLogin++;
                                    }
                                }, TIME_LOGIN_TIMEOUT);
                            } else {
                                goToLogin(getString(R.string.toastUserError));
                            }

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
    };
}