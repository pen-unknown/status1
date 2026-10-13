package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.StatusItem
import com.example.model.StatusSource
import java.io.File
import java.io.FileOutputStream

object DemoStatusProvider {

    private val demoItems = mutableListOf<StatusItem>()

    @Synchronized
    fun getDemoStatuses(context: Context): List<StatusItem> {
        if (demoItems.isNotEmpty()) {
            return demoItems.toList()
        }

        val cacheDir = File(context.cacheDir, "demo_statuses")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val samples = listOf(
            SampleDef("غروب زیبای ساحل", false, 0xFF0D47A1.toInt(), 0xFFFF6F00.toInt(), "🌊 غروب رؤیایی ساحل"),
            SampleDef("صبح پرانرژی", false, 0xFF1B5E20.toInt(), 0xFF81C784.toInt(), "☕ یک فنجان قهوه و آغاز روز"),
            SampleDef("طبیعت بهاری", false, 0xFF311B92.toInt(), 0xFF8E24AA.toInt(), "🌸 شکوفه‌های زیبای بهاری"),
            SampleDef("ویدیو کوتاه انگیزشی", true, 0xFFB71C1C.toInt(), 0xFFFF5252.toInt(), "🔥 هرگز تسلیم نشو! مسیر پیروزی"),
            SampleDef("معماری تاریخی", false, 0xFF004D40.toInt(), 0xFF00BFA5.toInt(), "🕌 شاهکار معماری ایرانی"),
            SampleDef("ویدیو طبیعت بارانی", true, 0xFF263238.toInt(), 0xFF607D8B.toInt(), "🌧️ آرامش صدای باران در جنگل")
        )

        samples.forEachIndexed { index, sample ->
            val fileName = "status_sample_${index + 1}.${if (sample.isVideo) "mp4" else "jpg"}"
            val file = File(cacheDir, fileName)

            if (!file.exists()) {
                val bitmap = createDemoBitmap(sample.title, sample.caption, sample.c1, sample.c2, sample.isVideo)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            }

            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            demoItems.add(
                StatusItem(
                    id = "demo_sample_${index + 1}",
                    uri = uri,
                    filePath = file.absolutePath,
                    name = fileName,
                    posterName = "",
                    posterPhone = "",
                    isVideo = sample.isVideo,
                    timestamp = System.currentTimeMillis() - (index * 3600_000L * 2),
                    sizeBytes = file.length().coerceAtLeast(180_000L),
                    source = StatusSource.DEMO,
                    isSaved = false
                )
            )
        }

        return demoItems.toList()
    }

    private fun createDemoBitmap(
        title: String,
        caption: String,
        colorStart: Int,
        colorEnd: Int,
        isVideo: Boolean
    ): Bitmap {
        val width = 720
        val height = 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gradient = RadialGradient(
            width / 2f, height / 2f, width.toFloat(),
            colorStart, colorEnd,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Card Overlay
        paint.shader = null
        paint.color = Color.parseColor("#33000000")
        canvas.drawRoundRect(60f, 250f, width - 60f, height - 250f, 40f, 40f, paint)

        // Text Paint
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER

        // Title
        paint.textSize = 52f
        paint.isFakeBoldText = true
        canvas.drawText(title, width / 2f, 480f, paint)

        // Caption
        paint.textSize = 34f
        paint.isFakeBoldText = false
        canvas.drawText(caption, width / 2f, 580f, paint)

        // Status Demo Badge
        paint.color = Color.parseColor("#CC25D366")
        canvas.drawRoundRect(width / 2f - 180f, 680f, width / 2f + 180f, 760f, 40f, 40f, paint)

        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.isFakeBoldText = true
        val badge = if (isVideo) "▶ استوری ویدیویی نمونه" else "📷 استوری تصویری نمونه"
        canvas.drawText(badge, width / 2f, 730f, paint)

        return bitmap
    }

    private data class SampleDef(
        val title: String,
        val isVideo: Boolean,
        val c1: Int,
        val c2: Int,
        val caption: String
    )
}
