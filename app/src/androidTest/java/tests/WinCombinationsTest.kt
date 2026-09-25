package com.qa.tictactoe.tests

import com.google.common.truth.Expect
import com.qa.tictactoe.pageobjects.Board
import com.qa.tictactoe.pageobjects.GameScreen
import com.qa.tictactoe.testdata.WIN_CASES
import com.qa.tictactoe.testdata.WinCase
import com.qa.tictactoe.util.Config
import com.qa.tictactoe.util.FailureArtifacts
import com.qa.tictactoe.util.assert
import com.qa.tictactoe.util.waitFor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class WinCombinationsTest(private val case: WinCase) {

    // Outer rule, so it also sees the failures Expect reports after the test.
    @get:Rule(order = 0)
    val artifacts = FailureArtifacts()

    @get:Rule(order = 1)
    val expect: Expect = Expect.create()

    @Before
    fun launch() = GameScreen.launchFresh()

    @Test
    fun handlesWin() {
        // Arrange
        val winnerName = GameScreen.name(case.winner)
        val loser = if (case.winner == "X") "O" else "X"

        // Act
        Board.play(*case.moves.toTypedArray())

        // Assert. Each check waits first and then builds its message, so the message shows the screen after the wait.
        expect.assert(Board.isLineCrossed(case.winningLine), "Expected the win line to cross ${case.name}, but it was not found there.")
        val winAnnounced = GameScreen.isWinAnnounced()
        expect.assert(winAnnounced, "Expected a win to be announced, got \"${GameScreen.bannerText()}\" instead.")
        val banner = GameScreen.bannerText()
        expect.assert(banner == "$winnerName Wins!", "Expected the result banner to name $winnerName, got \"$banner\" instead.")
        val winnerScored = GameScreen.hasScore(case.winner, 1)
        expect.assert(winnerScored, "Expected $winnerName to get 1 point, got ${GameScreen.score(case.winner)} instead.")
        val loserScored = waitFor(Config.SCORE_TIMEOUT) { GameScreen.score(loser) != 0 }
        expect.assert(!loserScored, "Expected ${GameScreen.name(loser)} to have no points, got ${GameScreen.score(loser)} instead.")
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = WIN_CASES
    }
}
