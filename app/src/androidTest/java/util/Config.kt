package com.qa.tictactoe.util

import androidx.test.platform.app.InstrumentationRegistry
import org.yaml.snakeyaml.Yaml

fun readAsset(path: String): String =
    InstrumentationRegistry.getInstrumentation().context.assets.open(path).bufferedReader().use { it.readText() }

fun loadYaml(path: String): Map<String, Any> = Yaml().load(readAsset(path))

object Config {
    val yaml = loadYaml("config.yml")

    val APP = yaml["app"] as String
    val ACTIVITY = yaml["activity"] as String
    val ORIENTATION = InstrumentationRegistry.getArguments().getString("orientation") ?: yaml["orientation"] as String
    val BOARD_SIZE = yaml["board_size"] as Int
    val WAIT_TIMEOUT = (yaml["wait_timeout"] as Int).toLong()
    val MOVE_TIMEOUT = (yaml["move_timeout"] as Int).toLong()
    val SCORE_TIMEOUT = (yaml["score_timeout"] as Int).toLong()
    val LAUNCH_TIMEOUT = (yaml["launch_timeout"] as Int).toLong()
    val MAX_SCROLLS = yaml["max_scrolls"] as Int
}
