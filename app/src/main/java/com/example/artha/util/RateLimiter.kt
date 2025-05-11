// utils/RateLimiter.kt
package com.example.artha.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay

class RateLimiter(
    private val minDelayMillis: Long
) {
    private var lastExecutedTime = 0L
    private val mutex = Mutex()

    suspend fun <T> run(block: suspend () -> T): T {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastExecutedTime
            if (elapsed < minDelayMillis) {
                delay(minDelayMillis - elapsed)
            }
            lastExecutedTime = System.currentTimeMillis()
            block()
        }
    }
}
