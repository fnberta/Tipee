package ch.berta.fabio.tipee.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ch.berta.fabio.tipee.R;
import ch.berta.fabio.tipee.ui.dialogs.RoundingDownNotAdvisedDialogFragment;

import static ch.berta.fabio.tipee.AppConstants.INTENT_COUNTRY_CODES;
import static ch.berta.fabio.tipee.AppConstants.INTENT_COUNTRY_NAMES;

public class SettingsActivity extends AppCompatActivity implements
        RoundingDownNotAdvisedDialogFragment.RoundingDownNotAdvisedDialogFragmentInteractionListener {

    private static final String SETTINGS_FRAGMENT = "settings_fragment";

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
        String[] countryNames = i.getStringArrayExtra(INTENT_COUNTRY_NAMES);
        String[] countryCodes = i.getStringArrayExtra(INTENT_COUNTRY_CODES);

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, SettingsFragment
                            .newInstance(countryNames, countryCodes), SETTINGS_FRAGMENT)
                    .commit();
        }
    }
}
