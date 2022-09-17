/*
 * This control class provides to manage the notification in background,
 *  to notify the user about some error like device disconnected or wrong measure.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 2.0.0
 * @date 17th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller;

import static it.davidepalladino.airanalyzer.controller.APIService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.model.Notification.PREFERENCE_NOTIFICATION_LATEST_ID_WORKER;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.MainActivity;
import it.davidepalladino.airanalyzer.view.fragment.NotificationFragment;

public class NotificationDeviceWorker extends Worker {
    public static final String tag = "NotificationDeviceWorker";

    private final Context context;

    private NotificationChannel notificationChannel;
    private final String channelID;

    private final FileManager fileManager;
    private final SimpleDateFormat simpleDateFormat;

    private APIService apiService;

    private byte attemptsLogin = 3;

    public NotificationDeviceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i(NotificationDeviceWorker.class.getSimpleName(), "Launched constructor.");

        this.context = context;

        channelID = NotificationDeviceWorker.class.getSimpleName();

        if (Build.VERSION.SDK_INT > 25) {
            notificationChannel = new NotificationChannel(
                    channelID,
                    context.getString(R.string.notificationChannelName_Device),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.notificationChannelDescription_Device));
        }

        fileManager = new FileManager(context);

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(NotificationDeviceWorker.class.getSimpleName(), "Launched doWork.");

        context.registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        /*
         * Starting service only if is not previous started. However,
         *  will be started in foreground if the API level is greater than 25, to avoid
         *  the background limitation of Android.
         */
        Intent intentDatabaseService = new Intent(context, APIService.class);
        if (!APIService.isRunning) {
            if (Build.VERSION.SDK_INT > 25) {
                Log.i(NotificationDeviceWorker.class.getSimpleName(), "Started service in foreground.");
                context.startForegroundService(intentDatabaseService);
            } else {
                Log.i(NotificationDeviceWorker.class.getSimpleName(), "Started service in standard mode.");
                context.startService(intentDatabaseService);
            }
        }
        context.bindService(intentDatabaseService, serviceConnection, Context.BIND_IMPORTANT);

        return Result.success();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            apiService = localBinder.getService();

//            apiService.login((User) fileManager.readObject(User.NAMEFILE), NotificationFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN);
            apiService.getAllNotifications(null, null, NotificationDeviceWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL, Notification.TYPE_DEVICE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
            if (intentFrom != null) {
                if (intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY) && intentFrom.hasExtra(SERVICE_STATUS_CODE)) {
                    int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                    switch (statusCode) {
                        case 200:
                            // GET ALL NOTIFICATIONS BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(NotificationDeviceWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL) == 0) {
                                Log.i(NotificationDeviceWorker.class.getSimpleName(), "Get latest notification.");

                                ArrayList<Notification> arrayListNotificationsLatest = intentFrom.getParcelableArrayListExtra(SERVICE_BODY);

                                /* Updating the badge and the NotificationFragment if the current Activity is the MainActivity. */
                                Activity currentActivity = ((AirAnalyzerApplication) contextFrom).getCurrentActivity();
                                if (currentActivity instanceof MainActivity) {
                                    ((MainActivity) currentActivity).updateListNotificationFragment(arrayListNotificationsLatest);
                                    ((MainActivity) currentActivity).updateBadge(arrayListNotificationsLatest);
                                }

                                /* Searching the latest notification not seen. */
                                int latestID = fileManager.readPreferenceNotificationID(Notification.NAMEFILE, PREFERENCE_NOTIFICATION_LATEST_ID_WORKER);
                                for (Notification notification: arrayListNotificationsLatest) {
                                    if (latestID < notification.id && notification.isSeen == 0) {
                                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(contextFrom, channelID);

                                        byte notificationManagerID = 0;

                                        int smallIcon = 0;
                                        String title = null;
                                        String body = null;

                                        try {
                                            JSONObject contentJSON = new JSONObject(notification.content);

                                            switch (contentJSON.getString("subcategory")) {
                                                case Notification.SUBTYPE_DEVICE_DISCONNECTED:
                                                    notificationManagerID = 1;

                                                    smallIcon = R.drawable.ic_link_off;
                                                    title = contextFrom.getString(R.string.notificationTypeDevice_SubTypeDisconnected);
                                                    body = String.format(
                                                        "%s %s %s. %s",
                                                        contentJSON.getString("room_name"),
                                                        context.getString(R.string.notificationTypeDevice_SubTypeDisconnected_Body),
                                                        createSimpleDate(simpleDateFormat.parse(contentJSON.getString("last_datetime")).getTime(), Calendar.getInstance().getTimeInMillis()),
                                                        context.getString(R.string.notificationTypeDevice_End)
                                                    );

                                                    break;

                                                case Notification.SUBTYPE_DEVICE_VALIDATION_FAILED_MEASURE:
                                                    notificationManagerID = 2;

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

                                                    body = String.format(
                                                        "%s %s %s. %s",
                                                        contentJSON.getString("room_name"),
                                                        context.getString(R.string.notificationTypeDevice_SubTypeValidationFailed_Body),
                                                        String.join(", ", String.join(", ", measures)),
                                                        context.getString(R.string.notificationTypeDevice_End)
                                                    );
                                            }
                                        } catch (JSONException | ParseException e) { e.printStackTrace(); }

                                        if ((notificationManagerID != 0) && (title != null) && (body != null)) {
                                            notificationBuilder
                                                    .setSmallIcon(smallIcon)
                                                    .setContentTitle(title)
                                                    .setContentText(body)
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body));
                                        }

                                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                                .setAutoCancel(true)
                                                .setChannelId(channelID);

                                        NotificationManager notificationManager = (NotificationManager) contextFrom.getSystemService(Context.NOTIFICATION_SERVICE);

                                        if (Build.VERSION.SDK_INT > 25) {
                                            notificationManager.createNotificationChannel(notificationChannel);
                                        }

                                        notificationManager.notify(notificationManagerID, notificationBuilder.build());

                                        fileManager.savePreferenceNotificationID(Notification.NAMEFILE, PREFERENCE_NOTIFICATION_LATEST_ID_WORKER, notification.id);

                                        contextFrom.unbindService(serviceConnection);
                                        contextFrom.unregisterReceiver(broadcastReceiver);

                                        break;
                                    }
                                }

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(NotificationDeviceWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN) == 0) {
                                Log.i(NotificationDeviceWorker.class.getSimpleName(), "Executed login.");

                                apiService.getAllNotifications(null, null, NotificationDeviceWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL, Notification.TYPE_DEVICE);

                                attemptsLogin = 1;
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            Log.e(NotificationDeviceWorker.class.getSimpleName(), String.format("Error login on %d attempt.", attemptsLogin));

                            /* Checking the attempts for executing another login, or for unbinding the DatabaseService and the BroadcastReceiver. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(() -> {
                                    apiService.login((User) fileManager.readObject(User.NAMEFILE), NotificationFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN);
                                    attemptsLogin++;
                                }, TIME_LOGIN_TIMEOUT);
                            } else {
                                contextFrom.unbindService(serviceConnection);
                                contextFrom.unregisterReceiver(broadcastReceiver);
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    };

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
