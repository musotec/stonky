package tech.muso.graphly.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import tech.muso.graphly.model.GraphAnimatable
import tech.muso.graphly.model.SliceDrawable
import tech.muso.graphly.model.SliceAnimatable
import tech.muso.graphly.view.WeightedDonutGraph.Companion.CIRCLE_MORPH_ANIMATION
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * Animates each point vertically from the previous position to the current position.
 */
class CircleMorphAnimator(var oldSliceValues: List<SliceDrawable>) : Animator(), GraphlyAnimator<SliceDrawable> {

    private val animator: ValueAnimator

    init {
        animator = ValueAnimator.ofFloat(0f, 1f)
        Log.e("CircleMorphAnimator", "init")
    }

    private val innerRadius = 0f
    private val outerRadius = 0f
    private val scale = 1f

    private fun Float.morphFrom(startValue: Float, animationProgress: Float): Float {
        return (this - startValue) * (animationProgress) + startValue
    }

    private fun Int.morphFrom(startValue: Int, animationProgress: Float): Int {
        return ((this - startValue) * animationProgress).roundToInt() + startValue
    }

    private fun Int.morphColorFrom(@ColorInt startColor: Int, animationProgress: Float): Int {
        return evaluateColor(animationProgress, startColor, this)
    }

    private inline fun evaluateColor(
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

    /**
     * Perform a horizontal transform to reveal the
     */
    override fun getAnimation(chartView: GraphAnimatable<SliceDrawable>?): Animator? {

        if(chartView !is SliceAnimatable) throw RuntimeException("Animator provided does not work on Non-slice graphs!") // TODO: 2/12/2020 further scope GraphlyAnimator? make it Type generic maybe?

        val data = chartView.getSlices() ?: return null
        val animatedSlices = ArrayList(data.map { it.copy() })
        if (data.isEmpty()) return null

        animator.addUpdateListener { animation ->
            // how far from 0f to 1f are we in the animation
            val animationProgress = animation.animatedValue as Float

            // NOTE: it is important we use a map instead of changing the actual slices!!
            // TODO: update the animated slices so that we don't have to make a whole bunch of objects.
            data.forEachIndexed { index, new -> oldSliceValues[index].also { old ->
                // Slice =(var radiusIn: Float, var radiusOut: Float, var thetaStart: Float, var thetaEnd: Float, var scale: Float = 1f, var xTranslation: Int = 0, var yTranslation: Int = 0)
                if (index == 2) Log.d("SLICE-MORPH", "[${animationProgress}] thetaStart: ${new.thetaStart.morphFrom(old.thetaStart, animationProgress)}, thetaEnd: ${new.thetaEnd.morphFrom(old.thetaEnd, animationProgress)}")

                with(animatedSlices[index]) {
                    radiusIn = new.radiusIn.morphFrom(old.radiusIn, animationProgress)
                    radiusOut = new.radiusOut.morphFrom(old.radiusOut, animationProgress)
                    thetaStart = new.thetaStart.morphFrom(old.thetaStart, animationProgress)
                    thetaEnd = new.thetaEnd.morphFrom(old.thetaEnd, animationProgress)
                    color = new.color.morphColorFrom(old.color, animationProgress)
                    scale = new.scale.morphFrom(old.scale, animationProgress)
                    xTranslation = new.xTranslation.morphFrom(old.xTranslation, animationProgress)
                    yTranslation = new.yTranslation.morphFrom(old.yTranslation, animationProgress)
                }
            }}

            chartView.setAnimationData(animatedSlices)
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                oldSliceValues = animatedSlices
                chartView.onAnimationEnded(CIRCLE_MORPH_ANIMATION)
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
