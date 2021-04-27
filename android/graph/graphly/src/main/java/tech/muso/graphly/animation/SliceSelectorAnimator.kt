package tech.muso.graphly.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.IntRange
import androidx.core.graphics.values
import tech.muso.graphly.model.GraphAnimatable
import tech.muso.graphly.model.SliceDrawable
import tech.muso.graphly.model.SliceAnimatable
import tech.muso.graphly.util.morphTo
import kotlin.math.roundToInt

/**
 * Animates each point vertically from the previous position to the current position.
 */
class SliceSelectorAnimator(var selectedSliceIndex: Int? = null) : Animator(), GraphlyAnimator<SliceDrawable> {

    public companion object {
        const val SLICE_SELECTION_ANIMATION = 4
    }

    private val animator: ValueAnimator
    private val animatedSlices: List<SliceDrawable>

    init {
        animator = ValueAnimator.ofFloat(0f, 1f)
        animatedSlices = ArrayList()
        Log.e("SliceSelectAnimator", "init")
    }

    private val innerRadius = 0f
    private val outerRadius = 0f
    private val scale = 1f

    private val translateDistance = 20 // TODO: 2/16/2020 scale this by the view size.
    private val expandRatio = 0.05f

    private val rotateToCenter: Boolean = false

    /**
     * Perform a horizontal transform to reveal the
     */
    override fun getAnimation(chartView: GraphAnimatable<SliceDrawable>?): ValueAnimator? {

        if(chartView !is SliceAnimatable) throw RuntimeException("Animator provided does not work on Non-slice graphs!") // TODO: 2/12/2020 further scope GraphlyAnimator? make it Type generic maybe?

        val data = chartView.getSlices() ?: return null
        val animatedSlices = ArrayList(data.map { it.copy() })
        if (data.isEmpty()) return null

        Log.e("SLICE-SELECT", "Getting Animation - $data")
        animator.addUpdateListener { animation ->
            // how far from 0f to 1f are we in the animation
            val animationProgress = animation.animatedValue as Float

            if (selectedSliceIndex == null) return@addUpdateListener

            val prevIndex = (selectedSliceIndex!! - 1) % data.size
            val nextIndex = (selectedSliceIndex!! + 1) % data.size

            val selectedWeight = data[selectedSliceIndex!!].theta

            val rotateDegrees: Float = 0.75f - data[selectedSliceIndex!!].midTheta


            val rotateMatrix: Matrix = Matrix()
            rotateMatrix.reset()
            rotateMatrix.setRotate(360*rotateDegrees)
            val matrixValues = rotateMatrix.values()
            val iHat = 1f // matrixValues[0]
            val jHat = 1f // matrixValues[4]



            // NOTE: it is important we use a map instead of changing the actual slices!!
            // TODO: update the animated slices so that we don't have to make a whole bunch of objects.
            data.forEachIndexed{ index, slice ->
                // Slice =(var radiusIn: Float, var radiusOut: Float, var thetaStart: Float, var thetaEnd: Float, var scale: Float = 1f, var xTranslation: Int = 0, var yTranslation: Int = 0)

                val thetaAdjustFactor = if (index == selectedSliceIndex) 0f else (1f - expandRatio)/(2*data.size)

//                val thetaAdjustFactor =
//                if (index == selectedSlice) {
//                    0f
//                } else if (index == prevIndex)


//                val thetaAdjustFactor =
//                    if (data[index].midTheta < selectedSlice!!) {
//                        -data[index].theta * expandRatio
//                    } else if (this.selectedSlice!! < index) {
//                        data[index].theta * expandRatio
//                    } else {
//                        0f
//                    }

                with(animatedSlices[index]) {
                    if (index == 4 && (animationProgress * 100).roundToInt() % 5 == 0) Log.w("SLICE-SELECT", "[$index] ${animationProgress*100}% - selectedSlice: $selectedSliceIndex v(${"%.2f".format(thetaVectorX)}, ${"%.2f".format(thetaVectorY)}), thetaAdjust: $thetaAdjustFactor) ::: $this")

                    if (rotateToCenter) {
                        // TODO: fix the iHat/jHat rotation (slightly skewed) and also the actual touch selection is not based on animated? it's wrong??
                        thetaStart = slice.thetaStart.morphTo(slice.thetaStart + rotateDegrees, animationProgress)
                        thetaEnd = slice.thetaEnd.morphTo(slice.thetaEnd + rotateDegrees, animationProgress)
                    }


                    if(false)
                        shiftRelativeToSlice(data[selectedSliceIndex!!], expandRatio, animationProgress)

//                    thetaStart = slice.thetaStart.morphTo((slice.thetaStart +
//                            if (index == nextIndex) expandRatio
//                            else {
//                                thetaAdjustFactor *
//                                if (slice.thetaStart < data[selectedSlice!!].farTheta) 1
//                                else if (slice.thetaStart > data[selectedSlice!!].farTheta) -1
//                                else 0
//                            }).wrapAt(1f), animationProgress).wrapAt(1f)
//
//                    thetaEnd = slice.thetaEnd.morphTo((slice.thetaEnd +
//                            if (index == prevIndex) -expandRatio
//                            else {
//                                thetaAdjustFactor *
//                                if(slice.thetaEnd < data[selectedSlice!!].farTheta) 1
//                                else if (slice.thetaEnd > data[selectedSlice!!].farTheta) -1
//                                else 0
//                            }).wrapAt(1f), animationProgress).wrapAt(1f)
                    scale = slice.scale.morphTo(
                        if (index == selectedSliceIndex) slice.scale * 1.15f else slice.scale,
                        animationProgress
                    )
                    xTranslation = slice.xTranslation.morphTo(
                        if (index == selectedSliceIndex) slice.xTranslation + (slice.thetaVectorX * translateDistance * iHat).toInt() else slice.xTranslation,
                        animationProgress
                    )
                    yTranslation = slice.yTranslation.morphTo(
                        if (index == selectedSliceIndex) slice.yTranslation + (slice.thetaVectorY * translateDistance * jHat).toInt() else slice.yTranslation,
                        animationProgress
                    )
                }
            }
//            oldSliceValues = animatedSlices
            chartView.setAnimationData(animatedSlices)
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
//                oldSliceValues = animatedSlices

                chartView.onAnimationEnded(SLICE_SELECTION_ANIMATION)
            }
        })
        return animator
    }

    override fun getStartDelay(): Long {
        return animator.startDelay
    }

    override fun setStartDelay(@IntRange(from = 0) startDelay: Long) {
        animator.startDelay = startDelay
    }

    override fun setDuration(@IntRange(from = 0) duration: Long): Animator {
        return animator
    }

    override fun getDuration(): Long {
        return animator.duration
    }

    override fun setInterpolator(timeInterpolator: TimeInterpolator?) {
        animator.interpolator = timeInterpolator
    }

    override fun isRunning(): Boolean {
        return animator.isRunning
    }
}
