package ch.berta.fabio.tipee.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ch.berta.fabio.tipee.R;

/**
 * A {@link android.app.DialogFragment} subclass that displays a dialog informing the user that
 * the country could not be automatically detected.
 *
 * @author Fabio Berta
 */
public class CountryNotDetectedDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.country_not_detected_dialog_title)
                .setMessage(R.string.country_not_detected_dialog_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return dialogBuilder.create();
    }
}
