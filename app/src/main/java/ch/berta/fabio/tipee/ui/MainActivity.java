package ch.berta.fabio.tipee.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.berta.fabio.tipee.R;
import ch.berta.fabio.tipee.ui.adapters.TabsAdapter;
import ch.berta.fabio.tipee.ui.dialogs.CountryNotDetectedDialogFragment;
import ch.berta.fabio.tipee.ui.dialogs.TipIncludedDialogFragment;
import ch.berta.fabio.tipee.ui.dialogs.TippingNotCommonDialogFragment;

import static ch.berta.fabio.tipee.AppConstants.INTENT_COUNTRY_CODES;
import static ch.berta.fabio.tipee.AppConstants.INTENT_COUNTRY_NAMES;
import static ch.berta.fabio.tipee.AppConstants.LOG_TAG;
import static ch.berta.fabio.tipee.AppConstants.MAX_PERSONS;

public class MainActivity extends AppCompatActivity implements
        SplitFragment.SplitFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String OTHER_COUNTRY = "other";
    private static final String STATE_PERSONS = "number_persons";
    private static final String STATE_TIP = "tip_percentage";
    private static final String STATE_FROM_USER = "from_user";
    private static final String STATE_LOCALE = "locale";
    private static final String DIALOG_COUNTRY_NOT_DETECTED = "country_not_detected";
    private static final String DIALOG_TIPPING_NOT_COMMON = "tipping_not_common";
    private static final String DIALOG_TIP_ALREADY_INCLUDED = "tip_already_included";
    private static final String EVEN_SPLIT_FRAGMENT = "even_split_fragment";
    private static final String UNEVEN_SPLIT_FRAGMENT = "uneven_split_fragment";

    private boolean mFromUser;
    private boolean mFreshStart;

    private int mPersons;
    private int mPercentage;
    private int mFreshStartCount;

    private String mCountryCodeManuallySelected;
    private String mRoundMode;

    private EvenSplitFragment mEvenSplitFragment;
    private UnevenSplitFragment mUnevenSplitFragment;

    private Locale mChosenLocale;
    private SharedPreferences mSharedPrefs;

    private BiMap<String, String> mMapCountries;
    private Map<String, Integer> mMapTipValues;
    private Map<String, Integer> mMapTipIncluded;
    private List<String> mListCountries;
    private String[] mCountryNames;
    private String[] mCountryCodes;

    public Locale getChosenLocale() {
        return mChosenLocale;
    }

    public int getPersons() {
        return mPersons;
    }

    public int getPercentage() {
        return mPercentage;
    }

    public String getRoundMode() {
        return mRoundMode;
    }

    public List<String> getListCountries() {
        return mListCountries;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * Check if we are starting fresh or data needs to be reloaded from saved Bundle.
         * Additionally, set fresh start boolean to true or false (needed to decide whether to show
         * the alert dialogs or not)
         */
        if (savedInstanceState != null) {
            mPersons = savedInstanceState.getInt(STATE_PERSONS);
            mPercentage = savedInstanceState.getInt(STATE_TIP);
            mFromUser = savedInstanceState.getBoolean(STATE_FROM_USER);
            mChosenLocale = (Locale) savedInstanceState.getSerializable(STATE_LOCALE);
            mEvenSplitFragment = (EvenSplitFragment) getFragmentManager()
                    .getFragment(savedInstanceState, EVEN_SPLIT_FRAGMENT);
            mUnevenSplitFragment = (UnevenSplitFragment) getFragmentManager()
                    .getFragment(savedInstanceState, UNEVEN_SPLIT_FRAGMENT);

            mFreshStart = false;
            mFreshStartCount = 0;
        } else {
            mPersons = 0;
            mPercentage = 0;
            mChosenLocale = Locale.getDefault();

            mFreshStart = true;
            mFreshStartCount = 0;

            mEvenSplitFragment = new EvenSplitFragment();
            mUnevenSplitFragment = new UnevenSplitFragment();
        }

        setupPrefs();
        setupTabs();
        generateCountryMapAndLists();
    }

    /**
     * Sets up the default preferences.
     */
    private void setupPrefs() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
        updatePrefs();
    }


    /**
     * Updates member variable to changed shared preferences.
     */
    private void updatePrefs() {
        mCountryCodeManuallySelected = mSharedPrefs.getString("PREF_COUNTRY_LIST",
                getString(R.string.other));
        mRoundMode = mSharedPrefs.getString("PREF_ROUND_MODE", "0");
    }

    /**
     * Sets up the ViewPager and the tabs
     */
    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mEvenSplitFragment, getString(R.string.tab_even));
        tabsAdapter.addFragment(mUnevenSplitFragment, getString(R.string.tab_uneven));
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Generates and fills the various maps and lists with entries localized with the system's
     * locale
     */
    private void generateCountryMapAndLists() {
        // Get country names (keys), corresponding tip values (values) and tip already included
        // (values) from arrays.xml
        String[] countryCodes = getResources().getStringArray(R.array.country_codes);
        int[] countryTipValues = getResources().getIntArray(R.array.country_tip_values);
        int[] countryTipIncluded = getResources().getIntArray(R.array.country_tip_included);

        // Add countryCodes and countryTipValues to new Linked HashMap
        mMapTipValues = new LinkedHashMap<>();
        for (int i = 0; i < countryCodes.length; ++i) {
            mMapTipValues.put(countryCodes[i], countryTipValues[i]);
        }

        // Add countryCodes and countryTipIncluded to new Linked HashMap
        mMapTipIncluded = new LinkedHashMap<>();
        for (int i = 0; i < countryCodes.length; ++i) {
            mMapTipIncluded.put(countryCodes[i], countryTipIncluded[i]);
        }

        // Create HashBiMap with available countries and the corresponding country codes which are
        // retrieved from the system's locale
        mMapCountries = HashBiMap.create();
        for (String countryCode : Locale.getISOCountries()) {
            if (mMapTipValues.containsKey(countryCode)) {
                mMapCountries.put(countryCode, new Locale("", countryCode)
                        .getDisplayCountry(Locale.getDefault()));
            }
        }

        // Create string arrays for sorted countryNames and corresponding countryCodes
        // for usage in the SettingsActivity
        mCountryNames = mMapCountries.inverse().keySet()
                .toArray(new String[mMapCountries.inverse().size()]);
        Arrays.sort(mCountryNames);
        mCountryCodes = new String[mCountryNames.length];
        for (int i = 0; i < mCountryNames.length; i++) {
            mCountryCodes[i] = mMapCountries.inverse().get(mCountryNames[i]);
        }

        // Add "Other" to mMapCountries (put here because we don't want it in mCountryNames and
        // mCountryCodes)
        mMapCountries.put(OTHER_COUNTRY, getString(R.string.other));

        // Create sorted ArrayList with all the countries that have a tip value in mMapTipValues
        // for usage in the country spinner
        mListCountries = new ArrayList<>();
        for (String countryCode : mMapTipValues.keySet()) {
            mListCountries.add(mMapCountries.get(countryCode));
        }
        Collections.sort(mListCountries);
        mListCountries.add(getString(R.string.other));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clearAll();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(INTENT_COUNTRY_NAMES, mCountryNames)
                        .putExtra(INTENT_COUNTRY_CODES, mCountryCodes);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Resets the country spinner to its initial state and resets the various EditTexts. Also deals
     * with the special case to reset tip percentage when the user has not changed the country but
     * has manually changed the percentage.
     */
    private void clearAll() {
        setSpinnerToInitialState();

        onPersonsSelected(1);
        mEvenSplitFragment.setBillAmount("");
        mUnevenSplitFragment.resetBillAmounts();

        if (checkPrefCountrySetManually()) {
            mEvenSplitFragment.setPercentage(mMapTipValues.get(mCountryCodeManuallySelected));
        } else {
            String initialCountry = getInitialCountry();
            if (mEvenSplitFragment.getCountry().equals(initialCountry)) {
                String initialCountryCode = mMapCountries.inverse().get(initialCountry);
                if (mMapTipValues.containsKey(initialCountryCode)) {
                    mEvenSplitFragment.setPercentage(mMapTipValues.get(initialCountryCode));
                } else {
                    mEvenSplitFragment.setPercentage(0);
                }
            }
        }
    }

    /**
     * Sets the country spinner to the initial country
     */
    public void setSpinnerToInitialState() {
        if (checkPrefCountrySetManually()) {
            mEvenSplitFragment.setCountry(mMapCountries.get(mCountryCodeManuallySelected));
        } else {
            mEvenSplitFragment.setCountry(getInitialCountry());
        }
    }

    /**
     * Checks if the user has set "Select country manually" to true
     *
     * @return true if manual country selection is set and false if it is set to automatic
     */
    private boolean checkPrefCountrySetManually() {
        return mSharedPrefs.getBoolean("PREF_COUNTRY", false);
    }

    /**
     * Tries to get the country where the user is currently in using the network the device is
     * connected to. As this only works reliably on GSM networks, set the country to "Other" if
     * the user is using a CDMA device or a device without cellular network capability.
     *
     * @return the country the user is currently in or "Other" if he/she is using an unsupported
     * device
     */
    private String getInitialCountry() {
        TelephonyManager telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);

        String initialCountry = getString(R.string.other);

        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            String countryCode = telephonyManager.getNetworkCountryIso().toUpperCase();
            if (countryCode.length() > 0 && mMapTipValues.containsKey(countryCode)) {
                initialCountry = mMapCountries.get(countryCode);
            }
        }

        return initialCountry;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_PERSONS, mPersons);
        outState.putInt(STATE_TIP, mPercentage);
        outState.putBoolean(STATE_FROM_USER, mFromUser);
        outState.putSerializable(STATE_LOCALE, mChosenLocale);
        getFragmentManager().putFragment(outState, EVEN_SPLIT_FRAGMENT, mEvenSplitFragment);
        getFragmentManager().putFragment(outState, UNEVEN_SPLIT_FRAGMENT, mUnevenSplitFragment);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        updatePrefs();

        if (key.equals("PREF_COUNTRY") || key.equals("PREF_COUNTRY_LIST")) {
            setSpinnerToInitialState();
        }
    }

    /**
     * Handles clicks on the "minus 1 person" button in both fragments by setting the same amount of
     * persons in both fragments.
     */
    public void onMinusClicked() {
        if (mPersons > 1) {
            mPersons--;
            mEvenSplitFragment.setPersons(Integer.toString(mPersons));
            mUnevenSplitFragment.setPersons(Integer.toString(mPersons));
        }
    }

    /**
     * Handles clicks on the "plus 1 person" button in both fragments by setting the same amount of
     * persons in both fragments.
     */
    public void onPlusClicked() {
        if (mPersons < MAX_PERSONS) {
            mPersons++;
            mEvenSplitFragment.setPersons(Integer.toString(mPersons));
            mUnevenSplitFragment.setPersons(Integer.toString(mPersons));
        }
    }

    /**
     * Gets called when a number of persons is set in either fragment and sets the specified number
     * in the other fragment
     *
     * @param numberOfPersons the number of persons
     */
    public void onPersonsSelected(int numberOfPersons) {
        mPersons = numberOfPersons;

        if (mEvenSplitFragment.getPersons() != numberOfPersons) {
            mEvenSplitFragment.setPersons(Integer.toString(numberOfPersons));
        }

        if (mUnevenSplitFragment.getPersons() != numberOfPersons) {
            Log.e(LOG_TAG, "onPersonsSelected unEven " + numberOfPersons);
            mUnevenSplitFragment.setPersons(Integer.toString(numberOfPersons));
        }
    }

    /**
     * Gets called when a country is selected in either fragment. Sets the spinner in the other
     * fragment to the same country, looks up the appropriate 2-digit country code for the selected
     * country, and calls methods to setup a locale and the correct tip amount for the selected
     * country.
     *
     * @param selectedCountry the selected Country
     */
    public void onCountrySelected(String selectedCountry) {
        String selectedCountryCode = OTHER_COUNTRY;
        if (selectedCountry.length() > 0) {
            selectedCountryCode = mMapCountries.inverse().get(selectedCountry);
        }

        Locale oldLocale = mChosenLocale;

        mChosenLocale = getLocale(selectedCountryCode);
        mEvenSplitFragment.formatBillAmount(oldLocale);
        mUnevenSplitFragment.formatBillAmount(oldLocale);
        setCountryTip(selectedCountryCode);


        if (!mEvenSplitFragment.getCountry().equals(selectedCountry)) {
            mEvenSplitFragment.setCountry(selectedCountry);
        }

        if (!mUnevenSplitFragment.getCountry().equals(selectedCountry)) {
            mUnevenSplitFragment.setCountry(selectedCountry);
        }

        // TODO: Update EditText BillAmount in Fragments with new currency format
    }

    /**
     * Creates a new Locale object for the selected country. If no country is selected (Other), it
     * defaults to the system's default Locale.
     *
     * @param selectedCountryCode the 2-digit country code for the selected country
     */
    private Locale getLocale(String selectedCountryCode) {
        if (selectedCountryCode.equals("IN")) {
            return new Locale("en", selectedCountryCode);
        } else if (!selectedCountryCode.equals(OTHER_COUNTRY)) {
            for (Locale locale : Locale.getAvailableLocales()) {
                if (locale.getCountry().equals(selectedCountryCode)) {
                    return locale;
                }
            }

            return new Locale("", selectedCountryCode);
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * Sets the tip percentage seekBar to the appropriate value for the selected country
     *
     * @param selectedCountryCode the 2-digit country code for the selected country
     */
    private void setCountryTip(String selectedCountryCode) {
        if (!mFromUser) {
            mEvenSplitFragment.setPercentage(getCountryTip(selectedCountryCode));
            mUnevenSplitFragment.setPercentage(getCountryTip(selectedCountryCode));
        } else if (!mEvenSplitFragment.getCountry()
                .equals(mMapCountries.get(selectedCountryCode)) ||
                !mUnevenSplitFragment.getCountry().equals(mMapCountries.get(selectedCountryCode))) {
            mEvenSplitFragment.setPercentage(getCountryTip(selectedCountryCode));
            mUnevenSplitFragment.setPercentage(getCountryTip(selectedCountryCode));
        }
    }

    /**
     * Retrieves the appropriate tip percentage for the selected country.
     *
     * @param selectedCountryCode the 2-digit country code for the selected country
     * @return the appropriate tip percentage
     */
    private int getCountryTip(String selectedCountryCode) {
        int tip = 0;

        if (mMapTipValues.containsKey(selectedCountryCode)) {
            tip = mMapTipValues.get(selectedCountryCode);
        }

        return tip;
    }

    /**
     * Gets called when a tip percentage is set in either fragment and sets the chosen percentage
     * in the other fragment via the {@link #updatePercentage(int)} method. Additionally it does
     * some checks to decide if the tip percentage was set manually by the user and hence should be
     * left at the current value after a configuration change.
     *
     * @param percentage the chosen tip percentage
     * @param fromUser   if it was set by the user or the system
     */
    public void onPercentageSet(int percentage, boolean fromUser) {
        mPercentage = percentage;

        if (fromUser || mFromUser) {
            updatePercentage(percentage);

            fromUser = true;
        } else {
            updatePercentage(percentage);
        }

        mFromUser = fromUser;
    }

    private void updatePercentage(int percentage) {
        mEvenSplitFragment.setPercentage(percentage);
        mEvenSplitFragment.setPercentageText(percentage);

        mUnevenSplitFragment.setPercentage(percentage);
        mUnevenSplitFragment.setPercentageText(percentage);
    }

    /**
     * Shows the appropriate DialogFragments using the {@link #chooseDialog(String)} method. Also
     * deals with the case that if the dialogs have been dismissed, they should not be re-shown
     * after a configuration change. Uses a hack with a count variable because somehow
     * onItemSelected in the country spinner gets called only once on initial start but twice after
     * screen rotation which messes up the logic to detect if the app has started fresh or a
     * configuration change has happened.
     *
     * @param selectedCountry the selected Country
     */
    public void showDialog(String selectedCountry) {
        if (mFreshStart) {
            if (mFreshStartCount < 1) {
                if (selectedCountry.equals(getString(R.string.other))) {
                    chooseDialog(DIALOG_COUNTRY_NOT_DETECTED);
                } else if (mMapTipValues.get(mMapCountries.inverse().get(selectedCountry)) == 0) {
                    chooseDialog(DIALOG_TIPPING_NOT_COMMON);
                } else if (mMapTipIncluded.get(mMapCountries.inverse().get(selectedCountry)) == 1) {
                    chooseDialog(DIALOG_TIP_ALREADY_INCLUDED);
                }
            } else {
                mFreshStartCount--;
            }
        } else {
            mFreshStart = true;
            mFreshStartCount++;
        }
    }

    private void chooseDialog(String dialogId) {
        switch (dialogId) {
            case DIALOG_COUNTRY_NOT_DETECTED:
                CountryNotDetectedDialogFragment countryNotDetectedDialog =
                        new CountryNotDetectedDialogFragment();
                countryNotDetectedDialog.show(getFragmentManager(), "Alert_Dialog");
                break;
            case DIALOG_TIPPING_NOT_COMMON:
                TippingNotCommonDialogFragment tippingNotCommonDialog =
                        new TippingNotCommonDialogFragment();
                tippingNotCommonDialog.show(getFragmentManager(), "Alert_Dialog");
                break;
            case DIALOG_TIP_ALREADY_INCLUDED:
                TipIncludedDialogFragment tipIncludedDialog =
                        new TipIncludedDialogFragment();
                tipIncludedDialog.show(getFragmentManager(), "Alert_Dialog");
                break;
        }
    }
}