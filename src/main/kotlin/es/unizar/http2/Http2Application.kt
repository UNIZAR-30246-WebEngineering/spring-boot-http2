package es.unizar.http2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class Http2Application

fun main(args: Array<String>) {
    runApplication<Http2Application>(*args)
}

@RestController
class TestController {
    @GetMapping fun test() = "it works!"
}
