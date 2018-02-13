package seko0716.escp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.client.support.BasicAuthorizationInterceptor
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import javax.annotation.PostConstruct


fun ClosedRange<Int>.random() =
        Random().nextInt(endInclusive - start) + start

@SpringBootApplication
class EsCpApplication {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()

    @Autowired
    lateinit var restTemplate: RestTemplate

    @PostConstruct
    fun init() {
        val dataSet = File("./dataset/spring-framework").toPath()

        val headers = HttpHeaders()
        headers.add("Content-Type", "application/json")


        restTemplate.interceptors.add(BasicAuthorizationInterceptor("elastic", "343434"))
        Files.walkFileTree(dataSet, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val (src, language, title) = triple(file)

                val body = "{ \"title\": \"$title\",  \"language\": \"$language\",  \"src\": ${jacksonObjectMapper().writeValueAsString(src)},  \"visit_count\": ${(0..1000).random()} }"
                val request = HttpEntity(body, headers)

                restTemplate.put("http://0.0.0.0:9200/spec_6/spec_6/${UUID.randomUUID()}", request)
                return FileVisitResult.CONTINUE
            }

        })
    }

    private fun triple(file: Path): Triple<String, String, String> {
        val src = file.toFile().readText()
        val language = file.toFile().extension// расширение
        val title = file.toFile().nameWithoutExtension
        return Triple(src, language, title)
    }

}


fun main(args: Array<String>) {
    runApplication<EsCpApplication>(*args)
}


