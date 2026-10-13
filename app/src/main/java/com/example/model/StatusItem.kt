package com.example.model

import android.net.Uri

enum class StatusSource(val titleFa: String, val titleEn: String) {
    WHATSAPP("واتساپ", "WhatsApp"),
    WA_BUSINESS("واتساپ بیزینس", "WA Business"),
    SAVED("دانلودها", "Downloads"),
    DEMO("استوری نمونه", "Demo")
}

data class StatusItem(
    val id: String,
    val uri: Uri,
    val filePath: String? = null,
    val name: String,
    val posterName: String = "",
    val posterPhone: String = "",
    val isVideo: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L,
    val source: StatusSource = StatusSource.WHATSAPP,
    val isSaved: Boolean = false
) {
    fun getFormattedSize(isPersian: Boolean = true): String {
        if (sizeBytes <= 0) return ""
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            if (isPersian) String.format("%.1f مگابایت", mb) else String.format("%.1f MB", mb)
        } else {
            if (isPersian) String.format("%.0f کیلوبایت", kb) else String.format("%.0f KB", kb)
        }
    }

    fun getTimeAgo(isPersian: Boolean = true): String {
        val diff = System.currentTimeMillis() - timestamp
        val hours = diff / (1000 * 60 * 60)
        return if (isPersian) {
            when {
                hours < 1 -> "همین الان"
                hours == 1L -> "۱ ساعت پیش"
                hours in 2..24 -> "$hours ساعت پیش"
                else -> "دیروز"
            }
        } else {
            when {
                hours < 1 -> "Just now"
                hours == 1L -> "1h ago"
                hours in 2..24 -> "${hours}h ago"
                else -> "Yesterday"
            }
        }
    }

    fun getDisplayName(isPersian: Boolean = true): String {
        return when {
            posterName.isNotBlank() -> posterName
            source == StatusSource.DEMO -> if (isPersian) "استوری نمونه" else "Demo Story"
            source == StatusSource.SAVED -> if (isPersian) (if (isVideo) "ویدیو ذخیره‌شده" else "عکس ذخیره‌شده") else (if (isVideo) "Saved Video" else "Saved Photo")
            source == StatusSource.WA_BUSINESS -> if (isPersian) (if (isVideo) "ویدیو واتساپ بیزینس" else "عکس واتساپ بیزینس") else (if (isVideo) "Business Video" else "Business Photo")
            else -> if (isPersian) (if (isVideo) "ویدیو استوری واتساپ" else "عکس استوری واتساپ") else (if (isVideo) "WhatsApp Video" else "WhatsApp Photo")
        }
    }

    fun hasContact(): Boolean = posterPhone.isNotBlank() || posterName.isNotBlank()
}
