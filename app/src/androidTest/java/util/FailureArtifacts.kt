package com.qa.tictactoe.util

import android.graphics.Bitmap
import androidx.test.services.storage.TestStorage
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Saves a screenshot and the UI tree of a failed test. The Gradle run pulls them into
 * app/build/outputs/connected_android_test_additional_output/.
 *
 * Checks in tests throw AssertionError: such a failure is a bug the test found. Anything else means the test could
 * not run its scenario; it is marked with a broken.<test>.txt file, which fails the build (see app/build.gradle.kts).
 */
class FailureArtifacts : TestWatcher() {
    override fun failed(e: Throwable, description: Description) {
        val name = "${description.testClass.simpleName}.${description.methodName}"
        TestStorage().openOutputFile("$name.png").use { Device.screenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
        TestStorage().openOutputFile("$name.xml").use { Device.ui.dumpWindowHierarchy(it) }
        if (e !is AssertionError) {
            TestStorage().openOutputFile("broken.$name.txt").use { it.write(e.stackTraceToString().toByteArray()) }
        }
    }
}
