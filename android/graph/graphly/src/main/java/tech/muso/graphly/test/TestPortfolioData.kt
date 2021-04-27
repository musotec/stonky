package tech.muso.graphly.test

import tech.muso.stonky.common.PortfolioSlice
import tech.muso.graphly.model.GraphAdapter
import tech.muso.graphly.model.OldPortfolioSlice

//class TestPortfolioData : GraphAdapter<PortfolioSlice>() {
//
//
//    val stocks: List<OldPortfolioSlice>
//    val stocks2: List<OldPortfolioSlice>
//
//    init {
//        stocks = listOf ( // TODO: 2/12/2020 load this from a real api
//            PortfolioSlice("AMD", 0.1f, 11.0, 1.0),
//            PortfolioSlice("XLNX", 0.3f, 17.0, 1.0),
//            PortfolioSlice("SPXU", 0.2f, 18.0, 1.0),
//            PortfolioSlice("TSM", 0.35f, 34.0, 1.0),
//            PortfolioSlice("INTC", 0.05f, 6.0, 1.0)
//        )
//
//        stocks2 = listOf (
//            PortfolioSlice("AMD", 0.2f, 11.0, 1.0),
//            PortfolioSlice("XLNX", 0.45f, 17.0, 1.0),
//            PortfolioSlice("SPXU", 0.15f, 18.0, 1.0),
//            PortfolioSlice("TSM", 0.05f, 34.0, 1.0),
//            PortfolioSlice("INTC", 0.15f, 6.0, 1.0)
//        )
////        stocks["AMD"] = Stock(.1f, Holding(5, 5.50))
////        stocks["SPXU"] = Stock(.3f, Pair(10, 10.1))
////        stocks["XLNX"] = Stock(.05f, Pair(4, 7.1))
////        stocks["TSM"] = Stock(.525f, Pair(10, 8.0))
////        stocks["INTC"] = Stock(.025f, Pair(8, 3.9))
//    }
//
//    val total: Double = stocks.sumByDouble { it.value }
//
//    override fun getCount(): Int {
//        return stocks.size
//    }
//
//    override fun getItem(index: Int): PortfolioSlice {
//        return stocks[index]
//    }
//
//    override fun getY(index: Int): Float {
//        return (stocks[index].value/total).toFloat()
//    }
//
////    override fun getHorizontalWeights(): List<Float> {
////        return stocks.map { (it.value.first) }
////    }
//
//    override fun getData(): List<PortfolioSlice> {
//        return stocks
//    }
//}