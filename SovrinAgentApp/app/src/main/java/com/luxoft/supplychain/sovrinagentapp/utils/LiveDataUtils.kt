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
