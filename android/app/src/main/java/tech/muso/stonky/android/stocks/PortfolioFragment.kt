package tech.muso.stonky.android.stocks

import tech.muso.stonky.common.PortfolioSlice
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.stonky.StonkyAndroidApplication
import tech.muso.stonky.android.databinding.FragmentPortfolioBinding
import tech.muso.stonky.android.viewmodels.stocks.PortfolioViewModel
import tech.muso.graphly.`interface`.GraphInterface
import tech.muso.stonky.android.viewmodels.stocks.WatchlistViewModel

/**
 * Basic Fragment that just connects the ViewModel to the RecyclerView to display the Stocks.
 */
@ExperimentalCoroutinesApi
class PortfolioFragment : Fragment() {

    @FlowPreview
    private val viewModel: PortfolioViewModel by viewModels {
        StonkyAndroidApplication.getInstance().getViewModelProvider(requireContext(), this)
    }

    @FlowPreview
    private val viewModelList: WatchlistViewModel by viewModels {
        StonkyAndroidApplication.getInstance().getViewModelProvider(requireContext(), Any())
    }

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!     // This property is only valid between onCreateView and onDestroyView.

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            binding.portfolio.resetAnimation()
        } else {
            binding.portfolio.doAnimation()
        }

        super.onHiddenChanged(hidden)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        context ?: return binding.root

        // provide the viewModel to the binding
        binding.viewModel = viewModel

        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            binding.refreshLayout.isRefreshing = isLoading
        }

        binding.refreshLayout.setOnRefreshListener {
            viewModel.loadData(binding.refreshLayout)
        }

        viewModel.snackbar.observe(viewLifecycleOwner) { snackBarMessage ->
            snackBarMessage?.let {
                Snackbar.make(binding.root, snackBarMessage, Snackbar.LENGTH_SHORT).show()
                viewModel.resetSnackbarState()
            }
        }

        val adapter = PortfolioRecyclerViewAdapter { stock ->
            // TODO: change this to stock information
            ViewPortfolioSliceDialogFragment.newInstance(stock).show(childFragmentManager, "dialog_tag")
        }

        binding.stockList.adapter = adapter

        viewModel.portfolio.observe(viewLifecycleOwner) { portfolio ->
            binding.portfolio.rootPortfolio = portfolio
        }

        var lastSelected: PortfolioEntity? = null
        binding.portfolio.listener = object : GraphInterface {
            override fun onItemUnselected(unselectedObject: PortfolioSlice) {

            }

            override fun onItemSelected(position: Int, selectedSlice: PortfolioSlice?) {
                viewModel.onItemSelected(position, selectedSlice)   // FIXME: move the selection logic into this method.
                lastSelected?.selected = false

                binding.clearText.text = selectedSlice?.toDisplayString()

                // FIXME: scroll doesn't work, also stocklist livedata must be updated for objects to push to view
                binding.stockList.smoothScrollToPosition(position)

                lastSelected = viewModel.slices.value?.get(position)
                lastSelected?.currentPrice = lastSelected?.currentPrice ?: 0 + 10.0
                lastSelected?.selected = true
            }

            override fun onPortfolioEnter(selectedSlice: PortfolioSlice) {
                viewModel.enterPortfolio(selectedSlice)
            }

            override fun onPortfolioExit(portfolio: PortfolioSlice) {
                viewModel.exitPortfolio(portfolio)
            }
        }

        subscribeUi(adapter)

        return binding.root
    }

    private fun subscribeUi(adapter: PortfolioRecyclerViewAdapter) {
        viewModel.slices.observe(viewLifecycleOwner) { slices ->
            adapter.submitList(slices)
        }
    }


//    private fun subscribeUi(adapter: StockRecyclerViewAdapter) {
//        viewModelList.stocks.observe(viewLifecycleOwner) { stocks ->
//            adapter.submitList(stocks)
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

}
