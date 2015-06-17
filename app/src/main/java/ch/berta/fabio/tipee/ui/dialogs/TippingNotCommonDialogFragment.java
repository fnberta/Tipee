package ch.berta.fabio.tipee.ui.dialogs;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ch.berta.fabio.tipee.R;

/**
 * A {@link android.app.DialogFragment} subclass that displays a dialog informing the user that
 * tipping is not common in the country selected by the user.
 *
 * @author Fabio Berta
 */
public class TippingNotCommonDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.tipping_not_common_dialog_title)
                .setMessage(R.string.tipping_not_common_dialog_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return dialogBuilder.create();
    }
}