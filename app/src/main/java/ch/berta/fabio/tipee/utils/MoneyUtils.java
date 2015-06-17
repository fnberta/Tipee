package ch.berta.fabio.tipee.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by fabio on 16.06.15.
 */
public class MoneyUtils {

    private MoneyUtils() {
        // class cannot be instantiated
    }

    public static NumberFormat getCurrencyFormatter(Locale locale) {
        NumberFormat currencyFormatter;

        if (locale.getLanguage().equals("ar") || locale.getLanguage().equals("ne") ||
                locale.getLanguage().equals("fa")) {
            currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
            currencyFormatter.setCurrency(Currency.getInstance(locale));
        } else {
            currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        }

        return currencyFormatter;
    }

    public static double parseBillAmount(String string, NumberFormat parser) {
        try {
            Number billAmountNumber = parser.parse(string);
            return billAmountNumber.doubleValue();
        } catch (ParseException e) {
            return MoneyUtils.parseLocalizedString(string);
        }
    }

    public static double parseLocalizedString(String string) {
        NumberFormat decimalFormat = getDecimalFormatter(Locale.getDefault());
        try {
            return decimalFormat.parse(string).doubleValue();
        } catch (ParseException e) {
            return Double.parseDouble(string);
        }
    }

    public static NumberFormat getDecimalFormatter(Locale locale) {
        NumberFormat decimalFormat = NumberFormat.getInstance(Locale.getDefault());
        int maximumFractionDigits = getMaximumFractionDigits(locale);
        decimalFormat.setMaximumFractionDigits(maximumFractionDigits);

        return decimalFormat;
    }

    private static int getMaximumFractionDigits(Locale locale) {
        Currency groupCurrency = Currency.getInstance(locale);
        return groupCurrency.getDefaultFractionDigits();
    }
}
