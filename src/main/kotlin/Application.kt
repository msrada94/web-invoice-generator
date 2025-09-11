package com.alicefield

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val validUser = System.getenv("APP_USER") ?: "defaultUser"
    val validPass = System.getenv("APP_PASS") ?: "defaultPass"

    println("Starting server on port $port")

    embeddedServer(Netty, port = port) {
        module(validUser, validPass)
    }.start(wait = true)
}

fun Application.module(validUser: String, validPass: String) {
    configureSecurity(validUser,validPass)
    configureRouting()
}
