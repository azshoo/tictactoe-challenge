package com.qa.tictactoe.util

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.roundToInt
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

/** Rectangles are in screen pixels, the same coordinates as accessibility bounds, so they crop the screenshot as is. */
object CellVision {
    init {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
    }

    fun hasColor(screen: Bitmap, cell: Rect, colors: List<HsvRange>): Boolean {
        val hsv = hsv(screen, inset(cell, VisionConfig.CELL_INSET))
        try {
            return count(hsv, colors).toDouble() / hsv.total() > VisionConfig.MARK_THRESHOLD
        } finally {
            hsv.release()
        }
    }

    /** What the cell shows: "X", "O" or "" when empty. */
    fun mark(screen: Bitmap, cell: Rect): String {
        val hasX = hasColor(screen, cell, VisionConfig.X_COLORS)
        val hasO = hasColor(screen, cell, VisionConfig.O_COLORS)
        check(!(hasX && hasO)) { "Cell at $cell shows both X and O colours" }
        return when {
            hasX -> "X"
            hasO -> "O"
            else -> ""
        }
    }

    private fun inset(r: Rect, share: Double): Rect {
        val dx = (r.width() * share).roundToInt()
        val dy = (r.height() * share).roundToInt()
        return Rect(r.left + dx, r.top + dy, r.right - dx, r.bottom - dy)
    }

    private fun hsv(screen: Bitmap, r: Rect): Mat {
        val rgba = Mat()
        Utils.bitmapToMat(Bitmap.createBitmap(screen, r.left, r.top, r.width(), r.height()), rgba)
        val rgb = Mat()
        Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB)
        rgba.release()
        val hsv = Mat()
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV)
        rgb.release()
        return hsv
    }

    private fun count(hsv: Mat, colors: List<HsvRange>): Int {
        val mask = Mat.zeros(hsv.size(), CvType.CV_8UC1)
        val part = Mat()
        for (range in colors) {
            Core.inRange(hsv, scalar(range.min), scalar(range.max), part)
            Core.bitwise_or(mask, part, mask)
        }
        val n = Core.countNonZero(mask)
        mask.release()
        part.release()
        return n
    }

    private fun scalar(hsv: List<Int>) = Scalar(hsv[0].toDouble(), hsv[1].toDouble(), hsv[2].toDouble())
}
