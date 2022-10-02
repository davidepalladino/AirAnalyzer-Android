/*
 * This model class provides to store the latest measures on actual date.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 3rd September, 2022
 *
 */

package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Measure implements Parcelable {
    public String when;
    public float temperature;
    public float humidity;
    @SerializedName("room_number")
    public String roomNumber;
    @SerializedName("room_name")
    public String roomName;

    // PARCELIZATION
    protected Measure(Parcel in) {
        when = in.readString();
        temperature = in.readFloat();
        humidity = in.readFloat();
        roomNumber = in.readString();
        roomName = in.readString();
    }

    public static final Creator<Measure> CREATOR = new Creator<Measure>() {
        @Override
        public Measure createFromParcel(Parcel in) {
            return new Measure(in);
        }

        @Override
        public Measure[] newArray(int size) {
            return new Measure[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(when);
        parcel.writeFloat(temperature);
        parcel.writeFloat(humidity);
        parcel.writeString(roomNumber);
        parcel.writeString(roomName);
    }
}
