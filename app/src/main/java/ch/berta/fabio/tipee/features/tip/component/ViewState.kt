package ch.berta.fabio.tipee.features.tip.component

import ch.berta.fabio.tipee.data.models.Country
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.text.NumberFormat

@PaperParcel
data class TipRow(
        val amount: Double,
        val amountFormatted: String,
        val tip: String,
        val total: String,
        val hasFocus: Boolean
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
    val initialAmount = 0.0
    val initialAmountFormatted = formatter.format(initialAmount)
    val tipRows = listOf(createEmptyTipRow(formatter))
    return TipViewState(initialAmount, formatter.format(initialAmount), false, 1,
                        countries, countries.indexOf(initialCountry), false,
                        initialCountry.percentage, initialAmountFormatted, initialAmountFormatted,
                        initialAmountFormatted, initialAmountFormatted, initialAmountFormatted,
                        initialAmountFormatted, initialAmountFormatted, tipRows,
                        getRoundMode(), false, false, false)
}

fun createEmptyTipRow(formatter: NumberFormat): TipRow {
    val initialAmount = 0.0
    val initialFormatted = formatter.format(initialAmount)
    return TipRow(initialAmount, initialFormatted, initialFormatted, initialFormatted, false)
}
