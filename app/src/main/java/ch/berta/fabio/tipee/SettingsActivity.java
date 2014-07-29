package ch.berta.fabio.tipee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ch.berta.fabio.tipee.dialogs.RoundingDownNotAdvisedDialogFragment;

public class SettingsActivity extends Activity implements
        SettingsFragment.SettingsFragmentInteractionListener,
        RoundingDownNotAdvisedDialogFragment.RoundingDownNotAdvisedDialogFragmentInteractionListener {

    private static final String SETTINGS_FRAGMENT = "settingsFragment";

    private String[] mCountryNames;
    private String[] mCountryCodes;

    public String[] getCountryNames() {
        return mCountryNames;
    }

    public String[] getCountryCodes() {
        return mCountryCodes;
    }

    public void setToRoundUp() {
        SettingsFragment settingsFragment = (SettingsFragment) getFragmentManager()
                .findFragmentByTag(SETTINGS_FRAGMENT);
        settingsFragment.setToRoundUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        mCountryNames = i.getStringArrayExtra("countryNames");
        mCountryCodes = i.getStringArrayExtra("countryCodes");

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(), SETTINGS_FRAGMENT)
                .commit();
    }
}
