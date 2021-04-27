@file:Suppress("NOTHING_TO_INLINE", "WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET_ON_TYPE")

package tech.muso.graphly.util

import androidx.annotation.ColorInt
import kotlin.math.pow
import kotlin.math.roundToInt

inline fun wrap(value: Float, max: Float): Float {
    return (( value % max ) + max ) % max
}

inline fun Float.wrapAt(maxValue: Float): Float {
    return wrap(this, maxValue)
}

inline fun Float.morphFrom(startValue: Float, animationProgress: Float): Float {
    return (this - startValue) * (animationProgress) + startValue
}

inline fun Int.morphFrom(startValue: Int, animationProgress: Float): Int {
    return ((this - startValue) * animationProgress).roundToInt() + startValue
}

inline fun Int.morphTo(endValue: Int, animationProgress: Float): Int {
    return ((endValue - this) * animationProgress).roundToInt() + this
}

inline fun Float.morphTo(endValue: Float, animationProgress: Float): Float {
    return (endValue - this) * (animationProgress) + this
}

inline fun Int.morphColorFrom(@ColorInt startColor: Int, animationProgress: Float): Int {
    return evaluateColor(animationProgress, startColor, this)
}

inline fun evaluateColor(
    fraction: Float,
    startColor: Int,
    endColor: Int
): Int {
    val startA = (startColor shr 24 and 0xff) / 255.0f
    var startR = (startColor shr 16 and 0xff) / 255.0f
    var startG = (startColor shr 8 and 0xff) / 255.0f
    var startB = (startColor and 0xff) / 255.0f
    val endA = (endColor shr 24 and 0xff) / 255.0f
    var endR = (endColor shr 16 and 0xff) / 255.0f
    var endG = (endColor shr 8 and 0xff) / 255.0f
    var endB = (endColor and 0xff) / 255.0f
    // convert from sRGB to linear
    startR = startR.toDouble().pow(2.2).toFloat()
    startG = startG.toDouble().pow(2.2).toFloat()
    startB = startB.toDouble().pow(2.2).toFloat()
    endR = endR.toDouble().pow(2.2).toFloat()
    endG = endG.toDouble().pow(2.2).toFloat()
    endB = endB.toDouble().pow(2.2).toFloat()
    // compute the interpolated color in linear space
    var a = startA + fraction * (endA - startA)
    var r = startR + fraction * (endR - startR)
    var g = startG + fraction * (endG - startG)
    var b = startB + fraction * (endB - startB)
    // convert back to sRGB in the [0..255] range
    a *= 255.0f
    r = r.toDouble().pow(1.0 / 2.2).toFloat() * 255.0f
    g = g.toDouble().pow(1.0 / 2.2).toFloat() * 255.0f
    b = b.toDouble().pow(1.0 / 2.2).toFloat() * 255.0f
    return a.roundToInt() shl 24 or
            (r.roundToInt() shl 16) or
            (g.roundToInt() shl 8) or
            b.roundToInt()
}