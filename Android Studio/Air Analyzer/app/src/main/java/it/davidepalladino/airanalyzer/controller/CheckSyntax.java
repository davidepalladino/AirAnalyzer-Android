/*
 * This control class provides to check the semantic of text like the IPv4.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 17th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class CheckSyntax {
    private static final String REGEX_IPV4 =
            "^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";

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
