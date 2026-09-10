package com.everytrip.app.feature.region.data.remote

import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionLocation
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.RegionPolygon
import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.TourismDetail
import com.everytrip.app.feature.region.data.model.TourismPage
import com.everytrip.app.feature.region.data.model.TourismPlace
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal object RegionResponseMapper {
    fun toSidos(json: JsonElement): List<Sido> = parseRegionOptions(json).map { option ->
        Sido(
            id = option.regionId,
            name = option.name,
            hasChildren = option.hasChildren,
        )
    }

    fun toSigungus(json: JsonElement): List<RegionOption> = parseRegionOptions(json)

    fun toBoundary(json: JsonElement): RegionBoundary {
        val root = json.requireObject("Region boundary")
        val target = root.objectValue("data") ?: root

        return RegionBoundary(
            regionId = target.intValue("region_id") ?: 0,
            name = target.stringValue("name").orEmpty(),
            fullName = target.stringValue("full_name").orEmpty(),
            centroid = target.objectValue("centroid")?.toRegionLocation(),
            polygons = target.extractGeoJsonPolygons(),
        )
    }

    fun toTourismPage(json: JsonElement): TourismPage {
        val root = json.requireObject("Tourism page")
        val items = root.arrayValue("items")
            ?: error("Tourism page response does not contain items.")

        return TourismPage(
            items = items.map { element -> element.requireObject("Tourism place").toTourismPlace() },
            total = root.intValue("total")
                ?: error("Tourism page response does not contain total."),
            page = root.intValue("page")
                ?: error("Tourism page response does not contain page."),
            hasNext = root.booleanValue("has_next")
                ?: error("Tourism page response does not contain has_next."),
        )
    }

    fun toTourismDetail(json: JsonElement): TourismDetail {
        val root = json.requireObject("Tourism detail")
        return TourismDetail(
            contentId = root.requiredString("content_id"),
            name = root.requiredString("name"),
            overview = root.nonBlankString("overview"),
            homepage = root.nonBlankString("homepage"),
            tel = root.nonBlankString("tel"),
            address = root.nonBlankString("address"),
            addressDetail = root.nonBlankString("address_detail"),
            images = root.arrayValue("images")
                ?.mapNotNull { it.stringOrNull() }
                .orEmpty(),
        )
    }

    private fun parseRegionOptions(json: JsonElement): List<RegionOption> {
        val array = when {
            json.isJsonArray -> json.asJsonArray
            json.isJsonObject -> {
                val root = json.asJsonObject
                REGION_ARRAY_KEYS.firstNotNullOfOrNull { key -> root.arrayValue(key) } ?: JsonArray()
            }
            else -> JsonArray()
        }

        return array.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            RegionOption(
                regionId = item.intValue("region_id") ?: 0,
                name = item.stringValue("name").orEmpty(),
                fullName = item.stringValue("full_name").orEmpty(),
                level = item.stringValue("level").orEmpty(),
                parentRegionId = item.intValue("parent_region_id"),
                hasChildren = item.booleanValue("has_children") ?: false,
            )
        }.filter { it.name.isNotBlank() }
    }

    private fun JsonObject.toTourismPlace(): TourismPlace {
        return TourismPlace(
            contentId = requiredString("content_id"),
            name = requiredString("name"),
            thumbnailUrl = nonBlankString("thumbnail_url"),
            location = objectValue("location")?.toRegionLocation(),
            sidoName = nonBlankString("sido_name"),
            sigunguName = nonBlankString("sigungu_name"),
            category = nonBlankString("category") ?: "관광지",
        )
    }

    private fun JsonObject.extractGeoJsonPolygons(): List<RegionPolygon> {
        val geometry = when {
            stringValue("type") == "Feature" -> objectValue("geometry")
            objectValue("geometry") != null -> objectValue("geometry")
            objectValue("boundary") != null -> objectValue("boundary")
            has("coordinates") -> this
            else -> null
        }

        if (geometry != null) return geometry.toRegionPolygons()

        if (stringValue("type") == "FeatureCollection") {
            return arrayValue("features")
                ?.mapNotNull { it.takeIf(JsonElement::isJsonObject)?.asJsonObject }
                ?.flatMap { feature -> feature.objectValue("geometry")?.toRegionPolygons().orEmpty() }
                .orEmpty()
        }

        return emptyList()
    }

    private fun JsonObject.toRegionPolygons(): List<RegionPolygon> {
        return when (stringValue("type")) {
            "Polygon" -> listOfNotNull(arrayValue("coordinates")?.toRegionPolygon())
            "MultiPolygon" -> arrayValue("coordinates")
                ?.mapNotNull { it.takeIf(JsonElement::isJsonArray)?.asJsonArray?.toRegionPolygon() }
                .orEmpty()
            "GeometryCollection" -> arrayValue("geometries")
                ?.mapNotNull { it.takeIf(JsonElement::isJsonObject)?.asJsonObject }
                ?.flatMap { geometry -> geometry.toRegionPolygons() }
                .orEmpty()
            else -> emptyList()
        }
    }

    private fun JsonArray.toRegionPolygon(): RegionPolygon? {
        val rings = mapNotNull { element ->
            element.takeIf(JsonElement::isJsonArray)
                ?.asJsonArray
                ?.toRegionLocations()
                ?.takeIf { it.size >= MIN_POLYGON_POINT_COUNT }
        }
        val outerBoundary = rings.firstOrNull() ?: return null
        return RegionPolygon(
            outerBoundary = outerBoundary,
            holes = rings.drop(1),
        )
    }

    private fun JsonArray.toRegionLocations(): List<RegionLocation> = mapNotNull { element ->
        val coordinate = element.takeIf(JsonElement::isJsonArray)?.asJsonArray
            ?: return@mapNotNull null
        if (coordinate.size() < GEO_JSON_POSITION_MIN_SIZE) return@mapNotNull null

        val longitude = coordinate[0].doubleOrNull() ?: return@mapNotNull null
        val latitude = coordinate[1].doubleOrNull() ?: return@mapNotNull null
        RegionLocation(latitude = latitude, longitude = longitude)
    }

    private fun JsonObject.toRegionLocation(): RegionLocation {
        val latitude = doubleValue("latitude")
            ?: doubleValue("lat")
            ?: error("Region location response does not contain latitude.")
        val longitude = doubleValue("longitude")
            ?: doubleValue("lng")
            ?: doubleValue("lon")
            ?: error("Region location response does not contain longitude.")
        return RegionLocation(latitude = latitude, longitude = longitude)
    }

    private fun JsonElement.requireObject(label: String): JsonObject =
        takeIf { it.isJsonObject }?.asJsonObject ?: error("$label response must be an object.")

    private fun JsonObject.arrayValue(key: String): JsonArray? =
        get(key)?.takeIf { it.isJsonArray }?.asJsonArray

    private fun JsonObject.objectValue(key: String): JsonObject? =
        get(key)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.stringValue(key: String): String? = get(key)?.stringOrNull()

    private fun JsonObject.nonBlankString(key: String): String? =
        stringValue(key)?.takeIf(String::isNotBlank)

    private fun JsonObject.requiredString(key: String): String =
        stringValue(key) ?: error("Response does not contain $key.")

    private fun JsonObject.intValue(key: String): Int? = get(key)?.intOrNull()

    private fun JsonObject.doubleValue(key: String): Double? = get(key)?.doubleOrNull()

    private fun JsonObject.booleanValue(key: String): Boolean? =
        get(key)?.takeUnless(JsonElement::isJsonNull)?.let { runCatching { it.asBoolean }.getOrNull() }

    private fun JsonElement.stringOrNull(): String? =
        takeUnless(JsonElement::isJsonNull)?.let { runCatching { it.asString }.getOrNull() }

    private fun JsonElement.intOrNull(): Int? =
        takeUnless(JsonElement::isJsonNull)?.let { runCatching { it.asInt }.getOrNull() }

    private fun JsonElement.doubleOrNull(): Double? =
        takeUnless(JsonElement::isJsonNull)?.let { runCatching { it.asDouble }.getOrNull() }

    private val REGION_ARRAY_KEYS = listOf("data", "regions", "items", "sidos", "sigungus")
    private const val GEO_JSON_POSITION_MIN_SIZE = 2
    private const val MIN_POLYGON_POINT_COUNT = 3
}
