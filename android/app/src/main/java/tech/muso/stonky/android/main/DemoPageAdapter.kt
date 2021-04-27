package tech.muso.stonky.android.main

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tech.muso.stonky.android.R
import tech.muso.stonky.android.stocks.PortfolioFragment
import tech.muso.stonky.android.stocks.StockTradePairsFragment
import tech.muso.demo.theme.ThemeTestFragment
import tech.muso.stonky.android.stocks.GraphStockFragment

/**
 * A [FragmentPagerAdapter] that controls the display of a fragment for the current tab.
 */
class DemoPageAdapter(private val activity: FragmentActivity, fm: FragmentManager,
                      private val viewPagerScrollLock: TouchLockSemaphore)
    : FragmentStateAdapter(fm, activity.lifecycle) {

    /**
     * Factory to obtain a new fragment with a non-empty constructor.
     *
     * Although this is non-standard, and excessive in this basic example, the implementation is
     * both valuable and powerful for adherence to the Dependence Inversion and design-by-contract.
     *
     * The factory is owned by the page page adapter, which is tied to the parent Activity's
     * onCreate() lifecycle callback. This is FragmentFactory is best contained in this class, as
     * the ViewPager2 is owned by the Activity, and the locking mutex exists as a minimal interface
     * between the two. This keeps the business logic between reattaching the mutex contained within
     * the class that requires it in the first place.
     *
     * This way the MainActivity does not need to know _why_ the Adapter requires the mutex.
     * Since the Adapter is determining that it needs to pass it to the Fragments within it, the
     * MainActivity would need to know about what Fragments are in the adapter to explicitly
     * reattach the mutex.
     *
     * This is to achieve a MVP (Model-View-Presenter) pattern.
     *
     * With this Adapter class being the interface provided to the [TabLayoutMediator], which exists
     * as the controller/presenter in the middle.
     */
    private class FragmentFactoryImpl(val persistentTouchLock: TouchLockSemaphore) : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            return super.instantiate(classLoader, className)
//            return when (className) {
//                PinEntryFragment::class.java.name -> PinEntryFragment(
//                    persistentTouchLock
//                )
//                else -> super.instantiate(classLoader, className)
//            }
        }
    }

    // specify the fragment factory so that our activity can re-instantiate our PinEntryFragment
    // now that we have implemented the custom constructor
    init {
        fm.fragmentFactory = FragmentFactoryImpl(viewPagerScrollLock)
    }



    /**
     * Link the tab to the position by querying the current list of fragments.
     */
    fun link(tab: TabLayout.Tab, position: Int) {
        getInstance(viewPagerScrollLock)[position].apply {
            this.link(activity, tab)
        }
    }

    /**
     * Represent our fragment pages to give them icons or text and link it all up cleanly.
     */
    internal data class FragmentPage(
        val fragment: Fragment,
        @StringRes val stringRes: Int,
        @DrawableRes val drawableRes: Int
    ) {
        fun link(context: Context, tab: TabLayout.Tab) {
            // if we can resolve a valid drawable, set the icon for the tab
            context.getDrawable(drawableRes)?.let {
                tab.icon = it
            } ?: run { // otherwise, load the string resource, and set the title
                tab.text = context.resources.getString(stringRes)
            }
        }
    }

    override fun createFragment(position: Int): Fragment = getInstance(viewPagerScrollLock)[position].fragment
    override fun getItemCount(): Int = getInstance(viewPagerScrollLock).size


    val callback = object : ViewPager2.OnPageChangeCallback() {
        private var position = 0
        override fun onPageSelected(position: Int) {
            this.position = position
            super.onPageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == 0) pagesInstance?.get(position)?.fragment?.onHiddenChanged(false)
            super.onPageScrollStateChanged(state)
        }
    }

    override fun onViewDetachedFromWindow(holder: FragmentViewHolder) {
        pagesInstance?.get(holder.adapterPosition)?.fragment?.onHiddenChanged(true)
        super.onViewDetachedFromWindow(holder)
    }


    /**
     * Companion object as a helper for the SectionsPageAdapter class.
     *
     * This makes a Singleton of the pages in the adapter that is tied to the adapter object.
     */
    companion object {
        private var pagesInstance: List<FragmentPage>? = null

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun generateFragmentsList(viewPagerScrollMutex: TouchLockSemaphore): List<FragmentPage> {
            return ArrayList<FragmentPage>().apply {
//                add(
//                    FragmentPage(
//                        PinEntryFragment(viewPagerScrollMutex),
//                        R.string.fragment_name_pin,
//                        R.drawable.ic_baseline_lock_24)
//                )
                add(
                    FragmentPage(
                        PortfolioFragment(),
                        R.string.fragment_name_stocks,
                        R.drawable.ic_baseline_pie_chart_24
                    )
                )
                add(
                    FragmentPage(
                        GraphStockFragment(),
                        R.string.fragment_name_stocks,
                        R.drawable.ic_baseline_trending_up_24
                    )
                )
                add(
                    FragmentPage(
                        StockTradePairsFragment(),
                        R.string.fragment_name_stocks,
                        R.drawable.ic_baseline_list_24
                    )
                )
                add(
                    FragmentPage(
                        ThemeTestFragment(),
                        R.string.fragment_name_theme,
                        R.drawable.ic_android_color_control_normal_24dp)
                )
            }
        }

        internal fun getInstance(viewPagerScrollMutex: TouchLockSemaphore): List<FragmentPage> {
            return pagesInstance ?: generateFragmentsList(viewPagerScrollMutex)
                .also { generatedFragments ->
                    pagesInstance = generatedFragments
                }
        }
    }
}