/*
 * This model class provides to store the information about the user, get from an API request.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 20th September, 2021
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
import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class User implements Parcelable, Serializable {
    public static final String NAMEFILE = "user.dat";

    public String id;
    public String username;
    public String password;
    public String email;
    public String name;
    public String surname;
    public byte isActive;
    public String question1;
    public String question2;
    public String question3;
    public String answer1;
    public String answer2;
    public String answer3;
    @SerializedName("token_type")
    public String tokenType;
    public String token;

    /**
     * @brief This constructor provides only to allocate the object.
     */
    public User() {
    }

    /**
     * @brief This method provides to set the information about the login.
     * @param username Username necessary for the login. Maximum length is 20 characters.
     * @param password Password necessary for the login. Maximum length is 64 characters.
     */
    public void setLoginCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @brief This method provides to get the authorization string for several purpose.
     * @return String concatenation between "token" and "tokenType".
     */
    public String getAuthorization() {
        if (!token.isEmpty() && !tokenType.isEmpty()) {
            return tokenType + " " + token;
        } else {
            return null;
        }
    }

    // PARCELIZATION
    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        password = in.readString();
        email = in.readString();
        name = in.readString();
        surname = in.readString();
        isActive = in.readByte();
        question1 = in.readString();
        question2 = in.readString();
        question3 = in.readString();
        answer1 = in.readString();
        answer2 = in.readString();
        answer3 = in.readString();
        tokenType = in.readString();
        token = in.readString();
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
        parcel.writeByte(isActive);
        parcel.writeString(question1);
        parcel.writeString(question2);
        parcel.writeString(question3);
        parcel.writeString(answer1);
        parcel.writeString(answer2);
        parcel.writeString(answer3);
        parcel.writeString(tokenType);
        parcel.writeString(token);
    }
}