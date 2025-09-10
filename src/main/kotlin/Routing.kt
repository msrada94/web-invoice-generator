package com.alicefield

import com.alicefield.InvoiceGenerator.generateInvoiceByteArray
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static")

        get("/") {
            call.respondFile(File("src/main/resources/static/index.html"))
        }
        post("/submit") {
            val params = call.receiveParameters()

            val projects = mutableListOf<ProjectRecord>()
            var i = 0
            while (true) {
                val name = params["projects[$i].name"] ?: break
                val description = params["projects[$i].description"] ?: throw Exception("Description is required")
                val hours = params["projects[$i].hours"]?.toFloatOrNull() ?: throw Exception("Hours worked are required")
                projects.add(ProjectRecord(name, description, hours))
                i++
            }

            val invoice = InvoiceSummaryData(
                contactName = params["contactName"] ?: throw Exception("Contact name is required"),
                clientCompany = params["clientCompany"] ?: throw Exception("Client company is required"),
                address = params["address"] ?: throw Exception("Address is required"),
                phone = params["phone"] ?: throw Exception("Phone is required"),
                email = params["email"] ?: throw Exception("Email is required"),
                invoiceNumber = params["invoiceNumber"] ?.toIntOrNull() ?: throw Exception("Invoice number is required"),
                invoiceDate = params["invoiceDate"] ?: throw Exception("Invoice date is required"),
                dueDate = params["dueDate"] ?: throw Exception("Due date is required"),
                startDate = params["startDate"]?: throw Exception("Start date is required"),
                endDate = params["endDate"] ?: throw Exception("End date is required"),
                hourlyRate = params["hourlyRate"]?.toIntOrNull() ?: throw Exception("Hourly rate is required"),
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


