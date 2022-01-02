/*
 * This view class provides to show a screen, where the user can see the latest notifications.
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
import static android.graphics.Typeface.*;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.ManageDatetime;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;
import it.davidepalladino.airanalyzer.view.activity.MainActivity;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;
import it.davidepalladino.airanalyzer.view.widget.NotificationsAdapterView;

public class NotificationFragment extends Fragment {
    private Toolbar toolbar;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewNoNotification;
    private ListView listViewNotificationsLatest;
    private NotificationsAdapterView adapterViewNotificationsLatest;

    private GeneralToast generalToast;

    private FileManager fileManager;
    private User user;
    public ArrayList<Notification> arrayListNotificationsLatest;

    private Calendar date;
    private int utc;

    private DatabaseService databaseService;

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

        generalToast = new GeneralToast(getActivity(), getLayoutInflater());

        fileManager = new FileManager(getContext());
        user = (User) fileManager.readObject(User.NAMEFILE);

        date = Calendar.getInstance();
        utc = ManageDatetime.getUTC(date);
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
        View layoutFragment = inflater.inflate(R.layout.fragment_notification, container, false);

        toolbar = layoutFragment.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        toolbar.inflateMenu(R.menu.menu_notification);

        swipeRefreshLayout = layoutFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                databaseService.getNotificationsLatest(user.getAuthorization(), utc, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST);
            }
        });

        textViewNoNotification = layoutFragment.findViewById(R.id.textViewNoNotification);
        listViewNotificationsLatest = layoutFragment.findViewById(R.id.listViewNotifications);
        listViewNotificationsLatest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notification notificationSelected = arrayListNotificationsLatest.get(position);

                /* Checking if the item selected is unseen. */
                if (notificationSelected.isSeen == 0) {
                    notificationSelected.isSeen = 1;

                    /* Creating the JSON Array to send with the API request. */
                    JsonArray jsonArrayNotifications = new JsonArray();
                    try {
                        jsonArrayNotifications.add(generateJsonObjectNotification(notificationSelected));
                    } catch (JsonIOException e) {
                        e.printStackTrace();
                    }
                    databaseService.setStatusNotifications(user.getAuthorization(), jsonArrayNotifications, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSIONS_SET_STATUS_NOTIFICATIONS);

                    /* Updating the texts style of TextView and store the status for next update of database. */
                    TextView textViewType = view.findViewById(R.id.textViewType);
                    TextView textViewMessage = view.findViewById(R.id.textViewMessage);
                    textViewType.setTypeface(null, NORMAL);
                    textViewMessage.setTypeface(null, NORMAL);
                }
            }
        });

        return layoutFragment;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // MARK ALL READ
        if (id == R.id.menuItemMarkAllRead) {
            JsonArray jsonArrayNotifications = new JsonArray();
            try {
                /* Creating the JSON Array to send with the API request. */
                for (Notification notificationSelected:arrayListNotificationsLatest) {
                    if (notificationSelected.isSeen == 0) {
                        notificationSelected.isSeen = 1;
                        jsonArrayNotifications.add(generateJsonObjectNotification(notificationSelected));
                    }
                }
            } catch (JsonIOException e) {
                e.printStackTrace();
            }

            databaseService.setStatusNotifications(user.getAuthorization(), jsonArrayNotifications, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSIONS_SET_STATUS_NOTIFICATIONS);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @brief This method provides to generate a JSON message for sending to the server, about the updating of notification status in seen.
     * @param notificationSelected Notification that you want to update
     * @return JSON message with the information.
     */
    @NonNull
    private JsonObject generateJsonObjectNotification(Notification notificationSelected) {
        JsonObject jsonObjectNotification = new JsonObject();
        jsonObjectNotification.addProperty("ID", notificationSelected.id);
        jsonObjectNotification.addProperty("IsSeen", 1);
        return jsonObjectNotification;
    }

//TODO
    public void updateList(ArrayList<Notification> arrayListNotificationsLatest) {
        this.arrayListNotificationsLatest = arrayListNotificationsLatest;

        /* Setting the right visibility for the TextView and the ListView. */
        textViewNoNotification.setVisibility(View.GONE);
        listViewNotificationsLatest.setVisibility(View.VISIBLE);

        /* Updating the ListView with the AdapterView. */
        adapterViewNotificationsLatest = new NotificationsAdapterView(requireActivity().getApplicationContext(), arrayListNotificationsLatest);
        listViewNotificationsLatest.setAdapter(adapterViewNotificationsLatest);
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
            getActivity().finish();
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getNotificationsLatest(user.getAuthorization(), utc, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST);
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
                            // NOTIFICATIONS LATEST BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                /* Getting the measures and updating the ListView. */
                                updateList(intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE));

                                if (getContext() instanceof MainActivity) {
                                    ((MainActivity) getContext()).updateBadge(arrayListNotificationsLatest);
                                }

                            // SET STATUS NOTIFICATIONS BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSIONS_SET_STATUS_NOTIFICATIONS) == 0) {
                                adapterViewNotificationsLatest.notifyDataSetChanged();

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                user = (User) intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                                fileManager.saveObject(user, User.NAMEFILE);

                                attemptsLogin = 1;
                            }

                            break;
                        case 204:
                        // NOTIFICATIONS LATEST BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                /* Setting the right visibility for the TextView and the ListView. */
                                textViewNoNotification.setVisibility(View.VISIBLE);
                                listViewNotificationsLatest.setVisibility(View.GONE);
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            /* Checking the attempts for executing another login, or for launching the Login Activity. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        databaseService.login(user, Notification.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
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