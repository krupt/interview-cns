package ru.cns.util.error

import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import ru.cns.errors.BaseSimpleMessageException

@ControllerAdvice
class ErrorsTranslator {

    private companion object : KLogging()

    @ExceptionHandler(BaseSimpleMessageException::class)
    fun handleBaseSimpleMessage(e: BaseSimpleMessageException): ResponseEntity<String> {
        val annotation = e.javaClass.getAnnotation(ResponseStatus::class.java)
        val responseBuilder = if (annotation != null) {
            ResponseEntity.status(annotation.value)
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return responseBuilder.body(e.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<String> {
        logger.error("Request failed", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error. Please, contact system administrator")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException) =
            ResponseEntity.badRequest()
                    .body(e.bindingResult.allErrors
                            .map { it as FieldError }
                            .joinToString("\n") { "${it.field}: ${it.defaultMessage}" }
                    )
}
