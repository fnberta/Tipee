package ch.berta.fabio.tipee.tip

import org.junit.Test

class TipPercentageValue {

    @Test(expected = IllegalArgumentException::class)
    fun `throws if invalid percentage`() {
        TipPercentage(-1.0)
        TipPercentage(1.1)
    }
}
