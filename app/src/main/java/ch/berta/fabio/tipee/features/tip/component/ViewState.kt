package ch.berta.fabio.tipee.features.tip.component

import ch.berta.fabio.tipee.data.models.Country
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.text.NumberFormat

@PaperParcel
data class TipRow(
        val amount: Double,
        val amountFormatted: String,
        val isAmountFocused: Boolean,
        val tip: String,
        val total: String
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelTipRow.CREATOR
    }
}

@PaperParcel
data class TipViewState(
        val amount: Double,
        val amountFormatted: String,
        val isAmountFocused: Boolean,
        val persons: Int,
        val countries: List<Country>,
        val selectedCountryPos: Int,
        val isCountryAfterConfigChange: Boolean,
        val percentage: Int,
        val tip: String,
        val tipExact: String,
        val total: String,
        val totalExact: String,
        val tipPerPerson: String,
        val totalPerPerson: String,
        val totalPerPersonExact: String,
        val tipRows: List<TipRow>,
        val roundMode: RoundMode,
        val isOpenSettings: Boolean,
        val isShowTippingNotCommonDialog: Boolean,
        val isShowTipIncludedDialog: Boolean
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelTipViewState.CREATOR
    }
}

fun createInitialState(
        getCountryMappings: () -> List<Country>,
        getInitialCountry: (List<Country>) -> Country,
        getRoundMode: () -> RoundMode
): TipViewState {
    val countries = getCountryMappings()
    val initialCountry = getInitialCountry(countries)
    val formatter = getCurrencyFormatter(initialCountry.countryCode)
    val (initialAmount, initialAmountFormatted) = getInitialAmount(formatter)
    val tipRows = listOf(createEmptyTipRow(formatter, false))
    return TipViewState(initialAmount, initialAmountFormatted, false, 1,
                        countries, countries.indexOf(initialCountry), false,
                        initialCountry.percentage, initialAmountFormatted, initialAmountFormatted,
                        initialAmountFormatted, initialAmountFormatted, initialAmountFormatted,
                        initialAmountFormatted, initialAmountFormatted, tipRows,
                        getRoundMode(), false, false, false)
}

fun getInitialAmount(formatter: NumberFormat): Pair<Double, String> {
    val initialAmount = 0.0
    val initialAmountFormatted = formatter.format(initialAmount)
    return Pair(initialAmount, initialAmountFormatted)
}

fun createEmptyTipRow(formatter: NumberFormat, isAmountFocused: Boolean): TipRow {
    val (initialAmount, initialFormatted) = getInitialAmount(formatter)
    return TipRow(initialAmount, initialFormatted, isAmountFocused, initialFormatted, initialFormatted)
}
