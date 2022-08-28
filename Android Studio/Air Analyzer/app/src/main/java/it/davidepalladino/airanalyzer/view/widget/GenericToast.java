/*
 * This view class provides to show a personal Toast for this application.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 29th October, 2021
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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.davidepalladino.airanalyzer.R;

public class GenericToast {
    private final Activity activity;
    private final LayoutInflater layoutInflater;

    /**
     * @brief This constructor provides to create the object setting only the applicant Activity and Layout Inflater.
     * @param activity Applicant Activity.
     * @param layoutInflater Applicant Layout Inflater
     */
    public GenericToast(Activity activity, LayoutInflater layoutInflater) {
        this.activity = activity;
        this.layoutInflater = layoutInflater;
    }

    /**
     * @brief This method provides to make a Toast with black background.
     * @param icon Icon to visualize.
     * @param message Message to visualize.
     */
    public void make(int icon, String message) {
        View layout = layoutInflater.inflate(R.layout.toast_general, activity.findViewById(R.id.linearLayout));

        ImageView imageViewTypeMessageToast = layout.findViewById(R.id.imageViewIcon);
        TextView textViewMessageToast = layout.findViewById(R.id.textViewMessage);

        imageViewTypeMessageToast.setImageResource(icon);
        textViewMessageToast.setText(message);

        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
