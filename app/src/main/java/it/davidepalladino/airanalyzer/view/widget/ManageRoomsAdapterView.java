/*
 * This view class provides to show a personal View for the ListView of rooms in ManageRoom Activity.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 14th November, 2021
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

package it.davidepalladino.airanalyzer.view.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.view.activity.ManageRoomActivity;

public class ManageRoomsAdapterView extends ArrayAdapter<Room> {
    public interface ManageRoomsAdapterViewCallback {
        public void onPushAcceptButtonManageRoomsAdapterView(Room room);
        public void onPushDeleteButtonManageRoomsAdapterView(Room room);
    }

    private ManageRoomActivity context;
    private int resource;

    public ManageRoomsAdapterView(@NonNull Context context, @NonNull List<Room> objects) {
        super(context, R.layout.manage_rooms_adapter_view, objects);

        if (context instanceof ManageRoomActivity) {
            this.context = (ManageRoomActivity) context;
        }
        this.resource = R.layout.manage_rooms_adapter_view;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);

        Room room = getItem(position);

        TextView textViewID = convertView.findViewById(R.id.textViewID_ManageRoom);
        textViewID.setText(String.valueOf(room.id));

        TextView textViewRoom = convertView.findViewById(R.id.textViewRoom_ManageRoom);
        textViewRoom.setText(room.name);

        EditText editTextRoom = convertView.findViewById(R.id.editTextRoom);

        ImageButton imageViewEdit = convertView.findViewById(R.id.imageViewEdit);
        ImageButton imageViewRemove = convertView.findViewById(R.id.imageViewRemove);
        ImageButton imageViewAccept = convertView.findViewById(R.id.imageViewAccept);
        ImageButton imageViewDiscard = convertView.findViewById(R.id.imageViewDiscard);

        // EDIT BUTTON
        imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewRoom.setVisibility(View.GONE);

                editTextRoom.setText(room.name);
                editTextRoom.setVisibility(View.VISIBLE);

                imageViewEdit.setVisibility(View.GONE);
                imageViewRemove.setVisibility(View.GONE);

                imageViewAccept.setVisibility(View.VISIBLE);
                imageViewDiscard.setVisibility(View.VISIBLE);
            }
        });

        // REMOVE BUTTON
        imageViewRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.onPushDeleteButtonManageRoomsAdapterView(room);
            }
        });

        // ACCEPT BUTTON
        imageViewAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                room.name = String.valueOf(editTextRoom.getText());
                context.onPushAcceptButtonManageRoomsAdapterView(room);

                textViewRoom.setText(room.name);
                textViewRoom.setVisibility(View.VISIBLE);

                editTextRoom.setVisibility(View.GONE);

                imageViewEdit.setVisibility(View.VISIBLE);
                imageViewRemove.setVisibility(View.VISIBLE);

                imageViewAccept.setVisibility(View.GONE);
                imageViewDiscard.setVisibility(View.GONE);
            }
        });

        // DISCARD BUTTON
        imageViewDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewRoom.setText(room.name);
                textViewRoom.setVisibility(View.VISIBLE);

                editTextRoom.setVisibility(View.GONE);

                imageViewEdit.setVisibility(View.VISIBLE);
                imageViewRemove.setVisibility(View.VISIBLE);

                imageViewAccept.setVisibility(View.GONE);
                imageViewDiscard.setVisibility(View.GONE);
            }
        });

        return convertView;
    }
}