package tech.muso.stonky.repository.controller

import Bars
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import tech.muso.stonky.repository.service.TdaService

@RestController
@RequestMapping("/v1/marketdata")
class TdaController(
    val tdaService: TdaService,
//    val subscriptionService: SubscriptionService
) {

    @GetMapping(value = ["/{symbol}/priceHistory"])
    @ResponseStatus(HttpStatus.OK)
    private fun getPriceHistory(
        @PathVariable symbol: String,
        @RequestParam(required = true) dayWithUnixTimestamp: Long
    ): Bars = tdaService.getCandles(symbol, dayWithUnixTimestamp)

}