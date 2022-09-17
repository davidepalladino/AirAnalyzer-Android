/*
 * This view class provides to show a screen, where the user can see the latest notifications.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 2.0.0
 * @date 16th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.view.fragment;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.graphics.Typeface.*;
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
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
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
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;
import it.davidepalladino.airanalyzer.view.activity.MainActivity;
import it.davidepalladino.airanalyzer.view.widget.GenericToast;
import it.davidepalladino.airanalyzer.view.widget.NotificationsAdapterView;

public class NotificationFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout linearLayoutNoNotification;
    private ListView listViewNotificationsLatest;
    private NotificationsAdapterView adapterViewNotificationsLatest;

    private FileManager fileManager;
    private GenericToast genericToast;

    private User user;
    public ArrayList<Notification> arrayListNotificationsLatest;

    private APIService apiService;

    private int offsetNotifications = 0;
    private int limitNotifications = 100;
    private byte attemptsLogin = 1;

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        genericToast = new GenericToast(getActivity(), getLayoutInflater());
        fileManager = new FileManager(getContext());

        user = User.getInstance();
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
        View layoutFragment = inflater.inflate(R.layout.fragment_notification, container, false);

        Toolbar toolbar = layoutFragment.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        toolbar.inflateMenu(R.menu.menu_notification);
        toolbar.setVisibility(View.GONE);               // Delete it when there is least one item of menu.

        swipeRefreshLayout = layoutFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> apiService.getAllNotifications(offsetNotifications, limitNotifications, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL, null));

        linearLayoutNoNotification = layoutFragment.findViewById(R.id.linearLayoutNoNotification);
        listViewNotificationsLatest = layoutFragment.findViewById(R.id.listViewNotifications);
        listViewNotificationsLatest.setOnItemClickListener((parent, view, position, id) -> {
            Notification notificationSelected = arrayListNotificationsLatest.get(position);

            /* Checking if the item selected is unseen. */
            if (notificationSelected.isSeen == 0) {
                apiService.changeStatusViewNotification(notificationSelected.id, true, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_CHANGE_STATUS_VIEW);

                /* Updating the texts style of TextView and store the status for next update of database. */
                TextView textViewType = view.findViewById(R.id.textViewType);
                TextView textViewMessage = view.findViewById(R.id.textViewMessage);
                textViewType.setTypeface(null, NORMAL);
                textViewMessage.setTypeface(null, NORMAL);
            }
        });

        return layoutFragment;
    }

    /**
     * @brief This method provides to update the ListView.
     * @param arrayListNotificationsLatest List of elements to use for the ListView.
     */
    public void updateListView(ArrayList<Notification> arrayListNotificationsLatest) {
        this.arrayListNotificationsLatest = arrayListNotificationsLatest;

        /* Setting the right visibility for the TextView and the ListView. */
        if (arrayListNotificationsLatest.isEmpty()) {
            linearLayoutNoNotification.setVisibility(View.VISIBLE);
            listViewNotificationsLatest.setVisibility(View.GONE);
        } else {
            linearLayoutNoNotification.setVisibility(View.GONE);
            listViewNotificationsLatest.setVisibility(View.VISIBLE);

            /* Updating the ListView with the AdapterView. */
            adapterViewNotificationsLatest = new NotificationsAdapterView(requireActivity().getApplicationContext(), arrayListNotificationsLatest);
            listViewNotificationsLatest.setAdapter(adapterViewNotificationsLatest);
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
        fileManager.saveObject(user, User.NAMEFILE);

        Intent intentTo = new Intent(getActivity(), LoginActivity.class);

        if (toastMessage != null) {
            intentTo.putExtra(INTENT_TOAST_MESSAGE, getString(R.string.toastUserError));
        }

        /* Starting the Login Activity and finishing the hosting Activity for this fragment. */
        startActivity(intentTo);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APIService.LocalBinder localBinder = (APIService.LocalBinder) service;
            apiService = localBinder.getService();

            apiService.getAllNotifications(offsetNotifications, limitNotifications, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL, null);
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
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                /* Getting the measures and updating the ListView. */
                                updateListView(intentFrom.getParcelableArrayListExtra(SERVICE_BODY));

                                if (getContext() instanceof MainActivity) {
                                    ((MainActivity) getContext()).updateBadge(arrayListNotificationsLatest);
                                }

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN) == 0) {
                                apiService.getMe(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_GET_ME);
                                attemptsLogin = 1;
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_GET_ME) == 0) {
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
                                    apiService.login(user, NotificationFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN);
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