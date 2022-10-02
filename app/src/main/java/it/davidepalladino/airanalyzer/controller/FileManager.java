/*
 * This control class provides to manage the files int the memory of the phone.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 2.1.0
 * @date 3rd January, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static android.content.Context.MODE_PRIVATE;

@SuppressLint("ApplySharedPref")
@SuppressWarnings("unused")
public class FileManager {
    private final Context context;

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
     * @param nameFile Name of file for saving.
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
     * @param nameFile Name of file for reading.
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
     * @param nameFile Name of file for removing.
     * @return Value "true" if the file has been deleted successful; else, value "false" if there is some error.
     */
    public boolean removeFile(String nameFile) {
        return context.deleteFile(nameFile);
    }

    /**
     * @brief This method provides to store a generic integer value like a preference.
     * @param nameFile Name of file for saving.
     * @param namePreference Specific name of preference.
     * @param value Value to save.
     */
    public void savePreferenceInt(String nameFile, String namePreference, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(nameFile, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferenceEditor = sharedPreferences.edit();
        sharedPreferenceEditor.putInt(namePreference, value);
        sharedPreferenceEditor.commit();
    }

    /**
     * @brief This method provides to save an ID of notification. Usually is used to save the last ID.
     * @param nameFile Name of file for saving.
     * @param namePreference Specific name of preference.
     * @param ID ID of notification.
     */
    public void savePreferenceNotificationID(String nameFile, String namePreference, int ID) {
        savePreferenceInt(nameFile, namePreference, ID);
    }

    /**
     * @brief This method provides to read an ID of notification. Usually is used to read the last ID.
     * @param nameFile Name of file for reading.
     * @param namePreference Specific name of preference.
     * @return ID of notification, or 0.
     */
    public int readPreferenceNotificationID(String nameFile, String namePreference) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(nameFile, MODE_PRIVATE);
        return sharedPreferences.getInt(namePreference, 0);
    }

    /**
     * @brief This method provides to save the time of notification.
     * @param nameFile Name of file for saving.
     * @param namePreference Specific name of preference.
     * @param time Time of notification in minutes.
     */
    public void savePreferenceNotificationTime(String nameFile, String namePreference, int time) {
        savePreferenceInt(nameFile, namePreference, time);
    }

    /**
     * @brief This method provides to read the time of notification.
     * @param nameFile Name of file for reading.
     * @param namePreference Specific name of preference.
     * @return Time of notification in minutes, or 15.
     */
    public int readPreferenceNotificationTime(String nameFile, String namePreference) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(nameFile, MODE_PRIVATE);
        return sharedPreferences.getInt(namePreference, 15);
    }
}
