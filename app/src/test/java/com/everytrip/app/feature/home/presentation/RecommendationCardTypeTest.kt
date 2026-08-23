package com.everytrip.app.feature.home.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationCardTypeTest {
    @Test
    fun placeCardUsesSquareRatio() {
        assertEquals(1f, RecommendationCardType.PLACE.aspectRatio)
    }

    @Test
    fun workCardUsesPosterRatio() {
        assertEquals(2f / 3f, RecommendationCardType.WORK.aspectRatio)
    }
}
