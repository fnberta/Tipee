package ch.berta.fabio.tipee.features.tip.even

import ch.berta.fabio.tipee.features.tip.TipActivityListener
import com.jakewharton.rxrelay2.BehaviorRelay

interface TipEvenActivityListener : TipActivityListener {
    val amount: BehaviorRelay<CharSequence>
    val amountFocus: BehaviorRelay<Boolean>
    val amountClear: BehaviorRelay<Unit>
}