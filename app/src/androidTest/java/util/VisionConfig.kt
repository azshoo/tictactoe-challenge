package com.qa.tictactoe.util

/** Inclusive OpenCV HSV range: H 0..180, S and V 0..255. */
data class HsvRange(val min: List<Int>, val max: List<Int>)

object VisionConfig {
    val yaml = loadYaml("vision.yml")

    val X_COLORS = colors("x_colors")
    val O_COLORS = colors("o_colors")
    val LINE_COLORS = colors("line_colors")
    val MARK_THRESHOLD = yaml["mark_threshold"] as Double
    val CELL_INSET = yaml["cell_inset"] as Double

    @Suppress("UNCHECKED_CAST")
    fun colors(key: String) =
        (yaml[key] as List<Map<String, List<Int>>>).map { HsvRange(it.getValue("min"), it.getValue("max")) }
}
