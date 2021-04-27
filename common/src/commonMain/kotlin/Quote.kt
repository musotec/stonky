import kotlinx.serialization.Serializable

@Serializable
data class Quote(val symbol: String, val lastPrice: Double) {

    companion object {
        const val path = "/quote"
    }
}
