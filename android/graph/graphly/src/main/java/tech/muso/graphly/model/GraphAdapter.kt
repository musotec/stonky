package tech.muso.graphly.model

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.graphics.RectF
import androidx.annotation.VisibleForTesting

abstract class GraphAdapter<T> {
    private val observable = DataSetObservable()

    /**
     * @return the number of points to be drawn
     */
    abstract fun getCount(): Int

    /**
     * @return the object at the given index
     */
    abstract fun getItem(index: Int): T

    abstract fun getData(): List<T>

    /**
     * @return the float representation of the X value of the point at the given index.
     */
    open fun getX(index: Int): Float {
        return index.toFloat()
    }

    /**
     * @return the float representation of the Y value of the point at the given index.
     */
    abstract fun getY(index: Int): Float

    /**
     * Gets the float representation of the boundaries of the entire dataset. By default, this will
     * be the min and max of the actual data points in the adapter. This can be overridden for
     * custom behavior. When overriding, make sure to set RectF's values such that:
     *
     *
     *  * left = the minimum X value
     *  * top = the minimum Y value
     *  * right = the maximum X value
     *  * bottom = the maximum Y value
     *
     *
     * @return a RectF of the bounds desired around this adapter's data.
     */
    open fun getDataBounds(): RectF {
        val count = getCount()
        val hasBaseLine = hasBaseLine()
        var minY =
            if (hasBaseLine) getBaseLine() else Float.MAX_VALUE
        var maxY = if (hasBaseLine) minY else -Float.MAX_VALUE
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        for (i in 0 until count) {
            val x = getX(i)
            minX = Math.min(minX, x)
            maxX = Math.max(maxX, x)
            val y = getY(i)
            minY = Math.min(minY, y)
            maxY = Math.max(maxY, y)
        }
        // set values on the return object
        return createRectF(minX, minY, maxX, maxY)
    }

    /**
     * Hook for unit tests
     */
    @VisibleForTesting
    open fun createRectF(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): RectF {
        return RectF(left, top, right, bottom)
    }

    /**
     * @return true if you wish to draw a "base line" - a horizontal line across the graph used
     * to compare the rest of the graph's points against.
     */
    open fun hasBaseLine(): Boolean {
        return false
    }

    /**
     * @return the float representation of the Y value of the desired baseLine.
     */
    open fun getBaseLine(): Float {
        return 0f
    }

    /**
     * Notifies the attached observers that the underlying data has been changed and any View
     * reflecting the data set should refresh itself.
     */
    fun notifyDataSetChanged() {
        observable.notifyChanged()
    }

    /**
     * Notifies the attached observers that the underlying data is no longer valid or available.
     * Once invoked this adapter is no longer valid and should not report further data set
     * changes.
     */
    fun notifyDataSetInvalidated() {
        observable.notifyInvalidated()
    }

    /**
     * Register a [DataSetObserver] to listen for updates to this adapter's data.
     * @param observer    the observer to register
     */
    fun registerDataSetObserver(observer: DataSetObserver) {
        observable.registerObserver(observer)
    }

    /**
     * Unregister a [DataSetObserver] from updates to this adapter's data.
     * @param observer    the observer to unregister
     */
    fun unregisterDataSetObserver(observer: DataSetObserver) {
        observable.unregisterObserver(observer)
    }
}