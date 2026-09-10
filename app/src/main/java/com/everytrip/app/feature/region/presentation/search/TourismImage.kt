package com.everytrip.app.feature.region.presentation.search

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.everytrip.app.core.network.NetworkProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

private val tourismImageCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

internal suspend fun loadTourismBitmap(url: String?): Bitmap? {
    if (url.isNullOrBlank()) return null
    tourismImageCache.get(url)?.let { return it }

    return withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "image/*")
                .build()
            NetworkProvider.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bytes = response.body.bytes()
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 1
                    while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 1024) {
                        inSampleSize *= 2
                    }
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.also {
                    tourismImageCache.put(url, it)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun TourismImage(url: String?, name: String, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(null, url) {
        value = null
        value = loadTourismBitmap(url)
    }
    val loaded = bitmap
    if (loaded != null) {
        Image(loaded.asImageBitmap(), contentDescription = name,
            modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(Color(0xFFE8F2FF)), contentAlignment = Alignment.Center) {
            Text("이미지 없음", color = Color(0xFF667085))
        }
    }
}
