package ch.berta.fabio.tipee.util;

import android.view.View;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class AmountOnFocusChangeListener implements View.OnFocusChangeListener {

    private Locale mChosenLocale;

    public AmountOnFocusChangeListener(Locale locale) {
        mChosenLocale = locale;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // This listener will be attached to any view containing amounts.
        EditText amountView = (EditText) v;

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(mChosenLocale);

        if (hasFocus) {
            if (amountView.length() > 0) {

                String amountValue = amountView.getText().toString();
                double amount;

                try {
                    Number amountValueNumber = currencyFormatter.parse(amountValue);
                    amount = amountValueNumber.doubleValue();
                } catch (ParseException e) {
                    amount = Double.parseDouble(amountValue);
                }

                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setMaximumFractionDigits(2);
                amountView.setText(decimalFormat.format(amount));
                // Select all so the user can overwrite the entire amount in one shot.
                amountView.selectAll();
            }
        } else {
            if (amountView.length() > 0) {
                amountView.setText(currencyFormatter.format(Double.parseDouble(amountView.getText().toString())));
            }
        }
    }
}
