package com.everytrip.app.feature.auth.data.remote

class AuthHttpException(
    val statusCode: Int,
    val detail: String,
) : Exception(detail)
