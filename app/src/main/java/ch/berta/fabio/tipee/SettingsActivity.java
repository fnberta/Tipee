package ch.berta.fabio.tipee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ch.berta.fabio.tipee.dialogs.RoundingDownNotAdvisedDialogFragment;

public class SettingsActivity extends Activity implements
        RoundingDownNotAdvisedDialogFragment.RoundingDownNotAdvisedDialogFragmentInteractionListener {

    private static final String SETTINGS_FRAGMENT = "settingsFragment";

    /**
     * Sets rounding mode in the settings fragment to "Round up".
     */
    public void setToRoundUp() {
        SettingsFragment settingsFragment = (SettingsFragment) getFragmentManager()
                .findFragmentByTag(SETTINGS_FRAGMENT);
        settingsFragment.setToRoundUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get data passed on in intent from MainActivity
        Intent i = getIntent();
        String[] countryNames = i.getStringArrayExtra("countryNames");
        String[] countryCodes = i.getStringArrayExtra("countryCodes");

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, SettingsFragment
                        .newInstance(countryNames, countryCodes), SETTINGS_FRAGMENT)
                .commit();
    }
}
