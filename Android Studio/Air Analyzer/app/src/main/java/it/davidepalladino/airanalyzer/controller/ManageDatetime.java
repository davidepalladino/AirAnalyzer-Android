/*
 * This control class provides to manage the datetime. In example is possible to create a date in a specific format.
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

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ManageDatetime {
    /**
     * @brief This method provides to create a date in a specific format.
     * @param dateSelected Calendar object where to get the date.
     * @param format Pattern for creating the string.
     * @return String wth the date in specific format.
     */
    public static String createDateFormat(Calendar dateSelected, String format) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(format);

        return formatter.format(dateSelected.getTime());
    }

    /**
     * @brief This method provides to get the actual UTC.
     * @param calendar Calendar needed to get the UTC.
     * @return Value of UTC.
     */
    public static int getUTC(Calendar calendar) {
        return (int) (TimeUnit.MILLISECONDS.toHours(calendar.getTimeZone().getRawOffset()) + TimeUnit.MILLISECONDS.toHours(calendar.get(Calendar.DST_OFFSET)));
    }
}
