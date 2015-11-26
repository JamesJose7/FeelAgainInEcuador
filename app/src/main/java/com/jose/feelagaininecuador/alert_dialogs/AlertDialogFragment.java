package com.jose.feelagaininecuador.alert_dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Jose on 11/25/2015.
 */
public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Lo siento!")
                .setMessage("Hubo un error, por favor intentar de nuevo.")
                .setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
