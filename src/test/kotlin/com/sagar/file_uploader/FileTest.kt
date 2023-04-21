package com.sagar.file_uploader

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.unit.DataSize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.nio.file.Path
import javax.naming.SizeLimitExceededException

@ExtendWith(MockitoExtension::class)
class FileTest {

    @InjectMocks
    private lateinit var service: FileService

    @Test
    fun testUploadFile() {

        service.path = "src/main/resources/files"
        service.max = DataSize.ofMegabytes(5)
        var filePart: FilePart = mock(FilePart::class.java)
        Mockito.`when`(filePart.filename()).thenReturn("test.jpg")
        Mockito.`when`(filePart.transferTo(Mockito.any(Path::class.java))).thenReturn(Mono.empty())
        val response: Mono<ResponseEntity<Path>> = service.uploadFile("test", filePart.toMono(), DataSize.ofMegabytes(2).toBytes())
        StepVerifier.create(response).expectNextMatches {res:ResponseEntity<Path> ->
            res.statusCode == HttpStatusCode.valueOf(200)
        }.verifyComplete()
    }

    @Test
    fun testEmptyFile(){

        var filePart: FilePart = mock(FilePart::class.java)
        Mockito.`when`(filePart.filename()).thenReturn("")
        var response: Mono<ResponseEntity<Path>> = service.uploadFile("test", filePart.toMono(), DataSize.ofMegabytes(1).toBytes())
        StepVerifier.create(response).verifyError(FileNotFoundException::class.java)
    }

    @Test
    fun testFileFormat(){

        var filePart: FilePart = mock(FilePart::class.java)
        Mockito.`when`(filePart.filename()).thenReturn("test.txt")
        var response: Mono<ResponseEntity<Path>> = service.uploadFile("test", filePart.toMono(), DataSize.ofMegabytes(1).toBytes())
        StepVerifier.create(response).verifyError(FileFormatException::class.java)
    }

    @Test
    fun testDuplicateFile(){

        service.path = "src/main/resources/files"
        service.max = DataSize.ofMegabytes(5)
        var filePart: FilePart = mock(FilePart::class.java)
        Mockito.`when`(filePart.filename()).thenReturn("jpg_car.jpg") // This file must exists in the File folder
        var response: Mono<ResponseEntity<Path>> = service.uploadFile("test", filePart.toMono(), DataSize.ofMegabytes(1).toBytes())
        StepVerifier.create(response).verifyError(FileExistsException::class.java)
    }

    @Test
    fun testFileSize() {

        service.path = "src/main/resources/files"
        service.max = DataSize.ofMegabytes(5)
        var filePart: FilePart = mock(FilePart::class.java)
        Mockito.`when`(filePart.filename()).thenReturn("test.jpg")
        val response: Mono<ResponseEntity<Path>> = service.uploadFile("test", filePart.toMono(), DataSize.ofMegabytes(6).toBytes())
        StepVerifier.create(response).verifyError(SizeLimitExceededException::class.java)
    }

    @Test
    fun testUserNotFound() {

        service.path = "src/main/resources/files"
        val response: Mono<ResponseEntity<FileSystemResource>> = service.getFile("test1", "test.jpg")
        StepVerifier.create(response).verifyError(UserNotFoundException::class.java)
    }

    @Test
    fun testFileNotFound() {

        service.path = "src/main/resources/files"
        val response: Mono<ResponseEntity<FileSystemResource>> = service.getFile("test", "test.jpg")
        StepVerifier.create(response).verifyError(FileNotFoundException::class.java)
    }

    @Test
    fun testGetFile() {

        service.path = "src/main/resources/files"
        val response: Mono<ResponseEntity<FileSystemResource>> = service.getFile("test", "jpg_car.jpg") // this file must exists
        StepVerifier.create(response).expectNextMatches{res->
            res.statusCode.is2xxSuccessful // The 2xx successful codes are a range of status codes from 200 to 299 that indicate successful HTTP requests.
        }.verifyComplete()
    }

    @Test
    fun testMultipleUploadFile(){

        service.path = "src/main/resources/files"
        service.max = DataSize.ofMegabytes(5)
        val filePart1: FilePart = Mockito.mock(FilePart::class.java)
        val filePart2: FilePart = Mockito.mock(FilePart::class.java)
        `when`(filePart1.filename()).thenReturn("test1.pdf")
        `when`(filePart2.filename()).thenReturn("test2.pdf")
        Mockito.`when`(filePart1.transferTo(Mockito.any(Path::class.java))).thenReturn(Mono.empty())
        Mockito.`when`(filePart2.transferTo(Mockito.any(Path::class.java))).thenReturn(Mono.empty())

        val response: Flux<Path> = service.uploadMultipleFile("test", Flux.just(filePart1, filePart2), DataSize.ofMegabytes(1).toBytes())

        StepVerifier.create(response).expectNextMatches {path->
            path.endsWith(filePart1.filename())
        }.expectNextMatches {path->
            path.endsWith(filePart2.filename())
        }.verifyComplete()
    }

    @Test
    fun testMultipleFileException(){

        val filePart1: FilePart = Mockito.mock(FilePart::class.java)
        val filePart2: FilePart = Mockito.mock(FilePart::class.java)

        `when`(filePart1.filename()).thenReturn("test1.txt")

        val response: Flux<Path> = service.uploadMultipleFile("test", Flux.just(filePart1, filePart2), DataSize.ofMegabytes(1).toBytes())

        StepVerifier.create(response).verifyError(FileFormatException::class.java)
    }
}
