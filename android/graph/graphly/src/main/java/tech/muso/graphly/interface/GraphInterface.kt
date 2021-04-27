package tech.muso.graphly.`interface`

import tech.muso.stonky.common.PortfolioSlice

interface GraphInterface {
    fun onItemUnselected(unselectedObject: PortfolioSlice)
    fun onItemSelected(index: Int, selectedObject: PortfolioSlice?)
    fun onPortfolioEnter(selectedSlice: PortfolioSlice)
    fun onPortfolioExit(rootPortfolio: PortfolioSlice)
}