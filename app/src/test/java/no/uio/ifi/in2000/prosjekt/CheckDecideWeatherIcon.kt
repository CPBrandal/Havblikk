package no.uio.ifi.in2000.prosjekt

import junit.framework.TestCase.assertEquals
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.getIconDrawable
import org.junit.Test

class CheckDecideWeatherIcon {
    /* This tests checks if getIconDrawable function returns the correct drawable resource ID for any given weather condition icon string input  */
    @Test
    fun test_clearSkyDay_ReturnsCorrectDrawable() {
        val expected = R.drawable.clearskyd
        val actual = getIconDrawable("clearsky_day")
        assertEquals(expected, actual)
    }

    @Test
    fun test_clearsky_night_ReturnsCorrectDrawable() {
        val expected = R.drawable.clearskyn
        val actual = getIconDrawable("clearsky_night")
        assertEquals(expected, actual)
    }

    @Test
    fun test_fair_day_ReturnsCorrectDrawable() {
        val expected = R.drawable.lightcloudd
        val actual = getIconDrawable("fair_day")
        assertEquals(expected, actual)
    }

    @Test
    fun test_fair_night_ReturnsCorrectDrawable() {
        val expected = R.drawable.lightcloudn
        val actual = getIconDrawable("fair_night")
        assertEquals(expected, actual)
    }

    @Test
    fun test_partlycloudy_day_ReturnsCorrectDrawable() {
        val expected = R.drawable.partlycloudyd
        val actual = getIconDrawable("partlycloudy_day")
        assertEquals(expected, actual)
    }

    @Test
    fun test_partlycloudy_night_ReturnsCorrectDrawable() {
        val expected = R.drawable.partlycloudyn
        val actual = getIconDrawable("partlycloudy_night")
        assertEquals(expected, actual)
    }

    @Test
    fun test_cloudy_ReturnsCorrectDrawable() {
        val expected = R.drawable.cloudy
        val actual = getIconDrawable("cloudy")
        assertEquals(expected, actual)
    }

    @Test
    fun test_rainshowers_day_ReturnsCorrectDrawable() {
        val expected = R.drawable.rainshowersd
        val actual = getIconDrawable("rainshowers_day")
        assertEquals(expected, actual)
    }

    @Test
    fun test_rainshowers_night_ReturnsCorrectDrawable() {
        val expected = R.drawable.rainshowersn
        val actual = getIconDrawable("rainshowers_night")
        assertEquals(expected, actual)
    }
    @Test
    fun test_rainshowersandthunder_day_ReturnsCorrectDrawable() {
        val expected = R.drawable.rainshowersandthunderd
        val actual = getIconDrawable("rainshowersandthunder_day")
        assertEquals(expected, actual)
    }
    @Test
    fun test_rainshowersandthunder_night_ReturnsCorrectDrawable() {
        val expected = R.drawable.rainshowersandthundern
        val actual = getIconDrawable("rainshowersandthunder_night")
        assertEquals(expected, actual)
    }

}