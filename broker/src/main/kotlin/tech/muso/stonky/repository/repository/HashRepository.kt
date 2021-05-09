package tech.muso.stonky.repository.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.QueryByExampleExecutor
import org.springframework.stereotype.Repository
import tech.muso.stonky.repository.model.HashedStockMetadata

@Repository
interface HashRepository : CrudRepository<HashedStockMetadata, String>, QueryByExampleExecutor<HashedStockMetadata>