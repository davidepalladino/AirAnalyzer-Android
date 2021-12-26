/*
 * This model class provides to store the hour average measures on specific date.
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

public class MeasuresDateAverage implements Parcelable {
    @SerializedName("Hour")
    public byte hour;
    @SerializedName("Temperature")
    public float temperature;
    @SerializedName("Humidity")
    public float humidity;

    // PARCELIZATION
    protected MeasuresDateAverage(Parcel in) {
        hour = in.readByte();
        temperature = in.readFloat();
        humidity = in.readFloat();
    }

    public static final Creator<MeasuresDateAverage> CREATOR = new Creator<MeasuresDateAverage>() {
        @Override
        public MeasuresDateAverage createFromParcel(Parcel in) {
            return new MeasuresDateAverage(in);
        }

        @Override
        public MeasuresDateAverage[] newArray(int size) {
            return new MeasuresDateAverage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(hour);
        dest.writeFloat(temperature);
        dest.writeFloat(humidity);
    }
}
