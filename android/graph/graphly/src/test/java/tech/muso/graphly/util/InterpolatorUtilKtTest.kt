package tech.muso.graphly.util

import junit.framework.TestCase
import kotlin.math.roundToInt

class InterpolatorUtilKtTest : TestCase() {

    private inline fun Float.roundPlaces(decimalPlaces: Int): Float {
        return (this * (decimalPlaces * 10)).roundToInt() / (decimalPlaces * 10f)
    }

    fun testRoundFloat() {
        val close = 0.0999995f
        assertEquals(0.1f, close.roundPlaces(1))
    }

    fun testEdgeWrap() {
//        assertEquals(1f, 1f.wrapAt(1f))
    }

    fun testWrap() {
        assertEquals(0.1f, wrap(1.1f, 1f).roundPlaces(1))
        assertEquals(0.9f, wrap(-0.1f, 1f).roundPlaces(1))
        assertEquals(0.9f, wrap(-2.1f, 1f).roundPlaces(1))
        assertEquals(0.5f, wrap(0.5f, 1f).roundPlaces(1))
        assertEquals(0.1f, wrap(2.1f, 1f).roundPlaces(1))
    }
}