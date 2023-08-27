package ch.berta.fabio.tipee.tip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class TipState(initialCountry: Country) {
    var billAmount by mutableStateOf("")
        private set

    var persons by mutableStateOf(1)
        private set

    var selectedCountry by mutableStateOf(initialCountry)
        private set

    var percentage by mutableStateOf(initialCountry.percentage.value.toFloat())
        private set

    val tipPercentage: TipPercentage
        get() = percentage.tipPercentage

    val calculation: TipCalculation
        get() {
            val billAmount = BillAmount(if (billAmount.isEmpty()) 0.0 else billAmount.toDouble())
            val tip = tipPercentage.tip(billAmount, persons)
            val total = Total(billAmount.value + tip.value)
            return TipCalculation(tip, total, TotalPerPerson(total.value / persons))
        }

    fun onBillAmountChange(value: String) {
        billAmount = value
    }

    fun onBillAmountClear() {
        billAmount = ""
    }

    fun onPersonsChange(value: String) {
        persons = value.toIntOrNull() ?: persons
    }

    fun onPersonRemove() {
        if (persons > 1) {
            persons -= 1
        }
    }

    fun onPersonAdd() {
        persons += 1
    }

    fun onSelectedCountryChange(country: Country) {
        selectedCountry = country
        percentage = selectedCountry.percentage.value.toFloat()
    }

    fun onPercentageChange(value: Float) {
        percentage = value
    }
}

@Composable
fun rememberTipState(initialCountry: Country): TipState = remember { TipState(initialCountry) }

private val Float.tipPercentage: TipPercentage
    get() = TipPercentage(toDouble())
