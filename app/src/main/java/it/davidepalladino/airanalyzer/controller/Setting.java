package it.davidepalladino.airanalyzer.controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import it.davidepalladino.airanalyzer.model.Login;

import static android.content.Context.MODE_PRIVATE;

public class Setting {
    public static final String NAMEFILE_LOGIN = "login";
    public static final String NAMEFILE_ROOM = "room";
    public static final String NAMEPREFERENCE_TOKEN = "token";
    public static final String NAMEPREFERENCE_PAGE = "page";

    private Context context;

    public Setting(Context context) {
        this.context = context;
    }

    public Login readLogin() {
        Login login = null;

        try {
            FileInputStream loginFOS = context.openFileInput(NAMEFILE_LOGIN);

            ObjectInputStream loginOOS = new ObjectInputStream(loginFOS);
            login = (Login) loginOOS.readObject();
            loginOOS.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return login;
        }

        return login;
    }

    public boolean saveLogin(Login login) {
        try {
            FileOutputStream loginFOS = context.openFileOutput(NAMEFILE_LOGIN, MODE_PRIVATE);

            ObjectOutputStream loginOOS = new ObjectOutputStream(loginFOS);
            loginOOS.writeObject(login);
            loginOOS.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public String readToken() {
        SharedPreferences loginPreference = context.getSharedPreferences(NAMEFILE_LOGIN, MODE_PRIVATE);
        return loginPreference.getString(NAMEPREFERENCE_TOKEN, "");
    }

    public void saveToken(String token) {
        SharedPreferences loginPreference = context.getSharedPreferences(NAMEFILE_LOGIN, MODE_PRIVATE);
        SharedPreferences.Editor loginEdit = loginPreference.edit();
        loginEdit.putString(NAMEPREFERENCE_TOKEN, token).apply();
    }

    public int readRoomPage() {
        SharedPreferences roomPreference = context.getSharedPreferences(NAMEFILE_ROOM, MODE_PRIVATE);
        return roomPreference.getInt(NAMEPREFERENCE_PAGE, 0);
    }

    public void saveRoomPage(int page) {
        SharedPreferences roomPreference = context.getSharedPreferences(NAMEFILE_ROOM, MODE_PRIVATE);
        SharedPreferences.Editor roomEdit = roomPreference.edit();
        roomEdit.putInt(NAMEPREFERENCE_PAGE, page).apply();
    }
}
