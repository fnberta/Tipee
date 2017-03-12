package ch.berta.fabio.tipee.extensions

import android.support.v7.preference.Preference
import android.widget.*
import io.reactivex.Observable

fun TextView.setTextIfNotEqual(text: CharSequence) {
    if (this.text != text) {
        this.text = text
    }
}

fun EditText.setTextIfNotEqual(text: CharSequence) {
    if (this.text.toString() != text.toString()) {
        this.setText(text)
    }
}

fun ProgressBar.setProgressIfNotEqual(progress: Int) {
    if (this.progress != progress) {
        this.progress = progress
    }
}

fun <T> ArrayAdapter<T>.addAllIfEmpty(items: List<T>) {
    if (this.isEmpty && items.isNotEmpty()) {
        this.addAll(items)
    }
}

fun <T : Adapter> AdapterView<T>.setSelectionIfNotSelected(selectedItemPosition: Int) {
    if (selectedItemPosition != this.selectedItemPosition) {
        this.setSelection(selectedItemPosition)
    }
}

data class PreferenceChange<out T : Preference>(val preference: T, val newValue: Any)

fun <T : Preference> T.preferenceChanges(): Observable<PreferenceChange<T>> =
        Observable.create {
            this.setOnPreferenceChangeListener { preference, newValue ->
                @Suppress("UNCHECKED_CAST")
                it.onNext(PreferenceChange(preference as T, newValue))
                true
            }
        }
