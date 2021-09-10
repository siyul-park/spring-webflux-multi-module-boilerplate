package io.github.siyual_park.data

import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
open class CoroutineTest {
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

    fun async(func: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            launch(Dispatchers.Main) {
                func(this)
            }
        }
    }
}