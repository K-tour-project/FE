package com.everytrip.app.feature.region.data.remote

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionResponseMapperTest {
    @Test
    fun `wrapped region options are mapped to sidos`() {
        val json = JsonParser.parseString(
            """
            {
              "data": [
                {
                  "region_id": 1,
                  "name": "서울특별시",
                  "full_name": "서울특별시",
                  "level": "sido",
                  "parent_region_id": null,
                  "has_children": true
                }
              ]
            }
            """.trimIndent(),
        )

        val result = RegionResponseMapper.toSidos(json)

        assertEquals(1, result.size)
        assertEquals(1, result.single().id)
        assertEquals("서울특별시", result.single().name)
        assertTrue(result.single().hasChildren)
    }

    @Test
    fun `geo json polygon is mapped with longitude latitude order`() {
        val json = JsonParser.parseString(
            """
            {
              "region_id": 1,
              "name": "서울특별시",
              "full_name": "서울특별시",
              "centroid": { "latitude": 37.5, "longitude": 127.0 },
              "boundary": {
                "type": "Polygon",
                "coordinates": [
                  [[127.0, 37.5], [127.1, 37.5], [127.1, 37.6], [127.0, 37.5]]
                ]
              }
            }
            """.trimIndent(),
        )

        val result = RegionResponseMapper.toBoundary(json)

        assertEquals(37.5, result.centroid?.latitude ?: 0.0, 0.0)
        assertEquals(1, result.polygons.size)
        assertEquals(127.0, result.polygons.single().outerBoundary.first().longitude, 0.0)
        assertEquals(37.5, result.polygons.single().outerBoundary.first().latitude, 0.0)
    }

    @Test
    fun `tourism page maps nullable fields and default category`() {
        val json = JsonParser.parseString(
            """
            {
              "items": [
                {
                  "content_id": "100",
                  "name": "테스트 장소",
                  "thumbnail_url": null,
                  "location": { "lat": 37.5, "lng": 127.0 },
                  "sido_name": "서울",
                  "sigungu_name": null,
                  "category": ""
                }
              ],
              "total": 1,
              "page": 0,
              "has_next": false
            }
            """.trimIndent(),
        )

        val result = RegionResponseMapper.toTourismPage(json)

        assertEquals(1, result.total)
        assertEquals("관광지", result.items.single().category)
        assertEquals(127.0, result.items.single().location?.longitude ?: 0.0, 0.0)
    }
}
