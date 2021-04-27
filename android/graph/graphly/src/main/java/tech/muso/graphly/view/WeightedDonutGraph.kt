package tech.muso.graphly.view

import tech.muso.stonky.common.PortfolioSlice
import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import org.hsluv.HUSLColorConverter
import tech.muso.graphly.BuildConfig
import tech.muso.graphly.`interface`.GraphInterface
import tech.muso.graphly.animation.*
import tech.muso.graphly.model.SliceDrawable
import tech.muso.graphly.model.SliceAnimatable
import java.util.*
import kotlin.collections.ArrayList
import tech.muso.graphly.*
import tech.muso.stonky.common.PortfolioSlice.Companion.toJson
import kotlin.math.*


class WeightedDonutGraph(context: Context, attrs: AttributeSet?): View(context, attrs),
    View.OnClickListener,
    SliceAnimatable {

    val graphMinRadius = 150f
    val graphMaxRadius = graphMinRadius + 100f

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
////        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//
//        // Add padding to maximum width calculation.
//        val desiredWidth = Math.round(graphMaxRadius*2 + getPaddingLeft() + getPaddingRight())
//
//        // Add padding to maximum height calculation.
//        val desiredHeight = Math.round(graphMaxRadius*2 + getPaddingTop()  + getPaddingBottom())
//
//        // Reconcile size that this view wants to be with the size the parent will let it be.
//        val measuredWidth = reconcileSize(desiredWidth, widthMeasureSpec)
//        val measuredHeight = reconcileSize(desiredHeight, heightMeasureSpec)
//
//        // Store the final measured dimensions.
//        setMeasuredDimension(measuredWidth, measuredHeight);
//    }

    /**
     * Reconcile a desired size for the view contents with a [android.view.View.MeasureSpec]
     * constraint passed by the parent.
     *
     * This is a simplified version of [View.resolveSize]
     *
     * @param contentSize Size of the view's contents.
     * @param measureSpec A [android.view.View.MeasureSpec] passed by the parent.
     * @return A size that best fits `contentSize` while respecting the parent's constraints.
     */
    private fun reconcileSize(contentSize: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> if (contentSize < specSize) {
                contentSize
            } else {
                specSize
            }
            MeasureSpec.UNSPECIFIED -> contentSize
            else -> contentSize
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    private var _animator: GraphlyAnimator<SliceDrawable>? = null
    private val animator: Animator? get() = _animator?.getAnimation(this)

    private var selectAnimator: SliceSelectorAnimator? = null

    var listener: GraphInterface? = null

    private val slicePaint: Paint
    private val strokePaint: Paint
    private val centerPaint: Paint

    private var slices: List<SliceDrawable>? = null
    private var animatedSlices: List<SliceDrawable>? = null

    fun rotateData() {
        Log.w("SLICE", "rotating slices ${slices}")
//        slices = slices?.asReversed()
        slices = slices?.run {
//            val r1 = get(0).radiusIn
//            val r2 = get(0).radiusOut
            val t0 = get(0).thetaStart
            val t1 = get(0).thetaEnd
            mapIndexed { index, slice ->
                if (index != size - 1) {
//                    slice.radiusIn = slices[]
                    slice.thetaStart = get(index + 1).thetaStart
                    slice.thetaEnd = get(index + 1).thetaEnd
                    slice.color = getRandomColor()
                } else {
                    slice.thetaStart = t0
                    slice.thetaEnd = t1
                    slice.color = getRandomColor()
                }

                slice
            }
        }

        Log.i("SLICE", "rotated: $slices")
    }

    override fun onClick(v: View?) {
        // TODO: 2/12/2020 determine the select slice logic
        doAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
//        sparkFillPaint.setShader(GraphFillShader.generate(fillColor, h.toFloat()))
        updateContentRect()
//        populatePath()
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
//        populateData()
    }

    private val contentRect = RectF()

    override fun getPaddingStart(): Int {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) super.getPaddingStart() else paddingLeft
    }

    override fun getPaddingEnd(): Int {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) super.getPaddingEnd() else paddingRight
    }

    /**
     * Gets the rect representing the 'content area' of the view. This is essentially the bounding
     * rect minus any padding.
     */
    private fun updateContentRect() {
        contentRect.set(
            paddingStart.toFloat(),
            paddingTop.toFloat(),
            width - paddingEnd.toFloat(),
            height - paddingBottom
                .toFloat()
        )
    }

    override fun getSlices(): List<SliceDrawable>? {
        return slices
    }

    override fun getAnimatedSlices(): List<SliceDrawable>? {
        return animatedSlices
    }

