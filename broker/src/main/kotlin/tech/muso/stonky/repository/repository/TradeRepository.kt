package tech.muso.stonky.repository.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import tech.muso.stonky.repository.model.TestTrade

@Repository
interface TradeRepository : CrudRepository<TestTrade, String>