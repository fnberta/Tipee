package ch.berta.fabio.tipee.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import ch.berta.fabio.tipee.R;
import ch.berta.fabio.tipee.dialogs.RoundingDownNotAdvisedDialogFragment;

public class SettingsActivity extends ActionBarActivity implements
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
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get data passed on in intent from MainActivity
        Intent i = getIntent();
        String[] countryNames = i.getStringArrayExtra("countryNames");
        String[] countryCodes = i.getStringArrayExtra("countryCodes");

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, SettingsFragment
                            .newInstance(countryNames, countryCodes), SETTINGS_FRAGMENT)
                    .commit();
        }
    }
}
