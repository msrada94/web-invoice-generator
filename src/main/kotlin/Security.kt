package com.alicefield

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.form
import io.ktor.server.response.respondRedirect

fun Application.configureSecurity() {
    install(Authentication) {
        form("formAuth") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == "alice" && credentials.password == "0210") {
                    UserIdPrincipal(credentials.name)
                } else null
            }
            challenge {
                call.respondRedirect("/")
            }
        }
    }
}