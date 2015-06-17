package ch.berta.fabio.tipee.ui.dialogs;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ch.berta.fabio.tipee.R;

/**
 * A {@link android.app.DialogFragment} subclass that displays a dialog informing the user that
 * rounding down is not advised because you run the risk of under tipping.
 *
 * @author Fabio Berta
 */
public class RoundingDownNotAdvisedDialogFragment extends DialogFragment {

    private RoundingDownNotAdvisedDialogFragmentInteractionListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (RoundingDownNotAdvisedDialogFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SeparateBillFragmentCallback");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.round_down_dialog_title)
                .setMessage(R.string.round_down_dialog_message)
                .setPositiveButton(R.string.keep_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.change_to_up, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.setToRoundUp();
                        dismiss();
                    }
                });
        return dialogBuilder.create();
    }

    public interface RoundingDownNotAdvisedDialogFragmentInteractionListener {
        void setToRoundUp();
    }
}