package com.alicefield

import com.alicefield.InvoiceGenerator.generateInvoiceByteArray
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text.Html
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.html.FormMethod
import kotlinx.html.body
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.passwordInput
import kotlinx.html.submitInput
import kotlinx.html.textInput
import kotlinx.html.*


fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            println("Error: ${cause.message}")
            call.respondRedirect("/error?msg=${cause.message}")
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respondRedirect("/error?msg=Página no encontrada")
        }
    }

    routing {
        staticResources("/static", "static")

        get("/") {
            call.respondHtml {
                body {
                    form(action = "/login", method = FormMethod.post) {
                        h2 { +"Invoice Generator" }
                        textInput { name = "username"; placeholder = "Username" }
                        passwordInput { name = "password"; placeholder = "Password" }
                        submitInput { value = "Login" }
                    }
                }
            }
        }

        authenticate("formAuth") {
            post("/login") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.sessions.set(MySession(principal.name))
                call.respondRedirect("form")
            }
        }

        authenticate("sessionAuth") {
            get("/form") {
                call.respondText(this::class.java.classLoader.getResource("static/index.html")!!.readText(), Html)
            }

            post("/submit") {
                val params = call.receiveParameters()

                val projects = mutableListOf<ProjectRecord>()
                var i = 0
                while (true) {
                    val name = params["projects[$i].name"] ?: break
                    val description = params["projects[$i].description"]
                        ?: throw Exception("Description is required")
                    val hours = params["projects[$i].hours"]?.toFloatOrNull()
                        ?: throw Exception("Hours worked are required")
                    projects.add(ProjectRecord(name, description, hours))
                    i++
                }
                val addressString = formatAddress(params)

                val invoice = InvoiceSummaryData(
                    contactName = params["contactName"] ?: throw Exception("Contact name is required"),
                    clientCompany = params["clientCompany"] ?: throw Exception("Client company is required"),
                    address = addressString,
                    phone = params["phone"] ?: throw Exception("Phone is required"),
                    email = params["email"] ?: throw Exception("Email is required"),
                    invoiceNumber = params["invoiceNumber"]?.toIntOrNull()
                        ?: throw Exception("Invoice number is required"),
                    invoiceDate = params["invoiceDate"] ?: throw Exception("Invoice date is required"),
                    dueDate = params["dueDate"] ?: throw Exception("Due date is required"),
                    startDate = params["startDate"] ?: throw Exception("Start date is required"),
                    endDate = params["endDate"] ?: throw Exception("End date is required"),
                    hourlyRate = params["hourlyRate"]?.toIntOrNull()
                        ?: throw Exception("Hourly rate is required"),
                    projects = projects
                )

                val invoiceData = invoice.toInvoiceData()
                val pdfBytes = generateInvoiceByteArray(invoiceData)

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Inline.withParameter(
                        ContentDisposition.Parameters.FileName,
                        "${invoiceData.invoiceInfo.invoiceID}.pdf"
                    ).toString()
                )

                call.respondBytes(pdfBytes, ContentType.Application.Pdf)
            }

            get("/error") {
                val msg = call.request.queryParameters["msg"] ?: "Unknown error"
                call.respondHtml {
                    head { title { +"Error" } }
                    body {
                        style = "font-family:Arial,sans-serif;text-align:center;margin-top:50px;"
                        h1 { style = "color:red;"; +"Error" }
                        h2 { +msg }
                        button {
                            type = ButtonType.button
                            style = "padding:10px 20px;font-size:16px;"
                            attributes["onclick"] = "window.history.back();"
                            +"Back to form"
                        }
                    }
                }
            }
        }
    }
}


private fun formatAddress(params: Parameters): String {
    val addressLine1 = params["addressLine1"] ?: throw Exception("Address line 1 is required")
    val town = params["town"] ?: throw Exception("Town is required")
    val city = params["city"] ?: throw Exception("City is required")
    val postcode = params["postcode"] ?: throw Exception("Postcode is required")

    val lines = mutableListOf<String>()
    lines.add(splitAddress(addressLine1))
    lines.add("$town,")
    lines.add(city)
    lines.add(postcode)

    return lines.joinToString("\n")
}

fun splitAddress(input: String): String {
    val parts = input.split(',').map { it.trim() }.filter { it.isNotBlank() }

    val formattedParts = parts.map { if (it.endsWith(",")) it else "$it," }

    return formattedParts.joinToString("\n")
}
