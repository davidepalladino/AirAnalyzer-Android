/*
 * This view class provides to show a screen, where the user can manage several settings.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 26th December, 2021
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

package it.davidepalladino.airanalyzer.view.fragment;

import static androidx.work.ExistingPeriodicWorkPolicy.REPLACE;
import static it.davidepalladino.airanalyzer.model.Notification.PREFERENCE_NOTIFICATION_ERROR_TIME;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import it.davidepalladino.airanalyzer.BuildConfig;
import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.controller.NotificationErrorWorker;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;

public class SettingFragment extends Fragment {
    private Toolbar toolbar;

    private Spinner spinnerNotificationErrors;
    private TextView textViewVersionName;
    private TextView textViewDeveloperName;

    private FileManager fileManager;
    private User user;

    int[] timesNotificationErrors;
    int latestPositionSpinnerNotificationErrors;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fileManager = new FileManager(getContext());
        user = (User) fileManager.readObject(User.NAMEFILE);

        timesNotificationErrors = requireActivity().getResources().getIntArray(R.array.timesNotificationErrors);

        View layoutFragment = inflater.inflate(R.layout.fragment_setting, container, false);

        toolbar = layoutFragment.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        toolbar.inflateMenu(R.menu.menu_setting);

        spinnerNotificationErrors = layoutFragment.findViewById(R.id.spinnerNotificationErrors);

        ArrayAdapter<String> adapterNotificationErrors = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, getResources().getStringArray(R.array.spinnerNotificationErrors));
        adapterNotificationErrors.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerNotificationErrors.setAdapter(adapterNotificationErrors);

        /* Searching the position of latest time minutes set, to select the right element into spinner. */
        int savedTimeNotificationErrors = fileManager.readNotificationTime(PREFERENCE_NOTIFICATION_ERROR_TIME);
        for (int p = 0; p < timesNotificationErrors.length; p++) {
            if (timesNotificationErrors[p] == savedTimeNotificationErrors) {
                latestPositionSpinnerNotificationErrors = p;
                spinnerNotificationErrors.setSelection(latestPositionSpinnerNotificationErrors);
                break;
            }
        }

        spinnerNotificationErrors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /* The changing will be apply only if there is a difference between the latest and new position. */
                if (position != latestPositionSpinnerNotificationErrors) {
                    latestPositionSpinnerNotificationErrors = position;
                    fileManager.saveNotitificationTime(PREFERENCE_NOTIFICATION_ERROR_TIME, timesNotificationErrors[position]);

                    WorkManager workManager = WorkManager.getInstance(requireActivity());
                    PeriodicWorkRequest notificationRequest = new PeriodicWorkRequest.Builder(NotificationErrorWorker.class, timesNotificationErrors[position], TimeUnit.MINUTES)
                            .build();
                    workManager.enqueueUniquePeriodicWork(NotificationErrorWorker.tag, REPLACE, notificationRequest);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        textViewVersionName = layoutFragment.findViewById(R.id.textViewVersionName);
        textViewVersionName.setText(BuildConfig.VERSION_NAME);

        textViewDeveloperName = layoutFragment.findViewById(R.id.textViewDeveloperName);
        textViewDeveloperName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "me@davidepalladino.com" });
                startActivity(intentEmail);
            }
        });

        return layoutFragment;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // LOGOUT
        if (id == R.id.menuItemLogout) {
            goToLogin();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @brief This method provides to launch the Login Activity, if the credentials are bad or if
     *  there is a logout request.
     */
    private void goToLogin() {
        /* Deleting the information for the login. */
        user.password = "";
        user.token = "";
        fileManager.saveObject(user, User.NAMEFILE);

        Intent intentTo = new Intent(getActivity(), LoginActivity.class);

        /* Starting the Login Activity and finishing the hosting Activity for this fragment. */
        startActivity(intentTo);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}