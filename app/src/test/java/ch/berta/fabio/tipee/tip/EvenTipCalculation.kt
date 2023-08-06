package ch.berta.fabio.tipee.tip

import kotlin.test.assertEquals
import org.junit.Test

class EvenTipCalculation {
    private val tipPercentage = TipPercentage(0.02)
    private val billAmount = BillAmount(100.0)

    @Test
    fun `is correct for single person`() {
        assertEquals(Tip(2.0), tipPercentage.tip(billAmount = billAmount, persons = 1))
    }

    @Test
    fun `is correct for multiple persons`() {
        assertEquals(Tip(1.0), tipPercentage.tip(billAmount = billAmount, persons = 2))
        assertEquals(Tip(0.5), tipPercentage.tip(billAmount = billAmount, persons = 4))
    }
}
