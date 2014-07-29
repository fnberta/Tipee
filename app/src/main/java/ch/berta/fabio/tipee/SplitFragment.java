package ch.berta.fabio.tipee;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ch.berta.fabio.tipee.util.InputFilterMinMax;

/**
 * A simple Fragment that allows the user to calculate the appropriate tip values for various
 * countries. Serves as a base for {@link EvenSplitFragment} and
 * {@link UnevenSplitFragment} which are actually displayed in the app. This fragment
 * only contains the commonly shared methods.
 * <p/>
 * Activities that contain this fragment must implement the
 * {@link SplitFragment.SplitFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Fabio Berta
 */
public class SplitFragment extends Fragment {

    static final String LOG_TAG = "ch.berta.fabio.tipee";
    static final String ROUND_EXACT = "0";
    static final String ROUND_UP = "1";
    static final String ROUND_DOWN = "2";

    EditText etPersons;
    Button bPersonsMinus, bPersonsPlus;
    Spinner spCountry;
    SeekBar sbPercentage;
    TextView tvResult;

    SplitFragmentInteractionListener mListener;

    public SplitFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SplitFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SplitFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        etPersons.setFilters(new InputFilter[]{new InputFilterMinMax(0,
                mListener.getMaxPersons())});

        bPersonsMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onMinusClicked();
            }
        });

        bPersonsPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPlusClicked();
            }
        });

        ArrayAdapter<String> spCountryDataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mListener.getListCountries());
        spCountryDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(spCountryDataAdapter);
    }

    /**
     * Returns the currently selected country in the spinner
     *
     * @return the currently selected country
     */
    public String getCountry() {
        if (spCountry != null) {
            return spCountry.getSelectedItem().toString();
        } else {
            return getString(R.string.other);
        }
    }

    /**
     * Sets the country spinner to the chosen country
     *
     * @param country the selected country
     */
    public void setCountry(String country) {
        if (spCountry != null) {
            ArrayAdapter spCountryAdapter = (ArrayAdapter) spCountry.getAdapter();
            spCountry.setSelection(spCountryAdapter.getPosition(country));
        }
    }

    /**
     * Returns the currently set amount of persons
     *
     * @return the currently set amount of persons
     */
    public int getPersons() {
        int persons = 0;
        if (etPersons != null && etPersons.length() > 0) {
            persons = Integer.parseInt(etPersons.getText().toString());
        }
        return persons;
    }

    /**
     * Sets the EditText etPersons to the chosen amount of persons
     *
     * @param amountOfPersons the chosen amount of persons
     */
    public void setPersons(String amountOfPersons) {
        if (etPersons != null) {
            etPersons.setText(amountOfPersons);
        }
    }

    /**
     * Sets the SeekBar to the chosen value
     *
     * @param tip the chosen tip value
     */
    public void setPercentage(int tip) {
        if (sbPercentage != null) {
            sbPercentage.setProgress(tip);
        }
    }

    /**
     * Sets the TextView next to the SeekBar with the value chosen in the SeekBar
     *
     * @param tip the chosen SeekBar value
     */
    public void setPercentageText(int tip) {
        if (tvResult != null) {
            tvResult.setText(Integer.toString(tip) + "%");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface SplitFragmentInteractionListener {
        public void showDialog(String selectedCountry);

        public int getMaxPersons();

        public void onMinusClicked();

        public void onPlusClicked();

        public void onPersonsSelected(int numberOfPersons);

        public void onCountrySelected(String selectedCountry);

        public void onPercentageSet(int percentage, boolean fromUser);

        public int getPercentage();

        public int getPersons();

        public Locale getChosenLocale();

        public void setSpinnerToInitialState();

        public List<String> getListCountries();

        public String getRoundMode();
    }
}
