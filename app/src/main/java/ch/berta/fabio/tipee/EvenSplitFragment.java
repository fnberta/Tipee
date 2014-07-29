package ch.berta.fabio.tipee;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A {@link SplitFragment} subclass. Provides an UI to split bills evenly among a
 * chosen amount of persons.
 *
 * @author Fabio Berta
 */

public class EvenSplitFragment extends SplitFragment {

    private ImageButton bClear;
    private EditText etBillAmount;
    private TextView tvTipAmount, tvTotalAmount, tvTotalPerPerson;

    /*
    public static EvenSplitFragment newInstance(String arg) {
        EvenSplitFragment fragment = new EvenSplitFragment();

        Bundle args = new Bundle();
        args.putString("test", arg);
        fragment.setArguments(args);

        return fragment;
    }
    */

    public EvenSplitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_even_split_headers_nomargins, container, false);

        tvTipAmount = (TextView) rootView.findViewById(R.id.tvTipAmount);
        tvTotalAmount = (TextView) rootView.findViewById(R.id.tvTotalAmount);
        tvTotalPerPerson = (TextView) rootView.findViewById(R.id.tvTotalPerPerson);
        tvResult = (TextView) rootView.findViewById(R.id.tvResult);
        etBillAmount = (EditText) rootView.findViewById(R.id.etBillAmount);
        etPersons = (EditText) rootView.findViewById(R.id.etPersons);
        bClear = (ImageButton) rootView.findViewById(R.id.ibClear);
        bPersonsMinus = (Button) rootView.findViewById(R.id.bPersonsMinus);
        bPersonsPlus = (Button) rootView.findViewById(R.id.bPersonsPlus);
        spCountry = (Spinner) rootView.findViewById(R.id.spCountry);
        sbPercentage = (SeekBar) rootView.findViewById(R.id.sbPercentage);

        return rootView;
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
    }

    @Override
    public void onResume() {
        super.onResume();

        calculateTip();
    }

    public void setBillAmount(String billAmount) {
        etBillAmount.setText(billAmount);
    }

    public void calculateTip() {
        int persons = mListener.getPersons();
        int percentage = mListener.getPercentage();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(mListener.getChosenLocale());

        if (etBillAmount.length() > 0 && persons != 0) {
            String billAmountString = etBillAmount.getText().toString();
            double billAmount;

            try {
                Number billAmountNumber = currencyFormatter.parse(billAmountString);
                billAmount = billAmountNumber.doubleValue();
            } catch (ParseException e) {
                billAmount = Double.parseDouble(billAmountString);
            }

            double tipAmount = ((billAmount * percentage) / 100);
            double totalAmount = (tipAmount + billAmount);

            BigDecimal totalAmountBig = new BigDecimal(totalAmount);

            switch (mListener.getRoundMode()) {
                case ROUND_EXACT:
                    break;
                case ROUND_UP:
                    totalAmount = totalAmountBig.setScale(0, BigDecimal.ROUND_CEILING).doubleValue();
                    tipAmount = (totalAmount - billAmount);
                    break;
                case ROUND_DOWN:
                    totalAmount = totalAmountBig.setScale(0, BigDecimal.ROUND_FLOOR).doubleValue();
                    if (totalAmount <= billAmount) {
                        totalAmount = billAmount;
                    }
                    tipAmount = (totalAmount - billAmount);
                    break;
            }

            double totalPerPerson = (totalAmount / persons);

            tvTipAmount.setText(currencyFormatter.format(tipAmount));
            tvTotalAmount.setText(currencyFormatter.format(totalAmount));
            tvTotalPerPerson.setText(currencyFormatter.format(totalPerPerson));
        } else {
            tvTipAmount.setText(currencyFormatter.format(0));
            tvTotalAmount.setText(currencyFormatter.format(0));
            tvTotalPerPerson.setText(currencyFormatter.format(0));
        }
    }
}