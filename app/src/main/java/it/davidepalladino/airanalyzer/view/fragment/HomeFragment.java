/*
 * This view class provides to show a screen, where the user can see the latest measures of all rooms.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 1.0.0
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

import static android.content.Context.BIND_AUTO_CREATE;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.Consts.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.MeasuresTodayLatest;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;
import it.davidepalladino.airanalyzer.view.widget.MeasuresTodayAdapterView;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewNoMeasures;
    private TextView textViewHomeNameSurname;
    private ListView listViewHomeMeasuresToday;

    private GeneralToast generalToast;

    private FileManager fileManager;
    private User user;

    private DatabaseService databaseService;

    private byte attemptsLogin = 1;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        generalToast = new GeneralToast(getActivity(), getLayoutInflater());

        fileManager = new FileManager(getContext());

        user = (User) fileManager.readObject(User.NAMEFILE);
        textViewHomeNameSurname.setText(String.format("%s %s", user.name, user.surname));
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(getActivity(), DatabaseService.class);
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
        View layoutFragment = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = layoutFragment.findViewById(R.id.toolbar);

        swipeRefreshLayout = layoutFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                databaseService.getMeasuresTodayLatest(user.getAuthorization(), HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST);
            }
        });

        TextView textViewHomeWelcome = layoutFragment.findViewById(R.id.textViewWelcome);
        textViewNoMeasures = layoutFragment.findViewById(R.id.textViewNoMeasures);
        textViewHomeNameSurname = layoutFragment.findViewById(R.id.textViewNameSurname);
        listViewHomeMeasuresToday = layoutFragment.findViewById(R.id.listViewMeasuresToday);

        Calendar datetimeActual = Calendar.getInstance();
        byte hour = (byte) datetimeActual.get(Calendar.HOUR_OF_DAY);
        if (hour > 6 && hour <= 13) {
            textViewHomeWelcome.setText(R.string.textViewWelcomeGoodMorning);
        } else if (hour > 13 && hour <= 17) {
            textViewHomeWelcome.setText(R.string.textViewWelcomeGoodAfternoon);
        } else {
            textViewHomeWelcome.setText(R.string.textViewWelcomeGoodEvening);
        }

        return layoutFragment;
    }

    /**
     * @brief This method provides to update the measures.
     */
    public void updateMeasures() {
        /*
         * Checking the existence of ServiceDatabase object, to prevent the crash of the application
         *  on the premature call of this method by the MainActivity.
         */
        if (databaseService != null) {
            databaseService.getMeasuresTodayLatest(user.getAuthorization(), HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST);
        }
    }

    /**
     * @brief This method provides to launch the Login Activity, if the credentials are bad or if
     *  there is a logout request.
     * @param toastMessage A message for Toast. The value can be "null", if you wish; in this case
     *  there will be not any Toast.
     */
    private void goToLogin(String toastMessage) {
        /* Deleting the information for the login. */
        user.password = "";
        user.token = "";
        fileManager.saveObject(user, User.NAMEFILE);

        Intent intentTo = new Intent(getActivity(), LoginActivity.class);

        if (toastMessage != null) {
            intentTo.putExtra(INTENT_TOAST_MESSAGE, getString(R.string.toastUserError));
        }

        /* Starting the Login Activity and finishing the hosting Activity for this fragment. */
        startActivity(intentTo);
        if (getActivity() != null) {
            requireActivity().finish();
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getMeasuresTodayLatest(user.getAuthorization(), HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST);
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
                            // MEASURES TODAY LATEST BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                /* Setting the right visibility for the TextView and the ListView. */
                                textViewNoMeasures.setVisibility(View.GONE);
                                listViewHomeMeasuresToday.setVisibility(View.VISIBLE);

                                /* Getting the measures and setting the ListView. */
                                ArrayList<MeasuresTodayLatest> measuresTodayLatest = intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE);
                                MeasuresTodayAdapterView adapterViewMeasureToday = new MeasuresTodayAdapterView(requireActivity().getApplicationContext(), measuresTodayLatest);
                                listViewHomeMeasuresToday.setAdapter(adapterViewMeasureToday);

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                user = (User) intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                                fileManager.saveObject(user, User.NAMEFILE);

                                attemptsLogin = 1;
                            }

                            break;
                        case 204:
                            // MEASURES TODAY LATEST BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                /* Setting the right visibility for the TextView and the ListView. */
                                textViewNoMeasures.setVisibility(View.VISIBLE);
                                listViewHomeMeasuresToday.setVisibility(View.GONE);
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            /* Checking the attempts for executing another login, or for launching the Login Activity. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        databaseService.login(user, HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                        attemptsLogin++;
                                    }
                                }, TIME_LOGIN_TIMEOUT);
                            } else {
                                goToLogin(getString(R.string.toastUserError));
                            }

                            break;
                        case 404:
                        case 500:
                            generalToast.make(R.drawable.ic_error, getString(R.string.toastServerOffline));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };
}