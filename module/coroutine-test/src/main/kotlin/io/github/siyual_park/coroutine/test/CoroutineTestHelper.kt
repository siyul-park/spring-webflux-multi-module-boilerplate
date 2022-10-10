package io.github.siyual_park.coroutine.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class CoroutineTestHelper {
    @BeforeEach
    open fun setUp() {
    }

    @AfterEach
    open fun tearDown() {
    }

    fun blocking(func: suspend CoroutineScope.() -> Unit) {
        runBlocking(Dispatchers.Default) {
            func(this)
        }
    }
}
