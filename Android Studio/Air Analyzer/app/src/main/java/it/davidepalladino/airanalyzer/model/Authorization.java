/*
 * This singleton model class provides to store the information about the authentication.
 * Must be created and used only from APIService.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 1.0.0
 * @date 27th August, 2022
 *
 */

package it.davidepalladino.airanalyzer.model;

public class Authorization {
    public static Authorization instance = null;
    public String tokenType;
    public String token;

    private Authorization() { }

    public static synchronized Authorization getInstance() {
        if (instance == null) {
            instance = new Authorization();
        }

        return instance;
    }

    public static synchronized void setInstance(Authorization instanceFrom) {
        instance = instanceFrom;
    }

    /**
     * @brief This method prepares the authentication token.
     * @return String concatenation between "token" and "tokenType".
     */
    public String getAuthorization() {
        if (!token.isEmpty() && !tokenType.isEmpty()) {
            return tokenType + " " + token;
        } else {
            return null;
        }
    }
}