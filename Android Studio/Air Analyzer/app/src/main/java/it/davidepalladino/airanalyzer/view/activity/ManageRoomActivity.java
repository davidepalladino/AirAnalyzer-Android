/*
 * This view class provides to show a screen, where the user can manage the rooms. Is possible to rename,
 *  delete or add a specific room.
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

import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.Consts.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.dialog.RemoveRoomDialog;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;
import it.davidepalladino.airanalyzer.view.widget.ManageRoomsAdapterView;

public class ManageRoomActivity extends AppCompatActivity implements ManageRoomsAdapterView.ManageRoomsAdapterViewCallback, RemoveRoomDialog.RemoveRoomDialogCallback {
    private TextView textViewNoRoom;
    private ListView listViewRoom;
    private ManageRoomsAdapterView adapterViewManageRooms;

    private GeneralToast generalToast;

    private FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    private byte attemptsLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewNoRoom = findViewById(R.id.textViewNoRoom);
        listViewRoom = findViewById(R.id.listViewRoom);
    }

    @Override
    protected void onStart() {
        super.onStart();

        generalToast = new GeneralToast(ManageRoomActivity.this, getLayoutInflater());

        fileManager = new FileManager(ManageRoomActivity.this);
        user = (User) fileManager.readObject(User.NAMEFILE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(ManageRoomActivity.this, DatabaseService.class);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuItemAddRoom) {
            Intent intentTo = new Intent(ManageRoomActivity.this, AddRoomActivity.class);
            startActivity(intentTo);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onPushAcceptButtonManageRoomsAdapterView(Room room) {
        databaseService.renameRoom(user.getAuthorization(), room.id, room.name, ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_RENAME_ROOM);
    }

    @Override
    public void onPushDeleteButtonManageRoomsAdapterView(Room room) {
        RemoveRoomDialog removeRoomDialog = new RemoveRoomDialog();
        removeRoomDialog.room = room;
        removeRoomDialog.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onPushOkButtonRemoveRoomDialog(Room room) {
        databaseService.deactivateRoom(user.getAuthorization(), room.id, ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_REMOVE_ROOM);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getRooms(user.getAuthorization(), true, ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS);
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
                            // ACTIVE ROOMS BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS) == 0) {
                                ArrayList<Room> arrayListRoom = intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE);

                                /*  Checking the list of rooms to update or not the ListView dedicated. */
                                if (!arrayListRoom.isEmpty()) {
                                    textViewNoRoom.setVisibility(View.GONE);
                                    listViewRoom.setVisibility(View.VISIBLE);

                                    arrayListRoom = intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE);
                                    adapterViewManageRooms = new ManageRoomsAdapterView(ManageRoomActivity.this, arrayListRoom);
                                    listViewRoom.setAdapter(adapterViewManageRooms);
                                } else {
                                    textViewNoRoom.setVisibility(View.VISIBLE);
                                    listViewRoom.setVisibility(View.GONE);
                                }

                            // RENAME ROOM BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_RENAME_ROOM) == 0) {
                                databaseService.getRooms(user.getAuthorization(), true, ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS);
                                generalToast.make(R.drawable.ic_check_circle, getString(R.string.toastRoomRenamed));

                            // REMOVE ROOM BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_REMOVE_ROOM) == 0) {
                                databaseService.getRooms(user.getAuthorization(), true, ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS);
                                generalToast.make(R.drawable.ic_check_circle, getString(R.string.toastRoomRemoved));

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(ManageRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                user = (User) intentFrom.getParcelableExtra(SERVICE_RESPONSE);
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
                                        databaseService.login((User) fileManager.readObject(User.NAMEFILE), MainActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                        attemptsLogin++;
                                    }
                                }, TIME_LOGIN_TIMEOUT);
                            } else {
                                finish();
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