//    private inline fun RectF.inset(delta: Float) {
//        // FIXME: makes the assumption that the view is square. instead need delta vertical/horizontal
//        this.inset(delta, delta)
//    }

    private val drawRectF: RectF = RectF(contentRect)

//    val colors: IntArray = intArrayOf(
//        0xFF005F57.toInt(),
//        0xFF00BD7A.toInt(),
//        0xFF00F4B6.toInt(),
//        0xFF00804D.toInt(),
//        0xFF00BD7A.toInt(),
//        0xFF00FF00.toInt()
//    )

//    val colors: IntArray = intArrayOf(
//        /* Color Theme Swatches in Hex */
//        0xFFB0BF7A.toInt(),
//        0xFF575942.toInt(),
//        0xFFA5A692.toInt(),
//        0xFFD9D7C5.toInt(),
//        0xFFF2F2F2.toInt(),
//    )


    val darkColor = -0xd9d7d1

    /* Color Theme Screen-Shot Swatches in Hex */
    val colors = intArrayOf(
        0xff10555B.toInt(),
        0xff136E73.toInt(),
        0xff79BFB2.toInt(),
        0xffF9A88E.toInt(),
        0xffE9665D.toInt(),
    )



//    val darkColor = 0xFF22261A.toInt()

//    val colors: IntArray = intArrayOf(
//    /* Color Theme Swatches in Hex */
//    0xFF3B594B.toInt(),
//    0xFF36402C.toInt(),
//    0xFFF2E49B.toInt(),
//    0xFFF2E1C2.toInt(),
//        0xFFB0BF7A.toInt(),
//    )


    private fun getRandomColor(): Int {
        return colors.random()
    }

    /**
     * Draw logic.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.w("ANIMATION", "onDraw() $contentRect")

        // draw the background for the chart
        drawRectF.set(contentRect)
        drawRectF.inset(drawRectF.width() / 2 - graphMaxRadius, drawRectF.height() / 2 - graphMaxRadius)
        canvas.drawArc(drawRectF, 0f, 360f, true, centerPaint)

        //
        animatedSlices?.forEach {
            canvas.withTranslation(it.xTranslation.toFloat(), it.yTranslation.toFloat()) {
                withScale(it.scale, it.scale, width.toFloat()/2, height.toFloat()/2) {

                    // update color before we draw slices
                    slicePaint.color = it.color

                    drawRectF.set(contentRect)
                    with(drawRectF) {
                        inset(width() / 2 - it.radiusOut, height() / 2 - it.radiusOut)

                        drawArc(this, it.thetaStart * 360, it.theta * 360, true, slicePaint)
                        drawArc(this, it.thetaStart * 360, it.theta * 360, true, strokePaint)

                        inset(it.sliceWidth, it.sliceWidth)
                        drawArc(this, it.thetaStart * 360, it.theta * 360, true, centerPaint)
                    }
                }
            }
        }

        // draw the smaller radius over the slices to complete the effect
        drawRectF.set(contentRect)
        drawRectF.inset(drawRectF.width() / 2 - graphMinRadius, drawRectF.height() / 2 - graphMinRadius)
        canvas.drawArc(drawRectF, 0f, 360f, true, centerPaint)
    }

    private var data: List<PortfolioSlice> = ArrayList()
    private var useAltData: Boolean = false

    object HSLuvUtil {
        fun argbIntToTuple(colorInt: Int): DoubleArray? {
            return doubleArrayOf(
                (colorInt shr 16 and 0xff) / 255.0,
                (colorInt shr 8 and 0xff) / 255.0,
                (colorInt and 0xff) / 255.0,
            )
        }

        fun rgbTupleToArgb(doubleArray: DoubleArray): Int {
            return 0xFF shl 24 or
                    ((doubleArray[0] * 0xFF).roundToInt() shl 16) or
                    ((doubleArray[1] * 0xFF).roundToInt() shl 8) or
                    (doubleArray[2] * 0xFF).roundToInt()
        }

        private val random = Random()//(-878723815933874252) // (101234567890)
            .also {
                val seed = it.nextLong()
                println("SEED: $seed") //9126629157025823895
                it.setSeed(seed)
            }

        fun randomizeHue(colorInt: Int, maxDegrees: Float = 100f): Int {
            var hpluv = HUSLColorConverter.rgbToHsluv(argbIntToTuple(colorInt))
//            println("COLOR1: ${hpluv[0]}, ${hpluv[1]}, ${hpluv[2]}")
            hpluv[0] += (random.nextDouble() * maxDegrees)// - maxDegrees
//            println("COLOR2: ${hpluv[0]}, ${hpluv[1]}, ${hpluv[2]}")
            hpluv = HUSLColorConverter.hsluvToRgb(hpluv)
//            println("COLOR3: ${hpluv[0]}, ${hpluv[1]}, ${hpluv[2]}")
            val result = rgbTupleToArgb(hpluv)
            println("COLOR4: ${Integer.toHexString(result)}")
            return result
        }
    }

    var rootPortfolio: PortfolioSlice? = null
        set(value) {
            if (field != value) {
                field = value
                resetAnimation()    // change back to default animation
                populateData()
                doAnimation()
            }
        }

//    private val count get() = rootPortfolio?.iterator()?.size ?: 0

    private fun populateData() {
        if (rootPortfolio == null) return
        if (width == 0 || height == 0) return

        rootPortfolio?.run {
            println(rootPortfolio?.toJson())

//            val adapterCount: Int = count
//
//            if (adapterCount < 2) {
//                clearData()
//                return
//            }

            var baseColor = 0xff79BFB2.toInt() // ContextCompat.getColor(context, R.color.md_green_500)

            var thetaStart: Float = 0f
            var thetaEnd: Float = 0f
            val total = marketValue
            val actualWeight = rootPortfolio!!.map { (abs(it.marketValue) / abs(it.target)).toFloat() }
            val highestWeight = actualWeight.maxOrNull()
            val scaledActualWeights = actualWeight.map { it * 2f/(highestWeight ?: 0f) }

            // generate the base slices that we will use to draw the graph.
            // this is prior to applying any animations.
            slices = rootPortfolio!!.mapIndexed { i, stock ->
                thetaEnd = thetaStart + stock.weight
//                val radiusOut = ((graphMaxRadius - graphMinRadius) * actualWeight[i]) + graphMinRadius
                val radiusOut = ((graphMaxRadius - graphMinRadius) * actualWeight[i]) + graphMinRadius
//                val actualWeight = (stock.value / total) / stock.weight
//                val color = if (useAltData) colors[i+1] else colors[i]
                baseColor = HSLuvUtil.randomizeHue(baseColor)
                SliceDrawable(
                    stock,
                    graphMinRadius,
                    radiusOut,
                    thetaStart,
                    thetaEnd,
                    baseColor,
                    1f,
                    0,
                    0
                ).also { thetaStart = thetaEnd }
            }
        }

        invalidate()
    }

    fun doAnimation() = animator?.start()
    override fun setAnimationData(data: List<SliceDrawable>) {
        animatedSlices = data
        invalidate()
    }

    var selectedSlice: PortfolioSlice? = null
    set(value) {
        if (field != value) {
            if (field != null) listener?.onItemUnselected(field!!)
            field = value
            listener?.onItemSelected(0, value)
        }
    }

    fun unselectSlice(doOnEnd: (() -> Unit)? = null) {
        if (selectedSlice != null) {
            selectAnimator?.getAnimation(this@WeightedDonutGraph)?.run{
                doOnEnd {
                    doOnEnd?.invoke()
                    selectedSlice = null
                    selectAnimator?.selectedSliceIndex = null
                    removeAllListeners()
                }
                reverse()
            }
        }
    }

    /** Change back to default animation and clear any animated data. */
    fun resetAnimation() {
        // TODO: add flatten animation
//        selectAnimator = null
        animator?.removeAllListeners()
        _animator = CircularRevealAnimator()
        animatedSlices = null
        invalidate()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return portfolioSliceSelectTouchListener.onTouch(this, event)
    }

    // custom listener that consumes events only if they are touched.
    private val portfolioSliceSelectTouchListener = object : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when(event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val originX = contentRect.centerX()
                    val originY = contentRect.centerY()
                    val x = event.x - originX
                    val y = event.y - originY
                    var theta = atan2(y, x) / PI
                    if (theta < 0) theta += 2
                    theta /= 2
                    Log.i("GRAPH.onTouch", "onTouch (x,y): ${x},${y} -> atan = ${theta}")

                    val dist = sqrt(x*x + y*y)

                    var valid: Boolean = false
                    // on click on the animated slices because this represents where the slice is at present time.
                    animatedSlices?.forEachIndexed { index, drawnSlice ->
                        if (dist > drawnSlice.radiusIn && (dist < graphMaxRadius || dist < drawnSlice.radiusOut) && drawnSlice.thetaStart <= theta && theta <= drawnSlice.thetaEnd) {
                            valid = true
                            Log.w("GRAPH.onTouch", "onTouch (x,y): ${x},${y} -> atan = ${theta} - HIT: #${Integer.toHexString(drawnSlice.color)}")
                            if (selectedSlice != drawnSlice.slice) {
                                selectedSlice = drawnSlice.slice
                                selectAnimator?.selectedSliceIndex = index
                                selectAnimator?.getAnimation(this@WeightedDonutGraph)?.start()
                            } else {
                                unselectSlice {
                                    if (selectedSlice?.isComplex() == true)
                                        listener?.onPortfolioEnter(selectedSlice!!)
                                }
                            }
                        }
                    }

                    when {
                        // if we changed data
                        valid -> invalidate()
                        // if we can unselect
                        selectedSlice != null -> unselectSlice()
                        // if there's a parent portfolio and we click center
                        rootPortfolio?.parent != null && dist < graphMinRadius -> listener?.onPortfolioExit(rootPortfolio!!)
                        // else.
                        else -> return false
                    }

                    v?.performClick()  // FIXME: if this doesn't do anything, fix the audio to click
                    return true
                }
                MotionEvent.ACTION_UP -> {}
                else -> {}
            }
            // unhandled
            return false
        }
    }

    public companion object {
        const val CIRCLE_REVEAL_ANIMATION = 1
        const val CIRCLE_EXTRUDE_ANIMATION = 2
        const val CIRCLE_MORPH_ANIMATION = 3
        const val CIRCLE_FLATTEN_ANIMATION = 4
    }

    override fun onAnimationEnded(type: Int) {
        when(type) {
            // REVEAL -> EXTRUDE
            CIRCLE_REVEAL_ANIMATION -> {
                _animator = CircularExtrudeAnimator()
                doAnimation()
            }
            // EXTRUDE -> MORPH & SELECT
            CIRCLE_EXTRUDE_ANIMATION -> {
                this.setOnClickListener(null)
                selectAnimator = SliceSelectorAnimator()
                _animator = CircleMorphAnimator(animatedSlices!!) // CircularRevealAnimator()
                (_animator as CircleMorphAnimator).duration = 1500
//                setOnTouchListener(portfolioSliceSelectTouchListener)
            }
            // FLATTEN -> REVEAL
            // FIXME: idk can't enable this?
//            CIRCLE_FLATTEN_ANIMATION -> {
//                _animator = CircularExtrudeAnimator()
//                doAnimation()
//            }
        }
    }

    init {
        // TODO: 2/12/2020 load in attributes
//        val a = context.obtainStyledAttributes(
//                attrs, R.styleable.Graph
//        )


        slicePaint = Paint()
        slicePaint.color = Color.GREEN
        slicePaint.isDither = true
        slicePaint.isAntiAlias = true
        slicePaint.style = Paint.Style.FILL

        centerPaint = Paint()
        centerPaint.color = darkColor
        centerPaint.isDither = true
        centerPaint.isAntiAlias = true
        centerPaint.style = Paint.Style.FILL

        strokePaint = Paint(centerPaint)
        strokePaint.strokeWidth = 8f
        strokePaint.style = Paint.Style.STROKE

        if (BuildConfig.DEBUG) {
            _animator = CircularRevealAnimator()
//            adapter = TestPortfolioData()
        }

        // attach onclick listener to this
        setOnClickListener(this)


//        sliceTouchDetector = GestureDetector(this, handler, touchSlop)
//        scrubGestureDetector.setEnabled(scrubEnabled)

    }

}