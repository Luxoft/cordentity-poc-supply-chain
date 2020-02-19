package com.luxoft.supplychain.sovrinagentapp.utils

/**
 * Joins a list to string: "el1, el2, el3 and el4"
 * */
fun <T> List<T>.joinToStringPrettyAnd(): String = when(size) {
    0 -> ""
    1 -> first().toString()
    else -> this.dropLast(1).joinToString(separator = ", ") + " and " + this.last().toString()
}

/**
 * Joins a list of elements in a `-` separated vertical item list
 * */
fun <T> Iterable<T>.formatAsVerticalList(): String =
    this.map { e -> "  - $e"}
        .joinToString(separator = "\n")

/**
 * Abbreviates a string such that its length is not greater than [maxWidth].
 * If [this] length is greater than [maxWidth], the rest of it is replaced with [ending]
 * */
fun String.abbreviate(maxWidth: Int, ending: String = "..."): String {
    return when {
        length <= maxWidth -> return this
        maxWidth < ending.length -> "?"
        else -> this.take(maxWidth - ending.length).trimEnd() + ending
    }
}