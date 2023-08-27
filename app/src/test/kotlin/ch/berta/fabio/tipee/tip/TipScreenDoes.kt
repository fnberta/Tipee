package ch.berta.fabio.tipee.tip

import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class TipScreenDoes {
    private val switzerland =
        Country(CountryName("Switzerland"), CountryCode("ch"), TipPercentage(0.02))
    private lateinit var tipState: TipState

    @Before
    fun setUp() {
        tipState = TipState(switzerland)
    }

    @Test
    fun `start with 1 person`() {
        assertEquals(1, tipState.persons)
    }

    @Test
    fun `add 1 person on add`() {
        tipState.onPersonAdd()
        assertEquals(2, tipState.persons)
    }

    @Test
    fun `keep 1 person on remove when current value is 1`() {
        tipState.onPersonRemove()
        assertEquals(1, tipState.persons)
    }

    @Test
    fun `remove 1 person on remove when current value is greater than 1`() {
        tipState.onPersonAdd()
        tipState.onPersonRemove()
        assertEquals(1, tipState.persons)
    }

    @Test
    fun `clear custom percentage when new country is selected`() {
        tipState.onPercentageChange(0.5f)
        tipState.onSelectedCountryChange(switzerland)
        assertEquals(switzerland.percentage.value.toFloat(), tipState.percentage)
    }

    @Test
    fun `start with empty bill amount`() {
        assertEquals("", tipState.billAmount)
    }

    @Test
    fun `reset bill amount on clear`() {
        tipState.onBillAmountChange("120")
        tipState.onBillAmountClear()
        assertEquals("", tipState.billAmount)
    }

    @Test
    fun `derive correct tip calculation`() {
        tipState.onBillAmountChange("120")
        assertEquals(
            TipCalculation(
                tip = Tip(value = 2.4),
                total = Total(value = 122.4),
                totalPerPerson = TotalPerPerson(value = 122.4)
            ),
            tipState.calculation
        )
    }
}
