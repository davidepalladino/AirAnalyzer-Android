/*
 * This model class provides to store the information about the room, get from an API request.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 16th October, 2021
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

package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Room implements Parcelable, Serializable {
    public static final String NAMEFILE = "room.dat";

    @SerializedName("ID")
    public byte id;
    @SerializedName("Name")
    public String name;
    @SerializedName("LocalIP")
    public String localIP;

    public Room() {}

    // PARCELIZATION
    protected Room(Parcel in) {
        id = in.readByte();
        name = in.readString();
        localIP = in.readString();
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    @Override
    public String toString() {
        String idToString = Byte.toString(id);

        if (name != null) {
            idToString += " | " + name;
        } else {
            idToString += " | <No name>";
        }

        return idToString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(id);
        parcel.writeString(name);
        parcel.writeString(localIP);
    }
}
