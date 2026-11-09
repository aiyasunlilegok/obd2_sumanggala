package com.example.obd

/**
 * OBD-II Protocol standards & ELM327 AT commands.
 */
object ObdProtocol {
    // ELM327 initialization commands
    const val CMD_RESET = "ATZ"
    const val CMD_ECHO_OFF = "ATE0"
    const val CMD_LINEFEED_OFF = "ATL0"
    const val CMD_SPACES_OFF = "ATS0"
    const val CMD_HEADERS_OFF = "ATH0"
    const val CMD_AUTO_PROTOCOL = "ATSP0"
    const val CMD_READ_VOLTAGE = "ATRV"

    // Standard Mode 01 PIDs
    const val PID_ENGINE_LOAD = "0104"      // Calculated engine load (%)
    const val PID_COOLANT_TEMP = "0105"     // Engine coolant temperature (°C)
    const val PID_ENGINE_RPM = "010C"       // Engine speed RPM
    const val PID_VEHICLE_SPEED = "010D"    // Vehicle speed (km/h)
    const val PID_INTAKE_TEMP = "010F"      // Intake air temperature (°C)
    const val PID_THROTTLE_POS = "0111"     // Throttle position (%)
    const val PID_FUEL_LEVEL = "012F"       // Fuel tank level input (%)
    const val PID_CONTROL_MODULE_VOLT = "0142" // Control module voltage (Alt battery volt)

    /**
     * Parses the response from an ELM327 command.
     */
    fun parseRpm(response: String): Int? {
        val clean = cleanHexResponse(response, "410C") ?: return null
        if (clean.length < 4) return null
        return try {
            val a = clean.substring(0, 2).toInt(16)
            val b = clean.substring(2, 4).toInt(16)
            // Suzuki RPM Calculation
            ((a * 256) + b) / 4
        } catch (_: Exception) {
            null
        }
    }

    fun parseSpeed(response: String): Float? {
        val clean = cleanHexResponse(response, "410D") ?: return null
        if (clean.length < 2) return null
        return try {
            clean.substring(0, 2).toInt(16).toFloat()
        } catch (_: Exception) {
            null
        }
    }

    fun parseCoolantTemp(response: String): Float? {
        val clean = cleanHexResponse(response, "4105") ?: return null
        if (clean.length < 2) return null
        return try {
            // Direct raw value mapping for Suzuki
            (clean.substring(0, 2).toInt(16)).toFloat()
        } catch (_: Exception) {
            null
        }
    }

    fun parseThrottle(response: String): Float? {
        val clean = cleanHexResponse(response, "4111") ?: return null
        if (clean.length < 2) return null
        return try {
            (clean.substring(0, 2).toInt(16) * 100f) / 255f
        } catch (_: Exception) {
            null
        }
    }

    fun parseIntakeTemp(response: String): Float? {
        val clean = cleanHexResponse(response, "410F") ?: return null
        if (clean.length < 2) return null
        return try {
            (clean.substring(0, 2).toInt(16) - 40).toFloat()
        } catch (_: Exception) {
            null
        }
    }

    fun parseFuelLevel(response: String): Float? {
        val clean = cleanHexResponse(response, "412F") ?: return null
        if (clean.length < 2) return null
        return try {
            (clean.substring(0, 2).toInt(16) * 100f) / 255f
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parse ATRV response like "13.8V" or "12.4V"
     */
    fun parseBatteryVoltage(response: String): Float? {
        val sanitized = response.replace("\r", "").replace("\n", "").trim()
        val regex = Regex("""([0-9]+\.?[0-9]*)\s*V?""", RegexOption.IGNORE_CASE)
        val match = regex.find(sanitized)
        if (match != null) {
            return match.groupValues[1].toFloatOrNull()
        }
        // Fallback for Mode 01 PID 42 (Control module voltage: ((A*256)+B)/1000)
        val clean = cleanHexResponse(sanitized, "4142")
        if (clean != null && clean.length >= 4) {
            return try {
                val a = clean.substring(0, 2).toInt(16)
                val b = clean.substring(2, 4).toInt(16)
                ((a * 256) + b) / 1000f
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun cleanHexResponse(raw: String, expectedHeader: String): String? {
        val clean = raw.replace("\r", "")
            .replace("\n", "")
            .replace(" ", "")
            .replace(">", "")
            .uppercase()
        val index = clean.indexOf(expectedHeader)
        if (index >= 0) {
            return clean.substring(index + expectedHeader.length)
        }
        return null
    }
}
