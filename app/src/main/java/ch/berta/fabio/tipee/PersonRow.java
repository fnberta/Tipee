package ch.berta.fabio.tipee;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import ch.berta.fabio.tipee.utils.MoneyUtils;


/**
 * Created by fabio on 18.11.14.
 */
public class PersonRow {

    private final View mTipRow;
    private final EditText mEditTextBillAmount;
    private final TextView mTextViewTipAmount;
    private final TextView mTextViewTotalAmount;

    public PersonRow(View tipRow, EditText etBillAmount, TextView tvTipAmount, TextView tvTotalAmount) {
        mTipRow = tipRow;
        mEditTextBillAmount = etBillAmount;
        mTextViewTipAmount = tvTipAmount;
        mTextViewTotalAmount = tvTotalAmount;
    }

    public View getTipRow() {
        return mTipRow;
    }

    public String getBillAmount() {
        return mEditTextBillAmount.getText().toString();
    }

    public void setBillAmount(String billAmount) {
        mEditTextBillAmount.setText(billAmount);
    }

    public void setTipAmount(String tipAmount) {
        mTextViewTipAmount.setText(tipAmount);
    }

    public void setTotalAmount(String totalAmount) {
        mTextViewTotalAmount.setText(totalAmount);
    }

    public void formatBillAmount(Locale oldLocale, NumberFormat formatter) {
        String amountValue = mEditTextBillAmount.getText().toString();
        if (TextUtils.isEmpty(amountValue) || mEditTextBillAmount.hasFocus()) {
            return;
        }

        double amount = MoneyUtils.parseBillAmount(amountValue, MoneyUtils.getCurrencyFormatter(oldLocale));
        mEditTextBillAmount.setText(formatter.format(amount));
    }
}
