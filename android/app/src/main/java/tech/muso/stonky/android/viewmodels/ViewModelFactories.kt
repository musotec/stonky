package tech.muso.stonky.android.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.*
import tech.muso.demo.repos.PortfolioDataRepository
import tech.muso.demo.repos.StockDataRepository
import tech.muso.stonky.android.viewmodels.stocks.PortfolioViewModel
import tech.muso.stonky.android.viewmodels.stocks.StocksTradePairsViewModel
import tech.muso.stonky.android.viewmodels.stocks.WatchlistViewModel

// Define some aliases for readability
typealias StockFilter = Int
const val NoFilter: StockFilter = 0

/**
 * Factory class, which serves up the repository singleton to the ViewModels that are created.
 *
 * This way we avoid worrying about multiple instances of the Stocks repository across fragments.
 *
 * e.g. if there were multiple collections of stocks, one for watch list, one for long positions,
 * and a third for short positions; we wouldn't need to manage three different repositories for
 * accessing the same data.
 *
 * This also allows us to determine what type of ViewModel we will need to generate based on the
 * class name.
 */
@Suppress("UNCHECKED_CAST")
class WatchlistViewModelFactory(
    private val repository: StockDataRepository
) : ViewModelProvider.NewInstanceFactory() {

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass.name) {
            WatchlistViewModel::class.java.name ->
                WatchlistViewModel(
                    repository
                )
            else ->
                StocksTradePairsViewModel(
                    repository
                )
        } as T
    }
}


@Suppress("UNCHECKED_CAST")
class PortfolioViewModelFactory(
    private val repository: PortfolioDataRepository
) : ViewModelProvider.NewInstanceFactory() {

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass.name) {
            PortfolioViewModel::class.java.name ->
                PortfolioViewModel(
                    repository
                )
            else ->
                PortfolioViewModel(
                    repository
                )
        } as T
    }
}