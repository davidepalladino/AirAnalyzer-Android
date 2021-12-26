package it.davidepalladino.airanalyzer.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import it.davidepalladino.airanalyzer.R;

public class SignupDialog extends DialogFragment {
    private Activity activity;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_signup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout)
                .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }
                });
        return builder.create();
    }

    public void setActivity(Activity activity) { this.activity = activity; }
}
