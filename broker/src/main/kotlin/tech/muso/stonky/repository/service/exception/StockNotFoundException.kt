package tech.muso.stonky.repository.service.exception

import java.lang.Exception

class StockNotFoundException(override val message:String) : Exception(message)