/*
 * This control class provides to manage the datetime. In example is possible to create a date in a specific format.
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
