package com.sagar.file_uploader

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Path

@RestController
class FileController {

    @Autowired
    lateinit var service: FileService

    @PostMapping("/file")
    fun uploadFile(@RequestPart("user") user: String, @RequestPart("file") file: Mono<FilePart>, @RequestHeader("Content-Length") size: Long): Mono<ResponseEntity<Path>> {

        return service.uploadFile(user, file, size)
    }

    @PostMapping("/multi-file")
    fun uploadMutipleFile(@RequestPart("user") user: String, @RequestPart("file") file: Flux<FilePart>, @RequestHeader("Content-Length") size: Long): Flux<Path> {

        return service.uploadMultipleFile(user, file, size)
    }

    @GetMapping("file/{user}/{fileName}")
    fun getFile(@PathVariable user: String, @PathVariable fileName: String): Mono<ResponseEntity<FileSystemResource>>{

        return service.getFile(user, fileName)
    }
}