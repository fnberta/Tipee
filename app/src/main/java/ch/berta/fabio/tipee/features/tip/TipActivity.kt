package ch.berta.fabio.tipee.features.tip

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.data.helpers.makeSharedPrefs
import ch.berta.fabio.tipee.data.models.makeCountryMappings
import ch.berta.fabio.tipee.data.models.makeInitialCountry
import ch.berta.fabio.tipee.extensions.bindTo
import ch.berta.fabio.tipee.extensions.saveForConfigChange
import ch.berta.fabio.tipee.features.base.BaseActivity
import ch.berta.fabio.tipee.features.settings.SettingsActivity
import ch.berta.fabio.tipee.features.tip.component.*
import ch.berta.fabio.tipee.features.tip.dialogs.TipIncludedDialogFragment
import ch.berta.fabio.tipee.features.tip.dialogs.TippingNotCommonDialogFragment
import ch.berta.fabio.tipee.features.tip.even.TipEvenActivityListener
import ch.berta.fabio.tipee.features.tip.even.TipEvenFragment
import ch.berta.fabio.tipee.features.tip.uneven.TipUnevenActivityListener
import ch.berta.fabio.tipee.features.tip.uneven.TipUnevenFragment
import com.jakewharton.rxrelay.BehaviorRelay
import kotlinx.android.synthetic.main.activity_tip.*
import rx.Observable

class TipActivity : BaseActivity(), TipEvenActivityListener, TipUnevenActivityListener {

    override val persons: BehaviorRelay<CharSequence> = BehaviorRelay.create()
    override val personsPlusMinus: BehaviorRelay<Int> = BehaviorRelay.create()
    override val selectedCountry: BehaviorRelay<Int> = BehaviorRelay.create()
    override val percentage: BehaviorRelay<Int> = BehaviorRelay.create()
    override val menu: BehaviorRelay<MenuEvents> = BehaviorRelay.create()
    override val amount: BehaviorRelay<CharSequence> = BehaviorRelay.create()
    override val amountFocus: BehaviorRelay<Boolean> = BehaviorRelay.create()
    override val amountClear: BehaviorRelay<Unit> = BehaviorRelay.create()
    override val amountPerson: BehaviorRelay<TipRowAmountChange> = BehaviorRelay.create()
    override val amountFocusPerson: BehaviorRelay<TipRowFocusChange> = BehaviorRelay.create()
    override lateinit var state: Observable<TipViewState>
    val dialogShown: BehaviorRelay<String> = BehaviorRelay.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setSupportActionBar(toolbar)
        setupTabs()
        state = setupComponent(savedInstanceState)
        subscribeToState(state)
    }

    private fun setupTabs() {
        val tabsAdapter = TipTabsAdapter(supportFragmentManager)
        tabsAdapter.addFragment(TipEvenFragment(), getString(R.string.tab_even))
        tabsAdapter.addFragment(TipUnevenFragment(), getString(R.string.tab_uneven))
        viewPager.adapter = tabsAdapter
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupComponent(savedInstanceState: Bundle?): Observable<TipViewState> {
        val intent = TipIntention(activityResult, activityStarted, dialogShown, menu,
                                  personsPlusMinus, persons, selectedCountry, percentage, amount,
                                  amountFocus, amountClear, amountPerson, amountFocusPerson)
        val sharedPrefs = makeSharedPrefs(PreferenceManager.getDefaultSharedPreferences(this))
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val getInitialCountry = makeInitialCountry(sharedPrefs, telephonyManager)
        val getCountryMappings = makeCountryMappings(resources)
        return model(savedInstanceState, intent, getCountryMappings, getInitialCountry,
                     sharedPrefs.getRoundMode)
    }

    private fun subscribeToState(state: Observable<TipViewState>) {
        state.saveForConfigChange(lifecycleHandler.lifecycle, lifecycleHandler.outStateBundle,
                                  VIEW_STATE, configChangeReducer()).subscribe()
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
    }

    private fun render(state: TipViewState) {
        renderSettingsActivity(state.isOpenSettings)
        renderDialogs(state)
    }

    private fun renderSettingsActivity(openSettings: Boolean) {
        if (openSettings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, SETTINGS_REQ_CODE)
            activityStarted.call(intent.component.className)
        }
    }

    private fun renderDialogs(state: TipViewState) {
        if (state.isShowTippingNotCommonDialog) {
            TippingNotCommonDialogFragment.display(supportFragmentManager)
            dialogShown.call(TippingNotCommonDialogFragment.tag)
        }

        if (state.isShowTipIncludedDialog) {
            TipIncludedDialogFragment.display(supportFragmentManager)
            dialogShown.call(TipIncludedDialogFragment.tag)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear -> {
                menu.call(MenuEvents.RESET)
                return true
            }
            R.id.action_settings -> {
                menu.call(MenuEvents.SETTINGS)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
