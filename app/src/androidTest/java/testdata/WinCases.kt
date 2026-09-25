package com.qa.tictactoe.testdata

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** [moves] are cells [row, col], X first; [winningLine] is the cells the win line must cross; [name] goes into the test name. */
@Serializable
class WinCase(val winner: String, val name: String, val winningLine: List<List<Int>>, val moves: List<List<Int>>) {
    override fun toString() = "$winner wins on $name"
}

val WIN_CASES: List<WinCase> = Json.decodeFromString(WinCase::class.java.getResource("/win_cases.json")!!.readText())
