package seko0716.escp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
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

    @PostConstruct
    fun init() {
        val dataSet = File("./dataset/spring-framework").toPath()

        Files.walkFileTree(dataSet, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val src = file.toFile().readText()
                val language = file.toFile().extension// расширение
                val title = file.toFile().nameWithoutExtension

                restTemplate.put("http://10.106.12.119:9201/spec_6/spec_6/${UUID.randomUUID()}", "{ \"title\": $title,  \"language\": $language,  \"src\": $src,  \"visit_count\": ${(0..1000).random()} }")
                return FileVisitResult.CONTINUE
            }
        })
    }

}

@Autowired
lateinit var restTemplate: RestTemplate

fun main(args: Array<String>) {
    runApplication<EsCpApplication>(*args)
}


