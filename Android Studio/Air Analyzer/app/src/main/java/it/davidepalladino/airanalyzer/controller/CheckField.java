/*
 * This control class provides to check a complex Edit Text syntax, like password and email.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 8th October, 2021
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

import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckField {
    private static final String REGEX_USERNAME = "^[a-z0-9_-]{5,20}$";
    private static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#])[A-Za-z\\d@$!%*?&.#]{16,24}$";
    private static final String REGEX_EMAIl = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    private static final String REGEX_IPV4 =
            "^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";

    /**
     * @brief This method provides to check the username.
     * @param editText EditText where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkUsername(EditText editText) {
        return checkSyntax(editText, REGEX_USERNAME);
    }

    /**
     * @brief This method provides to check the password.
     * @param editText EditText where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkPassword(EditText editText) {
        return checkSyntax(editText, REGEX_PASSWORD);
    }

    /**
     * @brief This method provides to check the email.
     * @param editText EditText where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkEmail(EditText editText) {
        return checkSyntax(editText, REGEX_EMAIl);
    }

    /**
     * @brief This method provides to check the IPv4.
     * @param editText EditText where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkIPv4(EditText editText) {
        return checkSyntax(editText, REGEX_IPV4);
    }

    /**
     * @brief This method is necessary to the purposes of the public methods.
     * @param editText EditText where the syntax will be controlled.
     * @param regex Regex necessary for checking.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    private static boolean checkSyntax(EditText editText, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(editText.getText().toString());

        return matcher.matches();
    }
}
