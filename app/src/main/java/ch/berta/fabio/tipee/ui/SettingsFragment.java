package ch.berta.fabio.tipee.ui;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ch.berta.fabio.tipee.R;
import ch.berta.fabio.tipee.dialogs.RoundingDownNotAdvisedDialogFragment;

public class SettingsFragment extends PreferenceFragment {

    private static final String COUNTRY_ENTRIES = "country_entries";
    private static final String COUNTRY_CODE_VALUES = "country_code_values";
    private static final int ROUND_UP = 1;
    private static final int ROUND_DOWN = 2;
    private ListPreference mRoundMode;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(CharSequence[] countryEntries,
                                               CharSequence[] countryCodeValues) {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putCharSequenceArray(COUNTRY_ENTRIES, countryEntries);
        args.putCharSequenceArray(COUNTRY_CODE_VALUES, countryCodeValues);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        mRoundMode = (ListPreference) findPreference("PREF_ROUND_MODE");

        setupCountryListPreference();

        setupRoundModeListPreference();
    }

    private void setupRoundModeListPreference() {
        mRoundMode.setSummary(mRoundMode.getEntry());
        mRoundMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                int index = mRoundMode.findIndexOfValue(o.toString());

                if (index >= 0) {
                    preference.setSummary(mRoundMode.getEntries()[index]);
                } else {
                    preference.setSummary(null);
                }

                if (index == ROUND_DOWN) {
                    RoundingDownNotAdvisedDialogFragment roundingDownNotAdvisedDialog =
                            new RoundingDownNotAdvisedDialogFragment();
                    roundingDownNotAdvisedDialog.show(getFragmentManager(), "Alert_Dialog");
                }

                return true;
            }
        });
    }

    public void setToRoundUp() {
        mRoundMode.setValue(Integer.toString(ROUND_UP));
        mRoundMode.setSummary(mRoundMode.getEntry());
    }

    private void setupCountryListPreference() {
        // Get variables from NewInstance Bundle
        CharSequence[] countryEntries = getArguments().getCharSequenceArray(COUNTRY_ENTRIES);
        CharSequence[] countryCodeValues = getArguments().getCharSequenceArray(COUNTRY_CODE_VALUES);

        // Fill ListPreference with country list as entries and country codes as values, both
        // generated in the MainActivity
        final ListPreference countryList = (ListPreference) findPreference("PREF_COUNTRY_LIST");
        countryList.setEntries(countryEntries);
        countryList.setEntryValues(countryCodeValues);

        // If nothing is selected, default to the first entry.
        if (countryList.getEntry() == null) {
            countryList.setValueIndex(0);
        }
        // Set summary to the current Entry
        countryList.setSummary(countryList.getEntry());

        // Update the summary with currently selected value
        countryList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                int index = countryList.findIndexOfValue(o.toString());

                if (index >= 0) {
                    preference.setSummary(countryList.getEntries()[index]);
                } else {
                    preference.setSummary(null);
                }

                return true;
            }
        });
    }
}