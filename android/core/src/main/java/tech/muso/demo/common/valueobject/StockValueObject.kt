package tech.muso.demo.common.valueobject

/**
 * The value objects are simple objects who's equality is based on the same Value.
 *
 * Two different instances of a Value Object can be equal,
 * so long as the underlying data is equal, and represent the same entity.
 *
 * It is VERY important that if two equivalent/equal value objects are created, they remain equal.
 *
 * Here, a sample object that represents a Stock.
 */
class StockValueObject(val ticker: String) {

    /** Value objects MUST override the equals operator **/
    @Suppress("LiftReturnOrAssignment")
    override fun equals(other: Any?): Boolean {
        if (other is StockValueObject)
            return ticker == other.ticker
        else return false
    }
}