package tech.muso.graphly.model

class OldPortfolioSlice(val ticker: String, var weight: Float, var price: Double, var quantity: Double) {

    val value get() = price * quantity

    override fun toString(): String {
        return "\$${String.format("%.2f",value)}"
    }
}