package ch.berta.fabio.tipee.tip

fun Tip.currencyFormatted(countryCode: CountryCode): String = countryCode.format(value)

fun Total.currencyFormatted(countryCode: CountryCode): String = countryCode.format(value)

fun TotalPerPerson.currencyFormatted(countryCode: CountryCode): String = countryCode.format(value)
