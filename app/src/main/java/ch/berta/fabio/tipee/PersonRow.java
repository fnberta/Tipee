package ch.berta.fabio.tipee;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Created by fabio on 18.11.14.
 */
public class PersonRow {

    private View mTipRow;
    private EditText mEditTextBillAmount;
    private TextView mTextViewTipAmount;
    private TextView mTextViewTotalAmount;

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

    public void setImeOptions(int imeOptions) {
        mEditTextBillAmount.setImeOptions(imeOptions);
    }

    public void setTipAmount(String tipAmount) {
        mTextViewTipAmount.setText(tipAmount);
    }

    public void setTotalAmount(String totalAmount) {
        mTextViewTotalAmount.setText(totalAmount);
    }
}
