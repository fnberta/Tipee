package ch.berta.fabio.tipee.features.tip.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import ch.berta.fabio.tipee.R

/**
 * A [android.app.DialogFragment] subclass that displays a dialog informing the user that
 * rounding down is not advised because you run the risk of under tipping.

 * @author Fabio Berta
 */
class RoundingDownNotAdvisedDialogFragment : DialogFragment() {

    companion object {
        val tag: String = RoundingDownNotAdvisedDialogFragment::class.java.canonicalName

        fun display(fm: FragmentManager) {
            val dialog = RoundingDownNotAdvisedDialogFragment()
            dialog.show(fm, tag)
        }
    }

    private lateinit var listener: RoundingDownNotAdvisedListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        listener = activity as RoundingDownNotAdvisedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(R.string.round_down_dialog_title)
                .setMessage(R.string.round_down_dialog_message)
                .setPositiveButton(R.string.keep_it) { _, _ -> dismiss() }
                .setNegativeButton(R.string.change_to_up) { _, _ ->
                    listener.setToRoundUp()
                    dismiss()
                }
        return dialogBuilder.create()
    }

    interface RoundingDownNotAdvisedListener {
        fun setToRoundUp()
    }
}