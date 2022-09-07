/*
 * This view class provides to show a personal View for the ListView of rooms in ManageRoom Activity.
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

package it.davidepalladino.airanalyzer.view.widget;

import android.annotation.SuppressLint;
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
    @SuppressWarnings("unused")
    public interface ManageRoomsAdapterViewCallback {
        void onPushAcceptButtonManageRoomsAdapterView(Room room);
        void onPressDeleteButtonManageRoomsAdapterView(Room room);
    }

    private final ManageRoomActivity context;
    private final int resource;

    public ManageRoomsAdapterView(@NonNull Context context, @NonNull List<Room> objects) {
        super(context, R.layout.manage_rooms_adapter_view, objects);

        if (context instanceof ManageRoomActivity) {
            this.context = (ManageRoomActivity) context;
        } else {
            this.context = null;
        }
        this.resource = R.layout.manage_rooms_adapter_view;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (context != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, null);

            Room room = getItem(position);

            TextView textViewID = convertView.findViewById(R.id.textViewID_ManageRoom);
            textViewID.setText(String.valueOf(room.number));

            TextView textViewRoom = convertView.findViewById(R.id.textViewRoom_ManageRoom);
            textViewRoom.setText(room.name);

            EditText editTextRoom = convertView.findViewById(R.id.editTextRoom);

            ImageButton imageViewEdit = convertView.findViewById(R.id.imageViewEdit);
            ImageButton imageViewRemove = convertView.findViewById(R.id.imageViewRemove);
            ImageButton imageViewAccept = convertView.findViewById(R.id.imageViewAccept);
            ImageButton imageViewDiscard = convertView.findViewById(R.id.imageViewDiscard);

            // EDIT BUTTON
            imageViewEdit.setOnClickListener(v -> {
                textViewRoom.setVisibility(View.GONE);

                editTextRoom.setText(room.name);
                editTextRoom.setVisibility(View.VISIBLE);

                imageViewEdit.setVisibility(View.GONE);
                imageViewRemove.setVisibility(View.GONE);

                imageViewAccept.setVisibility(View.VISIBLE);
                imageViewDiscard.setVisibility(View.VISIBLE);
            });

            // REMOVE BUTTON
            imageViewRemove.setOnClickListener(v -> context.onPressDeleteButtonManageRoomsAdapterView(room));

            // ACCEPT BUTTON
            imageViewAccept.setOnClickListener(v -> {
                room.name = String.valueOf(editTextRoom.getText());
                context.onPushAcceptButtonManageRoomsAdapterView(room);

                textViewRoom.setText(room.name);
                textViewRoom.setVisibility(View.VISIBLE);

                editTextRoom.setVisibility(View.GONE);

                imageViewEdit.setVisibility(View.VISIBLE);
                imageViewRemove.setVisibility(View.VISIBLE);

                imageViewAccept.setVisibility(View.GONE);
                imageViewDiscard.setVisibility(View.GONE);
            });

            // DISCARD BUTTON
            imageViewDiscard.setOnClickListener(v -> {
                textViewRoom.setText(room.name);
                textViewRoom.setVisibility(View.VISIBLE);

                editTextRoom.setVisibility(View.GONE);

                imageViewEdit.setVisibility(View.VISIBLE);
                imageViewRemove.setVisibility(View.VISIBLE);

                imageViewAccept.setVisibility(View.GONE);
                imageViewDiscard.setVisibility(View.GONE);
            });
        }

        return convertView;
    }
}