package io.github.siyual_park.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.util.*

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
}