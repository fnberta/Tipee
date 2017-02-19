package ch.berta.fabio.tipee.features.base

import android.os.Bundle
import android.os.Parcelable
import com.jakewharton.rxrelay.BehaviorRelay

enum class Lifecycle {
    CREATE, ENTER, START, STOP, DESTROY
}

data class StateBundle<out T>(val bundle: Bundle?, val key: String, val state: T)

fun <T : Parcelable> startWithSavedState(savedState: Bundle?, key: String, initialState: T): T {
    @Suppress("UNCHECKED_CAST")
    return if (savedState != null && savedState.containsKey(key)) savedState.get(key) as T else initialState
}

class LifecycleHandler {
    val lifecycle: BehaviorRelay<Lifecycle> = BehaviorRelay.create()
    val outStateBundle: BehaviorRelay<Bundle?> = BehaviorRelay.create()

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            lifecycle.call(Lifecycle.CREATE)
        } else {
            lifecycle.call(Lifecycle.ENTER)
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        outStateBundle.call(outState)
    }

    fun onStart() {
        lifecycle.call(Lifecycle.START)
    }

    fun onStop() {
        lifecycle.call(Lifecycle.STOP)
    }

    fun onDestroy() {
        lifecycle.call(Lifecycle.DESTROY)
    }
}
