package no.uio.ifi.in2000.prosjekt.ui.commonUIUtils

import android.util.Log
import java.time.ZoneId
import java.time.ZonedDateTime

/* Function to retrieve the current hour in norwegian time */
fun getCurrentHour(): Int {
    try {
        val norwegianTimeZone = ZoneId.of("Europe/Oslo")
        val currentDateTime = ZonedDateTime.now(norwegianTimeZone)
        val currentHour = currentDateTime.hour
        return currentHour
    } catch (e: Exception) {
        Log.e("JavaTime","Error retrieving current hour")
        return 0
    }
}