package tech.muso.stonky.repository.controller.exception.handler

import tech.muso.stonky.repository.service.exception.TradeNotFoundException
import tech.muso.stonky.repository.service.exception.StockNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(value = [TradeNotFoundException::class, StockNotFoundException::class])
    fun doHandleExceptions(ex: Exception): ResponseEntity<Body> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Body(ex.message.orEmpty()))
    }

    data class Body(val message: String)
}