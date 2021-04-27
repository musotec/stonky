package tech.muso.demo.graph.spark.graph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

// TODO: rename class (horizontal bounded scrub?)
data class GraphRangeSelector(var startX: Float? = null, var endX: Float? = null) {

    val scrubLineWidth = 4f
    var scrubColor = Color.WHITE
    set(value) {
        scrubLinePaint.color = value
        field = value
    }
    var selectionBgColor = Color.WHITE
        set(value) {
            selectionBackgroundPaint.color = value
            field = value
        }

    inline val isRangeBound get() = endX != null

    private val scrubLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = scrubLineWidth
        color = scrubColor
        strokeCap = Paint.Cap.ROUND
    }

    private val selectionBackgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = selectionBgColor
        alpha = 0x10    // FIXME: should not override user alpha
    }

//    private val scrubLineStartPath = Path()
//    private val scrubLineEndPath = Path()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Canvas.drawVerticalAt(x: Float) {
        drawLine(x, 0f, x, height.toFloat(), scrubLinePaint)
    }

    fun reset() {
        startX = null
        endX = null
    }

    fun adjustPoints() {
        startX?.let { start ->
            endX?.let { end ->
                // swap points if needed
                if (start > end) {
                    startX = end
                    endX = start
                    Log.e("Scrub", "Swapped Start/End $start, $end -> $startX, $endX")
                }
            }
        }
    }

    fun drawBackground(canvas: Canvas) {
        if (startX == null || endX == null) return
        canvas.drawRect(startX!!, 0f, endX!!, canvas.height.toFloat(), selectionBackgroundPaint)
    }

    fun draw(canvas: Canvas) {
        // TODO: make vertical flavor
        startX?.let { canvas.drawVerticalAt(it) }
        endX?.let { canvas.drawVerticalAt(it) }
    }

}