package tech.muso.stonky.repository.service.exception

import java.lang.Exception

class TradeNotFoundException(override val message: String): Exception(message)