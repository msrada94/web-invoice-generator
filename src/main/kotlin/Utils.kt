package com.alicefield

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH

fun InvoiceSummaryData.toInvoiceData(): InvoiceData {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", ENGLISH)

    val billingInfo = BillingInfo(
        contactName = contactName,
        clientCompanyName = clientCompany,
        address = address,
        phone = phone,
        email = email
    )

    val invoiceInfo = InvoiceInfo(
        invoiceID = formatInvoiceNumber(invoiceNumber),
        invoiceDate = LocalDate.parse(invoiceDate, formatter),
        dueDate = LocalDate.parse(dueDate, formatter),
        hourlyRate = hourlyRate,
        startDate = LocalDate.parse(startDate, formatter),
        endDate = LocalDate.parse(endDate, formatter)
    )

    val projects = projects.map {
        Project(
            name = it.project,
            description = it.description,
            rawHours = it.hours,
            hourlyRate = hourlyRate
        )
    }

    return InvoiceData(
        invoiceInfo = invoiceInfo,
        billingInfo = billingInfo,
        projects = projects
    )
}

private fun formatInvoiceNumber(n: Int): String {
    return "INV" + String.format("%02d", n)
}
