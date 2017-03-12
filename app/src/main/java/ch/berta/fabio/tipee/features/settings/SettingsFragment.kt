package ch.berta.fabio.tipee.features.settings

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.data.helpers.PREF_COUNTRY
import ch.berta.fabio.tipee.data.helpers.PREF_COUNTRY_LIST
import ch.berta.fabio.tipee.data.helpers.PREF_ROUND_MODE
import ch.berta.fabio.tipee.data.models.makeCountryMappings
import ch.berta.fabio.tipee.features.tip.dialogs.RoundingDownNotAdvisedDialogFragment

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val RESULT_ROUND_CHANGED = 2
        const val RESULT_COUNTRY_CHANGED = 3
        val tag: String = SettingsFragment::class.java.canonicalName

        fun add(fm: FragmentManager) {
            fm.beginTransaction()
                    .add(R.id.container, SettingsFragment(), tag)
                    .commit()
        }

        fun find(fm: FragmentManager): SettingsFragment =
                fm.findFragmentByTag(SettingsFragment.tag) as SettingsFragment
    }

    private val ROUND_UP = 1
    private val ROUND_DOWN = 2

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupCountryPreference()
        setupCountryListPreference()
        setupRoundModeListPreference()
    }

    private fun setupCountryPreference() {
        val countryPref = findPreference(PREF_COUNTRY) as CheckBoxPreference
        countryPref.setOnPreferenceChangeListener { _, _ ->
            activity.setResult(RESULT_COUNTRY_CHANGED)
            true
        }
    }

    private fun setupCountryListPreference() {
        val countries = makeCountryMappings(resources)()

        val countriesPref = findPreference(PREF_COUNTRY_LIST) as ListPreference
        countriesPref.entries = countries.map { it.name }.toTypedArray()
        countriesPref.entryValues = countries.map { it.countryCode }.toTypedArray()

        // If nothing is selected, default to the first entry.
        if (countriesPref.entry == null) {
            countriesPref.setValueIndex(0)
        }
        // Set summary to the current Entry
        countriesPref.summary = countriesPref.entry

        // Update the summary with currently selected value
        countriesPref.setOnPreferenceChangeListener { preference, newValue ->
            val index = countriesPref.findIndexOfValue(newValue.toString())
            preference.summary = if (index >= 0) countriesPref.entries[index] else null

            activity.setResult(RESULT_COUNTRY_CHANGED)
            true
        }
    }

    private fun setupRoundModeListPreference() {
        val roundModePref = findPreference(PREF_ROUND_MODE) as ListPreference
        roundModePref.summary = roundModePref.entry
        roundModePref.setOnPreferenceChangeListener { preference, newValue ->
            val index = roundModePref.findIndexOfValue(newValue.toString())
            preference.summary = if (index >= 0) roundModePref.entries[index] else null

            if (index == ROUND_DOWN) {
                RoundingDownNotAdvisedDialogFragment.display(fragmentManager)
            }

            activity.setResult(RESULT_ROUND_CHANGED)
            true
        }
    }

    fun setToRoundUp() {
        val roundModePref = findPreference(PREF_ROUND_MODE) as ListPreference
        roundModePref.value = ROUND_UP.toString()
        roundModePref.summary = roundModePref.entry
    }
}