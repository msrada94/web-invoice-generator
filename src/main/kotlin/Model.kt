package com.alicefield

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH
import kotlin.math.round

data class Project(
    val name: String,
    val description: String,
    private val rawHours: Float,
    private val hourlyRate: Int = 45
) {
    val hours: Float = (round(rawHours * 10) / 10.0).toFloat()

    val total: BigDecimal
        get() = BigDecimal(hours.toDouble() * hourlyRate).setScale(2, RoundingMode.HALF_UP)

    val formattedTotal: String
        get() = "£${total}"

    val hourlyRateFormatted: String
        get() = "£%.2f".format(hourlyRate.toDouble())
}

data class BillingInfo(
    val contactName: String,
    val clientCompanyName: String,
    val address: String,
    val phone: String,
    val email: String
)

data class InvoiceInfo(
    val invoiceID: String,
    val hourlyRate: Int,
    private val invoiceDate: LocalDate,
    private val dueDate: LocalDate,
    private val startDate: LocalDate,
    private val endDate: LocalDate
){
    private val formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", ENGLISH)

    val invoiceDateFormatted: String
        get() = invoiceDate.format(formatter)
    val dueDateFormatted: String
        get() = dueDate.format(formatter)
    val startDateFormatted: String
        get() = startDate.format(formatter)
    val endDateFormatted: String
        get() = endDate.format(formatter)
}

data class InvoiceData(
    val invoiceInfo: InvoiceInfo,
    val billingInfo: BillingInfo,
    val projects: List<Project>
)

data class ProjectRecord(
    val project: String,
    val description: String,
    val hours: Float
)

data class InvoiceSummaryData(
    val contactName: String,
    val clientCompany: String,
    val address: String,
    val phone: String,
    val email: String,
    val invoiceNumber: Int,
    val invoiceDate: String,
    val dueDate: String,
    val startDate: String,
    val endDate: String,
    val hourlyRate: Int,
    val projects: List<ProjectRecord>
)
