package tech.muso.stonky.repository.controller

import TradeSet
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import tech.muso.stonky.repository.service.AlpacaService

@RestController
@RequestMapping("/v2/stocks")
class AlpacaController(
    val alpacaService: AlpacaService,
//    val subscriptionService: SubscriptionService
) {

    @GetMapping(value = ["/{symbol}/trades"])
    @ResponseStatus(HttpStatus.OK)
    private fun getTradeHistory(
        @PathVariable symbol: String,
        @RequestParam(required = true) dayWithUnixTimestamp: Long,
        @RequestParam(required = false) forceApiCall: Boolean = false
    ): TradeSet = if (forceApiCall) alpacaService.forceCacheTradesOfDay(symbol, dayWithUnixTimestamp) else alpacaService.getTrades(symbol, dayWithUnixTimestamp)

}