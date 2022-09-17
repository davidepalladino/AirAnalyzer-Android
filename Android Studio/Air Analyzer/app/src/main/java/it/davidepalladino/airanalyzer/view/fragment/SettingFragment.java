/*
 * This view class provides to show a screen, where the user can manage several settings.
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

package it.davidepalladino.airanalyzer.view.fragment;

import static android.content.Context.BIND_AUTO_CREATE;
import static androidx.work.ExistingPeriodicWorkPolicy.REPLACE;

import static it.davidepalladino.airanalyzer.controller.APIService.SERVICE_STATUS_CODE;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGOUT;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.INTENT_BROADCAST;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import it.davidepalladino.airanalyzer.BuildConfig;
import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.APIService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.controller.NotificationDeviceWorker;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;
import it.davidepalladino.airanalyzer.view.widget.GenericToast;

public class SettingFragment extends Fragment {
    private FileManager fileManager;
    private GenericToast genericToast;

    private APIService apiService;

    private int[] timesNotificationErrors;
    private int latestPositionSpinnerNotificationErrors;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(getActivity(), APIService.class);
        requireActivity().bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().unbindService(serviceConnection);
        requireActivity().unregisterReceiver(broadcastReceiver);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fileManager = new FileManager(getContext());
        genericToast = new GenericToast(requireActivity(), getLayoutInflater());

        timesNotificationErrors = requireActivity().getResources().getIntArray(R.array.timesNotificationErrors);

        View layoutFragment = inflater.inflate(R.layout.fragment_setting, container, false);

        Toolbar toolbar = layoutFragment.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        toolbar.inflateMenu(R.menu.menu_setting);

        Spinner spinnerNotificationErrors = layoutFragment.findViewById(R.id.spinnerNotificationErrors);

        ArrayAdapter<String> adapterNotificationErrors = new ArrayAdapter<>(getContext(), R.layout.item_spinner, getResources().getStringArray(R.array.spinnerNotificationErrors));
        adapterNotificationErrors.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerNotificationErrors.setAdapter(adapterNotificationErrors);

        /* Searching the position of latest time minutes set, to select the right element into spinner. */
        long savedTimeNotificationErrors = fileManager.readPreferenceNotificationTime(Notification.NAMEFILE, Notification.PREFERENCE_NOTIFICATION_TYPE_DEVICE_TIME);
        for (int t = 0; t < timesNotificationErrors.length; t++) {
            if (timesNotificationErrors[t] == savedTimeNotificationErrors) {
                latestPositionSpinnerNotificationErrors = t;
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
                    fileManager.savePreferenceNotificationTime(Notification.NAMEFILE, Notification.PREFERENCE_NOTIFICATION_TYPE_DEVICE_TIME, timesNotificationErrors[position]);

                    WorkManager workManager = WorkManager.getInstance(requireActivity());
                    PeriodicWorkRequest notificationRequest = new PeriodicWorkRequest
                            .Builder(NotificationDeviceWorker.class, timesNotificationErrors[position], TimeUnit.MINUTES)
                            .build();
                    workManager.enqueueUniquePeriodicWork(NotificationDeviceWorker.tag, REPLACE, notificationRequest);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        TextView textViewVersionName = layoutFragment.findViewById(R.id.textViewVersionName);
        textViewVersionName.setText(BuildConfig.VERSION_NAME);

        TextView textViewDeveloperName = layoutFragment.findViewById(R.id.textViewDeveloperName);
        textViewDeveloperName.setOnClickListener(v -> {
            Intent intentEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
            intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "davidepalladino@hotmail.com" });
            startActivity(intentEmail);
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
        User user = (User) fileManager.readObject(User.NAMEFILE);
        user.password = "";
        fileManager.saveObject(user, User.NAMEFILE);

        apiService.logout(SettingFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGOUT);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            apiService = localBinder.getService();
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
                            // LOGOUT BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(SettingFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGOUT) == 0) {
                                Intent intentTo = new Intent(getActivity(), LoginActivity.class);

                                /* Starting the Login Activity and finishing the hosting Activity for this fragment. */
                                startActivity(intentTo);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }

                            break;

                        case 404:
                        case 500:
                            genericToast.make(R.drawable.ic_error, getString(R.string.toastServerOffline));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };
}