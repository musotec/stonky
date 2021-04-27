package tech.muso.stonky.android.stocks

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import tech.muso.stonky.StonkyAndroidApplication
import tech.muso.stonky.android.databinding.FragmentStocksBinding
import tech.muso.stonky.android.viewmodels.stocks.WatchlistViewModel

/**
 * Basic Fragment that just connects the ViewModel to the RecyclerView to display the Stocks.
 */
@ExperimentalCoroutinesApi
class StockListFragment : Fragment() {

    @FlowPreview
    private val viewModel: WatchlistViewModel by viewModels {
        StonkyAndroidApplication.getInstance().getViewModelProvider(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStocksBinding.inflate(inflater, container, false)
        context ?: return binding.root

        // provide the viewModel to the binding
        binding.viewModel = viewModel

        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.snackbar.observe(viewLifecycleOwner) { snackBarMessage ->
            snackBarMessage?.let {
                Snackbar.make(binding.root, snackBarMessage, Snackbar.LENGTH_SHORT).show()
                viewModel.resetSnackbarState()
            }
        }

        val adapter = StockRecyclerViewAdapter() { stock ->
//            ViewPortfolioSliceDialogFragment.newInstance(stock).show(childFragmentManager, "dialog_tag")
        }

        binding.stockList.adapter = adapter
        subscribeUi(adapter)

        return binding.root
    }

    private fun subscribeUi(adapter: StockRecyclerViewAdapter) {
        viewModel.stocks.observe(viewLifecycleOwner) { stocks ->
            adapter.submitList(stocks)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

}
