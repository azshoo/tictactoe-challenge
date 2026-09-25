package com.qa.tictactoe.util

import android.graphics.Bitmap
import androidx.test.services.storage.TestStorage
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Saves a screenshot and the UI tree of a failed test. The Gradle run pulls them into
 * app/build/outputs/connected_android_test_additional_output/.
 */
class FailureArtifacts : TestWatcher() {
    override fun failed(e: Throwable, description: Description) {
        val name = "${description.testClass.simpleName}.${description.methodName}"
        TestStorage().openOutputFile("$name.png").use { Device.screenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
        TestStorage().openOutputFile("$name.xml").use { Device.ui.dumpWindowHierarchy(it) }
    }
}
