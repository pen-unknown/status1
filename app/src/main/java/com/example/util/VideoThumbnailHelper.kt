package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.LruCache
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.example.model.StatusItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Helper to reliably extract and cache video thumbnails for WhatsApp statuses.
 * Supports real MP4 video files, SAF content URIs, cached copies, and demo statuses with LruCache.
 */
object VideoThumbnailHelper {
    private val thumbnailCache = LruCache<String, Bitmap>(80)

    fun getEffectiveFile(context: Context, status: StatusItem): File? {
        if (status.filePath != null) {
            val f = File(status.filePath)
            if (f.exists() && f.length() > 0) return f
        }
        val cached = File(File(context.filesDir, "cached_statuses"), status.name)
        if (cached.exists() && cached.length() > 0) return cached
        return null
    }

    suspend fun getThumbnail(context: Context, status: StatusItem): Bitmap? = withContext(Dispatchers.IO) {
        val effectiveFile = getEffectiveFile(context, status)
        val cacheKey = effectiveFile?.absolutePath ?: status.filePath ?: status.uri.toString()
        thumbnailCache.get(cacheKey)?.let { return@withContext it }

        // 1. Try decoding as direct image first if file exists (handles demo items, photos and fast-path)
        if (effectiveFile != null) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(effectiveFile.absolutePath, bounds)
                if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                    val decodeOpts = BitmapFactory.Options().apply {
                        inSampleSize = (bounds.outWidth / 400).coerceAtLeast(1)
                    }
                    val bm = BitmapFactory.decodeFile(effectiveFile.absolutePath, decodeOpts)
                    if (bm != null) {
                        thumbnailCache.put(cacheKey, bm)
                        return@withContext bm
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. If it's a video, use MediaMetadataRetriever to extract the first frame
        if (status.isVideo) {
            var retriever: MediaMetadataRetriever? = null
            try {
                retriever = MediaMetadataRetriever()
                if (effectiveFile != null) {
                    retriever.setDataSource(effectiveFile.absolutePath)
                } else {
                    retriever.setDataSource(context, status.uri)
                }

                // Extract frame at 0.5s or closest keyframe
                val frame = retriever.getFrameAtTime(500_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime

                if (frame != null) {
                    thumbnailCache.put(cacheKey, frame)
                    return@withContext frame
                }
            } catch (_: Exception) {
            } finally {
                try {
                    retriever?.release()
                } catch (_: Exception) {}
            }
        }

        null
    }

    fun buildCoilRequest(context: Context, status: StatusItem): ImageRequest {
        val effectiveFile = getEffectiveFile(context, status)
        val data: Any = effectiveFile ?: status.uri

        return ImageRequest.Builder(context)
            .data(data)
            .decoderFactory(VideoFrameDecoder.Factory())
            .crossfade(true)
            .build()
    }
}
