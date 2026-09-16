package com.everytrip.app.feature.auth.data.local

internal object AuthStateValidator {
    private val deviceIdPattern = Regex("^[A-Za-z0-9_.:-]{8,128}$")

    fun isValidDeviceId(deviceId: String?): Boolean =
        deviceId != null && deviceIdPattern.matches(deviceId)

    fun hasConsistentTokenPair(accessToken: String?, refreshToken: String?): Boolean =
        (accessToken == null) == (refreshToken == null)
}
