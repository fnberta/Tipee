package ch.berta.fabio.tipee.features.tip.uneven

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.extensions.bindTo
import ch.berta.fabio.tipee.extensions.setTextIfNotEqual
import ch.berta.fabio.tipee.features.tip.TipBaseFragment
import ch.berta.fabio.tipee.features.tip.component.*
import com.jakewharton.rxbinding2.view.focusChanges
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_uneven_split.*
import kotlinx.android.synthetic.main.row_person.view.*

class TipUnevenFragment : TipBaseFragment<TipUnevenActivityListener>() {

    private val personRows: MutableList<View> = mutableListOf()
    private val amountPerson: PublishRelay<TipRowAmountChange> = PublishRelay.create()
    private val amountFocusPerson: PublishRelay<TipRowFocusChange> = PublishRelay.create()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_uneven_split, container, false)

    override fun setupRelays() {
        super.setupRelays()

        amountPerson.subscribe(listener.amountPerson)
        amountFocusPerson.subscribe(listener.amountFocusPerson)
    }

    override fun subscribeToState(state: Observable<TipViewState>) {
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
    }

    private fun render(state: TipViewState) {
        renderPersons(state.persons)
        renderPercentage(state.percentage)
        renderCountries(state.countries, state.selectedCountryPos)
        renderTipRows(state.tipRows)
    }

    private fun renderTipRows(tipRows: List<TipRow>) {
        when {
            personRows.size > tipRows.size -> removeTipRows(tipRows)
            personRows.size < tipRows.size -> addTipRows(tipRows)
            else -> updateTipRows(tipRows)
        }
    }

    tailrec private fun removeTipRows(tipRows: List<TipRow>) {
        val iterator = personRows.subList(tipRows.size, personRows.size).iterator()
        while (iterator.hasNext()) {
            val row = iterator.next()
            llMain.removeView(row)
            iterator.remove()
        }

        if (personRows.size > tipRows.size) {
            removeTipRows(tipRows)
        }
    }

    tailrec private fun addTipRows(tipRows: List<TipRow>) {
        val personRow = activity.layoutInflater.inflate(R.layout.row_person, llMain, false)
        personRows.add(personRow)

        personRow.etAmount.textChanges()
                .map { TipRowAmountChange(personRows.indexOf(personRow), it) }
                .subscribe(amountPerson)
        personRow.etAmount.focusChanges()
                .map { TipRowFocusChange(personRows.indexOf(personRow), it) }
                .subscribe(amountFocusPerson)
        val personHint = personRows.indexOf(personRow) + 1
        personRow.etAmount.hint = getString(R.string.hint_person, personHint)
        personRow.etAmount.filters =
                arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_BILL_AMOUNT_LENGTH))

        renderPersonRowValues(personRow, tipRows.last())
        llMain.addView(personRow)

        if (personRows.size < tipRows.size) {
            addTipRows(tipRows)
        }
    }

    private fun updateTipRows(tipRows: List<TipRow>) {
        personRows
                .zip(tipRows)
                .forEach { renderPersonRowValues(it.first, it.second) }
    }

    private fun renderPersonRowValues(personRow: View, tipRow: TipRow) {
        renderAmountView(personRow.etAmount, tipRow.amount, tipRow.amountFormatted,
                tipRow.isAmountFocused)
        personRow.tvTipAmount.setTextIfNotEqual(tipRow.tip)
        personRow.tvTotalAmount.setTextIfNotEqual(tipRow.total)
    }
}