// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import org.junit.Test // An annotation that marks a function as a test case.

import org.junit.Assert.* // Imports all the assertion functions from JUnit, like assertEquals.

/**
 * This is a Local Unit Test class. Unit tests run on your local computer's JVM (Java Virtual Machine),
 * not on an Android device. This makes them very fast.
 * They are used to test small, isolated pieces of code (units) that do not depend on the Android framework.
 * For example, you could use a unit test to check if a mathematical function in your code is returning the correct result.
 *
 * For more information, see the official Android testing documentation: http://d.android.com/tools/testing
 */
class ExampleUnitTest {
    /**
     * This is a simple example of a test case, marked by the '@Test' annotation.
     * It checks if the addition of 2 + 2 correctly equals 4.
     * This is a basic sanity check to ensure that the testing framework is working correctly.
     */
    @Test
    fun addition_isCorrect() {
        // 'assertEquals' is an assertion that checks if the expected value (4) is equal to the actual value (2 + 2).
        // If they are not equal, the test will fail.
        assertEquals(4, 2 + 2)
    }
}
