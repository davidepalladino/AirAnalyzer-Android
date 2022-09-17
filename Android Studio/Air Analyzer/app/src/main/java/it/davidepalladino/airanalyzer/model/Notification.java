/*
 * This model class provides to store the information about the notification, get from an API request.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 16th September, 2022
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
    public static final String PREFERENCE_NOTIFICATION_TYPE_DEVICE_TIME = "preferenceNotificationTypeDevice_Time";

    public static final String TYPE_DEVICE = "DEVICE";
    public static final String SUBTYPE_DEVICE_ERROR_SAVE_MEASURE = "ERROR_SAVE";
    public static final String SUBTYPE_DEVICE_VALIDATION_FAILED_MEASURE = "VALIDATION_FAILED";
    public static final String SUBTYPE_DEVICE_DISCONNECTED = "DISCONNECTED";

    public int id;
    public String type;
    public String content;
    @SerializedName("is_seen")
    public byte isSeen;
    public String when;

    // PARCELIZATION
    protected Notification(Parcel in) {
        id = in.readInt();
        type = in.readString();
        content = in.readString();
        isSeen = in.readByte();
        when = in.readString();
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
        dest.writeString(type);
        dest.writeString(content);
        dest.writeByte(isSeen);
        dest.writeString(when);
    }
}
