package com.everytrip.app

import android.app.Application
import com.everytrip.app.feature.auth.data.remote.AuthSessionManager
import com.kakao.sdk.common.KakaoSdk

class EveryTripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthSessionManager.get(this).initializeDeviceId()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
