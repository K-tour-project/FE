package com.everytrip.app.feature.auth.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthStateValidatorTest {
    @Test
    fun `generated Android device id satisfies backend format`() {
        val deviceId = "android_550e8400-e29b-41d4-a716-446655440000"

        assertTrue(AuthStateValidator.isValidDeviceId(deviceId))
    }

    @Test
    fun `device id rejects invalid length and characters`() {
        assertFalse(AuthStateValidator.isValidDeviceId("short"))
        assertFalse(AuthStateValidator.isValidDeviceId("android/device/id"))
        assertFalse(AuthStateValidator.isValidDeviceId("android device id"))
        assertFalse(AuthStateValidator.isValidDeviceId("a".repeat(129)))
    }

    @Test
    fun `access and refresh tokens must both exist or both be absent`() {
        assertTrue(AuthStateValidator.hasConsistentTokenPair(null, null))
        assertTrue(AuthStateValidator.hasConsistentTokenPair("access", "refresh"))
        assertFalse(AuthStateValidator.hasConsistentTokenPair("access", null))
        assertFalse(AuthStateValidator.hasConsistentTokenPair(null, "refresh"))
    }
}
