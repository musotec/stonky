package tech.muso.stonky.android.viewmodels.stocks

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tech.muso.stonky.android.R
import tech.muso.demo.common.entity.Fundamentals
import tech.muso.demo.common.entity.WorkingOrder
import tech.muso.demo.common.entity.Profile
import tech.muso.demo.common.entity.StockEntity
import tech.muso.demo.common.entity.StockEntity.Companion.deepCopy
import tech.muso.demo.graph.spark.*
import tech.muso.demo.graph.spark.graph.Line
import tech.muso.demo.graph.spark.helpers.getColorFromAttr
import tech.muso.demo.repos.StockDataRepository
import java.util.*
import kotlin.math.absoluteValue


@BindingAdapter("app:ratio_link")
fun bindRatioLink(view: Slider, viewModel: StocksTradePairsViewModel) {
    view.addOnChangeListener { slider, value, fromUser ->
        viewModel.ratio.value = value
    }

    view.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: Slider) {

        }

        // snap to the actual ratio when we end
        override fun onStopTrackingTouch(slider: Slider) {
            slider.value = viewModel.actualRatio
//            slider.
        }
    })
}

@BindingAdapter("app:short_long_state")
fun bindButtonShortLongState(view: MaterialButton, isShort: Boolean) {
    if (isShort) {
        ColorStateList.valueOf(view.context.getColorFromAttr(R.attr.colorShortPrimary))
    } else {
        ColorStateList.valueOf(view.context.getColorFromAttr(R.attr.colorLongPrimary))
    }.let {
        view.rippleColor = it
        view.setTextColor(it)
    }
}

@BindingAdapter("app:spark_lines")
fun bindSparkLines(view: LineGraphView, lines: List<Line.Builder>) {
    lines.forEach {
        view.add(it)
    }
//    view.onRangeSelected = view::zoom
}

@BindingAdapter("app:scrub_listener")
fun bindScrubListener(view: LineGraphView, listener: LineGraphView.ScrubStateListener) {
    view.scrubListener = listener
}

@BindingAdapter("app:spark_line_colors")
fun bindSparkLineColors(view: LineGraphView, colors: List<Int>) {
    if (colors.isEmpty()) return

//    view.lines.forEachIndexed { index, line ->
//        if (colors[index] != 0) {
//            line.lineColor = ContextCompat.getColor(view.context, colors[index])
//            line.fillColor = ContextCompat.getColor(view.context, colors[index])
//        }
//    }
}

@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("app:bindCursorController")
fun bindCursorController(view: EditText, viewModel: StocksTradePairsViewModel) {
    var lastCursorPosition = 0
    var fromUser = false
    view.setOnTouchListener { v, event ->
        when(event.action) {
            MotionEvent.ACTION_DOWN -> fromUser = true
        }
        return@setOnTouchListener false
    }
    // if the user starts typing, then they want to lock.
    view.setOnKeyListener { v, keyCode, event ->
        viewModel.priceLocked.value = true
        return@setOnKeyListener false
    }

    view.accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun sendAccessibilityEvent(host: View?, eventType: Int) {
            super.sendAccessibilityEvent(host, eventType)
            if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                // when from the user, (handled via touch, always accept new position)
                if (fromUser) {
                    lastCursorPosition = view.selectionStart
                    fromUser = false

                    // if we are selecting a range, lock value.
                    if (view.selectionStart != view.selectionEnd) {
                        viewModel.priceLocked.value = true
                    }

                    return
                }

                // if we aren't locked, keep cursor in same position (if in range)
                else if (viewModel.priceLocked.value == false
                    && lastCursorPosition != view.selectionStart
                    && lastCursorPosition <= view.text.length) {
                    view.setSelection(lastCursorPosition)
                    view.isCursorVisible = true
                }

                else if (lastCursorPosition != view.selectionStart) {
                    lastCursorPosition = view.selectionStart
                }
            }
        }
    }
}

@BindingAdapter("app:observe")
fun bindLockIcon(view: MaterialButton, isLocked: Boolean) {
    val lockIconRes = if (isLocked) R.drawable.ic_baseline_lock_24 else R.drawable.ic_baseline_lock_open_24
    view.icon = view.resources.getDrawable(lockIconRes,null)
}

