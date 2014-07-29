package ch.berta.fabio.tipee;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A {@link SplitFragment} subclass. Provides an UI to split bills among a chosen
 * amount of persons, providing an input field for every person to specify the share of the bill
 * they want to pay.
 *
 * @author Fabio Berta
 */

public class UnevenSplitFragment extends SplitFragment {

    private static final int MAX_BILL_AMOUNT_LENGTH = 9;

    private int mMaxPersons;
    private int mIdLlPerson;

    private EditText[] etBillAmountPerson;
    private TextView[] tvTipAmountPerson;
    private TextView[] tvTotalAmountPerson;
    private LinearLayout[] llPerson;
    private LinearLayout mLlMain;

    /*
    public static UnevenSplitFragment newInstance(String arg) {
        UnevenSplitFragment fragment = new UnevenSplitFragment();

        Bundle args = new Bundle();
        args.putString("test", arg);
        fragment.setArguments(args);

        return fragment;
    }
    */

    public UnevenSplitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_uneven_split_headers_nomargins, container, false);

        etPersons = (EditText) rootView.findViewById(R.id.etPersonsS);
        bPersonsMinus = (Button) rootView.findViewById(R.id.bPersonsMinusS);
        bPersonsPlus = (Button) rootView.findViewById(R.id.bPersonsPlusS);
        tvResult = (TextView) rootView.findViewById(R.id.tvResultS);
        mLlMain = (LinearLayout) rootView.findViewById(R.id.llMain);
        spCountry = (Spinner) rootView.findViewById(R.id.spCountryS);
        sbPercentage = (SeekBar) rootView.findViewById(R.id.sbPercentageS);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMaxPersons = mListener.getMaxPersons();
        mIdLlPerson = 0;

        etBillAmountPerson = new EditText[mMaxPersons];
        tvTipAmountPerson = new TextView[mMaxPersons];
        tvTotalAmountPerson = new TextView[mMaxPersons];
        llPerson = new LinearLayout[mMaxPersons];

        etPersons.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (etPersons.length() > 0) {
                    mListener.onPersonsSelected(Integer.parseInt(etPersons.getText().toString()));
                } else {
                    mListener.onPersonsSelected(0);
                }

                setUpViews();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                mListener.onCountrySelected(spCountry.getItemAtPosition(pos).toString());

                calculateTipSeparate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sbPercentage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mListener.onPercentageSet(progress, fromUser);

                calculateTipSeparate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        calculateTipSeparate();
    }

    /**
     * Dynamically creates and removes the appropriate views based on the selected amount of
     * persons.
     */
    public void setUpViews() {
        int persons = mListener.getPersons();

        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(mListener.getChosenLocale());

        if (mIdLlPerson > persons) {
            for (int i = mIdLlPerson - 1; i >= persons; i--) {
                mLlMain.removeView(llPerson[i]);
                if (persons > 0) {
                    etBillAmountPerson[persons - 1].setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
                mIdLlPerson = persons;
            }

        } else if (persons >= 1 && persons <= mMaxPersons) {

            if (mLlMain.getChildCount() == 0) {
                mIdLlPerson = 0;
            }

            for (int i = mIdLlPerson; i < persons; i++) {
                int personNumber = i + 1;
                int etBillAmountPersonId = mIdLlPerson + 20;

                llPerson[i] = new LinearLayout(getActivity());
                llPerson[i].setId(mIdLlPerson);
                llPerson[i].setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                llPerson[i].setOrientation(LinearLayout.HORIZONTAL);
                llPerson[i].setWeightSum(90);
                llPerson[i].setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                mLlMain.addView(llPerson[i]);

                etBillAmountPerson[i] = new EditText(getActivity());
                etBillAmountPerson[i].setId(etBillAmountPersonId);
                etBillAmountPerson[i].setHint(getString(R.string.person_hint) + " " + personNumber);
                etBillAmountPerson[i].setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 30));
                etBillAmountPerson[i].setTextAppearance(getActivity(),
                        android.R.style.TextAppearance_Small);
                etBillAmountPerson[i].setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL);
                etBillAmountPerson[i].setImeOptions(EditorInfo.IME_ACTION_DONE);
                etBillAmountPerson[i].setFilters(new InputFilter[]
                        {new InputFilter.LengthFilter(MAX_BILL_AMOUNT_LENGTH)});
                llPerson[i].addView(etBillAmountPerson[i]);
                etBillAmountPerson[i].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        calculateTipSeparate();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                tvTipAmountPerson[i] = new TextView(getActivity());
                tvTipAmountPerson[i].setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 30));
                tvTipAmountPerson[i].setTypeface(null, Typeface.BOLD);
                tvTipAmountPerson[i].setGravity(Gravity.RIGHT);
                llPerson[i].addView(tvTipAmountPerson[i]);
                tvTipAmountPerson[i].setText(numberFormatter.format(0));

                tvTotalAmountPerson[i] = new TextView(getActivity());
                tvTotalAmountPerson[i].setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 30));
                tvTotalAmountPerson[i].setTypeface(null, Typeface.BOLD);
                tvTotalAmountPerson[i].setGravity(Gravity.RIGHT);
                llPerson[i].addView(tvTotalAmountPerson[i]);
                tvTotalAmountPerson[i].setText(numberFormatter.format(0));

                mIdLlPerson++;
            }
        }

        // Set action button on soft keyboard to next for all etBillAmountPerson except the last
        // one. If not set manually, the done button would never be shown because of the adView at
        // the bottom.
        for (int i = 0; i < (persons - 1); i++) {
            etBillAmountPerson[i].setImeOptions(EditorInfo.IME_NULL);
        }
    }

    /**
     * Calculate the tip values in the dynamically created views and format the results with the
     * correct currency format based on the locale of the chosen country.
     */
    public void calculateTipSeparate() {
        int persons = mListener.getPersons();
        int percentage = mListener.getPercentage();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(
                mListener.getChosenLocale());

        for (int i = 0; i < persons; i++) {
            if (etBillAmountPerson[i].length() > 0) {
                String billAmountPersonString = etBillAmountPerson[i].getText().toString();
                double billAmountPerson;

                try {
                    Number billAmountPersonNumber = currencyFormatter.parse(billAmountPersonString);
                    billAmountPerson = billAmountPersonNumber.doubleValue();
                } catch (ParseException e) {
                    billAmountPerson = Double.parseDouble(billAmountPersonString);
                }

                double tipAmountPerson = ((billAmountPerson * percentage) / 100);
                double totalAmountPerson = (tipAmountPerson + billAmountPerson);

                BigDecimal totalAmountPersonBig = new BigDecimal(totalAmountPerson);

                switch (mListener.getRoundMode()) {
                    case ROUND_EXACT:
                        break;
                    case ROUND_UP:
                        totalAmountPerson = totalAmountPersonBig.setScale(0,
                                BigDecimal.ROUND_CEILING).doubleValue();
                        tipAmountPerson = (totalAmountPerson - billAmountPerson);
                        break;
                    case ROUND_DOWN:
                        totalAmountPerson = totalAmountPersonBig.setScale(0,
                                BigDecimal.ROUND_FLOOR).doubleValue();
                        if (totalAmountPerson <= billAmountPerson) {
                            totalAmountPerson = billAmountPerson;
                        }
                        tipAmountPerson = (totalAmountPerson - billAmountPerson);
                        break;
                }

                tvTipAmountPerson[i].setText(currencyFormatter.format(tipAmountPerson));
                tvTotalAmountPerson[i].setText(currencyFormatter.format(totalAmountPerson));
            } else {
                tvTipAmountPerson[i].setText(currencyFormatter.format(0));
                tvTotalAmountPerson[i].setText(currencyFormatter.format(0));
            }
        }
    }
}
