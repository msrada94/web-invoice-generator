import com.alicefield.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import kotlin.test.assertEquals

@DisplayName("Project Tests")
class ProjectTest {

    @Test
    @DisplayName("Debe crear un proyecto con valores por defecto")
    fun testProjectCreationWithDefaults() {
        val project = Project(
            name = "AB1234",
            description = "Test Project",
            rawHours = 5.5f
        )

        assertEquals("AB1234", project.name)
        assertEquals("Test Project", project.description)
    }


    @ParameterizedTest
    @CsvSource(
        "0.00, 0.00",
        "0.0, 0.00",
        "0, 0.00",
        "0.1, 0.10",     // 0.15 redondea a 0.20
        "0.10, 0.10",     // 0.15 redondea a 0.20
    )
    @DisplayName("Debe redondear horas correctamente 2")
    fun testHoursRounding2(rawHours: Float, expectedRounded: Float) {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = rawHours
        )

        assertEquals("%.2f".format(expectedRounded), project.hours)
    }

    @ParameterizedTest
    @CsvSource(
        "0.01, 0.10",     // 0.01 redondea a 0.10
        "0.15, 0.20",     // 0.15 redondea a 0.20
        "0.22, 0.25",     // 0.22 redondea a 0.25
        "0.25, 0.25",     // 0.25 se mantiene
        "0.26, 0.30",     // 0.26 redondea a 0.30
        "0.50, 0.50",     // 0.50 se mantiene
        "0.51, 0.60",     // 0.51 redondea a 0.60
        "0.75, 0.75",     // 0.75 se mantiene
        "0.76, 0.80",     // 0.76 redondea a 0.80
        "0.89, 0.90",     // 0.89 redondea a 0.90
        "0.95, 1.00",     // 0.95 redondea a 1.00
        "1.0, 1.00",      // 1.0 se mantiene
        "1.1, 1.10",      // 1.1 redondea a 1.10
        "2.5, 2.50",      // 2.5 se mantiene
        "2.51, 2.60",     // 2.51 redondea a 2.60
        "5.99, 6.00"      // 5.99 redondea a 6.00
    )
    @DisplayName("Debe redondear horas correctamente")
    fun testHoursRounding(rawHours: Float, expectedRounded: Float) {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = rawHours
        )

        assertEquals("%.2f".format(expectedRounded), project.hours)
    }


    @Test
    @DisplayName("Debe formatear horas con dos decimales")
    fun testHoursFormatting() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 5.5f
        )

        assertEquals("5.50", project.hours)
    }

    @Test
    @DisplayName("Debe formatear horas como 1.00 cuando rawHours es 0.99")
    fun testHoursFormattingWith099() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 0.99f
        )

        assertEquals("1.00", project.hours)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 45, 45.00",    // 1.0 * 45 = 45.00
        "2.0, 45, 90.00",    // 2.0 * 45 = 90.00
        "10.5, 45, 472.50",  // 10.5 * 45 = 472.50
        "1.0, 60, 60.00",    // 1.0 * 60 = 60.00
        "8.0, 55, 440.00",   // 8.0 * 55 = 440.00
        "0.25, 40, 10.00"    // 0.25 * 40 = 10.00
    )
    @DisplayName("Debe calcular el total correctamente")
    fun testTotalCalculation(rawHours: Float, hourlyRate: Int, expectedTotal: String) {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = rawHours,
            hourlyRate = hourlyRate
        )

        assertEquals(BigDecimal(expectedTotal), project.total)
    }

    @Test
    @DisplayName("Debe formatear el total con símbolo de libra")
    fun testFormattedTotal() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 10.0f,
            hourlyRate = 45
        )

        assertEquals("£450.00", project.formattedTotal)
    }

    @ParameterizedTest
    @CsvSource(
        "45, £45.00",
        "60, £60.00",
        "55, £55.00",
        "100, £100.00"
    )
    @DisplayName("Debe formatear el hourly rate correctamente")
    fun testHourlyRateFormatted(hourlyRate: Int, expectedFormatted: String) {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 1.0f,
            hourlyRate = hourlyRate
        )

        assertEquals(expectedFormatted, project.hourlyRateFormatted)
    }

    @Test
    @DisplayName("Debe manejar cero horas")
    fun testZeroHours() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 0.0f,
            hourlyRate = 45
        )

        assertEquals("0.00", project.hours)
        assertEquals(BigDecimal("0.00"), project.total)
    }

    @Test
    @DisplayName("Debe manejar valores muy pequeños de horas")
    fun testVerySmallHours() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 0.01f,
            hourlyRate = 40
        )

        assertEquals("0.10", project.hours)
        assertEquals(BigDecimal("4.00"), project.total)
    }

    @Test
    @DisplayName("Debe manejar valores grandes de horas")
    fun testLargeHours() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 100.5f,
            hourlyRate = 50
        )

        assertEquals("100.50", project.hours)
        assertEquals(BigDecimal("5025.00"), project.total)
    }

    @ParameterizedTest
    @CsvSource(
        "0.1, 0.10, 45, 4.50",
        "0.5, 0.50, 45, 22.50",
        "0.75, 0.75, 45, 33.75",
        "1.1, 1.10, 45, 49.50",
        "1.5, 1.50, 45, 67.50",
        "2.0, 2.00, 45, 90.00"
    )
    @DisplayName("Debe calcular correctamente horas y totales en conjunto")
    fun testHoursAndTotalTogether(
        rawHours: Float,
        expectedHours: String,
        hourlyRate: Int,
        expectedTotal: String
    ) {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = rawHours,
            hourlyRate = hourlyRate
        )

        assertEquals(expectedHours, project.hours)
        assertEquals(BigDecimal(expectedTotal), project.total)
    }

    @Test
    @DisplayName("Debe usar RoundingMode.HALF_UP para el total")
    fun testTotalRoundingMode() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 1.0f,
            hourlyRate = 45
        )

        // 45.00 debe tener exactamente 2 decimales
        assertEquals(2, project.total.scale())
    }

    @Test
    @DisplayName("Debe formatear el total con exactamente 2 decimales")
    fun testFormattedTotalDecimals() {
        val project = Project(
            name = "TEST",
            description = "Test",
            rawHours = 3.33f,
            hourlyRate = 37
        )

        val formatted = project.formattedTotal
        val amountPart = formatted.substring(1) // Remover £

        assertEquals(2, amountPart.split(".")[1].length)
    }

    @Test
    @DisplayName("Debe crear múltiples proyectos sin interferencia")
    fun testMultipleProjectsIndependence() {
        val project1 = Project(
            name = "AB1234",
            description = "Project 1",
            rawHours = 5.0f,
            hourlyRate = 45
        )

        val Project = Project(
            name = "CD5678",
            description = "Project 2",
            rawHours = 10.0f,
            hourlyRate = 60
        )

        assertEquals("5.00", project1.hours)
        assertEquals("£225.00", project1.formattedTotal)

        assertEquals("10.00", Project.hours)
        assertEquals("£600.00", Project.formattedTotal)
    }
}