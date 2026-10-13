package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R

object NotificationHelper {
    private const val CHANNEL_ID = "status_saver_permissions"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "اعلان‌های دسترسی"
            val descriptionText = "اعلان‌های راهنمای دسترسی و دانلود استوری"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAllFilesPermissionNotification(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        createNotificationChannel(context)

        val intent = try {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } catch (_: Exception) {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("نیاز به دسترسی کامل فایل‌ها")
            .setContentText("برای خواندن خودکار استوری‌های واتساپ در اندروید ۱۱+ لطفاً دسترسی را فعال کنید.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("استوری‌های واتساپ در پوشه سیستمی ذخیره می‌شوند. برای اینکه Status Saver بتواند استوری‌های دیده‌شده را به طور خودکار پیدا کند، لطفاً دسترسی «مدیریت همه فایل‌ها» را فعال نمایید.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS may not be granted yet; in-app banner covers this
        } catch (_: Exception) {
            // Safe fallback
        }
    }
}
