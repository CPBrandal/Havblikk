package no.uio.ifi.in2000.prosjekt

import junit.framework.TestCase.assertEquals
import no.uio.ifi.in2000.prosjekt.ui.map.formatToNorwegianTime
import org.junit.Test
class FormatNorwegianTime {
    @Test
    fun norwegianFormat(){
        /* Arrange */
        val time = "2024-06-10T13:00:00Z"
        /* Act */
        val outcome = formatToNorwegianTime(time)
        /* Assert */
        assertEquals("10.06.2024 kl. 15:00", outcome)
    }
}