package com.everytrip.app.feature.region.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentCategoryTest {
    @Test
    fun `api categories are converted only for display`() {
        assertEquals("영화", "MOVIE".toContentCategoryLabel())
        assertEquals("드라마", "DRAMA".toContentCategoryLabel())
        assertEquals("DOCUMENTARY", "DOCUMENTARY".toContentCategoryLabel())
    }
}
