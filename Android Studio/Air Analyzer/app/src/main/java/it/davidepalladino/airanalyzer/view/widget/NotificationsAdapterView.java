/*
 * This view class provides to show a personal View for the ListView of notifications in Main Activity.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 16th September, 2022
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);

        ImageView imageViewTypeNotification = convertView.findViewById(R.id.imageViewType);
        TextView textViewType = convertView.findViewById(R.id.textViewType);
        TextView textViewMessage = convertView.findViewById(R.id.textViewMessage);
        TextView textViewDateAndTime = convertView.findViewById(R.id.textViewDateAndTime);

        notification = getItem(position);

        JSONObject contentJSON = null;
        switch (notification.type) {
            case Notification.TYPE_DEVICE:
                try {
                    contentJSON = new JSONObject(notification.content);
                } catch (JSONException e) { e.printStackTrace(); }

                break;
        }

        try {
            if (contentJSON != null) {
                switch (contentJSON.getString("subcategory")) {
                    case Notification.SUBTYPE_DEVICE_DISCONNECTED:
                        imageViewTypeNotification.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_link_off));
                        textViewType.setText(context.getString(R.string.notificationTypeDevice_SubTypeDisconnected));
                        textViewMessage.setText(
                            String.format(
                                "%s %s %s. %s",
                                contentJSON.getString("room_name"),
                                context.getString(R.string.notificationTypeDevice_SubTypeDisconnected_Body),
                                createSimpleDate(simpleDateFormat.parse(contentJSON.getString("last_datetime")).getTime(), Calendar.getInstance().getTimeInMillis()),
                                context.getString(R.string.notificationTypeDevice_End)
                            )
                        );
                        break;

                    case Notification.SUBTYPE_DEVICE_VALIDATION_FAILED_MEASURE:
                        imageViewTypeNotification.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_question_mark));
                        textViewType.setText(context.getString(R.string.notificationTypeDevice_SubTypeValidationFailed));

                        JSONArray errorInfo = contentJSON.getJSONArray("error_info");
                        JSONObject valuesReceived = contentJSON.getJSONObject("values_received");

                        ArrayList<String> measures = new ArrayList<>();
                        for (int e = 0; e < errorInfo.length(); e++) {
                            String measure = "";
                            switch ((String) errorInfo.get(e)) {
                                case "temperature":
                                    measure += context.getString(R.string.temperature) + " (" + (String) valuesReceived.get((String) errorInfo.get(e)) + " °C)";
                                    break;
                                case "humidity":
                                    measure += context.getString(R.string.humidity) + " (" + (String) valuesReceived.get((String) errorInfo.get(e)) + " %)";
                                    break;
                            }

                            measures.add(measure);

                        }

                        textViewMessage.setText(
                            String.format(
                                "%s %s %s. %s",
                                contentJSON.getString("room_name"),
                                context.getString(R.string.notificationTypeDevice_SubTypeValidationFailed_Body),
                                String.join(", ", String.join(", ", measures)),
                                context.getString(R.string.notificationTypeDevice_End)
                            )
                        );
                }
            }
        } catch (JSONException|ParseException e) { e.printStackTrace(); }

        /* Setting the right style of TextView based of the status of notification. */
        if (notification.isSeen == 0) {
            textViewType.setTypeface(null, BOLD);
            textViewMessage.setTypeface(null, BOLD);
        } else {
            textViewType.setTypeface(null, NORMAL);
            textViewMessage.setTypeface(null, NORMAL);
        }

        try {
            Date date = simpleDateFormat.parse(notification.when);
            textViewDateAndTime.setText(createSimpleDate(date.getTime(), Calendar.getInstance().getTimeInMillis()));
        } catch (ParseException e) { e.printStackTrace(); }

        return convertView;
    }

    /**
     * @brief This method provides to create a simple date like notification sections of Social Network apps.
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