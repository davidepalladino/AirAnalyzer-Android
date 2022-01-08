/*
 * This control class provides to manage the notification in background,
 *  to notify the user about some error like device disconnected or wrong measure.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 1.0.1
 * @date 8th January, 2022
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

import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.model.Notification.*;

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

import java.util.ArrayList;
import java.util.Calendar;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.MainActivity;

public class NotificationErrorWorker extends Worker {
    public static final String tag = "NotificationErrorWorker";

    private final Context context;

    private NotificationChannel notificationChannel;
    private final String channelID;

    private final int utc;

    private final FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    private byte attemptsLogin = 1;

    public NotificationErrorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(NotificationErrorWorker.class.getSimpleName(), "Launched constructor.");

        this.context = context;

        channelID = NotificationErrorWorker.class.getSimpleName();

        if (Build.VERSION.SDK_INT > 25) {
            notificationChannel = new NotificationChannel(
                    channelID,
                    context.getString(R.string.notificationErrorChannelName),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.notificationErrorChannelDescription));
        }

        fileManager = new FileManager(context);
        user = (User) fileManager.readObject(User.NAMEFILE);

        Calendar date = Calendar.getInstance();
        utc = ManageDatetime.getUTC(date);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(NotificationErrorWorker.class.getSimpleName(), "Launched doWork.");

        context.registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        /*
         * Starting service only if is not previous started. However,
         *  will be started in foreground if the API level is greater than 25, to avoid
         *  the background limitation of Android.
         */
        Intent intentDatabaseService = new Intent(context, DatabaseService.class);
        if (!DatabaseService.isRunning) {
            if (Build.VERSION.SDK_INT > 25) {
                Log.d(NotificationErrorWorker.class.getSimpleName(), "Started service in foreground.");
                context.startForegroundService(intentDatabaseService);
            } else {
                Log.d(NotificationErrorWorker.class.getSimpleName(), "Started service in standard mode.");
                context.startService(intentDatabaseService);
            }
        }
        context.bindService(intentDatabaseService, serviceConnection, Context.BIND_IMPORTANT);

        return Result.success();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getNotificationsLatest(user.getAuthorization(), utc, NotificationErrorWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
            if (intentFrom != null) {
                if (intentFrom.hasExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY) && intentFrom.hasExtra(SERVICE_STATUS_CODE)) {
                    int statusCode = intentFrom.getIntExtra(SERVICE_STATUS_CODE, 0);
                    switch (statusCode) {
                        case 200:
                            // GET NOTIFICATIONS LATEST BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(NotificationErrorWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST) == 0) {
                                Log.d(NotificationErrorWorker.class.getSimpleName(), "Get latest notification.");

                                ArrayList<Notification> arrayListNotificationsLatest = intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE);

                                /* Updating the badge and the NotificationFragment if the current Activity is the MainActivity. */
                                Activity currentActivity = ((AirAnalyzerApplication) contextFrom).getCurrentActivity();
                                if (currentActivity instanceof MainActivity) {
                                    ((MainActivity) currentActivity).updateListNotificationFragment(arrayListNotificationsLatest);
                                    ((MainActivity) currentActivity).updateBadge(arrayListNotificationsLatest);
                                }

                                /* Searching the latest notification not seen. */
                                int latestID = fileManager.readPreferenceNotificationID(NAMEFILE, PREFERENCE_NOTIFICATION_LATEST_ID_WORKER);
                                for (Notification notification: arrayListNotificationsLatest) {
                                    if (latestID < notification.id && notification.isSeen == 0) {
                                        byte notificationManagerID = 0;

                                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(contextFrom, channelID);

                                        /* Preparing the right notification based of the type of error. */
                                        if (notification.type.equals("ERROR_NOT_UPDATED")) {
                                            notificationManagerID = 1;

                                            notificationBuilder.setContentTitle(contextFrom.getString(R.string.notificationErrorTypeNotUpdated))
                                                    .setContentText(String.format("%s %s", notification.name, contextFrom.getString(R.string.notificationErrorMessageNotUpdated)))
                                                    .setSmallIcon(R.drawable.ic_link_off)
                                                    .setStyle(new NotificationCompat.BigTextStyle()
                                                            .bigText(String.format("%s %s", notification.name, contextFrom.getString(R.string.notificationErrorMessageNotUpdated)))
                                                    );
                                        } else if (notification.type.equals("ERROR_MEASURE")) {
                                            notificationManagerID = 2;

                                            notificationBuilder.setContentTitle(contextFrom.getString(R.string.notificationErrorTypeNotUpdated))
                                                    .setContentText(String.format("%s %s", notification.name, contextFrom.getString(R.string.notificationErrorMessageMeasure)))
                                                    .setSmallIcon(R.drawable.ic_question_mark)
                                                    .setStyle(new NotificationCompat.BigTextStyle()
                                                            .bigText(String.format("%s %s", notification.name, contextFrom.getString(R.string.notificationErrorMessageMeasure)))
                                                    );
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

                                        fileManager.savePreferenceNotificationID(Notification.NAMEFILE, Notification.PREFERENCE_NOTIFICATION_LATEST_ID_WORKER, notification.id);

                                        contextFrom.unbindService(serviceConnection);
                                        contextFrom.unregisterReceiver(broadcastReceiver);

                                        break;
                                    }
                                }

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(NotificationErrorWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                Log.d(NotificationErrorWorker.class.getSimpleName(), "Executed login.");

                                user = intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                                fileManager.saveObject(user, User.NAMEFILE);

                                databaseService.getNotificationsLatest(user.getAuthorization(), utc, NotificationErrorWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST);

                                attemptsLogin = 1;
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            Log.e(NotificationErrorWorker.class.getSimpleName(), String.format("Error login on %d attempt.", attemptsLogin));

                            /* Checking the attempts for executing another login, or for unbinding the DatabaseService and the BroadcastReceiver. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(() -> {
                                    databaseService.login(user, NotificationErrorWorker.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                    attemptsLogin++;
                                }, TIME_LOGIN_TIMEOUT);
                            } else {
                                contextFrom.unbindService(serviceConnection);
                                contextFrom.unregisterReceiver(broadcastReceiver);
                            }

                        default:
                            break;
                    }
                }
            }
        }
    };
}
