package tech.muso.stonky.android.viewmodels.stocks

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import tech.muso.demo.common.entity.StockEntity
import tech.muso.demo.repos.StockDataRepository
import tech.muso.stonky.android.viewmodels.NoFilter
import tech.muso.stonky.android.viewmodels.StockFilter

@ExperimentalCoroutinesApi
@FlowPreview
class WatchlistViewModel internal constructor(
    private val repository: StockDataRepository
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

    /**
     * LiveData object that contains a list of the current stocks from the database.
     * TODO: implement filters LiveData and filters at the repository level
     */
    val stocks: LiveData<List<StockEntity>> = filters.switchMap { filter ->
        if (filter == NoFilter) {
            repository.stocks
        } else {
            repository.filterStocksBySubstring("filters")
        }
    }

    init {
        launchDataLoad {
            repository.tryUpdateStockCache()
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
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                // stop loading bar when done
                _loadingState.value = false
            }
        }
    }

}