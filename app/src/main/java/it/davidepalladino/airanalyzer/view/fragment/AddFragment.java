package it.davidepalladino.airanalyzer.view.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.ClientSocket;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.Setting;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.widget.Toast;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.WIFI_SERVICE;
import static it.davidepalladino.airanalyzer.controller.ClientSocket.ERROR_SOCKET;
import static it.davidepalladino.airanalyzer.controller.ClientSocket.MESSAGE_SOCKET;
import static it.davidepalladino.airanalyzer.controller.ClientSocket.REQUEST_CODE_SOCKET;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.REQUEST_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.STATUS_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_BROADCAST;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_ROOM;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_USER;

public class AddFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String BROADCAST_REQUEST_CODE_MASTER = "AddFragment";
    public static final String BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM = "GetActiveRooms";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS = "GetInactiveRooms";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_GET_USER = "GetUser";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM = "AddRoom";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_1 = "SocketWrite1";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_READ_2 = "SocketRead2";

    private LinearLayout linearLayoutAddRoom;
    private Spinner spinnerRooms;
    private EditText editTextLocalIP;
    private Button buttonAddRoom;
    private Button buttonAddDevice;
    private AlertDialog dialogAddDevice;

    private Toast toast;
    private Setting setting;
    private ClientSocket clientSocket;

    private Room roomSelected;
    private User user = null;

    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        toast = new Toast(getActivity(), getLayoutInflater());
        setting = new Setting(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(getActivity(), DatabaseService.class);
        getActivity().bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unbindService(serviceConnection);
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layoutFragment = inflater.inflate(R.layout.fragment_add, container, false);

        linearLayoutAddRoom = layoutFragment.findViewById(R.id.linearLayoutAddRoom);

        spinnerRooms = layoutFragment.findViewById(R.id.spinnerRooms);
        spinnerRooms.setOnItemSelectedListener(this);

        editTextLocalIP = layoutFragment.findViewById(R.id.editTextLocalIP);

        buttonAddRoom = layoutFragment.findViewById(R.id.buttonAddRoom);
        buttonAddRoom.setOnClickListener(this);

        buttonAddDevice = layoutFragment.findViewById(R.id.buttonAddDevice);
        buttonAddDevice.setOnClickListener(this);

        return layoutFragment;
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
                databaseService.addRoom(setting.readToken(), roomSelected, BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM);

                break;
            case R.id.buttonAddDevice:
                if (editTextLocalIP.getText().toString().length() != 0) {
                    WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(WIFI_SERVICE);

                    InetAddress systemIP = null;
                    InetAddress getIP = null;

                    try {
                        systemIP = InetAddress.getByName(Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress()));
                        getIP = InetAddress.getByName(editTextLocalIP.getText().toString());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    if (systemIP.getAddress()[0] == getIP.getAddress()[0]) {
                        clientSocket = new ClientSocket(getContext(), editTextLocalIP.getText().toString(), 8008);
                        clientSocket.write(1, user.getId(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_1);
                    } else {
                        toast.makeToastBlue(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorLocalNetwork));
                    }
                }

                break;
        }
    }

    public Room getRoomSelected() {
        return roomSelected;
    }

    public DatabaseService databaseService;
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getInactiveRooms(setting.readToken(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS);
            databaseService.getUser(setting.readToken(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_USER);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
            // FROM DATABASESERVICE
            if (intentFrom != null) {
                if (intentFrom.hasExtra(REQUEST_CODE_SERVICE) && intentFrom.hasExtra(STATUS_CODE_SERVICE)) {
                    int statusCode = intentFrom.getIntExtra(STATUS_CODE_SERVICE, 0);
                    switch (statusCode) {
                        case 200:
                            // GET INACTIVE ROOMS BROADCAST
                            if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS) == 0) {
                                linearLayoutAddRoom.setVisibility(View.VISIBLE);

                                ArrayList<Room> listRooms = intentFrom.getParcelableArrayListExtra(INTENT_ROOM);

                                ArrayAdapter<Room> arrayRooms = new ArrayAdapter<Room>(getContext(), android.R.layout.simple_spinner_item, listRooms);
                                arrayRooms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                spinnerRooms.setAdapter(arrayRooms);

                            // ADD ROOM BROADCAST
                            } else if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM) == 0) {
                                databaseService.getActiveRooms(setting.readToken(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM);

                            // GET USER
                            } else if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_USER) == 0) {
                                user = intentFrom.getParcelableExtra(INTENT_USER);
                            }

                            break;
                        case 204:
                            // GET INACTIVE ROOMS BROADCAST
                            if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS) == 0) {
                                linearLayoutAddRoom.setVisibility(View.GONE);
                            }

                            break;
                    }

                // FROM CLIENTSOCKET
                } else if (intentFrom.hasExtra(ClientSocket.REQUEST_CODE_SOCKET)) {
                    if (intentFrom.getStringExtra(REQUEST_CODE_SOCKET).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_1) == 0) {
                        if (!intentFrom.getBooleanExtra(ERROR_SOCKET, true)) {
                            dialogAddDevice = new AlertDialog.Builder(getContext())
                                    .setMessage(R.string.dialogSettingNewDevice)
                                    .show();
                            clientSocket.read(2, BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_READ_2);
                        } else {
                            toast.makeToastBlue(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorConnectionDevice));
                        }
                    } else if (intentFrom.getStringExtra(REQUEST_CODE_SOCKET).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_READ_2) == 0) {
                        if (!intentFrom.getBooleanExtra(ERROR_SOCKET, true)) {
                            roomSelected = new Room(intentFrom.getStringExtra(MESSAGE_SOCKET), null);
                            databaseService.addRoom(setting.readToken(), roomSelected, BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM);

                            dialogAddDevice.dismiss();
                        } else {
                            dialogAddDevice.dismiss();
                            toast.makeToastBlue(R.drawable.ic_baseline_error_24, getString(R.string.toastErrorConnectionDevice));
                        }
                    }
                }
            }
        }
    };
}