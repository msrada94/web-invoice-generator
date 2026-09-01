package com.alicefield

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH

data class Project(
    val name: String,
    val description: String,
    private val rawHours: Float,
    private val hourlyRate: Int = 45
) {
    private val validDecimals = listOf(0.1f, 0.2f, 0.25f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.75f, 0.8f, 0.9f)

    private fun roundToValidDecimal(value: Float): Float {
        val tolerance = 0.0001f
        val integerPart = value.toInt()
        val decimalPart = value - integerPart

        if (decimalPart < tolerance) {
            return integerPart.toFloat()
        }

        // Encontrar el primer valor >= decimalPart
        val roundedDecimal = validDecimals.firstOrNull { it >= decimalPart - tolerance }
        return if (roundedDecimal != null) {
            integerPart + roundedDecimal
        } else {
            // Si no hay válido (ej: 0.95), redondea al siguiente entero
            (integerPart + 1).toFloat()
        }
    }

    private val roundedHours: Float = roundToValidDecimal(rawHours)

    val hours: String = String.format("%.2f", roundedHours)
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
) {
    private val formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", ENGLISH)

    init {
        require(startDate <= endDate) { "Start date must be before or equal to End date" }
        require(invoiceDate <= dueDate) { "Invoice date must be before or equal to Due date" }
    }

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
