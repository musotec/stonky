package tech.muso.graphly.animation

import android.animation.Animator
import tech.muso.graphly.model.GraphAnimatable

interface GraphlyAnimator<T> {

    /**
     * Returns an Animator that performs the desired animation. Must call
     * [GraphAnimatable.setAnimationData] for each animation frame.
     *
     * @param sparkView The SparkView object
     */
    fun getAnimation(chartView: GraphAnimatable<T>?): Animator?
}