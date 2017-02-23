package ch.berta.fabio.tipee.data.helpers

import android.content.SharedPreferences
import ch.berta.fabio.tipee.features.tip.component.RoundMode

data class SharedPrefs(
        val isCountryManuallySet: () -> Boolean,
        val getSelectedCountry: () -> String,
        val getRoundMode: () -> RoundMode
)

const val PREF_COUNTRY = "PREF_COUNTRY"
const val PREF_COUNTRY_LIST = "PREF_COUNTRY_LIST"
const val PREF_ROUND_MODE = "PREF_ROUND_MODE"

fun makeSharedPrefs(sharedPrefs: SharedPreferences): SharedPrefs = SharedPrefs(
        { sharedPrefs.getBoolean(PREF_COUNTRY, false) },
        { sharedPrefs.getString(PREF_COUNTRY_LIST, "") },
        getRoundMode(sharedPrefs)
)

private fun getRoundMode(sharedPrefs: SharedPreferences): () -> RoundMode = {
    val mode = sharedPrefs.getString(PREF_ROUND_MODE, "1")
    when (mode) {
        "1" -> RoundMode.UP
        "2" -> RoundMode.DOWN
        else -> RoundMode.EXACT
    }
}
