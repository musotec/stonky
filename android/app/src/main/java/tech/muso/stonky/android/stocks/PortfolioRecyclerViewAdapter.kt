package tech.muso.stonky.android.stocks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tech.muso.demo.common.entity.PortfolioEntity
import tech.muso.stonky.android.databinding.ListItemPortfolioCardViewBinding

/**
 * The commented out code below has a lot of the same code as if we were to simply use the
 * [DiffUtil.ItemCallback] instead of the full [DiffUtil.Callback].
 *
 * The benefits here are that if we can reduce our code by standardizing the list updating logic,
 * which will usually be useless boilerplate code unless something more custom is desired.
 *
 * For this reason, we are extending the [ListAdapter] class, providing the [DiffUtil.ItemCallback].
 * [ListAdapter] introduced in API 27, provides us this convenience wrapper and uses an
 * [AsyncListDiffer] in order to automatically run the DiffUtil on a background thread, as well as
 * managing the logic of swapping lists using the [ListAdapter.submitList] method.
 *
 * Reducing boilerplate leaves less room for the introduction of bugs due to "mis-implementation".
 *
 * I prefer this for new development, but examples that might fall outside of this scope could be:
 * A case where multiple lists of completely different objects are displayed in the same adapter,
 * Or perhaps customized items for "section headers" thrown into the list.
 * However, these use cases should carefully consider alternative development approaches.
 */

// NOTE: commented code intentionally left to emphasize code reduction

//class StockRecyclerViewAdapter(private var items: List<Stock> = arrayListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    fun submitList(newItemList: List<Stock>) {
//        val oldList = items
//        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
//                StockDiffCallback2(
//                        oldList,
//                        newItemList
//                )
//        )
//
//        items = newItemList
//        diffResult.dispatchUpdatesTo(this)
//    }
//
//    private fun getItem(index: Int): Stock {
//        return items[index]
//    }
//
//    override fun getItemCount(): Int {
//        return items.size
//    }

class PortfolioRecyclerViewAdapter(private val viewClickCallback: ((PortfolioEntity) -> Unit)? = null) : ListAdapter<PortfolioEntity, RecyclerView.ViewHolder>(PortfolioSliceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PortfolioItemViewHolder(
                ListItemPortfolioCardViewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val slice = getItem(position)
        (holder as PortfolioItemViewHolder).bind(slice, viewClickCallback)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        println(":detach ${holder.adapterPosition}")
        super.onViewDetachedFromWindow(holder)
    }

    /**
     * This custom class helps us transform our ViewBinding into a [RecyclerView.ViewHolder].
     * This way we can attach the item data to the list item view, and provide the component to the
     * RecyclerView so that it may handle View caching and recycling logic from there.
     */
    class PortfolioItemViewHolder(
        private val binding: ListItemPortfolioCardViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PortfolioEntity, viewClickCallback: ((PortfolioEntity) -> Unit)? = null) {
            binding.apply {
                this.root.setOnClickListener {
                    viewClickCallback?.invoke(item)
                }

                slice = item
                // the explicit executePendingBindings() call is required in order to update the
                // bindings immediately, instead of on the next draw frame.
                executePendingBindings()
                // This avoids a race condition between the RecyclerView measurement of the view
                // and the data binding. This can create many issues with the wrong data being in
                // the fields of the ViewHolder at the time of the DiffUtil calculations.
                // The end result being lots of hard to track down issues.

                // Maybe in the future Google will simplify this logic as well by making an API
                // where a ViewHolder can extend or take in some generic ViewBinding class.

                // I've learned not to create your own implementations of these things, it has made
                // migration more difficult when they choose to implement slightly differently due
                // to use cases outside of your scope.
            }
        }
    }
}

//private class StockDiffCallback(
//    val oldList: List<Stock>,
//    val newList: List<Stock>
//) : DiffUtil.Callback() {
//    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        return oldList[oldItemPosition].symbol == newList[newItemPosition].symbol
//    }
//
//    override fun getOldListSize(): Int {
//        return oldList.size
//    }
//
//    override fun getNewListSize(): Int {
//        return newList.size
//    }
//
//    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
//        return super.getChangePayload(oldItemPosition, newItemPosition)
//    }
//
//    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        return oldList[oldItemPosition] == newList[newItemPosition]
//    }
//}

/**
 * DiffUtil provides for us to obtain a space optimized, O(N + D^2) update to the RecyclerView.
 * Where D is the cost of executing the comparison code.
 *
 * This is far preferred over the default implementation, which requires the developer to either
 * write their code to explicitly state indices to update, as well as the logic.
 *
 * Because this usually ends up becoming extra work, I have found that laziness almost always leads
 * to developers invalidating the entire list. Which is extremely inefficient, and can lead to bugs
 * with components in the ViewHolder due to excessive view creation/binding.
 */
private class PortfolioSliceDiffCallback : DiffUtil.ItemCallback<PortfolioEntity>() {

    override fun areItemsTheSame(oldItem: PortfolioEntity, newItem: PortfolioEntity): Boolean {
        return oldItem.symbol == newItem.symbol// && oldItem.selected == newItem.selected
    }

    override fun areContentsTheSame(oldItem: PortfolioEntity, newItem: PortfolioEntity): Boolean {
        return oldItem == newItem// && oldItem.selected == newItem.selected
    }
}