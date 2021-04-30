package tech.muso.stonky.repository.controller

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import tech.muso.stonky.repository.model.Trade
import tech.muso.stonky.repository.model.Stock
import tech.muso.stonky.repository.service.TradeService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Past

@RestController
@RequestMapping("/v1/trades")
class TradeController(val tradeService: TradeService) {

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    private fun createTrade(@Validated trade: TradeDto): Trade = tradeService.createTrade(trade)

    @GetMapping(value = ["/{id}"])
    @ResponseStatus(HttpStatus.OK)
    private fun getTradeById(@PathVariable id: String): Trade = tradeService.getTrade(id)

    @PutMapping(value = ["/{id}"])
    @ResponseStatus(HttpStatus.OK)
    private fun updateTrade(@PathVariable id: String, @Validated trade: TradeDto): Trade = tradeService.updateTrade(id, trade)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private fun getTrades(): List<Trade> = tradeService.getAllTrades()

    @DeleteMapping(value = ["/{id}"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun deleteTrade(id: String) = tradeService.deleteTrade(id)

//    @PatchMapping(value = ["/{tradeId}/link/{symbol}"])
//    @ResponseStatus(HttpStatus.OK)
//    private fun addTradeToSymbol(@PathVariable tradeId: String, @PathVariable stockId: String): Stock {
//        return tradeService.addTradeToStock(tradeId, stockId)
//    }

    data class TradeDto(
            @get:NotBlank val foo: String,
            @get:NotBlank val bar: String,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @field:JsonDeserialize(using = LocalDateDeserializer::class)
            @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            @get:Past val date: LocalDate
    )
}