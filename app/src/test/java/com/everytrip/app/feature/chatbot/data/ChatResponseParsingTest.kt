package com.everytrip.app.feature.chatbot.data

import com.everytrip.app.core.network.NetworkProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatResponseParsingTest {
    @Test
    fun `parses mixed case response fields`() {
        val json = """
            {
              "answer": "코스를 추천했어요.",
              "intent": "course_recommendation",
              "works": [{
                "id": 101,
                "title": "별빛 여행",
                "type": "DRAMA",
                "releaseDate": "2024-03-09",
                "genres": ["로맨스", "드라마"],
                "posterUrl": "https://example.com/work.jpg",
                "rating": 8.2,
                "filmingLocations": [{"placeId": "501", "name": "해변 산책로"}]
              }],
              "places": [{
                "place_id": "501",
                "place_name": "해변 산책로",
                "poster_url": "https://example.com/place.jpg",
                "lat": 35.16,
                "lng": 129.16
              }],
              "course": {
                "total_places": 1,
                "estimated_minutes": 30,
                "transport": "도보+대중교통",
                "works": ["별빛 여행"],
                "stops": [{
                  "order": 1,
                  "place_id": "501",
                  "place_name": "해변 산책로",
                  "poster_url": "https://example.com/stop.jpg",
                  "stay_minutes": 30,
                  "travel_minutes_from_prev": 0
                }]
              }
            }
        """.trimIndent()

        val response = NetworkProvider.gson.fromJson(json, ChatResponse::class.java)

        assertEquals("2024-03-09", response.works?.single()?.releaseDate)
        assertEquals("https://example.com/work.jpg", response.works?.single()?.posterUrl)
        assertEquals("501", response.works?.single()?.filmingLocations?.single()?.placeId)
        assertEquals("https://example.com/place.jpg", response.places?.single()?.posterUrl)
        assertEquals(30, response.course?.estimatedMinutes)
        assertEquals("501", response.course?.stops?.single()?.placeId)
    }

    @Test
    fun `accepts null and empty works as empty result payloads`() {
        val nullWorks = NetworkProvider.gson.fromJson(
            """{"answer":"검색 결과가 없습니다.","intent":"work_search","works":null}""",
            ChatResponse::class.java,
        )
        val emptyWorks = NetworkProvider.gson.fromJson(
            """{"answer":"검색 결과가 없습니다.","intent":"work_search","works":[]}""",
            ChatResponse::class.java,
        )

        assertNull(nullWorks.works)
        assertEquals(emptyList<ChatWork>(), emptyWorks.works)
    }
}
