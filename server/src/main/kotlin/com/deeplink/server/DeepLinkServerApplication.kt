package com.deeplink.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DeepLinkServerApplication

fun main(args: Array<String>) {
    runApplication<DeepLinkServerApplication>(*args)
}

