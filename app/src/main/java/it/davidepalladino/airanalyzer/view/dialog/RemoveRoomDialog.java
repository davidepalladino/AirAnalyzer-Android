package it.davidepalladino.airanalyzer.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;


import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.model.Room;

import static android.content.Context.BIND_AUTO_CREATE;

public class RemoveRoomDialog extends DialogFragment {
    public static final String BROADCAST_REQUEST_CODE_MASTER = "RemoveRoomDialog";
    public static final String BROADCAST_REQUEST_CODE_EXTENSION_REMOVE_ROOM = "RemoveRoom";

    private String token;

    private Room room;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_remove_room, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout)
                .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        databaseService.removeRoom(token, room, BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_REMOVE_ROOM);
                    }
                })

                .setNegativeButton(R.string.buttonCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RemoveRoomDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intentDatabaseService = new Intent(getActivity(), DatabaseService.class);
        getActivity().bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unbindService(serviceConnection);
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setToken(String token) { this.token = token; }

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
}
