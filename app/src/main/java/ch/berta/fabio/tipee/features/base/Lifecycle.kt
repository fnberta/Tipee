package ch.berta.fabio.tipee.features.base

import android.os.Bundle
import com.jakewharton.rxrelay2.BehaviorRelay

enum class Lifecycle {
    CREATE, ENTER, START, STOP, DESTROY
}

data class StateBundle<out T>(val bundle: Bundle, val key: String, val state: T)

class LifecycleHandler {
    val lifecycle: BehaviorRelay<Lifecycle> = BehaviorRelay.create()
    val outStateBundle: BehaviorRelay<Bundle> = BehaviorRelay.create()

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            lifecycle.accept(Lifecycle.CREATE)
        } else {
            lifecycle.accept(Lifecycle.ENTER)
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        if (outState != null) {
            outStateBundle.accept(outState)
        }
    }

    fun onStart() {
        lifecycle.accept(Lifecycle.START)
    }

    fun onStop() {
        lifecycle.accept(Lifecycle.STOP)
    }

    fun onDestroy() {
        lifecycle.accept(Lifecycle.DESTROY)
    }
}
