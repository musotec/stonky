package tech.muso.demo.common.entity

data class PairsTrade(val shortPosition: StockEntity, val longPosition: StockEntity) {
    val name: String get() = "${longPosition.symbol}-${shortPosition.symbol}\nPairs Trade"
    val profit: Double get() = shortPosition.profit + longPosition.profit
    val profitDay: Double get() =
                (shortPosition.currentPrice - shortPosition.openPrice) +
                (longPosition.currentPrice - longPosition.openPrice)
    val notionalValue: Double get() = longPosition.currentPrice - shortPosition.currentPrice
}