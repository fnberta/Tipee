package ch.berta.fabio.tipee.features.tip.component

import ch.berta.fabio.tipee.features.tip.component.RoundMode.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

enum class RoundMode {
    EXACT, UP, DOWN
}

data class TipValues(
        val tip: String,
        val tipExact: String,
        val total: String,
        val totalExact: String,
        val totalPerPerson: String,
        val totalPerPersonExact: String
)

data class TipSingleValues(val tipSingle: String, val totalSingle: String)

fun calculateTip(
        amount: Double,
        percentage: Int,
        persons: Int,
        roundMode: RoundMode,
        formatter: NumberFormat
): TipValues {
    val tipExact = amount * percentage / 100
    val totalExact = tipExact + amount
    val totalPerPersonExact = totalExact / persons
    val (tip, total, totalPerPerson) = getRoundedValues(amount, roundMode, tipExact,
            totalExact, totalPerPersonExact, persons)
    return TipValues(formatter.format(tip), formatter.format(tipExact),
            formatter.format(total), formatter.format(totalExact),
            formatter.format(totalPerPerson), formatter.format(totalPerPersonExact))
}

private fun getRoundedValues(
        amount: Double,
        roundMode: RoundMode,
        tipExact: Double,
        totalExact: Double,
        totalPerPersonExact: Double = 0.0,
        persons: Int = 1
): Triple<Double, Double, Double> {
    val totalAmountExactBig = BigDecimal(totalExact)
    when (roundMode) {
        EXACT -> return Triple(tipExact, totalExact, totalPerPersonExact)
        UP -> {
            val totalAmount = totalAmountExactBig.setScale(0, BigDecimal.ROUND_CEILING).toDouble()
            val tipAmount = totalAmount - amount
            val totalPerPerson = totalAmount / persons
            return Triple(tipAmount, totalAmount, totalPerPerson)
        }
        DOWN -> {
            val totalAmountFloor = totalAmountExactBig.setScale(0,
                    BigDecimal.ROUND_FLOOR).toDouble()
            val totalAmount = if (totalAmountFloor > amount) totalAmountFloor else amount
            val tipAmount = totalAmount - amount
            val totalPerPerson = totalAmount / persons
            return Triple(tipAmount, totalAmount, totalPerPerson)
        }
    }
}

fun calculateTipSingle(
        amount: Double,
        percentage: Int,
        roundMode: RoundMode,
        formatter: NumberFormat
): TipSingleValues {
    val tipExact = amount * percentage / 100
    val totalExact = tipExact + amount
    val (tip, total, _) = getRoundedValues(amount, roundMode, tipExact, totalExact)
    return TipSingleValues(formatter.format(tip), formatter.format(total))
}

fun getCurrencyFormatter(countryCode: String): NumberFormat {
    val locale = Locale("", countryCode)
    val currencyFormatter: NumberFormat

    if (locale.language == "ar" || locale.language == "ne" || locale.language == "fa") {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyFormatter.currency = Currency.getInstance(locale)
    } else {
        currencyFormatter = NumberFormat.getCurrencyInstance(locale)
    }

    return currencyFormatter
}
