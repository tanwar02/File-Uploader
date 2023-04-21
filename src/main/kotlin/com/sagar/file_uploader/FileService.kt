package com.sagar.file_uploader

import org.reactivestreams.Subscriber
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.util.unit.DataSize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.naming.SizeLimitExceededException

@Service
class FileService {

    @Value("\${folder}")
    lateinit var path: String

    @Value("\${size}")
    lateinit var max:DataSize

    fun getFileNames(user: String): Flux<String> {

        return Flux.fromIterable(Files.list(Paths.get("$path/$user")).toList())
            .map { name->
                name.fileName.toString()
            }
    }

    fun checkEmptyUser(user: String){

        if(user == "")
            throw UserNotFoundException("User can not be Empty.")
    }

    fun checkEmptyFile(fileName: String){

        if(fileName == "")
            throw FileNotFoundException("File is not selected.")
    }

    fun checkFileFormat(fileName: String){

        try{
            var res: FileExtension = FileExtension.valueOf(fileName.split(".").last().uppercase())
        }
        catch(ex: IllegalArgumentException){
            throw FileFormatException("Only PNG, JPEG, PDF, CSV, XLS and DOC files are allowed.")
        }
    }

    fun checkDuplicateFile(user: String, fileName: String){

        if(!File("$path/$user").exists()){

            File("$path/$user").mkdir()
            return
        }

        var hasError= false

        getFileNames(user).filter {fName->
            fName == fileName
        }.doOnNext {
            hasError = true
        }.subscribe()

        if(hasError)
            throw FileExistsException("File with same name exists.")
    }

    fun checkFileSize(size: Long){

        if(size > max.toBytes())
            throw SizeLimitExceededException("File size should not exceed 5MB and the size of current file is ${DataSize.ofBytes(size).toMegabytes()}MB.")
    }

    fun uploadFile(user: String, file: Mono<FilePart>, size: Long): Mono<ResponseEntity<Path>> {

        return file
            .doOnNext{ part ->
                checkEmptyUser(user)
                checkEmptyFile(part.filename())
                checkFileFormat(part.filename())
                checkFileSize(size)
                checkDuplicateFile(user, part.filename())
            }
            .flatMap { part ->
                val target: Path = Paths.get("$path/$user/${part.filename()}")
                part.transferTo(target).thenReturn(ResponseEntity.ok().body(target)).onErrorMap {
                    Exception("Something went wrong")
                }
            }
    }

    fun uploadMultipleFile(user: String, file: Flux<FilePart>, size: Long): Flux<Path> {

        checkEmptyUser(user)
        return file
            .doOnNext{ part ->
                checkEmptyFile(part.filename())
                checkFileFormat(part.filename())
                checkFileSize(size)
                checkDuplicateFile(user, part.filename())
            }
            .flatMap { part ->
                val target: Path = Paths.get("$path/$user/${part.filename()}")
                part.transferTo(target).thenReturn(target).onErrorMap {
                    Exception("Something went wrong")
                }
            }
    }

    fun getFile(user: String, fileName: String): Mono<ResponseEntity<FileSystemResource>>{

        return Mono.just(fileName).filter{
            File("$path/$user").exists()
        }.switchIfEmpty{
            throw UserNotFoundException("User $user does not exists.")
        }.filter{name->
            File("$path/$user/$name").exists()
        }.switchIfEmpty{
            throw FileNotFoundException("File $fileName does not exists.")
        }.flatMap {name->
            val headers = HttpHeaders()
            headers.setContentDispositionFormData(name, name)
            val resource = FileSystemResource("$path/$user/$name")
            Mono.just(ResponseEntity.ok().headers(headers).body(resource))
        }
    }
}
