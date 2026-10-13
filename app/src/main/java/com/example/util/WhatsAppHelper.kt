package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

data class WhatsAppAppInfo(
    val packageName: String,
    val nameFa: String,
    val nameEn: String
)

object WhatsAppHelper {

    val KNOWN_WHATSAPP_PACKAGES = listOf(
        WhatsAppAppInfo("com.whatsapp", "واتساپ معمولی", "WhatsApp"),
        WhatsAppAppInfo("com.whatsapp.w4b", "واتساپ بیزینس", "WhatsApp Business"),
        WhatsAppAppInfo("com.gbwhatsapp", "جی‌بی واتساپ", "GB WhatsApp"),
        WhatsAppAppInfo("com.whatsapp.plus", "واتساپ پلاس", "WhatsApp Plus"),
        WhatsAppAppInfo("com.yowhatsapp", "یو واتساپ", "YoWhatsApp"),
        WhatsAppAppInfo("com.fmwhatsapp", "اف‌ام واتساپ", "FM WhatsApp"),
        WhatsAppAppInfo("com.aero", "آئرو واتساپ", "Aero WhatsApp")
    )

    fun getInstalledWhatsAppApps(context: Context): List<WhatsAppAppInfo> {
        val pm = context.packageManager
        val installed = mutableListOf<WhatsAppAppInfo>()

        for (app in KNOWN_WHATSAPP_PACKAGES) {
            val isInstalled = try {
                pm.getLaunchIntentForPackage(app.packageName) != null ||
                        pm.getPackageInfo(app.packageName, 0) != null
            } catch (e: Exception) {
                false
            }
            if (isInstalled) {
                installed.add(app)
            }
        }
        return installed
    }

    fun openWhatsApp(context: Context, preferredPackage: String? = null): Boolean {
        val installed = getInstalledWhatsAppApps(context)

        // 1. If user has a preferred package and it is installed, launch it
        if (!preferredPackage.isNullOrEmpty()) {
            val target = installed.firstOrNull { it.packageName == preferredPackage }
            if (target != null) {
                return launchPackage(context, target.packageName)
            }
        }

        // 2. If any WhatsApp is installed, launch the first one
        if (installed.isNotEmpty()) {
            return launchPackage(context, installed.first().packageName)
        }

        // 3. Fallback: try default packages even if package query didn't catch them
        if (launchPackage(context, "com.whatsapp")) return true
        if (launchPackage(context, "com.whatsapp.w4b")) return true
        if (launchPackage(context, "com.gbwhatsapp")) return true

        return false
    }

    private fun launchPackage(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                val direct = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(direct)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun repostMedia(
        context: Context,
        uri: Uri,
        isVideo: Boolean,
        preferredPackage: String? = null
    ) {
        val installed = getInstalledWhatsAppApps(context)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (isVideo) "video/*" else "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri("Media", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // If specific package requested and available
        if (!preferredPackage.isNullOrEmpty() && installed.any { it.packageName == preferredPackage }) {
            shareIntent.setPackage(preferredPackage)
            try {
                context.startActivity(shareIntent)
                return
            } catch (_: Exception) {}
        }

        // If only 1 WhatsApp installed, send directly to it
        if (installed.size == 1) {
            shareIntent.setPackage(installed.first().packageName)
            try {
                context.startActivity(shareIntent)
                return
            } catch (_: Exception) {}
        }

        // Otherwise show chooser with title
        val chooser = Intent.createChooser(shareIntent, "ارسال به عنوان وضعیت در واتساپ").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Starts direct WhatsApp chat with proper URL construction (&text= instead of ?text=)
     * and clean digit stripping so WhatsApp never rejects the link.
     */
    fun startDirectChat(
        context: Context,
        rawPhone: String,
        message: String,
        preferredPackage: String? = null
    ): Boolean {
        // Strip everything except digits
        val cleanDigits = rawPhone.filter { it.isDigit() }
        if (cleanDigits.isEmpty()) {
            return false
        }

        val encodedMsg = if (message.isNotBlank()) {
            "&text=" + URLEncoder.encode(message.trim(), "UTF-8")
        } else ""

        // Correct URL: https://api.whatsapp.com/send?phone=...&text=...
        val apiUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanDigits$encodedMsg")

        val installed = getInstalledWhatsAppApps(context)

        // 1. Try launching with target package
        val targetPkg = when {
            !preferredPackage.isNullOrEmpty() && installed.any { it.packageName == preferredPackage } -> preferredPackage
            installed.isNotEmpty() -> installed.first().packageName
            else -> "com.whatsapp"
        }

        val directIntent = Intent(Intent.ACTION_VIEW, apiUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setPackage(targetPkg)
        }

        try {
            context.startActivity(directIntent)
            return true
        } catch (_: Exception) {
            // Target package failed, try standard ACTION_VIEW with apiUri without fixed package
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, apiUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                return true
            } catch (_: Exception) {
                // Secondary fallback: https://wa.me/
                try {
                    val waMeMsg = if (message.isNotBlank()) "?text=" + URLEncoder.encode(message.trim(), "UTF-8") else ""
                    val waMeUri = Uri.parse("https://wa.me/$cleanDigits$waMeMsg")
                    val browserIntent = Intent(Intent.ACTION_VIEW, waMeUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                    return true
                } catch (_: Exception) {
                    return false
                }
            }
        }
    }
}
