package com.qa.tictactoe.tests

import com.qa.tictactoe.pageobjects.Board
import com.qa.tictactoe.pageobjects.GameScreen
import com.qa.tictactoe.util.FailureArtifacts
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NinthMoveWinTest {

    @get:Rule
    val artifacts = FailureArtifacts()

    @Before
    fun launch() = GameScreen.launchFresh()

    @Test
    fun ninthMoveWinIsAnnounced() {
        // Arrange: eight moves, no line yet
        Board.play(listOf(1, 1), listOf(1, 2), listOf(2, 2), listOf(1, 3), listOf(2, 3), listOf(2, 1), listOf(3, 1), listOf(3, 2))

        // Act: X takes the last free cell, completing [1, 1], [2, 2], [3, 3]
        Board.play(listOf(3, 3))

        // Assert
        val winAnnounced = GameScreen.isWinAnnounced()
        assertTrue("Expected the 9th-move win to be announced, but the banner shows \"${GameScreen.bannerText()}\".", winAnnounced)
    }
}
