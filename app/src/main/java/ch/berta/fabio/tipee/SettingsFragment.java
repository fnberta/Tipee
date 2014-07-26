package ch.berta.fabio.tipee;


import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    private SettingsFragmentCallback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (SettingsFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SeparateBillFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Get variables from SettingsActivity
        CharSequence[] countryEntries = mCallback.getCountryNames();
        CharSequence[] countryCodeValues = mCallback.getCountryCodes();

        // Fill ListPreference with country list as entries and country codes as values, both
        // generated in the MainActivity
        final ListPreference countryList = (ListPreference) findPreference("PREF_COUNTRY_LIST");
        countryList.setEntries(countryEntries);
        countryList.setEntryValues(countryCodeValues);

        // On first app start, set default summary to first country in list
        // Afterwards set summary to last set entry
        if (countryList.getEntry() == null) {
            //countryList.setSummary(countryList.getEntries()[0]);
            countryList.setValueIndex(0);
            countryList.setSummary(countryList.getEntry());
        } else {
            countryList.setSummary(countryList.getEntry());
        }

        // Set up OnPreferenceChangeListener to update the summary with currently selected value
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

    public interface SettingsFragmentCallback {
        public String[] getCountryNames();

        public String[] getCountryCodes();
    }
}