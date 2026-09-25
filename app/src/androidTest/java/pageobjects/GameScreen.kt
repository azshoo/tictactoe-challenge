package com.qa.tictactoe.pageobjects

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.qa.tictactoe.util.CellLocator
import com.qa.tictactoe.util.Config
import com.qa.tictactoe.util.Device
import com.qa.tictactoe.util.waitFor
import java.util.regex.Pattern

object GameScreen {
    val BANNER = Pattern.compile(".+'s turn \\([XO]\\)|.+ Wins!|It's a Draw!")
    val NUMBER = Pattern.compile("-?\\d+")

    /** Clears the app's data and starts it: a new game with X to move and 0 : 0. */
    fun launchFresh() {
        check(Device.ui.executeShellCommand("pm clear ${Config.APP}").trim() == "Success") { "Could not clear ${Config.APP} data" }
        // pm clear reports success while the system is still killing the old process; a launch started then can be
        // killed with it.
        check(waitFor(Config.LAUNCH_TIMEOUT) { Device.ui.executeShellCommand("pidof ${Config.APP}").isBlank() }) {
            "${Config.APP} is still running after pm clear"
        }
        // Not `am start -W`: it has no timeout and blocks for minutes when the launching process is killed. Such a
        // launch is retried once.
        val started = (1..2).any {
            Device.ui.executeShellCommand("am start -n ${Config.APP}/${Config.ACTIVITY}")
            Device.ui.wait(Until.hasObject(By.pkg(Config.APP).desc(BANNER)), Config.LAUNCH_TIMEOUT)
        }
        check(started) {
            "${Config.APP} did not start. If it is not installed, pass its APK: ./gradlew connectedDebugAndroidTest -Paut=<path>"
        }
        // Rotated only once the app is on screen: a rotation set while the launcher is on top is reverted at app start.
        if (Config.ORIENTATION == "landscape") Device.ui.setOrientationLandscape() else Device.ui.setOrientationPortrait()
        check((Device.ui.displayWidth > Device.ui.displayHeight) == (Config.ORIENTATION == "landscape")) {
            "The screen did not turn ${Config.ORIENTATION}"
        }
        closeKeyboard()
        CellLocator.calibrate()
    }

    fun bannerText(): String = onScreen(By.pkg(Config.APP).desc(BANNER)).contentDescription

    fun isWinAnnounced(): Boolean = waitFor(Config.MOVE_TIMEOUT) { bannerText().endsWith(" Wins!") }

    /** Score shown on the score card: X's number is left of "vs", O's number is right of it. */
    fun score(player: String): Int {
        val vs = onScreen(By.pkg(Config.APP).desc("vs")).visibleCenter.x
        val number = Device.ui.findObjects(By.pkg(Config.APP).desc(NUMBER))
            .first { (it.visibleCenter.x < vs) == (player == "X") }
        return number.contentDescription.toInt()
    }

    fun hasScore(player: String, expected: Int): Boolean = waitFor(Config.SCORE_TIMEOUT) { score(player) == expected }

    fun name(player: String): String = nameField(player).text

    fun setName(player: String, name: String) {
        val field = nameField(player)
        field.click()
        field.text = name
        closeKeyboard()
    }

    fun resetGame() = onScreen(By.pkg(Config.APP).desc("Reset Game")).click()

    fun clearScoreboard() = onScreen(By.pkg(Config.APP).desc("Clear scoreboard")).click()

    fun nameField(player: String): UiObject2 {
        val fields = Device.ui.findObjects(By.pkg(Config.APP).clazz("android.widget.EditText")).sortedBy { it.visibleBounds.left }
        return if (player == "X") fields.first() else fields.last()
    }

    /** Scrolls only if the element is off screen, up first: most elements read here are above the board. */
    fun onScreen(selector: BySelector): UiObject2 {
        Device.ui.findObject(selector)?.let { return it }
        val area = CellLocator.scrollArea()
        return area.scrollUntil(Direction.UP, Until.findObject(selector))
            ?: checkNotNull(area.scrollUntil(Direction.DOWN, Until.findObject(selector))) { "$selector is not on screen" }
    }

    fun closeKeyboard() {
        if (Device.isKeyboardShown()) Device.ui.pressBack()
    }
}
