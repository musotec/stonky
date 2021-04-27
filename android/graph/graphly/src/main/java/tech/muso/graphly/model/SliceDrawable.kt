package tech.muso.graphly.model

import tech.muso.graphly.util.morphTo
import tech.muso.graphly.util.wrapAt
import tech.muso.stonky.common.PortfolioSlice
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

interface GraphAnimatable<T> {
    fun setAnimationData(data: List<T>) // do we want to force a return?
    fun onAnimationEnded(type: Int)
}

/**
 * Interface to be implemented by Views wishing to animate slices.
 */
interface SliceAnimatable : GraphAnimatable<SliceDrawable> {
    fun getSlices(): List<SliceDrawable>?
    fun getAnimatedSlices(): List<SliceDrawable>?
}

inline fun Float.rotateClockwiseBy(degrees: Float): Float {
    return (this + degrees).wrapAt(1f)
}

/**
 * theta is from 0f to 1f
 * radius is any float value in pixels
 * translation is integer translation
 */
data class SliceDrawable(var slice: PortfolioSlice, var radiusIn: Float, var radiusOut: Float, var thetaStart: Float, var thetaEnd: Float, var color: Int, var scale: Float = 1f, var xTranslation: Int = 0, var yTranslation: Int = 0) {
    val theta: Float inline get() = thetaEnd - thetaStart
    val midTheta: Float inline get() = thetaStart + theta/2
    val farTheta: Float inline get() = if (midTheta > 0.5f) midTheta - 0.5f else midTheta + 0.5f
    val sliceWidth: Float inline get() = radiusOut - radiusIn
    val visibleWidth: Float inline get() = sliceWidth * scale

    val thetaVectorX: Float inline get() = cos(midTheta * 2 * PI).toFloat()
    val thetaVectorY: Float inline get() = sin(midTheta * 2 * PI).toFloat()

    private var _thetaStart: Float? = null
    private var _thetaEnd: Float? = null

//    init {
////        if (thetaEnd == 1f) thetaEnd = 0.999f
//    }

    fun shiftRelativeToSlice(selectedSlice: SliceDrawable, byTotalAmount: Float, progress: Float = 1f) {
        val sliceThetaStart: Float = selectedSlice.thetaStart
        val sliceThetaEnd: Float = selectedSlice.thetaEnd

        if (this == selectedSlice) return

//        val rotateBy: Float = 0.5f - selectedSlice.farTheta

        if (_thetaStart == null)
            _thetaStart = thetaStart//.rotateClockwiseBy(rotateBy)
        if (_thetaEnd == null)
            _thetaEnd = thetaEnd//.rotateClockwiseBy(rotateBy)

//        _thetaStart?.let { start -> _thetaEnd?.let { end ->
//            var targetStart: Float = start
//            var targetEnd: Float = end
//
//
//            var partialAmount: Float = 0f
//
//            // get distance from the selected slice starting edge
//            partialAmount = (1f - (start - selectedSlice.thetaStart.rotateClockwiseBy(rotateBy)))
//
//            // if start position is above midpoint
//            if (start >= 0.5f) {
//                // shrink it down
//                targetStart -= partialAmount * byTotalAmount
//            } else {
//                // otherwise grow it
//                targetStart += partialAmount * byTotalAmount
//            }
//
//            // get distance from the selected slice trailing edge
//            partialAmount = (1f - (end - selectedSlice.thetaEnd.rotateClockwiseBy(rotateBy)))
//
//            if (end >= 0.5f) {
//                targetEnd -= partialAmount * byTotalAmount
//            } else {
//                targetEnd += partialAmount * byTotalAmount
//            }
//
//            // now that we have adjusted all the angles, we need to un-rotate them
//            start.rotateClockwiseBy(-rotateBy)
//            end.rotateClockwiseBy(-rotateBy)
//            targetStart.rotateClockwiseBy(-rotateBy)
//            targetEnd.rotateClockwiseBy(-rotateBy)
//
//            thetaStart = _thetaStart?.morphTo(targetStart, progress) ?: thetaStart
//            thetaEnd = _thetaEnd?.morphTo(targetEnd, progress) ?: thetaEnd
//        }}

        ///
        //
        // below is "better" but broken animation
        //
        ///

        var targetStart: Float = _thetaStart!!
        var targetEnd: Float = _thetaEnd!!
        var partialAmount: Float

        // if the slice start position is less than the opposite side (far theta)
        if (_thetaStart!! >= selectedSlice.farTheta) {
            partialAmount = (1f - (_thetaStart ?: 0 - selectedSlice.thetaStart))
            targetStart -= partialAmount * byTotalAmount
        } else {
            partialAmount = (1f - (_thetaStart ?: 0 - selectedSlice.thetaStart))
            targetStart += partialAmount * byTotalAmount
        }

        if ((_thetaEnd!!) >= (selectedSlice.farTheta)) {
            partialAmount = (1f - (_thetaEnd ?: 0 - selectedSlice.thetaEnd))
            targetEnd -= partialAmount * byTotalAmount
        } else {
            partialAmount = (1f - (_thetaEnd ?: 0 - selectedSlice.thetaEnd))
            targetEnd += partialAmount * byTotalAmount
        }

        thetaStart = _thetaStart?.morphTo(targetStart, progress) ?: thetaStart
        thetaEnd = _thetaEnd?.morphTo(targetEnd, progress) ?: thetaEnd

        // TODO: 2/17/2020 determine how this works when we animate during this animation in progress??
        if (progress >= 1f) {
            _thetaStart = null
            _thetaEnd = null
        }
    }

    override fun toString(): String {
        return "${super.toString()}{thetaStart: $thetaStart, thetaEnd: $thetaEnd, color: #${Integer.toHexString(color)}, scale: $scale, dx: $xTranslation, dY: $yTranslation"
    }

//    private fun copy(): Slice {
//        return Slice(radiusIn, radiusOut, thetaStart, thetaEnd, color, scale, xTranslation, yTranslation)
//    }
}