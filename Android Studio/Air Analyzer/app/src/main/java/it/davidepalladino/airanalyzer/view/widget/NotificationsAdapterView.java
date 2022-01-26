/*
 * This view class provides to show a personal View for the ListView of notifications in Main Activity.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.1
 * @date 26th January, 2022
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

package it.davidepalladino.airanalyzer.view.widget;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.model.Notification;

@SuppressLint({"ViewHolder", "SimpleDateFormat", "DefaultLocale"})
public class NotificationsAdapterView extends ArrayAdapter<Notification> {
    private final Context context;
    private final int resource;
    public Notification notification;

    public NotificationsAdapterView(@NonNull Context context, @NonNull List<Notification> objects) {
        super(context, R.layout.notifications_adapter_view, objects);

        this.context = context;
        this.resource = R.layout.notifications_adapter_view;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);

        notification = getItem(position);

        ImageView imageViewTypeNotification = convertView.findViewById(R.id.imageViewType);
        TextView textViewType = convertView.findViewById(R.id.textViewType);
        TextView textViewMessage = convertView.findViewById(R.id.textViewMessage);
        TextView textViewDateAndTime = convertView.findViewById(R.id.textViewDateAndTime);

        /* Setting the right icon and message based of the type of notification. */
        if (notification.type.compareTo("ERROR_NOT_UPDATED") == 0) {
            imageViewTypeNotification.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_link_off));
            textViewType.setText(context.getString(R.string.notificationErrorTypeNotUpdated));
            textViewMessage.setText(String.format("%s %s %s", context.getString(R.string.textViewNotificationDevice), notification.name,  context.getString(R.string.notificationErrorMessageNotUpdated)));
        } else if (notification.type.compareTo("ERROR_MEASURE") == 0) {
            imageViewTypeNotification.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_question_mark));
            textViewType.setText(context.getString(R.string.notificationErrorTypeMeasure));
            textViewMessage.setText(String.format("%s %s %s", context.getString(R.string.textViewNotificationDevice), notification.name,  context.getString(R.string.notificationErrorMessageMeasure)));
        }

        /* Setting the right style of TextView based of the status of notification. */
        if (notification.isSeen == 0) {
            textViewType.setTypeface(null, BOLD);
            textViewMessage.setTypeface(null, BOLD);
        } else {
            textViewType.setTypeface(null, NORMAL);
            textViewMessage.setTypeface(null, NORMAL);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = simpleDateFormat.parse(notification.dateAndTime);
            assert date != null;
            textViewDateAndTime.setText(createSimpleDate(date.getTime(), Calendar.getInstance().getTimeInMillis()));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    /**
     * @brief This method provides to create a simple date like into notification sections of Social Network apps.
     * @param previousDate Previous date in millis format.
     * @param actualDate Actual date in millis format.
     * @return Simple date in string format, with number and unit ("s" for seconds, "m" for minutes, "h" for hours, "d" for days, "w" for week.
     */
    private String createSimpleDate(long previousDate, long actualDate) {
        long oneSec = 1000L;
        long oneMin = 60000L;
        long oneHour = 3600000L;
        long oneDay = 86400000L;
        long oneWeek = 604800000L;

        long timeElapsed = actualDate - previousDate;
        double duration = 0;
        String unit = "sec";

        if (timeElapsed < oneMin) {
            duration = (double) (timeElapsed / oneSec);
            duration = Math.round(duration);

            unit = "s";

        } else if (timeElapsed < oneHour) {
            duration = (double) (timeElapsed / oneMin);
            duration = Math.round(duration);

            unit = "m";
        } else if (timeElapsed < oneDay) {
            duration = (double) (timeElapsed / oneHour);
            duration = Math.round(duration);

            unit = "h";
        } else if (timeElapsed < oneWeek) {
            duration = (double) (timeElapsed / oneDay);
            duration = Math.round(duration);

            unit = "d";
        } else if (timeElapsed > oneWeek) {
            duration = (double) (timeElapsed / oneWeek);
            duration = Math.round(duration);

            unit = "w";
        }

        return String.format("%.0f%s", duration, unit);
    }
}