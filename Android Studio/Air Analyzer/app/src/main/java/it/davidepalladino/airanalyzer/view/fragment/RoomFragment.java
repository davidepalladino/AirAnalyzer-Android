/*
 * This view class provides to show a screen, where the user can see the several measures about a specific room.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.1
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

package it.davidepalladino.airanalyzer.view.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.IBinder;
import android.util.MutableBoolean;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.controller.ManageDatetime;
import it.davidepalladino.airanalyzer.controller.FileManager;
import it.davidepalladino.airanalyzer.model.MeasuresDateAverage;
import it.davidepalladino.airanalyzer.model.MeasuresDateLatest;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.view.activity.AddRoomActivity;
import it.davidepalladino.airanalyzer.view.activity.LoginActivity;
import it.davidepalladino.airanalyzer.view.activity.ManageRoomActivity;
import it.davidepalladino.airanalyzer.view.widget.GeneralToast;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.graphics.Typeface.ITALIC;
import static android.graphics.Typeface.NORMAL;
import static it.davidepalladino.airanalyzer.controller.consts.BroadcastConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.IntentConst.*;
import static it.davidepalladino.airanalyzer.controller.consts.TimesConst.*;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.*;

@SuppressWarnings("deprecation")
@SuppressLint("DefaultLocale")
public class RoomFragment extends Fragment implements View.OnClickListener {
    private Toolbar toolbar;
    private LinearLayout linearLayoutChipRoom;
    private LinearLayout linearLayoutChipDate;
    private LinearLayout linearLayoutNoRoom;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView nestedScrollView;
    private ChipGroup chipGroupRoom;
    private final ArrayList<Chip> chipRoom = new ArrayList<>();
    private ImageView imageViewArrowDate;
    private ImageView imageViewArrowRoom;
    private TextView textViewRoomSelected;
    private TextView textViewDateSelected;
    private TextView textViewRoomLatestTime;
    private TextView textViewRoomLatestTemperature;
    private TextView textViewRoomLatestHumidity;
    private TextView textViewNoticeTemperatureGraph;
    private TextView textViewNoticeHumidityGraph;
    private DatePickerDialog datePickerDialog;
    private BarChart barChartTemperature;
    private BarChart barChartHumidity;

    private GeneralToast generalToast;

    private Calendar date;
    private int utc;

    private FileManager fileManager;
    private User user;
    private Room roomSelected;
    private ArrayList<Room> arrayListRoom;

    private DatabaseService databaseService;

    private DecimalFormat decimalFormat;
    private final MutableBoolean isOpenedMenuDate = new MutableBoolean(false);
    private final MutableBoolean isOpenedMenuRoom = new MutableBoolean(false);

    private byte attemptsLogin = 1;

    public static RoomFragment newInstance() {
        return new RoomFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        generalToast = new GeneralToast(getActivity(), getLayoutInflater());

        fileManager = new FileManager(getContext());
        user = (User) fileManager.readObject(User.NAMEFILE);
        roomSelected = (Room) fileManager.readObject(Room.NAMEFILE);

        date = Calendar.getInstance();
        utc = ManageDatetime.getUTC(date);

        /* Setting the float format for the values of graphs. */
        decimalFormat = new DecimalFormat("##.0");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
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
        View layoutFragment = inflater.inflate(R.layout.fragment_room, container, false);

        toolbar = layoutFragment.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        toolbar.inflateMenu(R.menu.menu_room);

        linearLayoutChipDate = layoutFragment.findViewById(R.id.linearLayoutChipDate);
        linearLayoutChipRoom = layoutFragment.findViewById(R.id.linearLayoutChipRoom);
        linearLayoutNoRoom = layoutFragment.findViewById(R.id.linearLayoutNoRoom);

        swipeRefreshLayout = layoutFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> databaseService.getRooms(user.getAuthorization(), true, RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS));

        nestedScrollView = layoutFragment.findViewById(R.id.nestedScrollView);

        chipGroupRoom = layoutFragment.findViewById(R.id.chipGroupRoom);
        Chip chipDateToday = layoutFragment.findViewById(R.id.chipDateToday);
        chipDateToday.setOnClickListener(this);
        Chip chipDateYesterday = layoutFragment.findViewById(R.id.chipDateYesterday);
        chipDateYesterday.setOnClickListener(this);
        Chip chipDateCustom = layoutFragment.findViewById(R.id.chipDateCustom);
        chipDateCustom.setOnClickListener(this);

        imageViewArrowRoom = layoutFragment.findViewById(R.id.imageViewArrowRoom);
        imageViewArrowRoom.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_arrow_drop_down));
        imageViewArrowRoom.setOnClickListener(this);
        imageViewArrowDate = layoutFragment.findViewById(R.id.imageViewArrowDate);
        imageViewArrowDate.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_arrow_drop_down));
        imageViewArrowDate.setOnClickListener(this);

        textViewRoomSelected = layoutFragment.findViewById(R.id.textViewRoom);
        textViewRoomSelected.setOnClickListener(this);
        textViewDateSelected = layoutFragment.findViewById(R.id.textViewDate);
        textViewDateSelected.setOnClickListener(this);
        textViewRoomLatestTime = layoutFragment.findViewById(R.id.textViewLatestTime);
        textViewRoomLatestTemperature = layoutFragment.findViewById(R.id.textViewLatestTemperature);
        textViewRoomLatestHumidity = layoutFragment.findViewById(R.id.textViewLatestHumidityMeasure);
        textViewNoticeTemperatureGraph = layoutFragment.findViewById(R.id.textViewGraphTemperature);
        textViewNoticeHumidityGraph = layoutFragment.findViewById(R.id.textViewGraphHumidity);

        Button buttonAdd = layoutFragment.findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(this);

        barChartTemperature = layoutFragment.findViewById(R.id.barChartTemperature);
        barChartHumidity = layoutFragment.findViewById(R.id.barChartHumidity);

        barChartTemperature.setNoDataText("");
        barChartHumidity.setNoDataText("");

        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            utc = ManageDatetime.getUTC(date);

            textViewDateSelected.setText(ManageDatetime.createDateFormat(date, getString(R.string.localFormatDate)));

            updateMeasures();
        };

        if (Build.VERSION.SDK_INT > 23) {
            datePickerDialog = new DatePickerDialog(getActivity(), R.style.DatePickerMain);
            datePickerDialog.setOnDateSetListener(onDateSetListener);
        } else {
            datePickerDialog = new DatePickerDialog(
                    getContext(),
                    R.style.DatePickerMain,
                    onDateSetListener ,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            );
        }
        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        /* Setting the current date. */
        textViewDateSelected.setText(ManageDatetime.createDateFormat(Calendar.getInstance(), getString(R.string.localFormatDate)));

        return layoutFragment;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // MANAGE ROOM
        if (id == R.id.menuItemManageRoom) {
            Intent intentTo = new Intent(getActivity(), ManageRoomActivity.class);
            startActivity(intentTo);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // BUTTON ADD
        if (id == R.id.buttonAdd) {
            Intent intentTo = new Intent(getActivity(), AddRoomActivity.class);
            startActivity(intentTo);

        // MENU ROOM
        } else if (id == R.id.textViewRoom || id == R.id.imageViewArrowRoom) {
            if (isOpenedMenuRoom.value) {
                /* Closing the menu room. */
                hideMenu(imageViewArrowRoom, linearLayoutChipRoom, isOpenedMenuRoom);
            } else {
                /* Closing the menu date, if is previously opened. */
                if (isOpenedMenuDate.value) {
                    hideMenu(imageViewArrowDate, linearLayoutChipDate, isOpenedMenuDate);
                }

                /* Opening the menu room.  */
                showMenu(imageViewArrowRoom, linearLayoutChipRoom, isOpenedMenuRoom);
            }

        // MENU DATE
        } else if (id == R.id.textViewDate || id == R.id.imageViewArrowDate) {
            if (isOpenedMenuDate.value) {
                /* Closing the menu date. */
                hideMenu(imageViewArrowDate, linearLayoutChipDate, isOpenedMenuDate);
            } else {
                /* Closing the menu room, if is previously opened. */
                if (isOpenedMenuRoom.value) {
                    hideMenu(imageViewArrowRoom, linearLayoutChipRoom, isOpenedMenuRoom);
                }

                /* Opening the menu date.  */
                showMenu(imageViewArrowDate, linearLayoutChipDate, isOpenedMenuDate);
            }

        // CHIP DATE TODAY
        } else if (id == R.id.chipDateToday) {
            date = Calendar.getInstance();
            utc = ManageDatetime.getUTC(date);

            updateMeasures();

            textViewDateSelected.setText(ManageDatetime.createDateFormat(date, getString(R.string.localFormatDate)));

        // CHIP DATE YESTERDAY
        } else if (id == R.id.chipDateYesterday) {
            date = Calendar.getInstance();
            date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH) - 1);
            utc = ManageDatetime.getUTC(date);

            updateMeasures();

            textViewDateSelected.setText(ManageDatetime.createDateFormat(date, getString(R.string.localFormatDate)));

        // CHIP DATE CUSTOM
        } else if (id == R.id.chipDateCustom) {
            datePickerDialog.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        // CHIP ROOM
        for (Room room:arrayListRoom) {
            if (id == room.id) {
                /* Saving the room selected. */
                roomSelected = room;
                fileManager.saveObject(roomSelected, Room.NAMEFILE);

                makeTextChipRoom(room);
                updateMeasures();

                break;
            }
        }
    }

    /**
     * @brief This method provides to update the measures.
     */
    public void updateMeasures() {
        /* Clearing the BarChart objects. */
        barChartTemperature.clear();
        barChartHumidity.clear();

        String timestamp = ManageDatetime.createDateFormat(date, getString(R.string.timestamp));

        databaseService.getMeasuresDateLatest(user.getAuthorization(), roomSelected.id, timestamp, utc, RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_LATEST + roomSelected.id);
        databaseService.getMeasuresDateAverage(user.getAuthorization(), roomSelected.id, timestamp, utc, RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_AVERAGE + roomSelected.id);
    }

    /**
     * @brief This method provides to create and set the String of room for the Chip.
     * @param room Room object where will be taken the String.
     */
    private void makeTextChipRoom(Room room) {
        if (room.toString().length() > 10) {
            textViewRoomSelected.setText(String.format("%s...", room.toString().substring(0, 9)));
        } else {
            textViewRoomSelected.setText(room.toString());
        }
    }

    /**
     * @brief This method provides to show a specific menu when the Room Fragment is active.
     * @param imageView Image of arrow to change.
     * @param linearLayout Layout of contents where will be changed the status to visible.
     * @param isOpenedMenu Boolean object that will be changed to "true" value.
     */
    private void showMenu(ImageView imageView, LinearLayout linearLayout, MutableBoolean isOpenedMenu) {
        imageView.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_arrow_drop_up));
        linearLayout.setVisibility(View.VISIBLE);

        isOpenedMenu.value = true;
    }

    /**
     * @brief This method provides to hide a specific menu when the Room Fragment is active.
     * @param imageView Image of arrow to change.
     * @param linearLayout Layout of contents where will be changed the status to gone.
     * @param isOpenedMenu Boolean object that will be changed to "false" value.
     */
    private void hideMenu(ImageView imageView, LinearLayout linearLayout, MutableBoolean isOpenedMenu) {
        imageView.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_arrow_drop_down));
        linearLayout.setVisibility(View.GONE);

        isOpenedMenu.value = false;
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

    /**
     * @brief This method provides to generate a Bar Graph for the temperature.
     * @param listMeasures List of measures required for creating the graph.
     */
    private void generateBarGraphTemperature(ArrayList<MeasuresDateAverage> listMeasures) {
        List<BarEntry> measuresTemperatureBarEntry = new ArrayList<>();
        for (MeasuresDateAverage measuresDateLatestRoom : listMeasures) {
            measuresTemperatureBarEntry.add(new BarEntry(measuresDateLatestRoom.hour, measuresDateLatestRoom.temperature));
        }

        BarDataSet temperatureBarDataSet = new BarDataSet(measuresTemperatureBarEntry, "Temperature");
        temperatureBarDataSet.setColor(getResources().getColor(R.color.secondaryColor));
        temperatureBarDataSet.setDrawValues(false);

        /* Set a listener for a selected value. In this case will be launched a Toast with the enclosed measures. */
        barChartTemperature.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                generalToast.make(R.drawable.ic_info, String.format("%s°C %s %02.0f:00", decimalFormat.format(e.getY()), getString(R.string.toastAtThe), e.getX()));
                resetColorBarGraph(barChartTemperature);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        drawBarGraph(barChartTemperature, new BarData((temperatureBarDataSet)));
    }

    /**
     * @brief This method provides to generate a Bar Graph for the humidity.
     * @param listMeasures List of measures required for creating the graph.
     */
    private void generateBarGraphHumidity(ArrayList<MeasuresDateAverage> listMeasures) {
        List<BarEntry> measuresHumidityBarEntry = new ArrayList<>();
        for (MeasuresDateAverage measure : listMeasures) {
            measuresHumidityBarEntry.add(new BarEntry(measure.hour, measure.humidity));
        }

        BarDataSet humidityBarDataSet = new BarDataSet(measuresHumidityBarEntry, "Humidity");
        humidityBarDataSet.setColor(getResources().getColor(R.color.secondaryColor));
        humidityBarDataSet.setDrawValues(false);

        /* Set a listener for a selected value. In this case will be launched a Toast with the enclosed measures. */
        barChartHumidity.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                generalToast.make(R.drawable.ic_info, String.format("%s%% %s %02.0f:00", decimalFormat.format(e.getY()), getString(R.string.toastAtThe), e.getX()));
                resetColorBarGraph(barChartHumidity);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        drawBarGraph(barChartHumidity, new BarData((humidityBarDataSet)));
    }

    /**
     * @brief This method provides to draw the graph.
     * @param graph Graph that will be drawn.
     * @param data Data necessary for drawing.
     */
    private void drawBarGraph(BarChart graph, BarData data) {
        graph.setData(data);

        graph.setHighlightPerDragEnabled(false);
        graph.getLegend().setEnabled(false);
        graph.getDescription().setEnabled(false);

        graph.animateY(500);

        graph.getXAxis().setGranularityEnabled(true);
        graph.getXAxis().setGranularity(1);
        graph.getXAxis().setAxisMinimum(-1);
        graph.getXAxis().setAxisMaximum(24);
        //graph.getXAxis().setAxisMaximum(arrayListMeasuresDateAverage.get(arrayListMeasuresDateAverage.size() - 1).hour + 1);
        graph.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value != -1 && value != 24) {
                    return String.format("%02.0f:00", value);
                } else {
                    return "";
                }
            }
        });

        graph.invalidate();
    }

    /**
     * @brief This method provides to reset the color of item select on the specific BarGraph after certain seconds.
     * @param barChart BarGraph where reset the color.
     */
    private void resetColorBarGraph(BarChart barChart) {
        new Handler().postDelayed(() -> {
            barChart.highlightValue(null);
            //barDataSet.setHighLightColor(getResources().getColor(R.color.secondaryColor));
        }, TIME_RESET_COLOR_BAR_GRAPH);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getRooms(user.getAuthorization(), true, RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS);
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
                            // GET ACTIVE ROOMS BROADCAST
                            if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS) == 0) {
                                swipeRefreshLayout.setRefreshing(false);

                                arrayListRoom = intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE);

                                /* Checking if there is at least one room, to create the Chips object. */
                                if (arrayListRoom.isEmpty()) {
                                    /*
                                     * Hiding the Toolbar, the menus and the report boxes;
                                     *  showing only a message and a button to add a Room.
                                     */
                                    toolbar.setVisibility(View.GONE);

                                    linearLayoutChipRoom.setVisibility(View.GONE);
                                    linearLayoutChipDate.setVisibility(View.GONE);
                                    linearLayoutNoRoom.setVisibility(View.VISIBLE);

                                    nestedScrollView.setVisibility(View.GONE);
                                } else {
                                    /* Showing the Toolbar, the menus and the report boxes. */
                                    toolbar.setVisibility(View.VISIBLE);
                                    linearLayoutNoRoom.setVisibility(View.GONE);
                                    nestedScrollView.setVisibility(View.VISIBLE);

                                    chipGroupRoom.removeAllViews();
                                    chipRoom.clear();

                                    for (Room room:arrayListRoom) {
                                        Chip newChip = new Chip(requireContext());
                                        newChip.setId(room.id);
                                        newChip.setText(room.toString());
                                        newChip.setChipBackgroundColorResource(R.color.secondaryColor);
                                        newChip.setTextColor(getResources().getColor(R.color.secondaryTextColor));
                                        newChip.setTextSize(14);
                                        newChip.setOnClickListener(RoomFragment.this);

                                        chipGroupRoom.addView(newChip);
                                        chipRoom.add(newChip);
                                    }
                                    /* Restoring the latest room selected, or select and set the first and previous room into the list. */
                                    int selected = 0;
                                    if (roomSelected != null) {
                                        /*
                                         * If the ID of room previously selected is contained into some Chip, will be set the TextView dedicated;
                                         *  else, will be select and set a previous room.
                                         */
                                        for (int c = (chipRoom.size() - 1); c >= 0; c--) {
                                            selected = c;
                                            int chipID = chipRoom.get(c).getId();

                                            if (chipID == roomSelected.id) {
                                                break;
                                            }
                                        }
                                    }

                                    makeTextChipRoom(arrayListRoom.get(selected));

                                    /* Saving the room selected. */
                                    roomSelected = arrayListRoom.get(selected);
                                    fileManager.saveObject(roomSelected, Room.NAMEFILE);

                                    updateMeasures();
                                }
                            }

                            // MEASURES DATE LATEST BROADCAST
                            else if (roomSelected != null && intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_LATEST + roomSelected.id) == 0) {
                                MeasuresDateLatest measuresDateLatest = intentFrom.getParcelableExtra(SERVICE_RESPONSE);

                                textViewRoomLatestTime.setTypeface(null, NORMAL);
                                textViewRoomLatestTemperature.setTypeface(null, NORMAL);
                                textViewRoomLatestHumidity.setTypeface(null, NORMAL);

                                textViewRoomLatestTime.setText(measuresDateLatest.time);
                                textViewRoomLatestTemperature.setText(String.format("%s °C", decimalFormat.format(measuresDateLatest.temperature)));
                                textViewRoomLatestHumidity.setText(String.format("%s %%", decimalFormat.format(measuresDateLatest.humidity)));

                            // MEASURES DATE AVERAGE BROADCAST
                            } else if (roomSelected != null && intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_AVERAGE + roomSelected.id) == 0) {
                                ArrayList<MeasuresDateAverage> arrayListMeasuresDateAverage = intentFrom.getParcelableArrayListExtra(SERVICE_RESPONSE);

                                barChartTemperature.setVisibility(View.VISIBLE);
                                barChartHumidity.setVisibility(View.VISIBLE);

                                textViewNoticeTemperatureGraph.setVisibility(View.GONE);
                                textViewNoticeHumidityGraph.setVisibility(View.GONE);

                                generateBarGraphTemperature(arrayListMeasuresDateAverage);
                                generateBarGraphHumidity(arrayListMeasuresDateAverage);

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                user = intentFrom.getParcelableExtra(SERVICE_RESPONSE);
                                fileManager.saveObject(user, User.NAMEFILE);

                                attemptsLogin = 1;
                            }

                            break;
                        case 204:
                            // MEASURES DATE LATEST BROADCAST
                            if (roomSelected != null && intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_LATEST + roomSelected.id) == 0) {
                                textViewRoomLatestTime.setTypeface(null, ITALIC);
                                textViewRoomLatestTemperature.setTypeface(null, ITALIC);
                                textViewRoomLatestHumidity.setTypeface(null, ITALIC);

                                textViewRoomLatestTime.setText(getString(R.string.textViewNone));
                                textViewRoomLatestTemperature.setText(getString(R.string.textViewNone));
                                textViewRoomLatestHumidity.setText(getString(R.string.textViewNone));

                            // MEASURES DATE AVERAGE BROADCAST
                            } else if (roomSelected != null && intentFrom.getStringExtra(BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY).compareTo(RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_AVERAGE + roomSelected.id) == 0) {
                                barChartTemperature.setVisibility(View.GONE);
                                barChartHumidity.setVisibility(View.GONE);

                                textViewNoticeTemperatureGraph.setVisibility(View.VISIBLE);
                                textViewNoticeTemperatureGraph.setText(R.string.noticeGraphNoMeasure);
                                textViewNoticeHumidityGraph.setVisibility(View.VISIBLE);
                                textViewNoticeHumidityGraph.setText(R.string.noticeGraphNoMeasure);
                            }

                            break;

                        // LOGIN BROADCAST
                        case 401:
                            /* Checking the attempts for executing another login, or for launching the Login Activity. */
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(() -> {
                                    databaseService.login(user, RoomFragment.class.getSimpleName() + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                    attemptsLogin++;
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