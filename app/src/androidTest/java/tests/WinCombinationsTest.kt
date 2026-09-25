package com.qa.tictactoe.tests

import com.google.common.truth.Expect
import com.qa.tictactoe.pageobjects.Board
import com.qa.tictactoe.pageobjects.GameScreen
import com.qa.tictactoe.testdata.WIN_CASES
import com.qa.tictactoe.testdata.WinCase
import com.qa.tictactoe.util.Config
import com.qa.tictactoe.util.FailureArtifacts
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

        // Assert
        expect.withMessage("The win line should cross ${case.name}.").that(Board.isLineCrossed(case.winningLine)).isTrue()
        expect.withMessage("The game should announce a win.").that(GameScreen.isWinAnnounced()).isTrue()
        expect.withMessage("The banner should name the winner.").that(GameScreen.bannerText()).isEqualTo("$winnerName Wins!")
        // Wait first, then build the message: it shows the score as it is after the wait.
        val winnerScored = GameScreen.hasScore(case.winner, 1)
        expect.withMessage("The winner should get 1 point, has ${GameScreen.score(case.winner)}.").that(winnerScored).isTrue()
        val loserScored = waitFor(Config.SCORE_TIMEOUT) { GameScreen.score(loser) != 0 }
        expect.withMessage("The loser should get no points, has ${GameScreen.score(loser)}.").that(loserScored).isFalse()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = WIN_CASES
    }
}
