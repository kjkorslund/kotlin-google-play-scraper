package io.github.kjkorslund.googleplayscraper

data class TimedResult<T>(val result: T, val millis: Long) {
    inline fun record(block: (Long) -> Unit): T {
        block(millis)
        return result
    }
}

inline fun <T> measureTimedResultMillis(block: () -> T): TimedResult<T> {
    val start = System.currentTimeMillis()
    val result = block()
    return TimedResult(result, System.currentTimeMillis() - start)
}