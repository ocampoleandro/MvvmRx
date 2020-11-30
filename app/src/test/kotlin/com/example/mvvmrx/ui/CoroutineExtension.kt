package com.example.mvvmrx.ui

import com.example.mvvmrx.util.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.*

/**
 * Junit 5 Extension that gives access to a [TestCoroutineDispatcher] and [TestCoroutineScope].
 *
 * It will cleanup the [testDispatcher] after each test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineExtension(
    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
): BeforeEachCallback, AfterEachCallback, ParameterResolver {

    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean {
        return parameterContext?.parameter?.type == DispatcherProvider::class.java ||
                parameterContext?.parameter?.type == TestCoroutineScope::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {
        return when(parameterContext!!.parameter!!.type) {
            DispatcherProvider::class.java -> DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
            TestCoroutineScope::class.java -> TestCoroutineScope()
            else -> throw IllegalArgumentException("Parameter not supported")
        }
    }
}