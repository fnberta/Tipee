package ch.berta.fabio.tipee.data.models

import android.content.res.Resources
import android.telephony.TelephonyManager
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.data.helpers.SharedPrefs
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

@PaperParcel
data class Country(val countryCode: String,
                   val name: String,
                   val percentage: Int,
                   val tipIncluded: Boolean
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelCountry.CREATOR
    }

    override fun toString(): String {
        return name
    }
}

fun makeCountryMappings(res: Resources): () -> List<Country> = {
    val countryCodes = res.getStringArray(R.array.country_codes).asList()
    val tipPercentage = res.getIntArray(R.array.country_tip_values).asList()
    val tipIncluded = res.getIntArray(R.array.country_tip_included).asList().map { it == 1 }
    val tip = tipPercentage.zip(tipIncluded)
    countryCodes.zip(tip, { cc, tipPair ->
        Country(cc, Locale("", cc).displayCountry, tipPair.first, tipPair.second)
    })
}

fun makeInitialCountry(sharedPrefs: SharedPrefs,
                       telephonyManager: TelephonyManager
): (List<Country>) -> Country = { countries ->
    when {
        sharedPrefs.isCountryManuallySet() ->
            getCountryFromCountryCode(countries, sharedPrefs.getSelectedCountry())
        telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_GSM ->
            getCountryFromCountryCode(countries, telephonyManager.networkCountryIso.toUpperCase())
        else -> countries.first()
    }
}

private fun getCountryFromCountryCode(countries: List<Country>,
                                      countryCode: String
): Country {
    val initialCountry = countries
            .filter { it.countryCode == countryCode }
            .firstOrNull()
    return initialCountry ?: countries.first()
}
