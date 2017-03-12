package ch.berta.fabio.tipee.features.tip

import ch.berta.fabio.tipee.features.tip.component.MenuEvents
import ch.berta.fabio.tipee.features.tip.component.TipViewState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

interface TipActivityListener {
    val persons: BehaviorRelay<CharSequence>
    val personsPlusMinus: BehaviorRelay<Int>
    val selectedCountry: BehaviorRelay<Int>
    val percentage: BehaviorRelay<Int>
    val menu: BehaviorRelay<MenuEvents>
    val state: Observable<TipViewState>
}