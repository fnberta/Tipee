package ch.berta.fabio.tipee.features.tip.even

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.extensions.bindTo
import ch.berta.fabio.tipee.extensions.setTextIfNotEqual
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

        etAmount.textChanges().subscribe(listener.amount)
        etAmount.focusChanges().subscribe(listener.amountFocus)
        ibClear.clicks().subscribe(listener.amountClear)
    }

    override fun subscribeToState(state: Observable<TipViewState>) {
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
    }

    private fun render(state: TipViewState) {
        renderAmountView(etAmount, state.amount, state.amountFormatted, state.isAmountFocused)
        renderPersons(state.persons)
        renderPercentage(state.percentage)
        renderCountries(state.countries, state.selectedCountryPos)
        renderTips(state)
    }

    private fun renderTips(state: TipViewState) {
        tvTipAmount.setTextIfNotEqual(state.tip)
        tvTotalAmount.setTextIfNotEqual(state.total)
        if (state.roundMode == RoundMode.EXACT) {
            llTipTotalExact.visibility = View.GONE
            tvTotalPerPerson.setTextIfNotEqual(state.totalPerPerson)
        } else {
            llTipTotalExact.visibility = View.VISIBLE
            tvTotalAmountExact.setTextIfNotEqual(getString(R.string.exact, state.totalExact))
            tvTipAmountExact.setTextIfNotEqual(getString(R.string.exact, state.tipExact))
            tvTotalPerPerson.setTextIfNotEqual(getString(R.string.total_per_person,
                    state.totalPerPerson, state.totalPerPersonExact))
        }
    }
}
