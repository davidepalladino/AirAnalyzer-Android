/*
 * This view class provides to show a screen, where the user can add an existing room or a new device.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 4th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.view.activity;

import static it.davidepalladino.airanalyzer.controller.CheckField.checkIPv4;
import static it.davidepalladino.airanalyzer.controller.ClientSocket.*;
import static it.davidepalladino.airanalyzer.controller.APIService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.ClientSocket;
import it.davidepalladino.airanalyzer.controller.APIService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.widget.GenericToast;

public class AddRoomActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private LinearLayout linearLayoutAddRoom;
    private Spinner spinnerRooms;
    private EditText editTextLocalIP;
    private AlertDialog dialogAddDevice;

    private GenericToast generalToast;

    private FileManager fileManager;
    private User user;
    private Room roomSelected;

    private APIService databaseService;

    private byte attemptsLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        linearLayoutAddRoom = findViewById(R.id.linearLayoutAddRoom);

        spinnerRooms = findViewById(R.id.spinnerRooms);
        spinnerRooms.setOnItemSelectedListener(this);

        editTextLocalIP = findViewById(R.id.editTextLocalIP);

        Button buttonAddRoom = findViewById(R.id.buttonAddRoom);
        buttonAddRoom.setOnClickListener(this);

        Button buttonAddDevice = findViewById(R.id.buttonAddDevice);
        buttonAddDevice.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        generalToast = new GenericToast(AddRoomActivity.this, getLayoutInflater());

        fileManager = new FileManager(AddRoomActivity.this);
        user = (User) fileManager.readObject(User.NAMEFILE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(AddRoomActivity.this, APIService.class);
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
        roomSelected = (Room) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddRoom:
                databaseService.changeStatusActivation((byte) roomSelected.number, true, AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM);

                break;
            case R.id.buttonAddDevice:
                /*
                 * Checking the field and the its syntax. If is correct, will be execute the communication with the device;
                 *  else, will be execute a toast message to notify the error.
                 */
                if (editTextLocalIP.getText().toString().length() != 0 && checkIPv4(editTextLocalIP.getText().toString())) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                    InetAddress mobileIP = null;
                    InetAddress deviceIP = null;

                    try {
                        mobileIP = InetAddress.getByName(Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress()));
                        deviceIP = InetAddress.getByName(editTextLocalIP.getText().toString());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    /* Checking if the device and the mobile are in the same local network. */
                    assert mobileIP != null;
                    assert deviceIP != null;
                    if (mobileIP.getAddress()[0] == deviceIP.getAddress()[0] && mobileIP.getAddress()[1] == deviceIP.getAddress()[1]  && mobileIP.getAddress()[2] == deviceIP.getAddress()[2]) {
                        /* Creating the JSON message and its serialization. */
                        JSONObject requestJSON = new JSONObject();
                        JSONObject messageJSON = new JSONObject();
                        try {
                            messageJSON.put("Username", user.username);
                            messageJSON.put("Password", user.password);

                            requestJSON.put("Request code", "1");
                            requestJSON.put("Message", messageJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String serializedJSON = requestJSON.toString();

                        dialogAddDevice = new AlertDialog.Builder(AddRoomActivity.this)
                                .setMessage(R.string.dialogSettingNewDevice)
                                .show();

                        /* Starting the socket connection and writing the JSON message. */
                        ClientSocket clientSocket = new ClientSocket(AddRoomActivity.this, editTextLocalIP.getText().toString(), 60000);
                        clientSocket.write(serializedJSON, AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_CREDENTIALS);
                    } else {
                        generalToast.make(R.drawable.ic_error, getString(R.string.toastErrorLocalNetwork));
                    }
                } else {
                    generalToast.make(R.drawable.ic_error, getString(R.string.toastEmptyFieldLocalNetwork));
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
     }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getAllRooms(false, AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS);
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
                            // INACTIVE ROOMS BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS) == 0) {
                                ArrayList<Room> arrayListRooms = intentFrom.getParcelableArrayListExtra(SERVICE_BODY);
                                /*  Checking the list of rooms to show or not the card about the addition. */
                                if (!arrayListRooms.isEmpty()) {
                                    linearLayoutAddRoom.setVisibility(View.VISIBLE);

                                    ArrayAdapter<Room> arrayAdapterRoom = new ArrayAdapter<>(AddRoomActivity.this, R.layout.item_spinner, arrayListRooms);
                                    arrayAdapterRoom.setDropDownViewResource(R.layout.item_spinner_dropdown);

                                    spinnerRooms.setAdapter(arrayAdapterRoom);
                                } else {
                                    linearLayoutAddRoom.setVisibility(View.GONE);
                                }

                            // ADD ROOM BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM) == 0) {
                                databaseService.getAllRooms(false, AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS);
                                generalToast.make(R.drawable.ic_check_circle, getString(R.string.toastRoomAdded));

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                user = intentFrom.getParcelableExtra(SERVICE_BODY);
                                fileManager.saveObject(user, User.NAMEFILE);

                                attemptsLogin = 1;
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            /* Checking the attempts for executing another login, or for launching the Login Activity. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(() -> {
                                    databaseService.login(user, AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                    attemptsLogin++;
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

                // FROM CLIENTSOCKET
                } else if (intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY)) {
                    if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(AddRoomActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_CREDENTIALS) == 0) {
                        /*
                         * Checking the status of socket connection. If there is not any error, this Activity will be killed with the Alert Dialog, to go back to the previously Activity;
                         *  else, there will be a Toast message to notify the user about the error.
                         */
                        if (!intentFrom.getBooleanExtra(ERROR_SOCKET, true)) {
                            new Handler().postDelayed(() -> {
                                generalToast.make(R.drawable.ic_check_circle, getString(R.string.toastDeviceAdded));
                                dialogAddDevice.dismiss();
                            }, TIME_ADD_DEVICE);
                        } else {
                            generalToast.make(R.drawable.ic_error, getString(R.string.toastErrorConnectionDevice));
                            dialogAddDevice.dismiss();
                        }
                    }
                }
            }
        }
    };
}