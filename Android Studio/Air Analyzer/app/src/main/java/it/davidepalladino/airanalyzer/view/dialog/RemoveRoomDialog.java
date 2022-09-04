/*
 * This view class provides to show a personal dialog for a remove room request.
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

package it.davidepalladino.airanalyzer.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.model.Room_OldClass;
import it.davidepalladino.airanalyzer.view.activity.ManageRoomActivity;

public class RemoveRoomDialog extends DialogFragment {
    @SuppressWarnings("unused")
    public interface RemoveRoomDialogCallback {
        void onPushOkButtonRemoveRoomDialog(Room_OldClass room);
    }

    private ManageRoomActivity context;
    public Room_OldClass room;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_remove_room, null);

        TextView textViewRemoveRoom = layout.findViewById(R.id.textViewRoom_RemoveRoom);
        textViewRemoveRoom.setText(room.name);

        if (getContext() instanceof ManageRoomActivity) {
            context = (ManageRoomActivity) getContext();
        } else {
            context = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogeTheme);
        builder.setView(layout)
                .setPositiveButton(R.string.buttonOk, (dialog, id) -> {
                    if (context != null) {
                        context.onPushOkButtonRemoveRoomDialog(room);
                    }
                })

                .setNegativeButton(R.string.buttonCancel, (dialog, id) -> Objects.requireNonNull(RemoveRoomDialog.this.getDialog()).cancel());

        return builder.create();
    }
}
