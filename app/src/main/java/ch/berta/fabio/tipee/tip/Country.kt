package ch.berta.fabio.tipee.tip

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class Country(val name: CountryName, val code: CountryCode, val percentage: TipPercentage)

@JvmInline value class CountryName(val value: String)

@JvmInline value class CountryCode(val value: String)

fun CountryCode.format(value: Number): String = formatter.format(value)

private val CountryCode.formatter: NumberFormat
    get() {
        val locale = Locale.Builder().setRegion(value).build()
        var formatter = NumberFormat.getCurrencyInstance(locale)
        if (locale.language == "ar" || locale.language == "ne" || locale.language == "fa") {
            formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatter.currency = Currency.getInstance(locale)
        }
        return formatter
    }

val Countries =
    listOf(
        Country(CountryName("Switzerland"), CountryCode("ch"), TipPercentage(0.02)),
        Country(CountryName("United States of America"), CountryCode("us"), TipPercentage(0.1)),
    )
