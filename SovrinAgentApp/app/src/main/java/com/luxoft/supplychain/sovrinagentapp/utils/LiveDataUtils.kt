package com.luxoft.supplychain.sovrinagentapp.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

/**
 * Returns a [LiveData] that observes [this] and constructs its value using [transform]
 * */
fun <T, R> LiveData<T>.map(transform: (T) -> R): LiveData<R> =
    Transformations.map(this, transform)

/**
 * Returns a [LiveData] that observes [this] and only updates if [transform] returns a non-null value
 * */
fun <T, R: Any> LiveData<T>.mapNotNull(transform: (T) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) { newValue ->
        val nextValue = transform(newValue)
        if(nextValue != null)
            result.value = nextValue
    }
    return result
}

/**
 * Returns a [LiveData] that observes [getter] but reevaluates it when [this] updates
 * */
fun <T, R> LiveData<T>.switch(getter: (T) -> LiveData<R>): LiveData<R> =
    Transformations.switchMap(this, getter)

/**
 * Returns a [LiveData] that depends on both [source1] and [source2] and updates when they both have been set
 * */
fun <T1: Any, T2: Any, R> combine(source1: LiveData<T1>, source2: LiveData<T2>, combinator: (T1, T2) -> R): LiveData<R> {
    val mediator = MediatorLiveData<R>()

    val updater: () -> Unit = {
        val v1 = source1.value
        val v2 = source2.value

        if (v1 != null && v2 != null)
            mediator.value = combinator(v1, v2)
    }

    mediator.addSource(source1) { updater() }
    mediator.addSource(source2) { updater() }

    return mediator
}

/**
 * Conviniently constructs a [MutableLiveData] with an initial value
 * */
fun <T> MutableLiveData(initialValue: T) = MutableLiveData<T>().apply { value = initialValue }


/**
 * A [LiveData] that can be manually refreshed calling the [refresh] method
 * */
class VolatileLiveDataHolder<T>(val source: LiveData<T>) {
    private val mediator = MediatorLiveData<T>()
    val liveData: LiveData<T> = mediator

    init {
        mediator.addSource(source) { value -> mediator.value = value }
        mediator.value = source.value
    }

    @MainThread
    fun refresh() {
         mediator.value = source.value
    }
}