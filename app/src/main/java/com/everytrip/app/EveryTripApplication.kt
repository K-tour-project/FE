package com.everytrip.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class EveryTripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
