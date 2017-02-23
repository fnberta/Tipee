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
class TippingNotCommonDialogFragment : DialogFragment() {

    companion object {
        val tag: String = TippingNotCommonDialogFragment::class.java.canonicalName

        fun display(fm: FragmentManager) {
            val dialog = TippingNotCommonDialogFragment()
            dialog.show(fm, tag)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(R.string.dialog_tipping_not_common_title)
                .setMessage(R.string.dialog_tipping_not_common_message)
                .setPositiveButton(android.R.string.ok) { _, _ -> dismiss() }
        return dialogBuilder.create()
    }
}