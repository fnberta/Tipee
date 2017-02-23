package ch.berta.fabio.tipee.features.tip

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.data.models.Country
import ch.berta.fabio.tipee.extensions.bindTo
import ch.berta.fabio.tipee.extensions.setProgressIfNotEqual
import ch.berta.fabio.tipee.extensions.setTextIfNotEqual
import ch.berta.fabio.tipee.features.base.BaseFragment
import ch.berta.fabio.tipee.features.tip.component.TipViewState
import ch.berta.fabio.tipee.features.tip.component.parseAmount
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.changes
import com.jakewharton.rxbinding.widget.itemSelections
import com.jakewharton.rxbinding.widget.textChanges
import kotlinx.android.synthetic.main.include_country_spinner.*
import kotlinx.android.synthetic.main.include_number_persons.*
import kotlinx.android.synthetic.main.include_seekbar.*
import rx.Observable

abstract class TipBaseFragment<T : TipActivityListener> : BaseFragment() {

    lateinit var listener: T
    lateinit var countryAdapter: ArrayAdapter<Country>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        @Suppress("UNCHECKED_CAST")
        listener = context as T
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCountrySpinner()
    }

    private fun setupCountrySpinner() {
        countryAdapter = ArrayAdapter<Country>(context, android.R.layout.simple_spinner_item)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCountry.adapter = countryAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupRelays()
        subscribeToState(listener.state)
    }

    open fun setupRelays() {
        Observable.merge(btPersonsPlus.clicks().map { 1 }, btPersonsMinus.clicks().map { -1 })
                .bindTo(lifecycleHandler.lifecycle)
                .subscribe(listener.personsPlusMinus)
        etPersons.textChanges()
                .bindTo(lifecycleHandler.lifecycle)
                .subscribe(listener.persons)
        sbPercentage.changes()
                .bindTo(lifecycleHandler.lifecycle)
                .subscribe(listener.percentage)
        spCountry.itemSelections()
                .bindTo(lifecycleHandler.lifecycle)
                .subscribe(listener.selectedCountry)
    }

    abstract fun subscribeToState(state: Observable<TipViewState>)

    fun renderPersons(persons: Int) {
        etPersons.setTextIfNotEqual(persons.toString())
    }

    fun renderCountries(state: TipViewState) {
        if (countryAdapter.isEmpty && state.countries.isNotEmpty()) {
            countryAdapter.addAll(state.countries)
        }
        if (spCountry.selectedItemPosition != state.selectedCountryPos) {
            spCountry.setSelection(state.selectedCountryPos)
        }
    }

    fun renderPercentage(percentage: Int) {
        sbPercentage.setProgressIfNotEqual(percentage)
        tvResult.text = getString(R.string.percentage, percentage)
    }

    fun renderAmountView(amount: Double,
                         amountFormatted: String,
                         hasFocus: Boolean,
                         amountView: EditText) {
        if (amount <= 0) {
            amountView.text.clear()
        } else {
            if (hasFocus) {
                if (parseAmount(amountView.text.toString()) != amount) {
                    amountView.setText(amount.toString())
                }
            } else {
                amountView.setTextIfNotEqual(amountFormatted)
            }
        }
    }
}