package com.alicefield

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants.BLACK
import com.itextpdf.kernel.colors.ColorConstants.BLUE
import com.itextpdf.kernel.colors.ColorConstants.GRAY
import com.itextpdf.kernel.colors.ColorConstants.WHITE
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.borders.Border.NO_BORDER
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.TextAlignment.RIGHT
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.TextAlignment.CENTER
import com.itextpdf.layout.properties.TextAlignment.JUSTIFIED
import com.itextpdf.layout.properties.TextAlignment.LEFT
import java.io.ByteArrayOutputStream
import java.math.BigDecimal


object InvoiceGenerator {

    fun generateInvoiceByteArray(invoiceData: InvoiceData): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf, PageSize.A4)

        addTitles(document)
        addContactInfo(document)
        addInvoiceInfo(document, invoiceData)
        addProjects(document, invoiceData.projects)
        addBalanceAndPayment(document, totalDueAmount = invoiceData.projects.sumOf { it.total })

        document.close()
        return outputStream.toByteArray()
    }

    // --- Aux functions ---
    private fun addTitles(document: Document) {
        document.add(getTitlesTable())
    }

    private fun addContactInfo(document: Document) {
        document.add(Paragraph(ALICE_INFO))
        addSeparator(document)
    }

    private fun addInvoiceInfo(document: Document, invoiceData: InvoiceData) {
        document.add(getInvoiceInfoTable(invoiceData.billingInfo, invoiceData.invoiceInfo))
        document.add(Paragraph("\n"))
        addSeparator(document)
    }

    private fun addProjects(document: Document, projects: List<Project>) {
        document.add(getProjectsTable(projects))
        addSeparator(document, repeat = 2, color = WHITE)
    }

    private fun addBalanceAndPayment(document: Document, totalDueAmount: BigDecimal) {
        document.add(getBalanceDueTable(totalDueAmount))
        document.add(getPaymentTable())
    }

    // --- Utils functions ---
    private fun addSeparator(document: Document, repeat: Int = 1, color: Color = BLACK) {
        repeat(repeat) {
            document.add(LineSeparator(SolidLine().apply { this.color = color }))
        }
    }

    private fun getPaymentTable(): Table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f))).apply {
        setWidth(UnitValue.createPercentValue(100f))
        addCell(Paragraph("Payment details:").setBold().setTextAlignment(LEFT))
        addCell(Paragraph("Invoice to be paid in GBP").setTextAlignment(RIGHT).setFontSize(10f))
        addCell(Paragraph("Bank: Natwest\nAccount number: 89801792\nSort code: 60-04-23"))
        children.forEach { cell -> (cell as Cell).setBorder(NO_BORDER) }
    }

    private fun getBalanceDueTable(totalDue: BigDecimal): Table =
        Table(UnitValue.createPercentArray(floatArrayOf(4f, 1f, 1f))).apply {
            setWidth(UnitValue.createPercentValue(100f)).setBold().setTextAlignment(RIGHT)
            addCell(Paragraph(""))
            addCell(Paragraph("Balance Due: "))
            addCell(Paragraph("£$totalDue").setBackgroundColor(GRAY))
            children.forEach { cell -> (cell as Cell).setBorder(NO_BORDER) }
        }

    private fun getProjectsTable(projects: List<Project>): Table =
        Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f, 1f, 1f, 1f))).apply {
            setWidth(UnitValue.createPercentValue(100f))
            addCell(Paragraph("Project").setBold())
            addCell(Paragraph("Description").setBold())
            addCell(Paragraph("Hours").setBold().setTextAlignment(CENTER))
            addCell(Paragraph("Hourly rate").setBold().setTextAlignment(CENTER))
            addCell(Paragraph("Total").setBold().setTextAlignment(RIGHT))
            projects.forEachIndexed { index, project ->
                val color = if (index % 2 == 0) SUPER_LIGHT_GRAY else WHITE
                addCell(projectCell(project.name, JUSTIFIED, color))
                addCell(projectCell(project.description, JUSTIFIED, color))
                addCell(projectCell(project.hours.toString(), CENTER, color))
                addCell(projectCell(project.hourlyRateFormatted, CENTER, color))
                addCell(projectCell(project.formattedTotal, RIGHT, color))
            }
            children.forEach { cell -> (cell as Cell).setBorder(NO_BORDER) }
        }

    private fun getInvoiceInfoTable(billingInfo: BillingInfo, invoiceInfo: InvoiceInfo): Table =
        Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f))).apply {
            setWidth(UnitValue.createPercentValue(100f))
            addCell(Paragraph("BILL TO").setBold().setFontColor(BLUE))
            addCell(Paragraph(""))
            addCell(getBillingInfo(billingInfo))
            addCell(Paragraph(getInvoiceInfo(invoiceInfo)).setTextAlignment(RIGHT))
            children.forEach { cell -> (cell as Cell).setBorder(NO_BORDER) }
        }

    private fun getTitlesTable(): Table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f))).apply {
        width = UnitValue.createPercentValue(100f)
        val aliceTitle = Paragraph("Alice Field\n").setFontSize(20f).setBold()
        val invoiceTitle = Paragraph("INVOICE").setBold().setFontSize(19f).setTextAlignment(RIGHT)
            .setFont(HELVETICA_BOLD)
        addCell(aliceTitle)
        addCell(invoiceTitle)
        children.forEach { cell -> (cell as Cell).setBorder(NO_BORDER) }
    }

    private fun projectCell(text: String, alignment: TextAlignment, backgroundColor: Color): Paragraph {
        return Paragraph(text)
            .setBackgroundColor(backgroundColor)
            .setFontSize(10f)
            .setTextAlignment(alignment)
            .setFontColor(lightTextColor)
    }

    private fun getInvoiceInfo(invoiceInfo: InvoiceInfo): String = "Invoice No: #${invoiceInfo.invoiceID}\n" +
            "Date range: ${invoiceInfo.startDateFormatted} to ${invoiceInfo.endDateFormatted}\n" +
            "Invoice Date: ${invoiceInfo.invoiceDateFormatted}\n" +
            "Due Date: ${invoiceInfo.dueDateFormatted}"

    private fun getBillingInfo(billingInfo: BillingInfo): String = "${billingInfo.contactName}\n" +
            "${billingInfo.clientCompanyName}\n" +
            "${billingInfo.address}\n" +
            "${billingInfo.email}\n" +
            "${billingInfo.phone}"

    val HELVETICA_BOLD: PdfFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
    val SUPER_LIGHT_GRAY = DeviceRgb(220, 220, 220)
    val lightTextColor = DeviceRgb(80, 80, 80)

    const val ALICE_INFO = "Senior Medical Editor\n101 Heron Rd, Northstowe, " +
            "Cambridge CB24 1AS\n alicefield91@gmail.com\n 07961533126\n\n"
}
