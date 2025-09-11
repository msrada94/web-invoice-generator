package com.alicefield

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.form
import io.ktor.server.auth.session
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import kotlinx.serialization.Serializable

fun Application.configureSecurity(validUser: String, validPass: String) {
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    install(Authentication) {
        form("formAuth") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == validUser && credentials.password == validPass) {
                    UserIdPrincipal(credentials.name)
                } else null
            }
            challenge {
                call.respondRedirect("/")
            }
        }
        session<MySession>("sessionAuth") {
            validate { session ->
                if (session.username.isNotEmpty()) {
                    UserIdPrincipal(session.username)
                } else null
            }
            challenge {
                call.respondRedirect("/")
            }
        }
    }

}

@Serializable
data class MySession(val username: String)
