package tech.muso.stonky.android.stocks

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.stonky.android.R

/**
 * Basic DialogFragment with temporary custom view for viewing the Stock object on click.
 */
class ViewPortfolioSliceDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(slice: PortfolioEntity): ViewPortfolioSliceDialogFragment {
            val args = Bundle()
            args.putString("title", slice.name)
            val fragment = ViewPortfolioSliceDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).apply {
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.view_portfolio_slice_details, null)
            setView(view)
            setMessage(arguments?.getString("title"))
            setCancelable(true)
        }.create()
    }
}