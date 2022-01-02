/*
 * This model class provides to store the information about the notification, get from an API request.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 19th December, 2021
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

public class Notification implements Parcelable {
    public static final String NAMEFILE = "notification.dat";
    public static final String PREFERENCE_NOTIFICATION_LATEST_ID_BADGE = "preferenceNotificationLatestID_Badge";
    public static final String PREFERENCE_NOTIFICATION_LATEST_ID_WORKER = "preferenceNotificationLatestID_Worker";
    public static final String PREFERENCE_NOTIFICATION_ERROR_TIME = "preferenceNotificationErrorTime";

    @SerializedName("ID")
    public int id;
    @SerializedName("DateAndTime")
    public String dateAndTime;
    @SerializedName("Type")
    public String type;
    @SerializedName("Name")
    public String name;
    @SerializedName("IsSeen")
    public byte isSeen;

    // PARCELIZATION
    protected Notification(Parcel in) {
        id = in.readInt();
        dateAndTime = in.readString();
        type = in.readString();
        name = in.readString();
        isSeen = in.readByte();
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(dateAndTime);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeByte(isSeen);
    }
}
