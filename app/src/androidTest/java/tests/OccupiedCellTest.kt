package com.qa.tictactoe.tests

import com.qa.tictactoe.pageobjects.Board
import com.qa.tictactoe.pageobjects.GameScreen
import com.qa.tictactoe.util.Config
import com.qa.tictactoe.util.waitFor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import com.qa.tictactoe.util.FailureArtifacts
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OccupiedCellTest {

    @get:Rule
    val artifacts = FailureArtifacts()

    @Before
    fun launch() = GameScreen.launchFresh()

    @Test
    fun tapOnOccupiedCellIsIgnored() {
        // Arrange: X takes [1, 1]
        Board.play(listOf(1, 1))

        // Act: O taps [1, 1]
        Board.move(1, 1)

        // Assert
        val overwritten = waitFor(Config.MOVE_TIMEOUT) { Board.cellState(1, 1) != "X" }
        assertFalse("Expected [1, 1] to stay X, but it shows \"${Board.cellState(1, 1)}\".", overwritten)
        val banner = GameScreen.bannerText()
        assertNotEquals("Expected O to keep the turn, but the banner shows \"$banner\".", "${GameScreen.name("X")}'s turn (X)", banner)
    }
}
