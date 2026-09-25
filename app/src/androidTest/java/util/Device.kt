package com.qa.tictactoe.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.UiAutomation
import android.graphics.Bitmap
import android.view.accessibility.AccessibilityWindowInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice

object Device {
    init {
        // UiAutomator makes every tree query wait for 500 ms of UI quiet; the tests wait for changes explicitly.
        Configurator.getInstance().waitForIdleTimeout = 0
    }

    val ui: UiDevice get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun otherWindows(): List<AccessibilityWindowInfo> = automation().windows.filter {
        it.type != AccessibilityWindowInfo.TYPE_APPLICATION && it.type != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY
    }

    fun isKeyboardShown() = automation().windows.any { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }

    fun screenshot(): Bitmap =
        checkNotNull(automation().takeScreenshot()) { "Screenshot failed" }.copy(Bitmap.Config.ARGB_8888, false)

    /** UiAutomation that reports all windows, not only the active one. */
    private fun automation(): UiAutomation {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val info = automation.serviceInfo
        if (info.flags and AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS == 0) {
            info.flags = info.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            automation.serviceInfo = info
        }
        return automation
    }
}
