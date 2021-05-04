package tech.muso.stonky.android.stocks

import Bars
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import tech.muso.demo.graph.core.CandleGraphable
import tech.muso.demo.graph.spark.LineGraphView
import tech.muso.demo.graph.spark.LineRekoilAdapter
import tech.muso.demo.graph.spark.graph.Line
import tech.muso.rekoil.core.Atom
import tech.muso.rekoil.core.RekoilScope
import tech.muso.rekoil.core.Selector
import tech.muso.rekoil.core.launch
import tech.muso.stonky.android.R
import tech.muso.stonky.android.subscribe
import java.text.SimpleDateFormat
import java.util.*


class GraphStockFragment : Fragment(), LifecycleOwner {

    init {
    }

    // Create a rekoil scope that lives only on the Fragment Lifecycle to avoid extraneous work.
//    private val rekoilScope: RekoilScope
//            = RekoilScope(lifecycleScope + Dispatchers.Main)


    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.stonky_quicktrade, container, false).also { rootView ->

            val defaultStock = "SPY"

            var selectionStartPointSubscriber: Job? = null
            var selectionEndPointSubscriber: Job? = null

            val graphView = rootView.findViewById<LineGraphView>(R.id.sparkLineGraph)
            val inputTextView = rootView.findViewById<EditText>(R.id.inputTextField)
            val titleTextView = rootView.findViewById<TextView>(R.id.title)
            val textStartPoint = rootView.findViewById<TextView>(R.id.start_point)
            val textEndPoint = rootView.findViewById<TextView>(R.id.end_point)

            textStartPoint.setLineSpacing(0f, 1.3f)
            textEndPoint.setLineSpacing(0f, 1.3f)
            inputTextView.isSingleLine = true
            inputTextView.filters = arrayOf<InputFilter>(AllCaps())
            inputTextView.setText(defaultStock)

            val startDateTime = Date(0L)
            val endDateTime = Date(0L)
            val outputFormat = SimpleDateFormat("MM/dd/yy\nHH:mm:ss a")
            fun updateUiState(selectedLine: LineRekoilAdapter) {
                // detach listeners
                selectionStartPointSubscriber?.cancel()
                selectionEndPointSubscriber?.cancel()

                // and make subscribers
                selectionStartPointSubscriber = selectedLine.selectionStartPoint.subscribe {
                    if (it != null) {
                        startDateTime.time = it.x.toInt() * 1000L
                        textStartPoint.text = outputFormat.format(startDateTime)
                    }
                }

                selectionEndPointSubscriber = selectedLine.selectionEndPoint.subscribe {
                    if (it != null) {
                        endDateTime.time = it.x.toInt() * 1000L
                        textEndPoint.text = outputFormat.format(endDateTime)
                    }
                }

                selectedLine.data.added.subscribe {
                    titleTextView.text = it.top.toString()
                }
            }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val rekoilScope = RekoilScope(lifecycleScope + this.coroutineContext + Dispatchers.Main)

            val stock: Atom<String> = rekoilScope.atom { defaultStock }
//            inputTextView.addTextChangedListener {
//                stock.value = it?.toString() ?: ""
//            }

            inputTextView.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    // close keyboard
                    val imm: InputMethodManager = context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }

            // create a selector for the current lines on the graph from the graph view's scope.
            val lines = rekoilScope.withScope(graphView.graphRekoilScope) {
                get(graphView.lines)
            }

            rekoilScope.launch {

                // create a line selection atom
                val selectedLineIndex: Atom<Int> = atom { 0 }


                // and an OnTabSelectedListener to serve as an adapter
                class LinesOnTabSelectedListener(
                    val selectedIndexAtom: Atom<Int>
                ) : TabLayout.OnTabSelectedListener {

                    override fun onTabSelected(tab: TabLayout.Tab) {
                        // update our atom with the correct position
                        selectedIndexAtom.value = tab.position
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}    // No-op
                    override fun onTabReselected(tab: TabLayout.Tab?) {}    // No-op
                }

                // Attachment of the tabs
                val tabListener = LinesOnTabSelectedListener(selectedLineIndex)
                val tabs = rootView.findViewById<TabLayout>(R.id.tabs).apply {
                    addOnTabSelectedListener(tabListener)
                }

//                // create a selector that watches the lines and initializes the graph
//                rekoilScope.selector {
//                    // when we update our selector, initialize the graph (add data)
//                    val lineList = get(lines)
////                    if (lineList != null) initializeGraph(lineList)
//                }

                val currentLineAdapter: Selector<LineRekoilAdapter?> = selector {
                    // if index or line array changes, then update our current line
                    val currentIndex = get(selectedLineIndex)
                    val lineList = get(lines)!!
                    if (lineList.isEmpty()) return@selector null
                    return@selector lineList[currentIndex]  // return adapter at index.
                }

                // selector to perform changes whenever line adapter changes.
                selector {
                    try {
                        val adapter = get(currentLineAdapter)
                        adapter?.let { updateUiState(it) }
                    } catch (ex: Exception) {}
                }

                // create a list of subscribers for each line color,
                // since we will want an easy way to remove them as the tabs are cleared.
                val tabSubscribers = mutableListOf<Job>()
                withScope(graphView.graphRekoilScope) {
                    // otherwise we create many selectors with no way to release when list changes
                    val lineList = get(graphView.lines)

                    // clear all tabs
                    tabs.removeAllTabs()
                    // cancel subscribers for any old tabs
                    tabSubscribers.forEach { it.cancel() }
                    tabSubscribers.clear()  // and clear the list.
                    // now iterate over new list of lines, and create the tab and subscriber
                    lineList.forEachIndexed { position, adapter ->
                        val newTab = tabs.newTab()
                        newTab.icon = context?.getDrawable(R.drawable.ic_baseline_trending_up_24)
                        tabs.addTab(newTab)
                        // create the subscription to the line color
                        adapter.line.lineColorAtom.subscribe { color ->
                            // and apply color filter to the tab icon whenever the color changes
                            newTab.icon?.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    color, BlendModeCompat.SRC_IN
                                )
                        }//.also { tabSubscribers.add(it) }   // and also add it to the list.
                    }
                }

                var inScrubbing = false
                // initialize graph with a few lines and touch listeners.
                graphView.apply {
                    this.clearLines()   // FIXME: extraneous call
                    this.scrubListener = object : LineGraphView.ScrubStateListener {
                        override fun onRangeSelected() {}
                        override fun onScrubStateChanged(isScrubbing: Boolean) {
                            inScrubbing = isScrubbing
                        }
                    }


                    this.bgColor = ContextCompat.getColor(context, R.color.colorBackgroundBlue)
                    fun color(i: Int) =
                        when (i) {
                            0 -> ContextCompat.getColor(context, R.color.colorAccentRed)
                            1 -> ContextCompat.getColor(context, R.color.colorAccentOrange)
                            2 -> ContextCompat.getColor(context, R.color.colorAccentYellow)
                            3 -> ContextCompat.getColor(context, R.color.colorAccentGreen)
                            4 -> ContextCompat.getColor(context, R.color.colorAccentTeal)
                            5 -> ContextCompat.getColor(context, R.color.colorAccentCyan)
                            6 -> ContextCompat.getColor(context, R.color.colorAccentBlue)
                            else -> ContextCompat.getColor(context, R.color.colorAccentOrange)
                        }

                    // TODO: factor this out into its own class!!!
                    this.add(Line.Builder().setColor(color(2)))
                        .also { line ->
                            // set identifier
                            line.identifier = "Stonky"
                            // start demo coroutine

                            val queue = line.adapter.data
                            var job: Job? = null
                            selector {
                                val symbol = get(stock)
                                if (symbol.isEmpty()) return@selector null
                                var startTime = -1L
                                job?.cancel()?.also { queue.clear() }
                                job = CoroutineScope(Dispatchers.IO).launch {
                                    val flow = subscribe(symbol, Bars.path)
                                    flow.collect { json ->
                                        println(json)
//                                        if (json.startsWith(IDENTIFIER_DRAWINGS)) {
//                                            val drawings = Drawings.fromJson(json)
//                                            if (drawings != null) {
//                                                val newDrawings = drawings[symbol]!!.map { drawing ->
//                                                    DrawingGraphable(
//                                                        drawing.startY.toFloat(),
//                                                        drawing.label
//                                                    )
//                                                }
//                                                line.adapter.drawings = newDrawings
//                                            }
//                                        } else if (json.startsWith(symbol)) {
//                                            val c = queue.removeLast()
//                                            c.
//                                        } else {
                                            val bars = Bars.fromJson(json)
                                            if (bars != null) {
                                                //                                        println("Bars[${queue.size}: ${bars.bars}")
                                                val newCandle = bars.candles.map { candle ->
                                                    CandleGraphable(
                                                        x = candle.timeSeconds.toFloat(),
                                                        time = candle.timeSeconds * 1000L,
                                                        open = candle.open.toFloat(),
                                                        close = candle.close.toFloat(),
                                                        high = candle.high.toFloat(),
                                                        low = candle.low.toFloat(),
                                                        volume = candle.volume
                                                    )
                                                }[0]

                                                if (startTime == -1L) {
                                                    startTime = newCandle.time
                                                }

                                                // add new candle
                                                queue.add(newCandle)
                                            }
//                                        }
                                    }
                                }.also {
//                                    it.invokeOnCompletion {
//                                        println("subscribe job completed [$it]")
//
//                                        if (it == null) {
//                                            job = CoroutineScope(Dispatchers.IO).launch {
//                                                val flow = subscribe(symbol, "/quote")
//                                                flow.collect { response ->
//                                                    val c = if (queue.isEmpty()) {
//                                                        CandleGraphable.EMPTY_CANDLE
//                                                    } else queue.removeLast()
//
////                                                    println("SERVER: updating $c")
//
//                                                    val price =
//                                                        response.substringBefore(':').toFloat()
//                                                    val time = response.substringAfter(':').toLong()
//                                                    if (c is CandleGraphable) {
//                                                        c.update(price, time).forEach { candle ->
//                                                            println("SERVER: adding $candle")
//                                                            queue.add(candle)
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
                                }
                            }
                        }


                }
            }

        }}
    }

}