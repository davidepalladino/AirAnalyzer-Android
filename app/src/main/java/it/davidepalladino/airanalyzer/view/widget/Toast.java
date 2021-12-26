package it.davidepalladino.airanalyzer.view.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.davidepalladino.airanalyzer.R;

public class Toast {
    private Activity activity;
    private LayoutInflater layoutInflater;

    public Toast(Activity activity, LayoutInflater layoutInflater) {
        this.activity = activity;
        this.layoutInflater = layoutInflater;
    }

    public void makeToastBlack(int image, String message) {
        View layout = layoutInflater.inflate(R.layout.toast_black, (ViewGroup) activity.findViewById(R.id.linearLayoutToast));

        ImageView imageViewTypeMessageToast = (ImageView) layout.findViewById(R.id.imageViewTypeMessageToast);
        TextView textViewMessageToast = (TextView) layout.findViewById(R.id.textViewMessageToast);

        imageViewTypeMessageToast.setImageResource(image);
        textViewMessageToast.setText(message);

        android.widget.Toast toast = new android.widget.Toast(activity);
        toast.setDuration(android.widget.Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void makeToastBlue(int image, String message) {
        View layout = layoutInflater.inflate(R.layout.toast_blue, (ViewGroup) activity.findViewById(R.id.linearLayoutToast));

        ImageView imageViewTypeMessageToast = (ImageView) layout.findViewById(R.id.imageViewTypeMessageToast);
        TextView textViewMessageToast = (TextView) layout.findViewById(R.id.textViewMessageToast);

        imageViewTypeMessageToast.setImageResource(image);
        textViewMessageToast.setText(message);

        android.widget.Toast toast = new android.widget.Toast(activity);
        toast.setDuration(android.widget.Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void makeToastBlueMeasure(int image, String dateString, String dateValue, String measureType, String MeasureValue) {
        View layout = layoutInflater.inflate(R.layout.toast_blue_measure, (ViewGroup) activity.findViewById(R.id.linearLayoutToast));

        ImageView imageViewTypeMessageToast = (ImageView) layout.findViewById(R.id.imageViewTypeMessageToast);

        TextView textViewDateString = (TextView) layout.findViewById(R.id.textViewDateString);
        TextView textViewDateValue = (TextView) layout.findViewById(R.id.textViewDateValue);
        TextView textViewMeasureType = (TextView) layout.findViewById(R.id.textViewMeasureType);
        TextView textViewMeasureValue = (TextView) layout.findViewById(R.id.textViewMeasureValue);

        imageViewTypeMessageToast.setImageResource(image);
        textViewDateString.setText(dateString);
        textViewDateValue.setText(dateValue);
        textViewMeasureType.setText(measureType);
        textViewMeasureValue.setText(MeasureValue);

        android.widget.Toast toast = new android.widget.Toast(activity);
        toast.setDuration(android.widget.Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
