package tech.muso.stonky.repository.controller

import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.service.StockService
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PastOrPresent

@RestController
@RequestMapping("/v1/stock")
class StockController(
    val stockService: StockService,
//    val subscriptionService: SubscriptionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    private fun createStock(@Valid stock: StockDto): Stock = stockService.createStock(stock)

    @GetMapping(value = ["/{id}"])
    @ResponseStatus(HttpStatus.OK)
    private fun getStockById(@PathVariable id: String): Stock = stockService.getStock(id)

    @PutMapping(value = ["/{id}"])
    @ResponseStatus(HttpStatus.OK)
    private fun updateStock(@PathVariable id: String, @Validated stock: StockDto): Stock = stockService.updateStock(id, stock)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private fun getStocks(): List<Stock> = stockService.getAllStocks()

    @DeleteMapping(value = ["/{id}"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun deleteStock(id: String) = stockService.deleteStock(id)


//    @GetMapping(value = ["/subscribe"], produces = [APPLICATION_STREAM_JSON_VALUE])
//    private fun subscribeToStock(): Subscriber = subscriptionService.subscribe(Subscriber())

    data class StockDto(
        @get:NotBlank val symbol: String?,
        @get:NotBlank val name: String?,
        @get:Min(value = 1900) @PastOrPresent val year: Int
    )
}