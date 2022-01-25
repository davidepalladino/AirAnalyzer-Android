/*
 * This view class provides to show a personal View for the ListView of measures in Main Activity.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.1
 * @date 25th January, 2022
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

package it.davidepalladino.airanalyzer.view.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.model.MeasuresTodayLatest;

public class MeasuresTodayAdapterView extends ArrayAdapter<MeasuresTodayLatest> {
    private final Context context;
    private final int resource;

    public MeasuresTodayAdapterView(@NonNull Context context, @NonNull List<MeasuresTodayLatest> objects) {
        super(context, R.layout.measures_today_adapter_view, objects);

        this.context = context;
        this.resource = R.layout.measures_today_adapter_view;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);

        TextView textViewID = convertView.findViewById(R.id.textViewID);
        TextView textViewName = convertView.findViewById(R.id.textViewName);
        TextView textViewTime = convertView.findViewById(R.id.textViewTime_MeasuresToday);
        TextView textViewTemperature = convertView.findViewById(R.id.textViewTemperature);
        TextView textViewHumidity = convertView.findViewById(R.id.textViewHumidity);

        MeasuresTodayLatest measuresTodayLatest = getItem(position);

        /* Setting the float format. */
        DecimalFormat decimalFormat = new DecimalFormat("##.0");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);

        /* Setting the TextViews with the information. */
        textViewID.setText(String.valueOf(measuresTodayLatest.id));
        textViewName.setText(measuresTodayLatest.name);
        textViewTime.setText(measuresTodayLatest.time);
        textViewTemperature.setText(String.format("%s °C", decimalFormat.format(measuresTodayLatest.temperature)));
        textViewHumidity.setText(String.format("%s %%", decimalFormat.format(measuresTodayLatest.humidity)));

        return convertView;
    }
}
