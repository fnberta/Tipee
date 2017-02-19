package ch.berta.fabio.tipee.extensions

import android.support.v7.preference.Preference
import android.widget.EditText
import android.widget.ProgressBar
import rx.Emitter
import rx.Observable

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

data class PreferenceChange<out T : Preference>(val preference: T, val newValue: Any)

fun <T : Preference> T.preferenceChanges(): Observable<PreferenceChange<T>> =
        Observable.fromEmitter<PreferenceChange<T>>(
                { emitter ->
                    this.setOnPreferenceChangeListener { preference, newValue ->
                        @Suppress("UNCHECKED_CAST")
                        emitter.onNext(PreferenceChange(preference as T, newValue))
                        true
                    }
                }, Emitter.BackpressureMode.LATEST)
