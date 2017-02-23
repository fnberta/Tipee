package ch.berta.fabio.tipee.features.settings

import android.os.Bundle
import ch.berta.fabio.tipee.R
import ch.berta.fabio.tipee.features.base.BaseActivity
import ch.berta.fabio.tipee.features.tip.dialogs.RoundingDownNotAdvisedDialogFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity(),
                         RoundingDownNotAdvisedDialogFragment.RoundingDownNotAdvisedListener {

    companion object {
        val tag = SettingsActivity::class.java.canonicalName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            SettingsFragment.add(supportFragmentManager)
        }
    }

    override fun setToRoundUp() {
        SettingsFragment.find(supportFragmentManager).setToRoundUp()
    }
}