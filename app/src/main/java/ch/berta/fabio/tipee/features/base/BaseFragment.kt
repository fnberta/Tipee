package ch.berta.fabio.tipee.features.base

import android.os.Bundle
import android.support.v4.app.Fragment

abstract class BaseFragment : Fragment() {

    val lifecycleHandler: LifecycleHandler = LifecycleHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleHandler.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        lifecycleHandler.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        lifecycleHandler.onStart()
    }

    override fun onStop() {
        super.onStop()

        lifecycleHandler.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleHandler.onDestroy()
    }
}