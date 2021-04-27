import react.*
import react.dom.*
import kotlinext.js.*
import kotlinx.html.js.*
import kotlinx.coroutines.*

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (shoppingList, setShoppingList) = useState(emptyList<WatchListItem>())

    useEffect(dependencies = listOf()) {
        scope.launch {
            setShoppingList(getStockList())
        }
    }

    h1 {
        +"Stock Watchlist"
    }

    child(
        InputComponent,
        props = jsObject {
            onSubmit = { input ->
                val cartItem = WatchListItem(input.replace("!", ""), input.count { it == '!' })
                scope.launch {
                    addStockListItem(cartItem)
                    setShoppingList(getStockList())
                }
            }
        }
    )

    ul {
        shoppingList.sortedByDescending(WatchListItem::priority).forEach { item ->
            li {
                key = item.toString()
                +"[${item.priority}] ${item.symbol} "
            }

            attrs.onClickFunction = {
                scope.launch {
                    deleteStockListItem(item)
                    setShoppingList(getStockList())
                }
            }
        }
    }

}