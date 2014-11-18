package ch.berta.fabio.tipee.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.tipee.PersonRow;
import ch.berta.fabio.tipee.R;

import static ch.berta.fabio.tipee.AppConstants.MAX_PERSONS;

/**
 * A {@link SplitFragment} subclass. Provides an UI to split bills among a chosen
 * amount of persons, providing an input field for every person to specify the share of the bill
 * they want to pay.
 *
 * @author Fabio Berta
 */

public class UnevenSplitFragment extends SplitFragment {

    private static final int MAX_BILL_AMOUNT_LENGTH = 9;

    private int mIdPersonRow = 0;

    private List<PersonRow> mListPersonRow = new ArrayList<>();
    private LinearLayout mLinearLayoutMain;

    public UnevenSplitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_uneven_split,
                container, false);

        etPersons = (EditText) rootView.findViewById(R.id.etPersons);
        bPersonsMinus = (Button) rootView.findViewById(R.id.bPersonsMinus);
        bPersonsPlus = (Button) rootView.findViewById(R.id.bPersonsPlus);
        tvResult = (TextView) rootView.findViewById(R.id.tvResult);
        mLinearLayoutMain = (LinearLayout) rootView.findViewById(R.id.llMain);
        spCountry = (Spinner) rootView.findViewById(R.id.spCountry);
        sbPercentage = (SeekBar) rootView.findViewById(R.id.sbPercentage);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

                calculateTip();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sbPercentage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mListener.onPercentageSet(progress, fromUser);

                calculateTip();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Dynamically creates and removes the appropriate views based on the selected amount of
     * persons.
     */
    public void setUpViews() {
        int persons = mListener.getPersons();

        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(
                mListener.getChosenLocale());

        if (mIdPersonRow > persons) {
            for (int i = mIdPersonRow - 1; i >= persons; i--) {
                mLinearLayoutMain.removeView(mListPersonRow.get(i).getTipRow());
                if (persons > 0) {
                    mListPersonRow.get(persons - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
                mListPersonRow.remove(i);
                mIdPersonRow = persons;
            }

        } else if (persons >= 1 && persons <= MAX_PERSONS) {

            if (mLinearLayoutMain.getChildCount() == 0) {
                mIdPersonRow = 0;
            }

            for (int i = mIdPersonRow; i < persons; i++) {
                int personNumber = i + 1;
                int etBillAmountPersonId = mIdPersonRow + 20;

                View tipRow = getActivity().getLayoutInflater()
                        .inflate(R.layout.row_tip, mLinearLayoutMain, false);
                tipRow.setId(mIdPersonRow);
                mLinearLayoutMain.addView(tipRow);

                EditText etBillAmountPerson = (EditText) tipRow.findViewById(R.id.et_bill_amount_person);
                etBillAmountPerson.setId(etBillAmountPersonId);
                etBillAmountPerson.setHint(getString(R.string.person_hint) + " " + personNumber);
                etBillAmountPerson.setFilters(new InputFilter[]
                        {new InputFilter.LengthFilter(MAX_BILL_AMOUNT_LENGTH)});
                etBillAmountPerson.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        calculateTip();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                TextView tvTipAmountPerson = (TextView) tipRow.findViewById(R.id.tv_tip_amount);
                tvTipAmountPerson.setText(numberFormatter.format(0));

                TextView tvTotalAmountPerson = (TextView) tipRow.findViewById(R.id.tv_total_amount);
                tvTotalAmountPerson.setText(numberFormatter.format(0));

                PersonRow personRow = new PersonRow(tipRow, etBillAmountPerson, tvTipAmountPerson,
                        tvTotalAmountPerson);
                mListPersonRow.add(personRow);

                mIdPersonRow++;
            }
        }

        // Set action button on soft keyboard to next for all mListBillAmountPerson except the last
        // one. If not set manually, the done button would never be shown because of the adView at
        // the bottom.
        for (int i = 0; i < (persons - 1); i++) {
            mListPersonRow.get(i).setImeOptions(EditorInfo.IME_NULL);
        }
    }

    /**
     * Calculate the tip values in the dynamically created views and format the results with the
     * correct currency format based on the locale of the chosen country.
     */
    @Override
    public void calculateTip() {
        super.calculateTip();

        for (int i = 0; i < mPersons; i++) {
            String billAmountPersonString = mListPersonRow.get(i).getBillAmount();
            if (billAmountPersonString.length() > 0) {
                double billAmountPerson;

                try {
                    Number billAmountPersonNumber = mCurrencyFormatter.parse(billAmountPersonString);
                    billAmountPerson = billAmountPersonNumber.doubleValue();
                } catch (ParseException e) {
                    billAmountPerson = Double.parseDouble(billAmountPersonString);
                }

                double tipAmountPerson = ((billAmountPerson * mPercentage) / 100);
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

                mListPersonRow.get(i).setTipAmount(mCurrencyFormatter.format(tipAmountPerson));
                mListPersonRow.get(i).setTotalAmount(mCurrencyFormatter.format(totalAmountPerson));
            } else {
                mListPersonRow.get(i).setTipAmount(mCurrencyFormatter.format(0));
                mListPersonRow.get(i).setTotalAmount(mCurrencyFormatter.format(0));
            }
        }
    }
}
