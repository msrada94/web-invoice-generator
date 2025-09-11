import com.alicefield.BillingInfo
import com.alicefield.InvoiceData
import com.alicefield.InvoiceGenerator
import com.alicefield.InvoiceInfo
import com.alicefield.Project
import java.time.LocalDate

class InvoiceGeneratorTest {

    @org.junit.jupiter.api.Test
    fun generateInvoiceTest() {
        val invoiceData = InvoiceData(
            invoiceInfo = InvoiceInfo(
                invoiceID = "INV00001",
                invoiceDate = LocalDate.of(2023, 11, 11),
                dueDate = LocalDate.of(2023, 12, 12),
                hourlyRate = 45,
                startDate = LocalDate.of(2023, 11, 1),
                endDate = LocalDate.of(2023, 11, 30)
            ),
            billingInfo = BillingInfo(
                contactName = "Contact Name",
                clientCompanyName = "Client Company Name",
                address = "123 Main St, London, UK",
                phone = "123456789",
                email = "invoices@company.com"
            ),
            projects = listOf(
                Project(
                    name = "Project 1",
                    description = "Description 1",
                    rawHours = 10f,
                ),
                Project(
                    name = "Project 2",
                    description = "Description 2",
                    rawHours = 10f
                ),
                Project(
                    name = "Project 3",
                    description = "Description 3",
                    rawHours = 10f
                ),
                Project(
                    name = "Project 4",
                    description = "Description 4",
                    rawHours = 10f
                ),
                Project(
                    name = "Project 5",
                    description = "Description 5",
                    rawHours = 10f
                )
            )
        )

        val result = InvoiceGenerator.generateInvoice(invoiceData)
    }

}