package ch.berta.fabio.tipee.features.tip.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import ch.berta.fabio.tipee.R

/**
 * A [android.app.DialogFragment] subclass that displays a dialog informing the user that
 * the selectedCountry could not be automatically detected.

 * @author Fabio Berta
 */
class CountryNotDetectedDialogFragment : DialogFragment() {

    companion object {
        val tag: String = CountryNotDetectedDialogFragment::class.java.canonicalName

        fun display(fm: FragmentManager) {
            val dialog = CountryNotDetectedDialogFragment()
            dialog.show(fm, tag)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle(R.string.country_not_detected_dialog_title)
                .setMessage(R.string.country_not_detected_dialog_message)
                .setPositiveButton(R.string.ok) { _, _ -> dismiss() }
        return dialogBuilder.create()
    }
}
