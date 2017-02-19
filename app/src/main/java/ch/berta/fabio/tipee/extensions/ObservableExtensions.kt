package ch.berta.fabio.tipee.extensions

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.preference.Preference
import ch.berta.fabio.tipee.features.base.Lifecycle
import ch.berta.fabio.tipee.features.base.StateBundle
import rx.Emitter
import rx.Observable
import timber.log.Timber

fun <T> Observable<T>.debug(tag: String): Observable<T> = this
        .doOnNext { Timber.d("$tag: $it") }

fun <T> Observable<T>.bindTo(lifecycle: Observable<Lifecycle>): Observable<T> = lifecycle
        .filter { it == Lifecycle.START }
        .switchMap { this.takeUntil(lifecycle.filter { it == Lifecycle.STOP }) }

fun <T : Parcelable> Observable<T>.saveForConfigChange(lifecycle: Observable<Lifecycle>,
                                                       outState: Observable<Bundle?>,
                                                       key: String,
                                                       beforeConfigChangeReducer: (T) -> T = { it }
): Observable<StateBundle<T>> = lifecycle
        .filter { it == Lifecycle.START }
        .switchMap {
            outState.withLatestFrom(this.map(beforeConfigChangeReducer),
                                    { bundle, state -> StateBundle(bundle, key, state) })
                    .takeUntil(lifecycle.filter { it == Lifecycle.STOP })
                    .doOnNext { it.bundle?.putParcelable(it.key, it.state) }
        }