package com.qa.tictactoe.util

import androidx.test.uiautomator.Condition
import androidx.test.uiautomator.UiDevice

/**
 * Adds no functionality: a thin wrapper over [UiDevice.wait] for shorter, more readable calls.
 * Waits up to [timeout] ms for [event] with UiAutomator's own polling; returns whether it happened.
 */
fun waitFor(timeout: Long = Config.WAIT_TIMEOUT, event: () -> Boolean): Boolean =
    Device.ui.wait(Condition { _: UiDevice -> event() }, timeout)
