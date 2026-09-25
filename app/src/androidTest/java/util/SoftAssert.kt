package com.qa.tictactoe.util

import com.google.common.truth.Expect

/** Soft assertion: if [condition] is false, records a failure with exactly [message] and lets the test go on. */
fun Expect.assert(condition: Boolean, message: String) {
    if (!condition) withMessage(message).fail()
}
