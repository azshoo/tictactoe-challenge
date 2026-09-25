package com.qa.tictactoe.util

import android.graphics.Rect
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Condition
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2

/**
 * Finds board cells by [row, col]. The app hides cell content from the accessibility tree, but each cell is a Button
 * whose node identity survives scrolling, rotation, moves and resets. Calibration maps the nodes to [row, col] once;
 * after that a cell is found by identity wherever it is scrolled.
 */
object CellLocator {
    private var cells: Map<List<Int>, UiObject2> = emptyMap()
    private var side = 0

    /**
     * Scrolls the screen down from the top, where a fresh app starts, and numbers board rows in the order they come
     * into view. A scroll step is shorter than the screen, so no row is skipped. Needs the keyboard closed.
     */
    fun calibrate() {
        val rows = mutableListOf<List<UiObject2>>()
        repeat(Config.MAX_SCROLLS) {
            for (row in rowsOnScreen()) {
                check(row.size == Config.BOARD_SIZE) { "A board row is cut off at the screen edge: ${row.size} cells visible" }
                if (row !in rows) rows.add(row)
            }
            if (rows.size == Config.BOARD_SIZE) {
                val numbered = mutableMapOf<List<Int>, UiObject2>()
                for ((r, row) in rows.withIndex()) {
                    for ((c, cell) in row.withIndex()) numbered[listOf(r + 1, c + 1)] = cell
                }
                cells = numbered
                // Scrolling cuts cells only at the top and bottom, so their width is the full side.
                side = rows.flatten().maxOf { it.visibleBounds.width() }
                return
            }
            scrollArea().scroll(Direction.DOWN, 1f)
        }
        error("Found ${rows.size} of ${Config.BOARD_SIZE} board rows after ${Config.MAX_SCROLLS} scrolls")
    }

    /** The cell's node, scrolled fully onto the screen and not covered by anything. */
    fun cell(row: Int, col: Int): UiObject2 {
        check(cells.isNotEmpty()) { "Board is not calibrated" }
        val target = checkNotNull(cells[listOf(row, col)]) { "Cell [$row, $col] is outside the board" }
        if (fullyShown(target) == null) scrollTo(row, target)
        val cell = checkNotNull(fullyShown(target)) { "Cell [$row, $col] can't be scrolled fully onto the screen" }
        describeCoveringElement(cell.visibleBounds)?.let { error("Cell [$row, $col] is covered by $it") }
        return cell
    }

    /**
     * A row above [row] on screen means the cell is further down. With no board row on screen the direction is a guess,
     * hence the second try the other way.
     */
    fun scrollTo(row: Int, target: UiObject2) {
        val onScreen = cellsOnScreen()
        val direction = if (cells.any { (address, node) -> address[0] < row && node in onScreen }) Direction.DOWN else Direction.UP
        val shown = Condition { _: UiObject2 -> fullyShown(target) != null }
        val area = scrollArea()
        area.scrollUntil(direction, shown) || area.scrollUntil(Direction.reverse(direction), shown)
    }

    /** The on-screen node of [target] if the whole cell is on screen, else null. */
    fun fullyShown(target: UiObject2): UiObject2? =
        cellsOnScreen().firstOrNull { it == target }?.takeIf { it.visibleBounds.width() == side && it.visibleBounds.height() == side }

    /** Cells on screen in rows, top to bottom, each left to right. Cells of a row share its top even when cut off. */
    fun rowsOnScreen(): List<List<UiObject2>> {
        val byTop = cellsOnScreen().groupBy { it.visibleBounds.top }
        return byTop.keys.sorted().map { top -> byTop.getValue(top).sortedBy { it.visibleBounds.left } }
    }

    fun cellsOnScreen() = Device.ui.findObjects(By.pkg(Config.APP).clazz("android.widget.Button")).filter(::isCell)

    // The app gives cells no id, text or description, and they sit in one flat list with the rest of the screen;
    // they are the only Buttons without a label.
    fun isCell(o: UiObject2) = o.contentDescription.isNullOrEmpty() && o.text.isNullOrEmpty()

    /** Gestures run in the strip left of the board: a swipe that starts on a mark drags the mark instead of scrolling. */
    fun scrollArea(): UiObject2 {
        val area = checkNotNull(Device.ui.findObject(By.pkg(Config.APP).scrollable(true))) { "The screen has no scrollable area" }
        val bounds = area.visibleBounds
        val boardLeft = cellsOnScreen().minOfOrNull { it.visibleBounds.left } ?: bounds.right
        area.setGestureMargins(0, bounds.height() / 10, bounds.right - boardLeft, bounds.height() / 10)
        return area
    }

    /**
     * A tap on a covered cell lands on whatever is on top (the keyboard, a text selection menu), so a covered cell
     * fails with the name of what covers it instead of as a wrong move. Null if nothing covers the cell.
     */
    fun describeCoveringElement(cell: Rect): String? {
        Device.otherWindows().firstOrNull { Rect.intersects(Rect().also(it::getBoundsInScreen), cell) }
            ?.let { return "window '${it.title}'" }
        return Device.ui.findObjects(By.pkg(Config.APP))
            .firstOrNull { it.childCount == 0 && !isCell(it) && Rect.intersects(it.visibleBounds, cell) }
            ?.let { "'${it.contentDescription ?: it.text ?: it.className}'" }
    }
}
