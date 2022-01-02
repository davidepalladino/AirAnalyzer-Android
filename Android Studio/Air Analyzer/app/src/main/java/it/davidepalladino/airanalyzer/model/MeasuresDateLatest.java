/*
 * This model class provides to store the latest measures on specific date.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 17th October, 2021
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

public class MeasuresDateLatest implements Parcelable {
    @SerializedName("Time")
    public String time;
    @SerializedName("Temperature")
    public float temperature;
    @SerializedName("Humidity")
    public float humidity;

    // PARCELIZATION
    protected MeasuresDateLatest(Parcel in) {
        time = in.readString();
        temperature = in.readFloat();
        humidity = in.readFloat();
    }

    public static final Creator<MeasuresDateLatest> CREATOR = new Creator<MeasuresDateLatest>() {
        @Override
        public MeasuresDateLatest createFromParcel(Parcel in) {
            return new MeasuresDateLatest(in);
        }

        @Override
        public MeasuresDateLatest[] newArray(int size) {
            return new MeasuresDateLatest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeFloat(temperature);
        dest.writeFloat(humidity);
    }
}
