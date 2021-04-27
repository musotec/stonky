package tech.muso.demo.common.entity

import androidx.room.*
import tech.muso.demo.common.valueobject.StockValueObject
import kotlin.math.abs

/**
 * The entity objects represent specific objects, and are not defined by their attributes.
 *
 * This way you can have multiple unique instances that are mutable.
 *
 * For this StockEntry example, the price of the stock can change, so the class is an entity.
 * This allows for separation from the Value Object variant, which should only contain attributes
 * common across instances, and that do not change after creation. Like ticker symbol, etc.
 */
@Entity
data class PortfolioEntity(
    @PrimaryKey @ColumnInfo(name = "id") val symbol: String,
    val name: String,
    @Embedded val fundamentals: Fundamentals,
    @Embedded val profile: Profile
) {
    @Ignore var selected = false

    val priceString get() = getPriceString(currentPrice)

    companion object {
        private val UNSET_VALUE = Double.MIN_VALUE
        @JvmStatic fun getPriceString(double: Double): String {
            return if (double >= 0) "$%.2f".format(double) else "($%.2f)".format(abs(double))
        }

        /**
         * Function that allows us to make a deep copy of the object.
         * We don't want to structure our code in a way that needs this long term.
         * However, I am doing this to avoid making higher order objects as I don't want to get
         * crazy with the scope given the current implementation; as this is just a demo.
         */
        fun PortfolioEntity.deepCopy(): PortfolioEntity {
            // regular copy() for data class copies only the constructor args
            return this.copy().also {
                it.openPrice = this.openPrice
                it.strikePrice = this.strikePrice
                it.currentPrice = this.currentPrice
                it.priceChangePercent = this.priceChangePercent
                it.amount = this.amount
                it.lastTradeVolume = this.lastTradeVolume
                it.tick = this.tick
            }
        }

    }

    var tick: Int = 0   // FIXME:
    var amount: Double = 0.0
    val isShort: Boolean get() = amount < 0
    val totalPrice: Double get() = amount * currentPrice
    var priceChangePercent: Double = UNSET_VALUE
    var openPrice: Double = UNSET_VALUE
    var strikePrice: Double = UNSET_VALUE
    var currentPrice: Double = UNSET_VALUE
        set(value) {
            // update the percent change
            if (openPrice != UNSET_VALUE) {
                priceChangePercent = ((value - openPrice) / openPrice) * 100
            } else {
                // or set the open price (i know this isn't how this actually works)
                openPrice = value
            }
            // update backing field
            field = value
        }

    val profit: Double get() = currentPrice - strikePrice
    var lastTradeVolume: Double = 0.0 // Double due to partial shares.

    // convenient reference of lightweight value object for comparison of non-changing data
    @Ignore // tell Room not to store this value object
    val valueObject: StockValueObject = StockValueObject(symbol)    // FIXME: check logic and change to Portfolio

    /**
     * Stocks should be equal if the two value objects are the same.
     * (the stock symbol will always represent the same company name, date of foundation, etc.)
     * This is faster than doing a full object comparison.
     *
     * The Stock will always have the same current price, last trade volume, etc.
     *
     * In an example use case, we are doing a comparison between two stock lists.
     * One Stock is updated with price information; while the other Stock object is in a watchlist.
     * Because the two objects contain different information as entities, we still want them to pass
     * an equality check.
     *
     * If we had two accounts with different amounts of the same stock, that case should use a
     * different Entity class. Then, the two holdings would always have different values unless they
     * are truly the same object.
     */
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false
        other as PortfolioEntity
        return valueObject == other.valueObject
    }
}