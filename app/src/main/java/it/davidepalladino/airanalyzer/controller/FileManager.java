/*
 * This control class provides to manage the files int the memory of the phone.
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

package it.davidepalladino.airanalyzer.controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import it.davidepalladino.airanalyzer.model.Notification;

import static android.content.Context.MODE_PRIVATE;
import static it.davidepalladino.airanalyzer.model.Notification.PREFERENCE_NOTIFICATION_LATEST_ID_BADGE;

public class FileManager {
    private Context context;

    /**
     * This constructor initializes the object setting only the context where will be utilized.
     * @param context Context where the object setting only the context where will be utilized.
     */
    public FileManager(Context context) {
        this.context = context;
    }

    /**
     * @brief This method provides to store an object into a file of the internal memory of the phone.
     * @param object Object to store.
     * @param nameFile Name of file for storing.
     * @return Value "true" if the object has been stored successful; else, value "false" if there is some error.
     */
    public boolean saveObject(Object object, String nameFile) {
        try {
            FileOutputStream loginFOS = context.openFileOutput(nameFile, MODE_PRIVATE);

            ObjectOutputStream loginOOS = new ObjectOutputStream(loginFOS);
            loginOOS.writeObject(object);
            loginOOS.close();
            loginFOS.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @brief This method provides to get an object from a file of the internal memory of the phone.
     * @param nameFile Name of file for recovering.
     * @return Value "true" if the object has been recovered successful; else, value "false" if there is some error.
     */
    public Object readObject(String nameFile) {
        Object object = null;

        try {
            FileInputStream loginFIS = context.openFileInput(nameFile);

            ObjectInputStream loginOIS = new ObjectInputStream(loginFIS);
            object = loginOIS.readObject();
            loginOIS.close();
            loginFIS.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return object;
        }

        return object;
    }

    /**
     * @brief This method provides to delete a file of the internal memory of the phone.
     * @param nameFile Name of file for deleting.
     * @return Value "true" if the file has been deleted successful; else, value "false" if there is some error.
     */
    public boolean removeFile(String nameFile) {
        return context.deleteFile(nameFile);
    }

    /**
     * @brief This method provides to save an ID of notification. Usually is used to save the last ID.
     * @param namePreference Specific name of preference.
     * @param ID ID of notification.
     */
    public void saveNotitificationID(String namePreference, int ID) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Notification.NAMEFILE, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferenceEditor = sharedPreferences.edit();
        sharedPreferenceEditor.putInt(namePreference, ID);
        sharedPreferenceEditor.commit();
    }

    /**
     * @brief This method provides to read an ID of notification. Usually is used to read the last ID.
     * @param namePreference Specific name of preference.
     * @return ID of notification, or 0.
     */
    public int readNotificationID(String namePreference) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Notification.NAMEFILE, MODE_PRIVATE);
        return sharedPreferences.getInt(namePreference, 0);
    }

    /**
     * @brief This method provides to save the time of notification.
     * @param namePreference Specific name of preference.
     * @param time Time of notification in minutes.
     */
    public void saveNotitificationTime(String namePreference, int time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Notification.NAMEFILE, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferenceEditor = sharedPreferences.edit();
        sharedPreferenceEditor.putInt(namePreference, time);
        sharedPreferenceEditor.commit();
    }

    /**
     * @brief This method provides to read the time of notification.
     * @param namePreference Specific name of preference.
     * @return Time of notification in minutes, or 15.
     */
    public int readNotificationTime(String namePreference) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Notification.NAMEFILE, MODE_PRIVATE);
        return sharedPreferences.getInt(namePreference, 15);
    }
}
