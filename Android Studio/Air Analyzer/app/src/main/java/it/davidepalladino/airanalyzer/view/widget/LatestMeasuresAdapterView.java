/*
 * This view class provides to show a personal View for the ListView of measures in Main Activity.
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
import it.davidepalladino.airanalyzer.model.Measure;

public class LatestMeasuresAdapterView extends ArrayAdapter<Measure> {
    private final Context context;
    private final int resource;

    public LatestMeasuresAdapterView(@NonNull Context context, @NonNull List<Measure> objects) {
        super(context, R.layout.measures_today_adapter_view, objects);

        this.context = context;
        this.resource = R.layout.measures_today_adapter_view;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);

        TextView textViewRoomNumber = convertView.findViewById(R.id.textViewRoomNumber);
        TextView textViewRoomName = convertView.findViewById(R.id.textViewRoomName);
        TextView textViewTime = convertView.findViewById(R.id.textViewTime_MeasuresToday);
        TextView textViewTemperature = convertView.findViewById(R.id.textViewTemperature);
        TextView textViewHumidity = convertView.findViewById(R.id.textViewHumidity);

        Measure measure = getItem(position);

        /* Setting the float format. */
        DecimalFormat decimalFormat = new DecimalFormat("##.0");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);

        /* Setting the TextViews with the information. */
        textViewRoomNumber.setText(String.valueOf(measure.roomNumber));
        textViewRoomName.setText(measure.roomName);
        textViewTime.setText(measure.when.substring(0, 5));
        textViewTemperature.setText(String.format("%s °C", decimalFormat.format(measure.temperature)));
        textViewHumidity.setText(String.format("%s %%", decimalFormat.format(measure.humidity)));

        return convertView;
    }
}
