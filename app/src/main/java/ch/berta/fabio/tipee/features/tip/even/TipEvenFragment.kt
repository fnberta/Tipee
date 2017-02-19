package ch.berta.fabio.tipee.features.tip.even

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.extensions.bindTo
import ch.berta.fabio.tipee.features.tip.TipBaseFragment
import ch.berta.fabio.tipee.features.tip.component.RoundMode
import ch.berta.fabio.tipee.features.tip.component.TipViewState
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.view.focusChanges
import com.jakewharton.rxbinding.widget.textChanges
import kotlinx.android.synthetic.main.fragment_even_split.*
import rx.Observable

class TipEvenFragment : TipBaseFragment<TipEvenActivityListener>() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_even_split, container, false)

    override fun setupRelays() {
        super.setupRelays()

        etAmount.textChanges().bindTo(lifecycleHandler.lifecycle).subscribe(listener.amount)
        etAmount.focusChanges().bindTo(lifecycleHandler.lifecycle).subscribe(listener.amountFocus)
        ibClear.clicks().bindTo(lifecycleHandler.lifecycle).subscribe(listener.amountClear)
    }

    override fun subscribeToState(state: Observable<TipViewState>) {
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
    }

    private fun render(state: TipViewState) {
        renderAmountView(state.amount, state.amountFormatted, state.isAmountFocused, etAmount)
        renderPersons(state.persons)
        renderPercentage(state.percentage)
        renderCountries(state)
        renderTips(state)
    }

    private fun renderTips(state: TipViewState) {
        tvTipAmount.text = state.tip
        tvTotalAmount.text = state.total
        if (state.roundMode == RoundMode.EXACT) {
            llTipTotalExact.visibility = View.GONE
            tvTotalPerPerson.text = state.totalPerPerson
        } else {
            llTipTotalExact.visibility = View.VISIBLE
            tvTotalAmountExact.text = getString(R.string.exact, state.totalExact)
            tvTipAmountExact.text = getString(R.string.exact, state.tipExact)
            tvTotalPerPerson.text = getString(R.string.total_per_person, state.totalPerPerson,
                                              state.totalPerPersonExact)
        }
    }
}
