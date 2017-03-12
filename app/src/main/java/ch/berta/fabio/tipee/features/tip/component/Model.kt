package ch.berta.fabio.tipee.features.tip.component

import ch.berta.fabio.tipee.data.models.Country
import ch.berta.fabio.tipee.extensions.debug
import ch.berta.fabio.tipee.features.base.ActivityResult
import ch.berta.fabio.tipee.features.settings.SettingsActivity
import ch.berta.fabio.tipee.features.settings.SettingsFragment
import io.reactivex.Observable

enum class MenuEvents {
    RESET, SETTINGS
}

data class TipRowAmountChange(val position: Int, val amount: CharSequence)

data class TipRowAmountParsedChange(val position: Int, val amount: Double)

data class TipRowFocusChange(val position: Int, val hasFocus: Boolean)

data class TipIntentions(
        val activityResult: Observable<ActivityResult>,
        val activityStarted: Observable<String>,
        val dialogShown: Observable<String>,
        val menu: Observable<MenuEvents>,
        val personsPlusMinus: Observable<Int>,
        val persons: Observable<CharSequence>,
        val selectedCountry: Observable<Int>,
        val percentage: Observable<Int>,
        val amount: Observable<CharSequence>,
        val amountFocus: Observable<Boolean>,
        val amountClear: Observable<Unit>,
        val amountPerson: Observable<TipRowAmountChange>,
        val amountFocusPerson: Observable<TipRowFocusChange>
)

const val NUMBER_OF_SUBSCRIBERS = 4
const val SETTINGS_REQ_CODE = 1

fun model(
        savedState: TipViewState?,
        intentions: TipIntentions,
        getCountryMappings: () -> List<Country>,
        getInitialCountry: (List<Country>) -> Country,
        getRoundMode: () -> RoundMode
): Observable<TipViewState> {
    val startState = savedState ?: createInitialState(getCountryMappings, getInitialCountry,
            getRoundMode)

    val settingsResult = intentions.activityResult
            .filter { it.requestCode == SETTINGS_REQ_CODE }
    val settingsRoundMode = settingsResult
            .filter { it.resultCode == SettingsFragment.RESULT_ROUND_CHANGED }
            .debug("settingsRoundMode")
            .map { settingsRoundModeReducer(getRoundMode) }
    val settingsCountry = settingsResult
            .filter { it.resultCode == SettingsFragment.RESULT_COUNTRY_CHANGED }
            .debug("settingsCountry")
            .map { settingsCountryReducer(getInitialCountry) }
    val dialogShown = intentions.dialogShown
            .debug("dialogShown")
            .map(::dialogShownReducer)
    val menuReset = intentions.menu
            .filter { it == MenuEvents.RESET }
            .debug("menuReset")
            .map { menuResetReducer(getInitialCountry) }
    val menuSettings = Observable.merge(
            intentions.menu
                    .filter { it == MenuEvents.SETTINGS }
                    .map { true },
            intentions.activityStarted
                    .filter { it == SettingsActivity.tag }
                    .map { false }
    )
            .debug("menuSettings")
            .map(::menuSettingsReducer)
    val personsPlusMinus = intentions.personsPlusMinus
            .debug("personsPlusMinus")
            .map(::personPlusMinusReducer)
    val persons = intentions.persons
            .filter { it.isNotEmpty() }
            .map { it.toString().toInt() }
            .debug("persons")
            .map(::personsReducer)
    val selectedCountry = intentions.selectedCountry
            .filter { it != -1 }
            .distinctUntilChanged()
            .debug("selectedCountry")
            .map(::selectedCountryReducer)
    val percentage = intentions.percentage
            .distinctUntilChanged()
            .debug("percentage")
            .map(::percentageReducer)
    val amount = intentions.amount
            .map(::parseAmount)
            .filter { it >= 0 }
            .debug("amount")
            .map(::amountReducer)
    val amountFocus = intentions.amountFocus
            .debug("isAmountFocused")
            .map(::amountFocusReducer)
    val clearAmount = intentions.amountClear
            .debug("clearAmount")
            .map { clearAmountReducer() }
    val amountPerson = intentions.amountPerson
            .map { TipRowAmountParsedChange(it.position, parseAmount(it.amount)) }
            .filter { it.amount >= 0 }
            .debug("amountPerson")
            .map(::amountPersonReducer)
    val amountFocusPerson = intentions.amountFocusPerson
            .debug("amountFocusPerson")
            .map(::amountFocusPersonReducer)

    val reducers = listOf(settingsRoundMode, settingsCountry, dialogShown, menuReset, menuSettings,
            personsPlusMinus, persons, selectedCountry, percentage, amount, amountFocus,
            clearAmount, amountPerson, amountFocusPerson)
    return Observable.merge(reducers)
            .scan(startState, { state, reducer -> reducer(state) })
            .debug("state")
            .publish()
            .autoConnect(NUMBER_OF_SUBSCRIBERS)
}

fun parseAmount(amount: CharSequence): Double {
    return if (amount.isEmpty()) {
        0.0
    } else try {
        amount.toString().toDouble()
    } catch (e: NumberFormatException) {
        -1.0
    }
}
