package com.sagar.file_uploader

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import reactor.core.publisher.Mono
import javax.naming.SizeLimitExceededException

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): Mono<ResponseEntity<Response>>{

        return Mono.just(ResponseEntity.badRequest().body(Response(ex.message, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value())))
    }
    @ExceptionHandler(FileNotFoundException::class)
    fun handleFileNotFound(ex: FileNotFoundException): Mono<ResponseEntity<Response>> {

        return Mono.just(ResponseEntity.badRequest().body(Response(ex.message, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value())))
    }

    @ExceptionHandler(FileExistsException::class)
    fun handleFileExists(ex: FileExistsException): Mono<ResponseEntity<Response>> {

        return Mono.just(ResponseEntity.badRequest().body(Response(ex.message, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value())))
    }

    @ExceptionHandler(FileFormatException::class)
    fun handleFileFormat(ex: FileFormatException): Mono<ResponseEntity<Response>> {

        return Mono.just(ResponseEntity.badRequest().body(Response(ex.message, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value())))
    }

    @ExceptionHandler(SizeLimitExceededException::class)
    fun handleSizeLimitExceed(ex: SizeLimitExceededException): Mono<ResponseEntity<Response>> {

        return Mono.just(ResponseEntity.badRequest().body(Response(ex.message, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value())))
    }

    @ExceptionHandler(Exception::class)
    fun handleExcpetion(ex: Exception): Mono<ResponseEntity<Response>>{

        return Mono.just(ResponseEntity.internalServerError().body(Response(ex.message, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value())))
    }
}