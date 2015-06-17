package ch.berta.fabio.tipee.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import ch.berta.fabio.tipee.R;
import ch.berta.fabio.tipee.utils.MoneyUtils;

/**
 * A {@link SplitFragment} subclass. Provides an UI to split bills evenly among a
 * chosen amount of persons.
 *
 * @author Fabio Berta
 */

public class EvenSplitFragment extends SplitFragment {

    private ImageButton bClear;
    private EditText etBillAmount;
    private LinearLayout llExact;
    private TextView tvTipAmount, tvTotalAmount, tvTotalPerPerson, tvTipAmountExact,
            tvTotalAmountExact;

    public EvenSplitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_even_split, container,
                false);

        findViews(rootView);

        return rootView;
    }

    @Override
    void findViews(View rootView) {
        super.findViews(rootView);

        etBillAmount = (EditText) rootView.findViewById(R.id.etBillAmount);
        bClear = (ImageButton) rootView.findViewById(R.id.ibClear);
        tvTipAmount = (TextView) rootView.findViewById(R.id.tvTipAmount);
        tvTotalAmount = (TextView) rootView.findViewById(R.id.tvTotalAmount);
        tvTotalPerPerson = (TextView) rootView.findViewById(R.id.tvTotalPerPerson);
        tvTipAmountExact = (TextView) rootView.findViewById(R.id.tvTipAmountExact);
        tvTotalAmountExact = (TextView) rootView.findViewById(R.id.tvTotalAmountExact);
        llExact = (LinearLayout) rootView.findViewById(R.id.linearLayoutExact);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        etBillAmount.addTextChangedListener(new TextWatcher() {
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
        etBillAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

        etPersons.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (etPersons.length() > 0) {
                    mListener.onPersonsSelected(Integer.parseInt(etPersons.getText().toString()));
                }

                calculateTip();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etBillAmount.setText("");
            }
        });

        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selectedCountry = spCountry.getItemAtPosition(pos).toString();
                mListener.onCountrySelected(selectedCountry);
                mListener.showDialog(selectedCountry);

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

        // Set country spinner to initial state
        mListener.setSpinnerToInitialState();

        // default to one person
        mListener.onPersonsSelected(1);
    }

    @Override
    public void onResume() {
        super.onResume();

        // only show the second line of TextViews if roundMode is not set to exact.
        if (mListener.getRoundMode().equals(ROUND_EXACT)) {
            llExact.setVisibility(View.GONE);
        } else {
            llExact.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void formatBillAmount(Locale oldLocale) {
        super.formatBillAmount(oldLocale);

        String amountValue = etBillAmount.getText().toString();
        if (TextUtils.isEmpty(amountValue) || etBillAmount.hasFocus()) {
            return;
        }

        double amount = MoneyUtils.parseBillAmount(amountValue,
                MoneyUtils.getCurrencyFormatter(oldLocale));
        etBillAmount.setText(mCurrencyFormatter.format(amount));
    }

    public void setBillAmount(String billAmount) {
        etBillAmount.setText(billAmount);
    }

    @Override
    public void calculateTip() {
        super.calculateTip();

        if (etBillAmount.length() > 0 && mPersons != 0) {
            String billAmountString = etBillAmount.getText().toString();
            double billAmount = MoneyUtils.parseBillAmount(billAmountString, mCurrencyFormatter);

            double tipAmountExact = ((billAmount * mPercentage) / 100);
            double totalAmountExact = (tipAmountExact + billAmount);
            double totalPerPersonExact = (totalAmountExact / mPersons);

            BigDecimal totalAmountExactBig = new BigDecimal(totalAmountExact);

            switch (mListener.getRoundMode()) {
                case ROUND_EXACT: {
                    tvTipAmount.setText(mCurrencyFormatter.format(tipAmountExact));
                    tvTotalAmount.setText(mCurrencyFormatter.format(totalAmountExact));
                    tvTotalPerPerson.setText(mCurrencyFormatter.format(totalPerPersonExact));
                    break;
                }
                case ROUND_UP: {
                    double totalAmount = totalAmountExactBig.setScale(0, BigDecimal.ROUND_CEILING)
                            .doubleValue();
                    double tipAmount = (totalAmount - billAmount);
                    double totalPerPerson = (totalAmount / mPersons);

                    tvTipAmount.setText(mCurrencyFormatter.format(tipAmount));
                    tvTipAmountExact.setText("(" + mCurrencyFormatter.format(tipAmountExact) + ")");
                    tvTotalAmount.setText(mCurrencyFormatter.format(totalAmount));
                    tvTotalAmountExact.setText("(" + mCurrencyFormatter.format(totalAmountExact) + ")");
                    tvTotalPerPerson.setText(mCurrencyFormatter.format(totalPerPerson) + " ("
                            + mCurrencyFormatter.format(totalPerPersonExact) + ")");
                    break;
                }
                case ROUND_DOWN: {
                    double totalAmount = totalAmountExactBig.setScale(0, BigDecimal.ROUND_FLOOR)
                            .doubleValue();
                    if (totalAmount <= billAmount) {
                        totalAmount = billAmount;
                    }
                    double tipAmount = (totalAmount - billAmount);
                    double totalPerPerson = (totalAmount / mPersons);

                    tvTipAmount.setText(mCurrencyFormatter.format(tipAmount));
                    tvTipAmountExact.setText("(" + mCurrencyFormatter.format(tipAmountExact) + ")");
                    tvTotalAmount.setText(mCurrencyFormatter.format(totalAmount));
                    tvTotalAmountExact.setText("(" + mCurrencyFormatter.format(totalAmountExact) + ")");
                    tvTotalPerPerson.setText(mCurrencyFormatter.format(totalPerPerson) + " ("
                            + mCurrencyFormatter.format(totalPerPersonExact) + ")");

                    break;
                }
            }
        } else {
            tvTipAmount.setText(mCurrencyFormatter.format(0));
            tvTipAmountExact.setText("(" + mCurrencyFormatter.format(0) + ")");
            tvTotalAmount.setText(mCurrencyFormatter.format(0));
            tvTotalAmountExact.setText("(" + mCurrencyFormatter.format(0) + ")");
            tvTotalPerPerson.setText(mCurrencyFormatter.format(0));
        }
    }
}