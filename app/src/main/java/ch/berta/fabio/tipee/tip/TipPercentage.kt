package ch.berta.fabio.tipee.tip

import java.text.NumberFormat

@JvmInline
value class TipPercentage(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Percentage needs to lie between 0 and 1." }
    }
}

fun TipPercentage.tip(billAmount: BillAmount, persons: Int) =
    Tip(value * billAmount.value / persons)

@JvmInline value class BillAmount(val value: Double)

@JvmInline value class Tip(val value: Double)

val TipPercentage.formatted: String
    get() {
        val formatter = NumberFormat.getPercentInstance()
        return formatter.format(value)
    }
