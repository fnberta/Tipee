package ch.berta.fabio.tipee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SettingsActivity extends Activity implements SettingsFragment.SettingsFragmentCallback {

    private String[] mCountryNames;
    private String[] mCountryCodes;

    public String[] getCountryNames() {
        return mCountryNames;
    }

    public String[] getCountryCodes() {
        return mCountryCodes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        mCountryNames = i.getStringArrayExtra("countryNames");
        mCountryCodes = i.getStringArrayExtra("countryCodes");

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
