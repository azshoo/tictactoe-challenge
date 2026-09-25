package com.qa.tictactoe.pageobjects

import com.qa.tictactoe.util.CellLocator
import com.qa.tictactoe.util.CellVision
import com.qa.tictactoe.util.Config
import com.qa.tictactoe.util.Device
import com.qa.tictactoe.util.VisionConfig
import com.qa.tictactoe.util.waitFor

/** The app keeps marks and the win line out of the accessibility tree, so they are read from a screenshot by colour. */
object Board {
    /** A single move that is not waited for: for moves the game should reject. */
    fun move(row: Int, col: Int) = CellLocator.cell(row, col).click()

    /** Waits for each mark before the next move: a move that comes sooner gives one player two moves. */
    fun play(vararg moves: List<Int>) {
        for ((row, col) in moves) {
            val cell = CellLocator.cell(row, col)
            val bounds = cell.visibleBounds
            cell.click()
            check(waitFor(Config.MOVE_TIMEOUT) { CellVision.mark(Device.screenshot(), bounds) != "" }) { "Expected a mark in [$row, $col] after the move, but the cell is still empty" }
        }
    }

    /** What the cell shows: "X", "O" or "" when empty. */
    fun cellState(row: Int, col: Int): String = CellVision.mark(Device.screenshot(), CellLocator.cell(row, col).visibleBounds)

    fun isLineCrossed(cells: List<List<Int>>): Boolean {
        for ((row, col) in cells) {
            val bounds = CellLocator.cell(row, col).visibleBounds
            if (!waitFor { CellVision.hasColor(Device.screenshot(), bounds, VisionConfig.LINE_COLORS) }) return false
        }
        return true
    }
}
