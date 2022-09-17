/*
 * This view class provides the initialization of application and the managing the tasks and the log.
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

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

public class AirAnalyzerApplication extends Application implements Configuration.Provider {
    private Activity currentActivity;

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        /* Creating the logging interceptor for debugging the WorkManager. */
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * @brief This method provides to get the current activity, for several purpose of the application like the Notification Workers.
     * @return The current Activity stored.
     */
    public Activity getCurrentActivity() {
        return this.currentActivity;
    }

    /**
     * @brief This method provides to store the current activity, for several purpose of the application like the Notification Workers.
     * @param currentActivity The current Activity to store.
     * @warning To avoid runtime error, is advisable to set null at the pause state or at the end of Activity.
     */
    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }
}
