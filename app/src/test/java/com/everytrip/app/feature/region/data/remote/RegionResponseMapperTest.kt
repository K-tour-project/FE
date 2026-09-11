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

    @Test
    fun `tourism page maps filming place ids`() {
        val json = JsonParser.parseString(
            """{
              "items": [{
                "content_id": "126121", "name": "촬영지", "thumbnail_url": null,
                "location": null, "sido_name": "서울", "sigungu_name": "종로구",
                "category": "촬영지", "place_ids": [321, 322]
              }],
              "total": 1, "page": 1, "has_next": false
            }""".trimIndent(),
        )

        val result = RegionResponseMapper.toTourismPage(json)

        assertEquals(listOf(321, 322), result.items.single().placeIds)
    }

    @Test
    fun `place detail maps nullable tour detail and navigation summaries`() {
        val result = RegionResponseMapper.toPlaceDetail(JsonParser.parseString(
            """{
              "place_id":123,"name":"촬영지","location":{"lat":37.1,"lng":127.1},
              "address":null,"road_address":"도로명","region":{},
              "contents":[{"product_id":10,"title":"작품","category":"drama","poster_url":null,"detail_path":"/contents/10"}],
              "detail":null,
              "related_places":[{"related_id":"r1","content_id":"654321","name":"관광지","sido_name":"서울","sigungu_name":null,"detail_path":"/tourism-places/654321"}]
            }""".trimIndent(),
        ))

        assertEquals(123, result.placeId)
        assertEquals(10, result.contents.single().productId)
        assertEquals("654321", result.relatedPlaces.single().contentId)
        assertEquals(null, result.detail)
    }

    @Test
    fun `content detail maps product fields without match similarity`() {
        val result = RegionResponseMapper.toContentDetail(JsonParser.parseString(
            """{
              "product_id":10,"title":"작품","overview":null,"is_overview_translated":true,
              "first_air_date":"2024-01-01","category":"drama","product_type":"Miniseries",
              "poster_url":null,"genres":"드라마|로맨스","networks":"tvN","episode_count":16,
              "rating":8.5,"popularity":12.3,"lead_actors":"배우1|배우2","place_count":5
            }""".trimIndent(),
        ))

        assertEquals(10, result.productId)
        assertEquals(8.5, result.rating ?: 0.0, 0.0)
        assertEquals(5, result.placeCount)
    }
}
