// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.test.platform.app.InstrumentationRegistry // Provides access to the application instrumentation environment.
import androidx.test.ext.junit.runners.AndroidJUnit4 // A special test runner that allows JUnit 4 tests to run on an Android device.

import org.junit.Test // An annotation that marks a function as a test case.
import org.junit.runner.RunWith // An annotation that tells JUnit which test runner to use.

import org.junit.Assert.* // Imports all the assertion functions from JUnit, like assertEquals.

/**
 * This is an Instrumented Test class. Instrumented tests are tests that run on a physical Android device or an emulator.
 * They have access to the full Android framework, including things like Context, which makes them suitable for testing
 * UI, database, and other parts of your app that depend on the Android system.
 *
 * The '@RunWith(AndroidJUnit4::class)' annotation tells the testing framework to use the AndroidJUnit4 runner to execute these tests.
 *
 * For more information, see the official Android testing documentation: http://d.android.com/tools/testing
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    /**
     * This is a test case, marked by the '@Test' annotation.
     * This specific test checks if the application's context is correctly retrieved and if the package name is what we expect.
     * It's a simple sanity check to make sure the test environment is set up correctly.
     */
    @Test
    fun useAppContext() {
        // Get the context of the app that is being tested.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // 'assertEquals' is an assertion. It checks if two values are equal. If they are not, the test will fail.
        // Here, we are asserting that the package name of our app context is "com.kimLunation.moon".
        assertEquals("com.kimLunation.moon", appContext.packageName)
    }
}
