package ch.berta.fabio.tipee;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.berta.fabio.tipee.dialogs.CountryNotDetectedDialogFragment;
import ch.berta.fabio.tipee.dialogs.TipIncludedDialogFragment;
import ch.berta.fabio.tipee.dialogs.TippingNotCommonDialogFragment;
import ch.berta.fabio.tipee.util.iab.IabHelper;
import ch.berta.fabio.tipee.util.iab.IabResult;
import ch.berta.fabio.tipee.util.iab.Inventory;
import ch.berta.fabio.tipee.util.iab.Purchase;

public class MainActivity extends Activity implements ActionBar.TabListener,
        SplitFragment.SplitFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "ch.berta.fabio.tipee";
    private static final String SKU_REMOVE_ADS = "ch.berta.fabio.tipee.removeads";
    private static final String MY_AD_UNIT_ID = "ca-app-pub-5341854211810034/6802745105";
    private static final String OTHER_COUNTRY = "other";
    private static final String STATE_PERSONS = "numberPersons";
    private static final String STATE_TIP = "tipPercentage";
    private static final String STATE_FROM_USER = "fromUser";
    private static final String STATE_PREMIUM = "premium";
    private static final String STATE_LOCALE = "locale";
    private static final String DIALOG_COUNTRY_NOT_DETECTED = "country_not_detected";
    private static final String DIALOG_TIPPING_NOT_COMMON = "tipping_not_common";
    private static final String DIALOG_TIP_ALREADY_INCLUDED = "tip_already_included";
    private static final String EVEN_SPLIT_FRAGMENT = "evenSplitFragment";
    private static final String UNEVEN_SPLIT_FRAGMENT = "unevenSplitFragment";

    private static final int MAX_PERSONS = 20;
    private static final int NUMBER_OF_TABS = 2;

    private boolean mIsPremium;
    private boolean mIabAvailable;
    private boolean mFromUser;
    private boolean mFreshStart;

    private int mPersons;
    private int mPercentage;
    private int mFreshStartCount;

    private String mCountryCodeManuallySelected;
    private String mRoundMode;

    private EvenSplitFragment mEvenSplitFragment;
    private UnevenSplitFragment mUnevenSplitFragment;

    private ViewPager mViewPager;
    private AdView adView;
    private IabHelper mHelper;

    private Locale mChosenLocale;
    private SharedPreferences mSharedPrefs;

    private BiMap<String, String> mMapCountries;
    private Map<String, Integer> mMapTipValues;
    private Map<String, Integer> mMapTipIncluded;
    private List<String> mListCountries;
    private String[] mCountryNames;
    private String[] mCountryCodes;

    public int getMaxPersons() {
        return MAX_PERSONS;
    }

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

        /**
         * Check if we are starting fresh or data needs to be reloaded from saved Bundle.
         * Additionally, set fresh start boolean to true or false (needed to decide whether to show
         * the alert dialogs or not)
         */
        if (savedInstanceState != null) {
            mPersons = savedInstanceState.getInt(STATE_PERSONS);
            mPercentage = savedInstanceState.getInt(STATE_TIP);
            mFromUser = savedInstanceState.getBoolean(STATE_FROM_USER);
            mIsPremium = savedInstanceState.getBoolean(STATE_PREMIUM);
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
            mIsPremium = true;
            mChosenLocale = Locale.getDefault();

            mFreshStart = true;
            mFreshStartCount = 0;

            mEvenSplitFragment = new EvenSplitFragment();
            mUnevenSplitFragment = new UnevenSplitFragment();
        }

        setupPrefs();
        setupActionBarTabs();
        setupIab();
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
     * Sets up the ActionBar, the ViewPager and the tabs
     */
    private void setupActionBarTabs() {
        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return mEvenSplitFragment;
                    case 1:
                        return mUnevenSplitFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return NUMBER_OF_TABS;
            }
        });
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        String[] mTabTitles = getResources().getStringArray(R.array.tabs);
        for (String tab_name : mTabTitles) {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }
    }

    /**
     * Sets up the ad banner at the bottom and the in-app billing framework to remove the ads.
     */
    private void setupIab() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            mIabAvailable = true;

            String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0e" +
                    "WacLBh96/OIGqFE1vYtSvN9dXWVVFK2DbtDjFPsM1MxiMdLi+tmZxQ5/jZd" +
                    "zsUzaL2uZbN5BNn/40pKFZtTSM3Bks+87IY5iLSZq/uUSkAB5VUdWvvuRA26mATbc" +
                    "6VEX71y0Y1VmVbohrvnv4lzGfL71GZmv7sGL2/1YyO6k22N7aUV0knt1+/ePz6z8+Hs4u8" +
                    "IuXvJMkjpEqo/fS89svqpel6z3Vekyss/hn3xoQmbUSKfxljkSipPmPILGrkj6M/hQouLMCWlJ53" +
                    "Cz+qx91elxRXlrq2ypXx8tZZ/wXtZmg16HxSeXG9yH0tBoJAU6oYCEaQNUZh+cGZTGdc+QIDAQAB";

            mHelper = new IabHelper(this, base64EncodedPublicKey);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess()) {
                        mHelper.queryInventoryAsync(true,
                                new IabHelper.QueryInventoryFinishedListener() {
                                    @Override
                                    public void onQueryInventoryFinished(IabResult result,
                                                                         Inventory inv) {
                                        if (result.isFailure()) {
                                            // TODO: Do something...
                                        } else {
                                            // Has the user made a purchase to disable ads?
                                            mIsPremium = inv.hasPurchase(SKU_REMOVE_ADS);
                                            setUpAds();
                                        }

                                        /*if (inv.hasPurchase(SKU_REMOVE_ADS)) {
                                            mHelper.consumeAsync(inv.getPurchase(SKU_REMOVE_ADS),
                                                    null);
                                        }*/
                                    }
                                }
                        );
                    }
                }
            });
        } else {
            mIsPremium = true;
            mIabAvailable = false;

            setUpAds();
        }
    }

    /**
     * Sets up an AdMob AdView at the bottom of the MainActivity if the user has not paid to
     * remove the ads (mIsPremium). Gets also called after a remove ads purchase and removes the
     * AdView and the "Remove Ads" menu item accordingly.
     */
    private void setUpAds() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutMain);

        if (!mIsPremium) {
            adView = new AdView(this);
            adView.setAdUnitId(MY_AD_UNIT_ID);
            adView.setAdSize(AdSize.BANNER);
            layout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // AVD Emulator
                    .addTestDevice("66A4E77F84BAD085025447015C00E481") // Nexus 5 Genymotion
                    .addTestDevice("B13BB7C694FA12DA9C79D67D36B64734") // Nexus 7 Genymotion
                    .addTestDevice("C03CF35F078528FBFC927D38CB1F8823") // Galaxy Nexus Genymotion
                    .addTestDevice("BC550DE6FADFBBE87BD789C1A8CB8993") // Galaxy S2 Genymotion
                    .addTestDevice("B737A079650C9B192028979385FEFD70") // HTC One Genymotion
                    .addTestDevice("FB3414657679E38E6F65D7453278FC87") // Galaxy S5 Genymotion
                            //.addTestDevice("5865A8795501EAC2756D62BEB230C1D2") // Nexus 5 Vera
                    .addTestDevice("0A5B6BD93C051FCF424FCEF1E66A4A00") // Nexus One Fabio
                    .build();
            adView.loadAd(adRequest);

            invalidateOptionsMenu();
        } else if (adView != null) {
            layout.removeView(adView);

            invalidateOptionsMenu();

            displayToast(getString(R.string.ads_removed));
        }
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
        if (mIsPremium || !mIabAvailable) {
            menu.findItem(R.id.action_remove_ads).setVisible(false);
        }
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
                intent.putExtra("countryNames", mCountryNames)
                        .putExtra("countryCodes", mCountryCodes);
                startActivity(intent);
                return true;
            case R.id.action_remove_ads:
                purchaseRemoveAds();
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

        mEvenSplitFragment.setPersons("");
        mEvenSplitFragment.setBillAmount("");
        mUnevenSplitFragment.setPersons("");

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

    /**
     * Removes the ad banner and the "Remove ads" menu item if the purchase is successful.
     */
    private void purchaseRemoveAds() {
        mHelper.launchPurchaseFlow(this, SKU_REMOVE_ADS, 10001,
                new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        if (result.isFailure()) {
                            displayToast(getString(R.string.purchase_failed));
                            mIsPremium = false;
                        } else if (info.getSku().equals(SKU_REMOVE_ADS)) {
                            // Disable ads
                            mIsPremium = true;
                            setUpAds();
                        }
                    }
                }
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_PERSONS, mPersons);
        outState.putInt(STATE_TIP, mPercentage);
        outState.putBoolean(STATE_FROM_USER, mFromUser);
        outState.putBoolean(STATE_PREMIUM, mIsPremium);
        outState.putSerializable(STATE_LOCALE, mChosenLocale);
        getFragmentManager().putFragment(outState, EVEN_SPLIT_FRAGMENT, mEvenSplitFragment);
        getFragmentManager().putFragment(outState, UNEVEN_SPLIT_FRAGMENT, mUnevenSplitFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
        } else {
            mEvenSplitFragment.setPersons("");
            mUnevenSplitFragment.setPersons("");
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
            mUnevenSplitFragment.setPersons(Integer.toString(numberOfPersons));
        }
    }

    /**
     * Gets called when a country is selected in either fragment. Sets the spinner in the other
     * fragment to the same country, looks up the appropriate 2-digit country code for the selected
     * country, and calls {@link #setLocale(String)} and  {@link #setCountryTip(String)} to setup a
     * locale and the correct tip amount for the selected country.
     *
     * @param selectedCountry the selected Country
     */
    public void onCountrySelected(String selectedCountry) {
        String selectedCountryCode = OTHER_COUNTRY;
        if (selectedCountry.length() > 0) {
            selectedCountryCode = mMapCountries.inverse().get(selectedCountry);
        }

        setLocale(selectedCountryCode);
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
    private void setLocale(String selectedCountryCode) {
        if (selectedCountryCode.equals("IN")) {
            mChosenLocale = new Locale("en", selectedCountryCode);
        } else if (!selectedCountryCode.equals(OTHER_COUNTRY)) {
            for (Locale locale : Locale.getAvailableLocales()) {
                if (locale.getCountry().equals(selectedCountryCode)) {
                    mChosenLocale = locale;
                    return;
                } else {
                    mChosenLocale = new Locale("", selectedCountryCode);
                }
            }
        } else {
            mChosenLocale = Locale.getDefault();
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