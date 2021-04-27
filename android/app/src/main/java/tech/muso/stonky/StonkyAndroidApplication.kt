package tech.muso.stonky

import android.app.Application
import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import tech.muso.demo.repos.utils.ViewModelProvider
import tech.muso.stonky.android.stocks.PortfolioFragment
import tech.muso.stonky.android.viewmodels.PortfolioViewModelFactory
import tech.muso.stonky.android.viewmodels.WatchlistViewModelFactory
import kotlin.collections.HashMap

/**
 * This class naturally provides us a global singleton for our application.
 *
 * A lot of Dependency Injection libraries use this singleton in order to do the Injection.
 * This is not always necessary, but can usually become the most logical way to achieve it, when the
 * injected parameter is an Android Context.
 */
class StonkyAndroidApplication: Application() {

    // java style singleton object for this class
    companion object {
        @Volatile
        private var instance: StonkyAndroidApplication? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: StonkyAndroidApplication().also { instance = it }
            }
    }


    /**
     * Manual Dependency Injection implementation.
     *
     * Here we are making a map of the pairs of ViewModelFactories and keys.
     * This allows us to have multiple factories at the same time.
     *
     * For instance, if we have each account or portfolio holding a certain list of stocks, we will
     * want each ViewModelFactory to be able to regenerate the ViewModel and provide the Repository
     * and any necessary parameters.
     */
    private val currentStockViewModelProviders: MutableMap<Int, androidx.lifecycle.ViewModelProvider.NewInstanceFactory> = HashMap()

    @ExperimentalCoroutinesApi
    @FlowPreview
    object Injector : ViewModelProvider() {
        // preemptively passing a context because we will need it for the eventual Room database
        override fun provideStocksViewModelFactory(context: Context): WatchlistViewModelFactory {
            return WatchlistViewModelFactory(
                getStockRepository(context)
            )
        }

        override fun providePortfolioViewModelFactory(context: Context): PortfolioViewModelFactory {
            return PortfolioViewModelFactory(
                getPortfolioRepository(context)
            )
        }
    }

    /**
     * Manual dependency injection allows for retrieval and generation of the multiple instances
     * of the StockListViewModelFactory.
     *
     * This returns a specific instance based on a hash mapping of the class of the [selector].
     * All instances of the Factory provide unique singletons.
     */
    @ExperimentalCoroutinesApi
    @FlowPreview
    fun getViewModelProvider(context: Context, selector: Any): androidx.lifecycle.ViewModelProvider.NewInstanceFactory {
        val key = selector.javaClass.hashCode()
        // return the provider if we already have one defined.
        if (currentStockViewModelProviders.containsKey(key)) {
            return currentStockViewModelProviders[key] ?: error("This cannot happen")
        }
        when(selector) {
            is PortfolioFragment -> {
                return Injector.providePortfolioViewModelFactory(context).also { factory ->
                    currentStockViewModelProviders[key] = factory
                }
            }
            else -> {
                return Injector.provideStocksViewModelFactory(context).also { factory ->
                    currentStockViewModelProviders[key] = factory
                }
            }
        }
    }
}