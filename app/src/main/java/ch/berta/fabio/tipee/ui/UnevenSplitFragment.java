package ch.berta.fabio.tipee.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ch.berta.fabio.tipee.PersonRow;
import ch.berta.fabio.tipee.R;
import ch.berta.fabio.tipee.utils.MoneyUtils;

import static ch.berta.fabio.tipee.AppConstants.MAX_PERSONS;

/**
 * A {@link SplitBaseFragment} subclass. Provides an UI to split bills among a chosen
 * amount of persons, providing an input field for every person to specify the share of the bill
 * they want to pay.
 *
 * @author Fabio Berta
 */

public class UnevenSplitFragment extends SplitBaseFragment {

    private static final int MAX_BILL_AMOUNT_LENGTH = 9;

    private final List<PersonRow> mPersonRows = new ArrayList<>();
    private LinearLayout mLinearLayoutMain;

    public UnevenSplitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_uneven_split,
                container, false);

        findViews(rootView);

        return rootView;
    }

    @Override
    void findViews(View rootView) {
        super.findViews(rootView);

        mLinearLayoutMain = (LinearLayout) rootView.findViewById(R.id.llMain);
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

        setUpViews();
    }

    /**
     * Dynamically creates and removes the appropriate views based on the selected amount of
     * persons.
     */
    private void setUpViews() {
        int numberOfPersons = mListener.getPersons();
        int numberOfRows = mPersonRows.size();

        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(
                mListener.getChosenLocale());

        if (numberOfRows > numberOfPersons) {
            for (int i = numberOfRows - 1; i >= numberOfPersons; i--) {
                mLinearLayoutMain.removeView(mPersonRows.get(i).getTipRow());
                mPersonRows.remove(i);
            }

        } else if (numberOfPersons >= 1 && numberOfPersons <= MAX_PERSONS) {
            for (int i = numberOfRows; i < numberOfPersons; i++) {
                final int rowNumber = i;
                int personNumber = i + 1;
                int etBillAmountPersonId = i + 20;

                View tipRow = getActivity().getLayoutInflater()
                        .inflate(R.layout.row_tip, mLinearLayoutMain, false);
                tipRow.setId(numberOfRows);
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
                        mPercentage = mListener.getPercentage();
                        setCurrencyFormatter();
                        calculateTipSingle(rowNumber);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });
                etBillAmountPerson.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        EditText amountView = (EditText) v;
                        if (amountView.length() <= 0) {
                            return;
                        }

                        String amountValue = amountView.getText().toString();
                        if (hasFocus) {
                            double amount = MoneyUtils.parseBillAmount(amountValue, mCurrencyFormatter);

                            NumberFormat decimalFormat = MoneyUtils.getDecimalFormatter(mChosenLocale);
                            amountView.setText(decimalFormat.format(amount));
                        } else {
                            double amount = MoneyUtils.parseLocalizedString(amountValue);
                            amountView.setText(mCurrencyFormatter.format(amount));
                        }
                    }
                });

                TextView tvTipAmountPerson = (TextView) tipRow.findViewById(R.id.tv_tip_amount);
                tvTipAmountPerson.setText(numberFormatter.format(0));

                TextView tvTotalAmountPerson = (TextView) tipRow.findViewById(R.id.tv_total_amount);
                tvTotalAmountPerson.setText(numberFormatter.format(0));

                PersonRow personRow = new PersonRow(tipRow, etBillAmountPerson, tvTipAmountPerson,
                        tvTotalAmountPerson);
                mPersonRows.add(personRow);
            }
        }
    }

    public void resetBillAmounts() {
        for (PersonRow personRow : mPersonRows) {
            personRow.setBillAmount("");
        }
    }

    @Override
    public void formatBillAmount(Locale oldLocale) {
        super.formatBillAmount(oldLocale);

        for (PersonRow personRow : mPersonRows) {
            personRow.formatBillAmount(oldLocale, mCurrencyFormatter);
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
            calculateTipSingle(i);
        }
    }

    private void calculateTipSingle(int position) {
        PersonRow personRow = mPersonRows.get(position);
        String billAmountPersonString = personRow.getBillAmount();
        if (billAmountPersonString.length() > 0) {
            double billAmountPerson = MoneyUtils.parseBillAmount(billAmountPersonString, mCurrencyFormatter);

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

            personRow.setTipAmount(mCurrencyFormatter.format(tipAmountPerson));
            personRow.setTotalAmount(mCurrencyFormatter.format(totalAmountPerson));
        } else {
            personRow.setTipAmount(mCurrencyFormatter.format(0));
            personRow.setTotalAmount(mCurrencyFormatter.format(0));
        }
    }
}
