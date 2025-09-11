package com.alicefield

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants.*
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border.NO_BORDER
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.element.Text
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

object InvoiceGenerator {

    const val ALICE_INFO = "101 Heron Rd, Northstowe, " +
            "Cambridge CB24 1AS\n alicefield91@gmail.com\n 07961533126\n\n"

    fun generateInvoiceByteArray(invoiceData: InvoiceData): ByteArray {
        val outputStream = ByteArrayOutputStream()

        PdfWriter(outputStream).use { writer ->
            PdfDocument(writer).use { pdf ->
                val helveticaBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

                Document(pdf, PageSize.A4).use { document ->
                    // Ejemplo: pasar la fuente a las funciones
                    addTitles(document, helveticaBold)
                    addContactInfo(document)
                    addInvoiceInfo(document, invoiceData, helveticaBold)
                    addProjects(document, invoiceData.projects, helveticaBold)
                    addBalanceAndPayment(document, invoiceData.projects.sumOf { it.total })
                }
            }
        }

        return outputStream.toByteArray()
    }

    fun generateInvoice(invoiceData: InvoiceData) {

        PdfWriter("${invoiceData.invoiceInfo.invoiceID}.pdf").use { writer ->
            PdfDocument(writer).use { pdf ->
                val helveticaBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

                Document(pdf, PageSize.A4).use { document ->
                    // Ejemplo: pasar la fuente a las funciones
                    addTitles(document, helveticaBold)
                    addContactInfo(document)
                    addInvoiceInfo(document, invoiceData, helveticaBold)
                    addProjects(document, invoiceData.projects, helveticaBold)
                    addBalanceAndPayment(document, invoiceData.projects.sumOf { it.total })
                }
            }
        }
    }


    // --- Aux functions ---
    private fun addTitles(document: Document, font: PdfFont) {
        document.add(getTitlesTable(font))
    }

    private fun addContactInfo(document: Document) {
        document.add(Paragraph(ALICE_INFO))
        addSeparator(document)
    }

    private fun addInvoiceInfo(document: Document, invoiceData: InvoiceData, helveticaBold: PdfFont) {
        document.add(getInvoiceInfoTable(invoiceData.billingInfo, invoiceData.invoiceInfo))
        addSeparator(document)
    }

    private fun addProjects(document: Document, projects: List<Project>, helveticaBold: PdfFont) {
        document.add(getProjectsTable(projects))
        addSeparator(document, repeat = 2, color = WHITE)
    }

    private fun addBalanceAndPayment(document: Document, totalDueAmount: BigDecimal) {
        document.add(getBalanceDueTable(totalDueAmount))
        document.add(getPaymentTable())
    }

    private fun addSeparator(document: Document, repeat: Int = 1, color: Color = BLACK) {
        repeat(repeat) {
            document.add(LineSeparator(SolidLine().apply { this.color = color }))
        }
    }

    // --- Table creators (nuevos objetos cada vez) ---
    private fun getTitlesTable(font: PdfFont): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
        table.setWidth(UnitValue.createPercentValue(100f))
        val aliceText = Text("Alice Field\n").setFontSize(20f).setFont(font).setBold()
        val jobTitle = Text("Senior Medical Editor").setFontSize(12f).setFont(font)
        val invoiceTitle = Paragraph("INVOICE").setFont(font).setBold().setFontSize(19f)
            .setTextAlignment(TextAlignment.RIGHT)

        table.addCell(Cell().add(Paragraph().add(aliceText).add(jobTitle)))
        table.addCell(Cell().add(invoiceTitle))
        table.children.forEach { (it as Cell).setBorder(NO_BORDER) }
        return table
    }

    private fun getInvoiceInfoTable(billingInfo: BillingInfo, invoiceInfo: InvoiceInfo): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
        table.setWidth(UnitValue.createPercentValue(100f))

        table.addCell(Cell().add(Paragraph("BILL TO").setBold().setFontColor(BLUE)))
        table.addCell(Cell())

        table.addCell(Cell().add(Paragraph(getBillingInfo(billingInfo))))
        table.addCell(Cell().add(Paragraph(getInvoiceInfo(invoiceInfo)).setTextAlignment(TextAlignment.RIGHT)))

        table.children.forEach { (it as Cell).setBorder(NO_BORDER) }
        return table
    }

    private fun getProjectsTable(projects: List<Project>): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f, 1f, 1f, 1f)))
        table.setWidth(UnitValue.createPercentValue(100f))

        // Headers
        table.addCell(Cell().add(Paragraph("Project").setBold()))
        table.addCell(Cell().add(Paragraph("Description").setBold()))
        table.addCell(Cell().add(Paragraph("Hours").setBold().setTextAlignment(TextAlignment.CENTER)))
        table.addCell(Cell().add(Paragraph("Hourly rate").setBold().setTextAlignment(TextAlignment.CENTER)))
        table.addCell(Cell().add(Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)))

        // Project rows
        projects.forEachIndexed { index, project ->
            val color = if (index % 2 == 0) DeviceRgb(220, 220, 220) else WHITE
            table.addCell(Cell().add(projectCell(project.name, TextAlignment.JUSTIFIED, color)))
            table.addCell(Cell().add(projectCell(project.description, TextAlignment.JUSTIFIED, color)))
            table.addCell(Cell().add(projectCell(project.hours.toString(), TextAlignment.CENTER, color)))
            table.addCell(Cell().add(projectCell(project.hourlyRateFormatted, TextAlignment.CENTER, color)))
            table.addCell(Cell().add(projectCell(project.formattedTotal, TextAlignment.RIGHT, color)))
        }

        table.children.forEach { (it as Cell).setBorder(NO_BORDER) }
        return table
    }

    private fun getBalanceDueTable(totalDue: BigDecimal): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(4f, 1f, 1f)))
        table.setWidth(UnitValue.createPercentValue(100f))
        table.setBold().setTextAlignment(TextAlignment.RIGHT)

        table.addCell(Cell()) // empty
        table.addCell(Cell().add(Paragraph("Balance Due: ")))
        table.addCell(Cell().add(Paragraph("£$totalDue").setBackgroundColor(GRAY)))

        table.children.forEach { (it as Cell).setBorder(NO_BORDER) }
        return table
    }

    private fun getPaymentTable(): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
        table.setWidth(UnitValue.createPercentValue(100f))

        table.addCell(Cell().add(Paragraph("Payment details:").setBold().setTextAlignment(TextAlignment.LEFT)))
        table.addCell(Cell().add(Paragraph("Invoice to be paid in GBP").setTextAlignment(TextAlignment.RIGHT).setFontSize(10f)))

        // Row spanning two columns
        table.addCell(Cell(1, 2).add(Paragraph("Bank: Natwest\nAccount number: 89801792\nSort code: 60-04-23")))

        table.children.forEach { (it as Cell).setBorder(NO_BORDER) }
        return table
    }

    private fun projectCell(text: String, alignment: TextAlignment, backgroundColor: Color): Paragraph {
        return Paragraph(text)
            .setBackgroundColor(backgroundColor)
            .setFontSize(10f)
            .setTextAlignment(alignment)
            .setFontColor(DeviceRgb(80, 80, 80))
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
}
