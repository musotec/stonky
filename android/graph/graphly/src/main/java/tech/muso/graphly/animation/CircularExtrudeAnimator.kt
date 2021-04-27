package tech.muso.graphly.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import androidx.annotation.IntRange
import tech.muso.graphly.model.GraphAnimatable
import tech.muso.graphly.model.SliceDrawable
import tech.muso.graphly.model.SliceAnimatable
import tech.muso.graphly.view.WeightedDonutGraph.Companion.CIRCLE_EXTRUDE_ANIMATION


/**
 * Animates each point vertically from the previous position to the current position.
 */
class CircularExtrudeAnimator : Animator(), GraphlyAnimator<SliceDrawable> {

    private val animator: ValueAnimator
//    private val animationPath: Path
    private var animatedSlices: List<SliceDrawable>

    init {
        animator = ValueAnimator.ofFloat(0f, 1f)
        animatedSlices = ArrayList()
    }

    private val innerRadius = 0f
    private val outerRadius = 0f
    private val scale = 1f

    /**
     * Perform a horizontal transform to reveal the
     */
    override fun getAnimation(chartView: GraphAnimatable<SliceDrawable>?): Animator? {

        if(chartView !is SliceAnimatable) throw RuntimeException("Animator provided does not work on Non-slice graphs!") // TODO: 2/12/2020 further scope GraphlyAnimator? make it Type generic maybe?

        val data = chartView.getSlices() ?: return null
        val animatedSlices = chartView.getAnimatedSlices() ?: ArrayList(data.map { it.copy() })
        if (data.isEmpty()) return null

        animator.addUpdateListener { animation ->
            // how far from 0f to 1f are we in the animation
            val animationProgress = animation.animatedValue as Float

            // NOTE: it is important we use a map instead of changing the actual slices!!
            // TODO: update the animated slices so that we don't have to make a whole bunch of objects.
            data.forEachIndexed { index, slice ->
                // Slice =(var radiusIn: Float, var radiusOut: Float, var thetaStart: Float, var thetaEnd: Float, var scale: Float = 1f, var xTranslation: Int = 0, var yTranslation: Int = 0)
                with(animatedSlices[index]) {
                    radiusIn = slice.radiusIn + 12
                    radiusOut = (slice.radiusOut - slice.radiusIn + 12) * animationProgress + (slice.radiusIn + 12)
                }
            }

            chartView.setAnimationData(animatedSlices)
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
//                oldYPoints = yPoints
                // TODO: 2/12/2020 store the finished animation values

                chartView.onAnimationEnded(CIRCLE_EXTRUDE_ANIMATION)
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
