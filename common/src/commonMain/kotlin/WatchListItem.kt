import kotlinx.serialization.Serializable

@Serializable
data class WatchListItem(val symbol: String, val priority: Int) {
    val id: Int = symbol.hashCode()

    companion object {
        const val path = "/watchlist"
    }
}