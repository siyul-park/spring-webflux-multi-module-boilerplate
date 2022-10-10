package io.github.siyual_park.coroutine.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

@Suppress("OPT_IN_USAGE")
abstract class CoroutineTestHelper {
    private val mainThreadSurrogate = newSingleThreadContext("Test thread")

    @BeforeEach
    open fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterEach
    open fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    fun blocking(func: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            launch(Dispatchers.Main) {
                func(this)
            }
        }
    }
}