@ExperimentalCoroutinesApi
@FlowPreview
/**
 * A ViewModel that controls the business logic for handling a Pairs Trade.
 * This connects the View to the same StockRepository as the List, but contains logic specifically
 * for performing the Pairs Trade feature.
 */
class StocksTradePairsViewModel internal constructor(
    private val repository: StockDataRepository
) : ViewModel() {

    /**
     * LiveData object that contains a list of the current stocks from the database.
     */
    val stocks: LiveData<List<StockEntity>> = repository.stocks

    val lines: LiveData<List<Line.Builder>> = MutableLiveData<List<Line.Builder>>(
        arrayListOf<Line.Builder>().apply {
            // generate two lines.
            for (i in 0..1) {
                val color =
                    when (i) {
                        0 -> Color.GREEN
                        else -> Color.RED
                    }
                this.add(
                    Line.Builder()
                        .setColor(color)
                )
            }
        }
    )

//                .apply {
//                    when(i) {
//                        0 -> {
//                            fillType = FillType.DOWN
//                            fillColor = Color.GREEN
//                            clipType = ClipType.CLIP_BELOW
//                        }
//                        1 -> {
//                            fillType = FillType.DOWN// or FillType.REVERSE_GRADIENT
//                            fillColor = Color.RED
//                            clipType = ClipType.CLIP_ABOVE
//                        }
//                    }
//                    scaleMode = ScaleMode.ALIGN_START
//                    identifier = if (i == 0) "SPY" else "QQQ"
//                }
//            )
//        }
//    })

    private val _first = MutableLiveData<StockEntity>().also {
//        it.asFlow().map {
//            // could monitor for changes to our stocks here and update the price this way.
//        }.flowOn(Dispatchers.IO)
    }
    val first: LiveData<StockEntity>
        get() = _first

    private val _second = MutableLiveData<StockEntity>()
    val second: LiveData<StockEntity>
        get() = _second

    val positions: List<LiveData<StockEntity>> = listOf(_first, _second)

    val positionList: LiveData<List<StockEntity?>> = MediatorLiveData<List<StockEntity?>>().apply {
         this.value = positions.mapIndexed { index, stockLiveData ->
             addSource(stockLiveData) { stock ->
                 // when we change, update the index in a new list, then pubish this list
                 this.value = this.value?.mapIndexed { i, oldStock ->
                     if (i != index) {
                         oldStock
                     } else {
                         stock.apply {
                             // and let's also update our line
                             lines.value?.get(index)?.let { line ->
                                 // update the ratio if it's different
//                                 if (line.scaleBy != amount.absoluteValue.toFloat()) {
//                                     line.scaleBy = amount.absoluteValue.toFloat()
//                                 }
//
//                                 // update the direction of the clipping
//                                 if (stock == positions.maxBy { it.value?.totalPrice ?: 0.0 }?.value) {
//                                     line.clipType = ClipType.CLIP_BELOW
//                                 } else {
//                                     line.clipType = ClipType.CLIP_ABOVE
//                                 }
                             }
                         }
                     }
                 }
             }
             return@mapIndexed stockLiveData.value
        }
    }

    val lineColors: LiveData<List<Int>> = positionList.switchMap {
        liveData {
            emit(it.mapNotNull { stock -> if (stock?.isShort == true) R.color.colorAccentRed else R.color.colorAccentTeal })
        }
    }

//    val lineColors: LiveData<List<Int>> = MediatorLiveData<List<Int>>().apply {
//        this.value = listOf(R.color.colorAccentRed, R.color.colorAccentTeal)
//        addSource(_first) {
//            val newColor = if (it.isShort) R.color.colorAccentRed else R.color.colorAccentTeal
//            this.value = (this.value)?.mapIndexed { index, color -> if(index == 0) newColor else color}
//        }
//        addSource(_second) {
//            val newColor = if (it.isShort) R.color.colorAccentRed else R.color.colorAccentTeal
//            this.value = (this.value)?.mapIndexed { index, color -> if(index == 1) newColor else color}
//        }
//    }

    val ratio = MutableLiveData<Float>(0.5f)
        .apply {
            observeForever(Observer { floatRatio ->
                // compute our nearest fraction
                CoroutineScope(Dispatchers.IO).launch {
                    with(ratioToFractionTuple(floatRatio)) {
                        viewModelScope.launch {
                            _first.value = _first.value?.deepCopy()?.apply {
                                val wasShort = isShort
                                amount = first
                                if (isShort != wasShort) amount *= -1
                            }
                            _second.value = _second.value?.deepCopy()?.apply {
                                val wasShort = isShort
                                amount =
                                    (first - second) // adjust "second" is denominator, so total adds to 1
                                if (isShort != wasShort) amount *= -1
                            }

                            // also unlock because price will be wrong
                            priceLocked.value = false
                        }
                    }
                }
            })
        }

    // TODO: fix ratio adjust at end of drag resulting in floor & not ciel
    val actualRatio: Float get() {
        val firstCount = _first.value?.amount?.absoluteValue?.toFloat() ?: 1f
        val secondCount = _second.value?.amount?.absoluteValue?.toFloat() ?: 1f
        return firstCount / (firstCount + secondCount)
    }

    /**
     * The list of positions in the current working order.
     */
    val order: LiveData<List<WorkingOrder>>
        get() = _order
    private val _order = MutableLiveData<List<WorkingOrder>>()
    
//    val lines: LiveData<List<Line>> = order.switchMap {
//        liveData {
//            emit(it.map {
//                Line(Color.RED, Color.RED, 4f, 4f, FillType.DOWN).apply {
//                    adapter = object : ZoomableGraphDataAdapter<PortfolioPosition>() {
//
//                    }
//                }
//            })
//        }
//    }

    /**
     * Compute the nearest fraction between [0, 1) by walking a Stern-Brocot tree.
     *
     * Runtime: O(log2(n))
     */
    private suspend fun ratioToFractionTuple(ratio: Float, errorMargin: Float = 0.05f): Pair<Int, Int> {
        // lower bound of tree 0/1
        var lowerNumerator = 0
        var lowerDenominator = 1
        // upper bound of tree 1/1
        var upperNumerator = 1
        var upperDenominator = 1

        while(true) {
            // calculate the mediant between upper and lower
            val midNumerator = lowerNumerator + upperNumerator
            val midDenominator = lowerDenominator + upperDenominator
            when {
                // if below the middle, and outside margin of error; walk down tree to the left
                midDenominator * (ratio + errorMargin) < midNumerator -> {
                    // walk left, mediant becomes upper bound
                    upperNumerator = midNumerator
                    upperDenominator = midDenominator
                }
                midNumerator < (ratio - errorMargin) * midDenominator -> {
                    // walk right, mediant becomes lower bound
                    lowerNumerator = midNumerator
                    lowerDenominator = midDenominator
                }
                else -> {
                    // exit condition, where we are within the margin of error
                    return Pair(midNumerator, midDenominator)
                }
            }
        }
    }

    var chartVisibility = MutableLiveData<Int>(View.VISIBLE)
    var priceLocked = MutableLiveData<Boolean>(false)

//    val stocksService = MarketDataWebService()
//
//    suspend fun getCurrentPrice(symbol: String): Double {
//        stocksService.getQuote(symbol)[symbol].apply {
//            (this as? Etf)?.let {
//                return (it.bidPrice + it.askPrice)/2
//            } ?: return 0.0
//        }
//        return 0.0
//    }

    suspend fun updateCurrentPrices() {
//        _first.value?.let { first -> _second.value?.let { second ->
//            stocksService.getQuotes(listOf(first.symbol, second.symbol)).apply {
//                (this[first.symbol] as? Futures)?.let {
//                    first.currentPrice = (it.bidPriceInDouble + it.askPriceInDouble)/2
//                    first.tick += 1
//                    _first.value = first.deepCopy()
//                }
//                (this[second.symbol] as? Futures)?.let {
//                    second.currentPrice = (it.bidPriceInDouble + it.askPriceInDouble)/2
//                    second.tick += 1
//                    _second.value = second.deepCopy()
//                }
//            }
//        }}
    }

    init {
        runBlocking {
            launch {
                val firstStock =
                    StockEntity(
                        "/ES",
                        "Spooders",
                        Fundamentals(0, 0.0, 0.0, 0),
                        Profile("")
                    ).apply {
//                        this.currentPrice = getCurrentPrice(this.symbol)
                        this.priceChangePercent = 0.0
                        this.amount = 1
                    }

                val secondStock =
                    StockEntity(
                        "/NQ",
                        "Queues",
                        Fundamentals(0, 0.0, 0.0, 0),
                        Profile("")
                    ).apply {
//                        this.currentPrice = getCurrentPrice(this.symbol)
                        this.priceChangePercent = 0.0
                        this.amount = -1
                    }

                _first.value = firstStock
                _second.value = secondStock

                CoroutineScope(Dispatchers.Main).launch {
//                    lines.value?.get(0)?.adapter = StockHistoryDataAdapter(firstStock)
//                    lines.value?.get(1)?.adapter = StockHistoryDataAdapter(secondStock)
                }

                launchDataLoad {
                    updateCurrentPrices()
                }
            }
        }
    }

    fun toggleShortLongSecondStock() {
        _second.value = second.value?.deepCopy()?.also {
            it.amount = -it.amount
        }
    }

    fun toggleShortLongFirstStock() {
        _first.value = first.value?.deepCopy()?.also {
            it.amount = -it.amount
        }
    }

    fun swapAmounts(view: View) {
        var _secondAmountCached = -1
        _second.value = second.value?.deepCopy()?.also {
            _secondAmountCached = it.amount
            it.amount = first.value?.amount ?: 1
        }
        _first.value = first.value?.deepCopy()?.also {
            it.amount = _secondAmountCached
        }
    }

    fun sendOrder(view: View) {
        viewModelScope.launch {
            Snackbar.make(view, "${first.value?.amount?.absoluteValue}:${second.value?.amount?.absoluteValue} Pairs trade sent! ${first.value?.symbol}-${second.value?.symbol} @ ${"%.2f".format(orderPrice)}", Snackbar.LENGTH_SHORT).show()
        }
        priceLocked.value = false
    }

    fun toggleLock(view: View) {
        if (view !is MaterialButton) return // only do this if material button, not the best
        val newPriceLockedValue = !(priceLocked.value?:false)
        priceLocked.value = newPriceLockedValue
    }

    fun testLoadingBarFunctionality() {
        Log.d("StockViewModel", "testInvocationFromOnClick()")

        viewModelScope.launch {
            delay(5000)
        }
    }

    suspend fun totalPrice(): Double {
        val totalPriceFirst = first.value?.totalPrice ?: 0.0
        val totalPriceSecond = second.value?.totalPrice ?: 0.0
        return totalPriceFirst + totalPriceSecond
    }

    fun onOrderPriceChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        orderPrice = s.toString().toDoubleOrNull() ?: 0.0
    }

    private var orderPrice: Double = 0.0

    val currentPrice: LiveData<Double> = flow {

        var firstPrice = first.value?.currentPrice ?: 0.0
        var secondPrice = second.value?.currentPrice ?: 0.0

        val freq = 100

        val random = Random()
        fun generateNextTick() {
            val delta = random.nextGaussian() / freq
            firstPrice += delta
            val delta2 = random.nextGaussian() / freq
            secondPrice += delta2
        }

        // this looks dangerous but is completely fine because of how coroutines work.
        // we don't need an exit condition because the Dispatchers.IO scope will handle it.
        while (true) {
            generateNextTick()
            delay(freq * 50L)
            viewModelScope.launch {
//                _first.value = first.value?.deepCopy()?.apply {
//                    currentPrice = getCurrentPrice(this.symbol); tick += 1
//                }
//                _second.value = second.value?.deepCopy()?.apply {
//                    currentPrice = getCurrentPrice(this.symbol); tick += 1
//                }
//                updateCurrentPrices()
            }
            // if we are price locked, don't compute the price
            if (priceLocked.value == true) continue
            // emit price computation
            emit(totalPrice())
        }

    }.flowOn(Dispatchers.IO).asLiveData()

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                block()
            } catch (error: Throwable) {
                // todo show error
            } finally {
                // stop loading bar when done
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        // TODO: remove
//        ratio.removeObserver()
//        ratio.removeObservers
    }
}