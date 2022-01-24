/*
 * This view class provides to show a personal dialog for a remove room request.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.1
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
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.view.activity.ManageRoomActivity;

public class RemoveRoomDialog extends DialogFragment {
    @SuppressWarnings("unused")
    public interface RemoveRoomDialogCallback {
        void onPushOkButtonRemoveRoomDialog(Room room);
    }

    private ManageRoomActivity context;
    public Room room;

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
