package com.example

import com.example.obd.ObdProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testObdSpeedParsing() {
        // Standard OBD-II speed response for 010D: 41 0D 50 (0x50 = 80 km/h)
        val response = "41 0D 50\r\n>"
        val speed = ObdProtocol.parseSpeed(response)
        assertNotNull(speed)
        assertEquals(80f, speed!!, 0.01f)
    }

    @Test
    fun testObdRpmParsing() {
        // Standard OBD-II RPM response for 010C: 41 0C 1F 40 ((0x1F*256 + 0x40)/4 = 8000/4 = 2000 RPM)
        val response = "41 0C 1F 40\r\n>"
        val rpm = ObdProtocol.parseRpm(response)
        assertNotNull(rpm)
        assertEquals(2000, rpm)
    }

    @Test
    fun testObdCoolantTempParsing() {
        // Standard OBD-II Coolant response for 0105: 41 05 82 (0x82 = 130 - 40 = 90 °C)
        val response = "41 05 82\r\n>"
        val temp = ObdProtocol.parseCoolantTemp(response)
        assertNotNull(temp)
        assertEquals(90f, temp!!, 0.01f)
    }

    @Test
    fun testObdBatteryVoltageParsing() {
        // ELM327 ATRV response: "13.8V\r\n>"
        val atrvResponse = "13.8V\r\n>"
        val voltage = ObdProtocol.parseBatteryVoltage(atrvResponse)
        assertNotNull(voltage)
        assertEquals(13.8f, voltage!!, 0.01f)
    }
}
