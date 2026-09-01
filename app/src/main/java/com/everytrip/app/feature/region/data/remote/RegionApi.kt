package com.everytrip.app.feature.region.data.remote

import com.everytrip.app.BuildConfig
import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionLocation
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

class RegionApi(
    private val baseUrl: String = BuildConfig.BACKEND_BASE_URL,
) {
    fun getSidos(): List<Sido> {
        val json = get(path = "/regions/sidos")
        return parseRegionOptions(json).map { option ->
            Sido(
                id = option.regionId,
                name = option.name,
                hasChildren = option.hasChildren,
            )
        }
    }

    fun getSigungus(sidoId: Int): List<RegionOption> {
        val json = get(path = "/regions/$sidoId/children")
        return parseRegionOptions(json)
    }

    fun getBoundary(regionId: Int): RegionBoundary {
        val json = get(path = "/regions/$regionId/boundary")
        return parseBoundary(json)
    }

    private fun get(path: String, query: Map<String, String> = emptyMap()): String {
        val normalizedBaseUrl = baseUrl.trimEnd('/')
        require(normalizedBaseUrl.isNotBlank()) {
            "BACKEND_BASE_URL is not configured."
        }

        val queryString = query.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        val url = URL(
            buildString {
                append(normalizedBaseUrl)
                append(path)
                if (queryString.isNotBlank()) {
                    append('?')
                    append(queryString)
                }
            },
        )

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (responseCode !in 200..299) {
                error("Region API request failed. code=$responseCode body=$body")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRegionOptions(json: String): List<RegionOption> {
        return extractArray(json).mapObjects { value ->
            require(value is JSONObject) {
                "Region option response item must be an object."
            }
            value.toRegionOption()
        }.filter { it.name.isNotBlank() }
    }

    private fun parseBoundary(json: String): RegionBoundary {
        val root = JSONObject(json)
        val target = root.optJSONObject("data") ?: root

        return RegionBoundary(
            regionId = target.optInt("region_id"),
            name = target.optString("name"),
            fullName = target.optString("full_name"),
            centroid = target.optJSONObject("centroid")?.toRegionLocation(),
        )
    }

    private fun extractArray(json: String): JSONArray {
        val trimmed = json.trim()
        if (trimmed.startsWith("[")) {
            return JSONArray(trimmed)
        }

        val root = JSONObject(trimmed)
        return root.optJSONArray("data")
            ?: root.optJSONArray("regions")
            ?: root.optJSONArray("items")
            ?: root.optJSONArray("sidos")
            ?: root.optJSONArray("sigungus")
            ?: JSONArray()
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun JSONObject.toRegionOption(): RegionOption {
        return RegionOption(
            regionId = optInt("region_id"),
            name = optString("name"),
            fullName = optString("full_name"),
            level = optString("level"),
            parentRegionId = optNullableInt("parent_region_id"),
            hasChildren = optBoolean("has_children", false),
            centroid = optJSONObject("centroid")?.toRegionLocation(),
        )
    }

    private fun JSONObject.toRegionLocation(): RegionLocation {
        val latitude = optNullableDouble("latitude")
            ?: optNullableDouble("lat")
            ?: error("Region location response does not contain latitude.")
        val longitude = optNullableDouble("longitude")
            ?: optNullableDouble("lng")
            ?: optNullableDouble("lon")
            ?: error("Region location response does not contain longitude.")

        return RegionLocation(latitude = latitude, longitude = longitude)
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (!has(name) || isNull(name)) {
            return null
        }
        return runCatching { getInt(name) }.getOrNull()
    }

    private fun JSONObject.optNullableDouble(name: String): Double? {
        if (!has(name) || isNull(name)) {
            return null
        }
        return runCatching { getDouble(name) }.getOrNull()
    }

    private inline fun <T> JSONArray.mapObjects(transform: (Any) -> T): List<T> {
        return List(length()) { index -> transform(get(index)) }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
    }
}
