package ch.berta.fabio.tipee.features.tip.component

import ch.berta.fabio.tipee.data.models.Country
import ch.berta.fabio.tipee.features.tip.dialogs.TipIncludedDialogFragment
import ch.berta.fabio.tipee.features.tip.dialogs.TippingNotCommonDialogFragment

typealias TipViewStateReducer = (TipViewState) -> TipViewState

const val MAX_PERSONS = 20
const val MAX_BILL_AMOUNT_LENGTH = 9

fun configChangeReducer(): TipViewStateReducer = { state ->
    state.copy(isCountryAfterConfigChange = true)
}

fun settingsRoundModeReducer(getRoundMode: () -> RoundMode): TipViewStateReducer = { state ->
    state.copy(roundMode = getRoundMode())
}

fun settingsCountryReducer(getInitialCountry: (List<Country>) -> Country): TipViewStateReducer = { state ->
    val selectedCountryPos = state.countries.indexOf(getInitialCountry(state.countries))
    state.copy(selectedCountryPos = selectedCountryPos)
}

fun dialogShownReducer(dialogTag: String): TipViewStateReducer = { state ->
    when (dialogTag) {
        TippingNotCommonDialogFragment.tag -> state.copy(isShowTippingNotCommonDialog = false)
        TipIncludedDialogFragment.tag -> state.copy(isShowTipIncludedDialog = false)
        else -> state
    }
}

fun menuResetReducer(initialState: TipViewState): TipViewStateReducer = { initialState }

fun menuSettingsReducer(openSettings: Boolean): TipViewStateReducer = { state ->
    state.copy(isOpenSettings = openSettings)
}

fun personPlusMinusReducer(personPlusMinus: Int): TipViewStateReducer = { state ->
    val personsNew = state.persons + personPlusMinus
    val persons = if (personsNew in 1..MAX_PERSONS) personsNew else state.persons
    state.copy(persons = persons)
}

fun personsReducer(persons: Int): TipViewStateReducer = { state ->
    val personsAdj = if (persons in 1..MAX_PERSONS) persons else state.persons
    val countryCode = state.countries[state.selectedCountryPos].countryCode
    val formatter = getCurrencyFormatter(countryCode)
    val (tip, tipExact, total, totalExact, totalPerPerson, totalPerPersonExact) =
            calculateTip(state.amount, state.percentage, personsAdj, state.roundMode, formatter)
    val tipRows = when {
        personsAdj > state.tipRows.size -> {
            val newItems = mutableListOf<TipRow>()
            while (personsAdj > newItems.size + state.tipRows.size) {
                newItems.add(createEmptyTipRow(formatter))
            }
            state.tipRows.plus(newItems)
        }
        personsAdj < state.tipRows.size -> {
            state.tipRows.subList(0, personsAdj)
        }
        else -> state.tipRows
    }

    state.copy(
            persons = personsAdj,
            tip = tip,
            tipExact = tipExact,
            total = total,
            totalExact = totalExact,
            totalPerPerson = totalPerPerson,
            totalPerPersonExact = totalPerPersonExact,
            tipRows = tipRows
    )
}

fun selectedCountryReducer(selectedCountryPos: Int): TipViewStateReducer = { state ->
    val country = state.countries[selectedCountryPos]
    val percentage = if (state.isCountryAfterConfigChange) state.percentage else country.percentage
    val isShowTippingNotCommonDialog = if (state.isCountryAfterConfigChange)
        state.isShowTippingNotCommonDialog else percentage == 0
    val isShowTipIncludedDialog = if (state.isCountryAfterConfigChange)
        state.isShowTipIncludedDialog else country.tipIncluded
    state.copy(selectedCountryPos = selectedCountryPos,
               isCountryAfterConfigChange = false,
               percentage = percentage,
               isShowTippingNotCommonDialog = isShowTippingNotCommonDialog,
               isShowTipIncludedDialog = isShowTipIncludedDialog
    )
}

fun percentageReducer(percentage: Int): TipViewStateReducer = { state ->
    val countryCode = state.countries[state.selectedCountryPos].countryCode
    val formatter = getCurrencyFormatter(countryCode)
    val (tip, tipExact, total, totalExact, totalPerPerson, totalPerPersonExact) =
            calculateTip(state.amount, percentage, state.persons, state.roundMode, formatter)
    val tipRows = state.tipRows.map {
        val (tipSingle, totalSingle) = calculateTipSingle(it.amount, percentage, state.roundMode,
                                                          formatter)
        it.copy(tip = tipSingle, total = totalSingle)
    }
    state.copy(
            percentage = percentage,
            tip = tip,
            tipExact = tipExact,
            total = total,
            totalExact = totalExact,
            totalPerPerson = totalPerPerson,
            totalPerPersonExact = totalPerPersonExact,
            tipRows = tipRows
    )
}

fun amountReducer(amount: Double): TipViewStateReducer = { state ->
    val countryCode = state.countries[state.selectedCountryPos].countryCode
    val formatter = getCurrencyFormatter(countryCode)
    val (tip, tipExact, total, totalExact, totalPerPerson, totalPerPersonExact) =
            calculateTip(amount, state.percentage, state.persons, state.roundMode, formatter)
    state.copy(
            amount = amount,
            tip = tip,
            tipExact = tipExact,
            total = total,
            totalExact = totalExact,
            totalPerPerson = totalPerPerson,
            totalPerPersonExact = totalPerPersonExact
    )
}

fun amountFocusReducer(hasFocus: Boolean): TipViewStateReducer = { state ->
    val amountFormatted = if (!hasFocus) {
        val countryCode = state.countries[state.selectedCountryPos].countryCode
        val formatter = getCurrencyFormatter(countryCode)
        formatter.format(state.amount)
    } else state.amountFormatted

    state.copy(isAmountFocused = hasFocus, amountFormatted = amountFormatted)
}

fun clearAmountReducer(): TipViewStateReducer = { state -> state.copy(amount = 0.0) }

fun amountPersonReducer(amountChange: TipRowAmountParsedChange): TipViewStateReducer = { state ->
    val countryCode = state.countries[state.selectedCountryPos].countryCode
    val formatter = getCurrencyFormatter(countryCode)
    val tipRows = state.tipRows.mapIndexed { index, tipRow ->
        if (index == amountChange.position) {
            val (tip, total) = calculateTipSingle(amountChange.amount, state.percentage,
                                                  state.roundMode, formatter)
            tipRow.copy(amount = amountChange.amount, tip = tip, total = total)
        } else tipRow

    }

    state.copy(tipRows = tipRows)
}


fun amountFocusPersonReducer(focusChange: TipRowFocusChange): TipViewStateReducer = { state ->
    val tipRows = state.tipRows.mapIndexed { index, tipRow ->
        if (index == focusChange.position) {
            val amountFormatted = if (!focusChange.hasFocus) {
                val countryCode = state.countries[state.selectedCountryPos].countryCode
                val formatter = getCurrencyFormatter(countryCode)
                formatter.format(tipRow.amount)
            } else tipRow.amountFormatted

            tipRow.copy(hasFocus = focusChange.hasFocus, amountFormatted = amountFormatted)
        } else tipRow

    }

    state.copy(tipRows = tipRows)
}
