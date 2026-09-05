package com.everytrip.app.feature.auth.data.remote

class SessionExpiredException(
    cause: Throwable? = null,
) : Exception("Authentication session has expired.", cause)
