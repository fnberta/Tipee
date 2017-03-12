package ch.berta.fabio.tipee.features.tip

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.data.models.Country
import ch.berta.fabio.tipee.extensions.addAllIfEmpty
import ch.berta.fabio.tipee.extensions.setProgressIfNotEqual
import ch.berta.fabio.tipee.extensions.setSelectionIfNotSelected
import ch.berta.fabio.tipee.extensions.setTextIfNotEqual
import ch.berta.fabio.tipee.features.base.BaseFragment
import ch.berta.fabio.tipee.features.tip.component.TipViewState
import ch.berta.fabio.tipee.features.tip.component.parseAmount
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.changes
import com.jakewharton.rxbinding2.widget.itemSelections
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Observable
import kotlinx.android.synthetic.main.include_country_spinner.*
import kotlinx.android.synthetic.main.include_number_persons.*
import kotlinx.android.synthetic.main.include_seekbar.*

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
                .subscribe(listener.personsPlusMinus)
        etPersons.textChanges().subscribe(listener.persons)
        sbPercentage.changes().subscribe(listener.percentage)
        spCountry.itemSelections().subscribe(listener.selectedCountry)
    }

    abstract fun subscribeToState(state: Observable<TipViewState>)

    fun renderPersons(persons: Int) {
        etPersons.setTextIfNotEqual(persons.toString())
    }

    fun renderCountries(countries: List<Country>, selectedCountryPos: Int) {
        countryAdapter.addAllIfEmpty(countries)
        spCountry.setSelectionIfNotSelected(selectedCountryPos)
    }

    fun renderPercentage(percentage: Int) {
        sbPercentage.setProgressIfNotEqual(percentage)
        tvResult.setTextIfNotEqual(getString(R.string.percentage, percentage))
    }

    fun renderAmountView(amountView: EditText,
                         amount: Double,
                         amountFormatted: String,
                         hasFocus: Boolean) {
        if (amount <= 0) {
            amountView.text.clear()
        } else {
            if (hasFocus) {
                if (parseAmount(amountView.text) != amount) {
                    amountView.setText(amount.toString())
                }
            } else {
                amountView.setTextIfNotEqual(amountFormatted)
            }
        }
    }
}