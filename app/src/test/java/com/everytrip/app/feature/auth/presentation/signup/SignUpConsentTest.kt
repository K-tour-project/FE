package com.everytrip.app.feature.auth.presentation.signup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpConsentTest {

    @Test
    fun `회원가입은 이용약관에만 동의하면 비활성화된다`() {
        assertFalse(canSubmitSignUp(termsAccepted = true, privacyAccepted = false))
    }

    @Test
    fun `회원가입은 개인정보 처리방침에만 동의하면 비활성화된다`() {
        assertFalse(canSubmitSignUp(termsAccepted = false, privacyAccepted = true))
    }

    @Test
    fun `회원가입은 모든 약관에 동의하지 않으면 비활성화된다`() {
        assertFalse(canSubmitSignUp(termsAccepted = false, privacyAccepted = false))
    }

    @Test
    fun `회원가입은 두 필수 약관에 모두 동의하면 활성화된다`() {
        assertTrue(canSubmitSignUp(termsAccepted = true, privacyAccepted = true))
    }
}
