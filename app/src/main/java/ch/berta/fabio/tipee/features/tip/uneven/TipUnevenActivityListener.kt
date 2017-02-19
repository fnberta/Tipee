package ch.berta.fabio.tipee.features.tip.uneven

import ch.berta.fabio.tipee.features.tip.TipActivityListener
import ch.berta.fabio.tipee.features.tip.component.TipRowAmountChange
import ch.berta.fabio.tipee.features.tip.component.TipRowFocusChange
import com.jakewharton.rxrelay.BehaviorRelay

interface TipUnevenActivityListener : TipActivityListener {

    val amountPerson: BehaviorRelay<TipRowAmountChange>
    val amountFocusPerson: BehaviorRelay<TipRowFocusChange>
}