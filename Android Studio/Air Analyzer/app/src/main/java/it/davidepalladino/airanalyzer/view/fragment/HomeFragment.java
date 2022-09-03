/*
 * This view class provides to show a screen, where the user can see the latest measures of all rooms.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 3rd September, 2022
 *
 */

package it.davidepalladino.airanalyzer.view.fragment;

import static android.content.Context.BIND_AUTO_CREATE;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;
import static it.davidepalladino.airanalyzer.controller.APIService.*;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import java.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.APIService;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.controller.ManageDatetime;
import it.davidepalladino.airanalyzer.model.Measure;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;
import it.davidepalladino.airanalyzer.view.activity.MainActivity;
import it.davidepalladino.airanalyzer.view.widget.LatestMeasuresAdapterView;
import it.davidepalladino.airanalyzer.view.widget.GenericToast;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout linearLayoutNoMeasures;
    private TextView textViewHomeNameSurname;
    private ListView listViewHomeMeasuresToday;

    private FileManager fileManager;
    private GenericToast genericToast;

    private User user;

    private APIService apiService;

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

        fileManager = new FileManager(requireActivity());
        genericToast = new GenericToast(requireActivity(), getLayoutInflater());

        user = User.getInstance();

        textViewHomeNameSurname.setText(String.format("%s %s", user.name, user.surname));
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
        View layoutFragment = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshLayout = layoutFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getLatestDay);

        TextView textViewHomeWelcome = layoutFragment.findViewById(R.id.textViewWelcome);
        linearLayoutNoMeasures = layoutFragment.findViewById(R.id.linearLayoutNoMeasures);
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
        if (apiService != null) {
            getLatestDay();
        }
    }

    /**
     * @brief This method provides to launch the API request to get the latest measures in actual date.
     */
    private void getLatestDay() {
        Calendar calendar = Calendar.getInstance();
        String date = ManageDatetime.createDateFormat(calendar, getString(R.string.formatDateDB));

        apiService.getLatestDay(date, HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST);
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
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            apiService = localBinder.getService();

            getLatestDay();
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
                            // MEASURES TODAY LATEST BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                /* Setting the right visibility for the TextView and the ListView. */
                                ArrayList<Measure> latestMeasures = intentFrom.getParcelableArrayListExtra(SERVICE_BODY);
                                if (latestMeasures.size() > 0) {
                                    linearLayoutNoMeasures.setVisibility(View.GONE);
                                    listViewHomeMeasuresToday.setVisibility(View.VISIBLE);

                                    /* Getting the measures and setting the ListView. */
                                    LatestMeasuresAdapterView adapterViewMeasureToday = new LatestMeasuresAdapterView(requireActivity().getApplicationContext(), latestMeasures);
                                    listViewHomeMeasuresToday.setAdapter(adapterViewMeasureToday);
                                } else {
                                    linearLayoutNoMeasures.setVisibility(View.VISIBLE);
                                    listViewHomeMeasuresToday.setVisibility(View.GONE);
                                }

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                apiService.getMe(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ME);
                                attemptsLogin = 1;
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(HomeFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ME) == 0) {
                                String passwordStored = user.password;
                                User.setInstance(intentFrom.getParcelableExtra(SERVICE_BODY));
                                user.password = passwordStored;
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            /* Checking the attempts for executing another login, or for launching the Login Activity. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(() -> {
                                    apiService.login(user, MainActivity.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                    attemptsLogin++;
                                }, TIME_LOGIN_TIMEOUT);
                            } else {
                                goToLogin(getString(R.string.toastIncorrectUsernamePassword));
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