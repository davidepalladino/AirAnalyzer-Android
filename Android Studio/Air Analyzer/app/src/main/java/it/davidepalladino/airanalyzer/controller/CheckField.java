/*
 * This control class provides to check a complex Edit Text syntax, like password and email.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.1
 * @date 3rd January, 2022
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class CheckField {
    private static final String REGEX_USERNAME = "^[a-z0-9_-]{5,20}$";
    private static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#])[A-Za-z\\d@$!%*?&.#]{8,24}$";
    private static final String REGEX_EMAIL = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    private static final String REGEX_IPV4 =
            "^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";

    /**
     * @brief This method provides to check the username.
     * @param username Text where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkUsername(String username) {
        return checkSyntax(username, REGEX_USERNAME);
    }

    /**
     * @brief This method provides to check the password.
     * @param password Text where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkPassword(String password) {
        return checkSyntax(password, REGEX_PASSWORD);
    }

    /**
     * @brief This method provides to check the email.
     * @param email Text where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkEmail(String email) {
        return checkSyntax(email, REGEX_EMAIL);
    }

    /**
     * @brief This method provides to check the IPv4.
     * @param ipv4 Text where the syntax will be controlled.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    public static boolean checkIPv4(String ipv4) {
        return checkSyntax(ipv4, REGEX_IPV4);
    }

    /**
     * @brief This method is necessary to the purposes of the public methods.
     * @param text Text where the syntax will be controlled.
     * @param regex Regex necessary for checking.
     * @return Value "true" if the syntax is corrected; else, value "false".
     */
    private static boolean checkSyntax(String text, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        return matcher.matches();
    }
}
