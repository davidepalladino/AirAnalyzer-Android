/*
 * This singleton model class provides to store the information about the user.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 27th August, 2022
 *
 */

package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class User implements Parcelable, Serializable {
    public static final String NAMEFILE = "user.dat";

    public static User instance = null;

    public String id;
    public String username;
    public String password;
    public String email;
    public String name;
    public String surname;
    public String timezone;

    private User() { }

    public static synchronized User getInstance() {
        if (instance == null) {
            instance = new User();
        }

        return instance;
    }

    public static synchronized void setInstance(User instanceFrom) {
        instance = instanceFrom;
    }

    // PARCELIZATION
    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        password = in.readString();
        email = in.readString();
        name = in.readString();
        surname = in.readString();
        timezone = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(email);
        parcel.writeString(name);
        parcel.writeString(surname);
        parcel.writeString(timezone);
    }
}