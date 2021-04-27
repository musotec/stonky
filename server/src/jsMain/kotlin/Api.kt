import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer

import kotlinx.browser.window

val endpoint = window.location.origin // only needed until https://github.com/ktorio/ktor/issues/1695 is resolved

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}

suspend fun getStockList(): List<WatchListItem> {
    return jsonClient.get(endpoint + WatchListItem.path)
}

suspend fun addStockListItem(stockListItem: WatchListItem) {
    jsonClient.post<Unit>(endpoint + WatchListItem.path) {
        contentType(ContentType.Application.Json)
        body = stockListItem
    }
}

suspend fun deleteStockListItem(stockListItem: WatchListItem) {
    jsonClient.delete<Unit>(endpoint + WatchListItem.path + "/${stockListItem.id}")
}