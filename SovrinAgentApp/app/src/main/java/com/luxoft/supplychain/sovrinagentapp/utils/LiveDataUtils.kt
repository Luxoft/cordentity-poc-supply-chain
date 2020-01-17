package com.luxoft.supplychain.sovrinagentapp.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

fun <T, R> LiveData<T>.map(transform: (T) -> R): LiveData<R> =
    Transformations.map(this, transform)


fun <T, R: Any> LiveData<T>.mapNotNull(transform: (T) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) { newValue ->
        val nextValue = transform(newValue)
        if(nextValue != null)
            result.value = nextValue
    }
    return result
}

fun <T, R> LiveData<T>.switch(getter: (T) -> LiveData<R>): LiveData<R> =
    Transformations.switchMap(this, getter)

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
