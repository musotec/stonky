package tech.muso.demo.repos.api

import androidx.lifecycle.LiveData
import tech.muso.demo.common.entity.PortfolioEntity

/**
 * Define all the methods that need to appear in our Stock Repository.
 *
 * It is internal to the package as we do not wish to expose this interface.
 *
 * The repository is the mediator between our ViewModel and the Database.
 */
internal interface CachedPortfolioRepo {

    companion object {
        const val API_VERSION = 1
    }

    /**
     * Get a list of all our Stocks
     */
    val portfolio: LiveData<List<PortfolioEntity>>

    /**
     * Get a filtered sublist of our Stocks who contain the search characters.
     *
     * TODO: perform a fuzzy search.
     */
    fun filterBySubstring(search: String): LiveData<List<PortfolioEntity>>
}