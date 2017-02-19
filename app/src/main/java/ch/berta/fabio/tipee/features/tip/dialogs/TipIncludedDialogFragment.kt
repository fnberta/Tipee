package ch.berta.fabio.tipee.features.tip.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import ch.berta.fabio.tipee.R

/**
 * A [android.app.DialogFragment] subclass that displays a dialog informing the user that
 * tipping is not common in the selectedCountry selected by the user.

 * @author Fabio Berta
 */
class TipIncludedDialogFragment : DialogFragment() {

    companion object {
        val tag: String = TipIncludedDialogFragment::class.java.canonicalName

        fun display(fm: FragmentManager) {
            val dialog = TipIncludedDialogFragment()
            dialog.show(fm, tag)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(R.string.tip_included_dialog_title)
                .setMessage(R.string.tip_included_dialog_message)
                .setPositiveButton(R.string.ok) { _, _ -> dismiss() }
        return dialogBuilder.create()
    }
}