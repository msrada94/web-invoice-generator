package com.alicefield

import com.alicefield.InvoiceGenerator.generateInvoiceByteArray
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Text.Html
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.FormMethod
import kotlinx.html.body
import kotlinx.html.form
import kotlinx.html.passwordInput
import kotlinx.html.submitInput
import kotlinx.html.textInput

fun Application.configureRouting() {

    routing {
        staticResources("/static", "static")

        get("/") {
            call.respondHtml {
                body {
                    form(action = "/login", method = FormMethod.post) {
                        textInput { name = "username"; placeholder = "Usuario" }
                        passwordInput { name = "password"; placeholder = "Contraseña" }
                        submitInput { value = "Iniciar sesión" }
                    }
                }
            }
        }

        authenticate("formAuth") {
            post("/login") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("¡Hola ${principal.name}, has iniciado sesión correctamente!")
            }
        }

        authenticate("formAuth") {
            get("/form") {
                call.respondText(
                    this::class.java.classLoader.getResource("static/index.html")!!.readText(),
                    ContentType.Text.Html
                )
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

                val invoice = InvoiceSummaryData(
                    contactName = params["contactName"] ?: throw Exception("Contact name is required"),
                    clientCompany = params["clientCompany"] ?: throw Exception("Client company is required"),
                    address = params["address"] ?: throw Exception("Address is required"),
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
        }
    }
}
