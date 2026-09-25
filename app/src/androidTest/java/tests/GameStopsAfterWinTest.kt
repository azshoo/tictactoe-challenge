package com.qa.tictactoe.tests

import com.qa.tictactoe.pageobjects.Board
import com.qa.tictactoe.pageobjects.GameScreen
import com.qa.tictactoe.util.Config
import com.qa.tictactoe.util.waitFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import com.qa.tictactoe.util.FailureArtifacts
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameStopsAfterWinTest {

    @get:Rule
    val artifacts = FailureArtifacts()

    @Before
    fun launch() = GameScreen.launchFresh()

    @Test
    fun boardAcceptsNoMovesAfterWin() {
        // Arrange: X wins on column 1 ([1, 1], [2, 1], [3, 1])
        Board.play(listOf(1, 1), listOf(1, 2), listOf(2, 1), listOf(1, 3), listOf(3, 1))
        check(GameScreen.isWinAnnounced()) { "X should have won on column 1" }
        val bannerAfterWin = GameScreen.bannerText()

        // Act: tap the empty [3, 3]
        Board.move(3, 3)

        // Assert
        assertFalse("[3, 3] should stay empty after the game is over.", waitFor(Config.MOVE_TIMEOUT) { Board.cellState(3, 3) != "" })
        assertEquals("The result banner should not change.", bannerAfterWin, GameScreen.bannerText())
    }
}
