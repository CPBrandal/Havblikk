package no.uio.ifi.in2000.prosjekt

import junit.framework.TestCase
import no.uio.ifi.in2000.prosjekt.ui.map.uvToText
import org.junit.Test

class UVtest {
    @Test
    fun uvTestLow(){
        val uvString = "Lavt nivå"
        val uv = uvToText(1.0)
        TestCase.assertEquals(uvString, uv)
    }

    @Test
    fun uvTestModerat(){
        val uvString = "Moderat nivå"
        val uv = uvToText(3.0)
        TestCase.assertEquals(uvString, uv)
    }

    @Test
    fun uvTestHigh(){
        val uvString = "Høyt nivå"
        val uv = uvToText(6.0)
        TestCase.assertEquals(uvString, uv)
    }

    @Test
    fun uvTestVeryHigh(){
        val uvString = "Svært høyt nivå"
        val uv = uvToText(8.0)
        TestCase.assertEquals(uvString, uv)
    }

    @Test
    fun uvTestExtreme(){
        val uvString = "Ekstremt nivå"
        val uv = uvToText(100.0)
        TestCase.assertEquals(uvString, uv)
    }
}