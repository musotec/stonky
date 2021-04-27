package tech.muso.stonky.android.viewmodels.stocks

import tech.muso.stonky.common.PortfolioSlice
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import tech.muso.demo.common.entity.Fundamentals
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.demo.common.entity.Profile
import tech.muso.demo.repos.PortfolioDataRepository
import tech.muso.stonky.android.subscribe
import tech.muso.stonky.android.viewmodels.StockFilter

@ExperimentalCoroutinesApi
@FlowPreview
class PortfolioViewModel internal constructor(
    private val repository: PortfolioDataRepository
) : ViewModel() {

    /** True if we are currently loading data. */
    val loadingState: LiveData<Boolean> get() = _loadingState
    private val _loadingState = MutableLiveData<Boolean>(false)

    /** Requested snackbar display message. Call [resetSnackbarState] after handling. */
    val snackbar: LiveData<String?> get() = _snackbar
    private val _snackbar = MutableLiveData<String?>(null)

    /** Reset the snackbar message after it has been handled. */
    fun resetSnackbarState() { _snackbar.value = null }

    /** The current filter set applied to the list of stocks. */
    private val filters = MutableLiveData<StockFilter>(0)   // TODO;

    /** The currently viewed Portfolio. */
    val portfolio: LiveData<PortfolioSlice> get() = _portfolio
    private val _portfolio = MutableLiveData<PortfolioSlice>()
    /** The currently selected slice. */
    val selectedSlice: LiveData<PortfolioSlice?> get() = _selectedSlice
    private val _selectedSlice = MutableLiveData<PortfolioSlice?>()

    /** Enter the selected portfolio if and only if it is within our current portfolio. */
    @MainThread
    fun enterPortfolio(selectedSlice: PortfolioSlice) {
        if (selectedSlice.parent == portfolio.value) {
            _portfolio.value = selectedSlice     // uses value directly. should only be called from main thread.
        }
    }

    /** Exit the portfolio if and only if it is our current portfolio. */
    @MainThread
    fun exitPortfolio(portfolio: PortfolioSlice) {
        if (portfolio == this.portfolio.value) {
            _portfolio.value = portfolio.parent     // uses value directly. should only be called from main thread.
        }   // NOTE: should already avoid duplicate posts for when parent = self for root portfolio
    }

    @MainThread
    fun onItemSelected(position: Int, selectedSlice: PortfolioSlice?) {
        _selectedSlice.value = selectedSlice
    }

    /**
     * LiveData object that contains a list of the current stocks from the database.
     * TODO: implement filters LiveData and filters at the repository level
     */
    val slices: LiveData<List<PortfolioEntity>> = portfolio.switchMap { rootPortfolio ->
        MutableLiveData<List<PortfolioEntity>>(
            rootPortfolio.flatListAll().map {
//                .flatMap {
//                it.map {
                    PortfolioEntity(
                        it.name, it.name,
                        Fundamentals(0, 0.0, 0.0, 0),
                        Profile("https://logos.m1finance.com/${it.name}?size=128")
                    ).apply {
                        currentPrice = it.marketValue
                    }
//                }
            }
        )
    }

    // TODO: remove this.
    fun loadData(view: View?) {
        launchDataLoad {
            // TODO: remove.
            val flow = subscribe("0", PortfolioSlice.path)
            flow.collect { json ->
                println("PORTFOLIO: $json")
                _portfolio.value = PortfolioSlice.fromJson(json)
            }
        }
    }

    fun testLoadingBarFunctionality() {
        Log.d("StockViewModel", "testInvocationFromOnClick()")

        viewModelScope.launch {
            _snackbar.value = "Test Snackbar Action"
            delay(5000)
        }
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _loadingState.value = true
                delay(1000) // TODO: remove artificial delay
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                // stop loading bar when done
                _loadingState.value = false
            }
        }
    }

    init {
        launchDataLoad {
            repository.tryUpdatePortfolioCache()
        }
    }
}