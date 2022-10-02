/*
 * This model class provides to store the information about the room, get from an API request.
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

package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Room implements Parcelable, Serializable {
    public static final String NAMEFILE = "room.dat";

    public byte number;
    public String name;
    @SerializedName("local_ip")
    public String localIP;

    public Room() {}

    // PARCELIZATION
    protected Room(Parcel in) {
        number = in.readByte();
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
        String idToString = Byte.toString(number);

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
        parcel.writeByte(number);
        parcel.writeString(name);
        parcel.writeString(localIP);
    }
}
