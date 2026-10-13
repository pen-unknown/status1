package com.example.data

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.example.model.StatusItem
import com.example.model.StatusSource
import com.example.util.ContactHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

sealed class SaveStatusResult {
    data object Success : SaveStatusResult()
    data object AlreadyExists : SaveStatusResult()
    data class Error(val message: String) : SaveStatusResult()
}

class StatusRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("status_saver_prefs", Context.MODE_PRIVATE)

    private val savedDir: File by lazy {
        val dir = File(context.getExternalFilesDir(null), "SavedStatuses")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun isDemoEnabled(): Boolean {
        // Defaults to true on first run, but user toggle is strictly persisted
        return prefs.getBoolean("include_demo", true)
    }

    fun setDemoEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("include_demo", enabled).apply()
    }

    fun getSafTreeUri(): Uri? {
        val uriStr = prefs.getString("saf_tree_uri", null) ?: return null
        return try {
            Uri.parse(uriStr)
        } catch (e: Exception) {
            null
        }
    }

    fun saveSafTreeUri(uri: Uri) {
        prefs.edit().putString("saf_tree_uri", uri.toString()).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "fa") ?: "fa"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString("app_theme", "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("app_theme", mode).apply()
    }

    suspend fun loadAllStatuses(includeDemo: Boolean): Triple<List<StatusItem>, List<StatusItem>, List<StatusItem>> =
        withContext(Dispatchers.IO) {
            val whatsappList = mutableListOf<StatusItem>()
            val businessList = mutableListOf<StatusItem>()
            val savedList = mutableListOf<StatusItem>()

            // 1. Load saved statuses
            savedList.addAll(scanSavedStatuses())

            // 2. Scan WhatsApp standard statuses across all possible directories
            for (dir in getWhatsAppFolderCandidates()) {
                val found = scanFolder(dir, StatusSource.WHATSAPP)
                for (item in found) {
                    if (whatsappList.none { it.name == item.name }) {
                        whatsappList.add(item)
                    }
                }
            }

            // 3. Scan WhatsApp Business statuses across all possible directories
            for (dir in getWABusinessFolderCandidates()) {
                val found = scanFolder(dir, StatusSource.WA_BUSINESS)
                for (item in found) {
                    if (businessList.none { it.name == item.name }) {
                        businessList.add(item)
                    }
                }
            }

            // 4. Scan custom SAF folder if user selected one
            val treeUri = getSafTreeUri()
            if (treeUri != null) {
                val safItems = scanSafTree(treeUri)
                for (item in safItems) {
                    if (whatsappList.none { it.name == item.name } && businessList.none { it.name == item.name }) {
                        whatsappList.add(item)
                    }
                }
            }

            // 4.5. Retain previously cached statuses (so even if WhatsApp auto-deletes them after 24/48 hours, they remain visible, openable and savable!)
            val cacheFolder = File(context.filesDir, "cached_statuses")
            if (cacheFolder.exists()) {
                val cachedFiles = cacheFolder.listFiles() ?: emptyArray()
                for (file in cachedFiles) {
                    if (file.isFile && file.length() > 0L) {
                        val name = file.name
                        if (whatsappList.none { it.name == name } && businessList.none { it.name == name } && savedList.none { it.name == name }) {
                            val ext = file.extension.lowercase()
                            val isImage = ext in listOf("jpg", "jpeg", "png", "webp")
                            val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov")
                            if (isImage || isVideo) {
                                val uri = try {
                                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                } catch (_: Exception) {
                                    Uri.fromFile(file)
                                }
                                whatsappList.add(
                                    StatusItem(
                                        id = file.absolutePath,
                                        uri = uri,
                                        filePath = file.absolutePath,
                                        name = file.name,
                                        isVideo = isVideo,
                                        timestamp = file.lastModified(),
                                        sizeBytes = file.length(),
                                        source = StatusSource.WHATSAPP,
                                        isSaved = false
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Add Demo statuses ONLY IF explicitly enabled AND no real statuses exist
            val hasRealStatuses = whatsappList.isNotEmpty() || businessList.isNotEmpty()
            if (includeDemo && !hasRealStatuses) {
                val demos = DemoStatusProvider.getDemoStatuses(context)
                whatsappList.addAll(demos)
            }

            // Attach assigned contacts and mark if already saved
            fun enrichItem(item: StatusItem): StatusItem {
                val assigned = ContactHelper.getAssignedContact(context, item.name)
                    ?: ContactHelper.getAssignedContact(context, item.id)
                val withContact = if (assigned != null) {
                    item.copy(posterName = assigned.name, posterPhone = assigned.phone)
                } else item

                val saved = isStatusAlreadySaved(withContact, savedList)
                return withContact.copy(isSaved = saved)
            }

            val markedWhatsapp = whatsappList.map { enrichItem(it) }
            val markedBusiness = businessList.map { enrichItem(it) }

            Triple(markedWhatsapp, markedBusiness, savedList)
        }

    private fun cacheFileLocally(file: File, name: String) {
        try {
            val cacheFolder = File(context.filesDir, "cached_statuses").apply { if (!exists()) mkdirs() }
            val dest = File(cacheFolder, name)
            if (!dest.exists() || dest.length() == 0L) {
                file.copyTo(dest, overwrite = true)
            }
        } catch (_: Exception) {}
    }

    private fun cacheUriLocally(uri: Uri, name: String) {
        try {
            val cacheFolder = File(context.filesDir, "cached_statuses").apply { if (!exists()) mkdirs() }
            val dest = File(cacheFolder, name)
            if (!dest.exists() || dest.length() == 0L) {
                context.contentResolver.openInputStream(uri)?.use { inStream ->
                    FileOutputStream(dest).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun scanFolder(folder: File, source: StatusSource): List<StatusItem> {
        val list = mutableListOf<StatusItem>()
        if (!folder.exists() || !folder.isDirectory) return list

        val files = folder.listFiles() ?: return list
        for (file in files) {
            if (file.isFile && !file.name.startsWith(".nomedia")) {
                val ext = file.extension.lowercase()
                val isImage = ext in listOf("jpg", "jpeg", "png", "webp")
                val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov")

                if (isImage || isVideo) {
                    cacheFileLocally(file, file.name)
                    val uri = try {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    } catch (e: Exception) {
                        Uri.fromFile(file)
                    }

                    list.add(
                        StatusItem(
                            id = file.absolutePath,
                            uri = uri,
                            filePath = file.absolutePath,
                            name = file.name,
                            isVideo = isVideo,
                            timestamp = file.lastModified(),
                            sizeBytes = file.length(),
                            source = source,
                            isSaved = false
                        )
                    )
                }
            }
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun scanSafTree(treeUri: Uri): List<StatusItem> {
        val list = mutableListOf<StatusItem>()
        try {
            val root = DocumentFile.fromTreeUri(context, treeUri) ?: return list
            val files = root.listFiles()
            for (df in files) {
                if (df.isFile && df.name?.startsWith(".nomedia") != true) {
                    val name = df.name ?: ""
                    val ext = name.substringAfterLast('.', "").lowercase()
                    val mime = df.type?.lowercase() ?: ""
                    val isImage = ext in listOf("jpg", "jpeg", "png", "webp") || mime.startsWith("image/")
                    val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov") || mime.startsWith("video/")

                    if (isImage || isVideo) {
                        cacheUriLocally(df.uri, name)
                        list.add(
                            StatusItem(
                                id = df.uri.toString(),
                                uri = df.uri,
                                filePath = null,
                                name = name.ifEmpty { "Status_${System.currentTimeMillis()}" },
                                isVideo = isVideo,
                                timestamp = df.lastModified(),
                                sizeBytes = df.length(),
                                source = StatusSource.WHATSAPP,
                                isSaved = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun scanSavedStatuses(): List<StatusItem> {
        val list = mutableListOf<StatusItem>()

        // 1. Scan app local saved directory
        if (savedDir.exists()) {
            val files = savedDir.listFiles() ?: emptyArray()
            for (file in files) {
                if (file.isFile) {
                    val ext = file.extension.lowercase()
                    val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov")
                    val uri = try {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    } catch (e: Exception) {
                        Uri.fromFile(file)
                    }
                    list.add(
                        StatusItem(
                            id = "saved_${file.name}",
                            uri = uri,
                            filePath = file.absolutePath,
                            name = file.name,
                            isVideo = isVideo,
                            timestamp = file.lastModified(),
                            sizeBytes = file.length(),
                            source = StatusSource.SAVED,
                            isSaved = true
                        )
                    )
                }
            }
        }

        // 2. Scan public Pictures/StatusSaver folder
        try {
            val publicDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "StatusSaver"
            )
            if (publicDir.exists()) {
                publicDir.listFiles()?.forEach { file ->
                    if (file.isFile && list.none { it.name == file.name }) {
                        val ext = file.extension.lowercase()
                        val isVideo = ext in listOf("mp4", "3gp", "mkv", "mov")
                        val uri = try {
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                        } catch (e: Exception) {
                            Uri.fromFile(file)
                        }
                        list.add(
                            StatusItem(
                                id = "pub_${file.name}",
                                uri = uri,
                                filePath = file.absolutePath,
                                name = file.name,
                                isVideo = isVideo,
                                timestamp = file.lastModified(),
                                sizeBytes = file.length(),
                                source = StatusSource.SAVED,
                                isSaved = true
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        return list.sortedByDescending { it.timestamp }
    }

    fun getDeterministicSavedName(status: StatusItem): String {
        val cleanBase = status.name.substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "_")
            .ifEmpty { "media" }
            .take(30)
        val ext = if (status.isVideo && status.source != StatusSource.DEMO) "mp4" else if (status.isVideo) "mp4" else "jpg"
        val hash = (status.id.hashCode() and 0xFFFF).toString(16)
        return "Status_${cleanBase}_$hash.$ext"
    }

    fun isStatusAlreadySaved(status: StatusItem, currentSavedList: List<StatusItem>? = null): Boolean {
        if (status.isSaved && status.source == StatusSource.SAVED) return true
        val savedKeys = prefs.getStringSet("saved_status_keys", emptySet()) ?: emptySet()
        if (savedKeys.contains(status.id) || savedKeys.contains(status.name)) {
            return true
        }
        val targetFileName = getDeterministicSavedName(status)
        if (File(savedDir, targetFileName).exists()) {
            return true
        }
        val listToCheck = currentSavedList ?: scanSavedStatuses()
        val baseName = status.name.substringBeforeLast('.').trim()
        val isMatched = listToCheck.any {
            it.name == targetFileName ||
            it.name == status.name ||
            (baseName.isNotEmpty() && it.name.contains(baseName)) ||
            (status.sizeBytes > 0 && it.sizeBytes == status.sizeBytes && it.isVideo == status.isVideo)
        }
        return isMatched
    }

    private fun markStatusAsSaved(status: StatusItem) {
        val current = prefs.getStringSet("saved_status_keys", emptySet()) ?: emptySet()
        val savedKeys = HashSet(current)
        savedKeys.add(status.id)
        savedKeys.add(status.name)
        val targetFileName = getDeterministicSavedName(status)
        savedKeys.add(targetFileName)
        val baseName = status.name.substringBeforeLast('.').trim()
        if (baseName.isNotEmpty()) {
            savedKeys.add(baseName)
        }
        prefs.edit().putStringSet("saved_status_keys", savedKeys).apply()
    }

    private fun unmarkStatusAsSaved(status: StatusItem) {
        val current = prefs.getStringSet("saved_status_keys", emptySet()) ?: emptySet()
        val savedKeys = HashSet(current)
        savedKeys.remove(status.id)
        savedKeys.remove(status.name)
        val targetFileName = getDeterministicSavedName(status)
        savedKeys.remove(targetFileName)
        val baseName = status.name.substringBeforeLast('.')
        savedKeys.removeAll { it.contains(status.name) || (baseName.isNotEmpty() && it.contains(baseName)) }
        prefs.edit().putStringSet("saved_status_keys", savedKeys).apply()
    }

    fun assignContactToStatus(identifier: String, name: String, phone: String) {
        ContactHelper.saveAssignedContact(context, identifier, name, phone)
    }

    fun clearCache(): Long {
        val cacheFolder = File(context.filesDir, "cached_statuses")
        var freedBytes = 0L
        if (cacheFolder.exists()) {
            cacheFolder.listFiles()?.forEach { file ->
                freedBytes += file.length()
                file.delete()
            }
        }
        return freedBytes
    }

    suspend fun saveStatus(status: StatusItem): SaveStatusResult = withContext(Dispatchers.IO) {
        try {
            if (isStatusAlreadySaved(status)) {
                markStatusAsSaved(status)
                return@withContext SaveStatusResult.AlreadyExists
            }

            val targetFileName = getDeterministicSavedName(status)
            val appFile = File(savedDir, targetFileName)

            val cachedCopy = File(File(context.filesDir, "cached_statuses"), status.name)
            val input: InputStream? = if (status.filePath != null && File(status.filePath).exists()) {
                FileInputStream(File(status.filePath))
            } else if (cachedCopy.exists() && cachedCopy.length() > 0L) {
                FileInputStream(cachedCopy)
            } else {
                try {
                    context.contentResolver.openInputStream(status.uri)
                } catch (_: Exception) {
                    if (cachedCopy.exists()) FileInputStream(cachedCopy) else null
                }
            }

            if (input != null) {
                FileOutputStream(appFile).use { out ->
                    input.copyTo(out)
                }
            } else {
                return@withContext SaveStatusResult.Error("فایل موردنظر در دسترس نیست")
            }

            // Save to public Android gallery via MediaStore
            saveToMediaStore(appFile, targetFileName, status.isVideo)

            // Mark as saved persistently
            markStatusAsSaved(status)

            SaveStatusResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            SaveStatusResult.Error(e.localizedMessage ?: "خطا در ذخیره‌سازی")
        }
    }

    private fun saveToMediaStore(file: File, fileName: String, isVideo: Boolean) {
        if (!file.exists()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }

                val mime = if (isVideo) "video/mp4" else "image/jpeg"
                val dir = if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "$dir/StatusSaver")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(collection, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output: OutputStream ->
                        FileInputStream(file).use { input: InputStream ->
                            input.copyTo(output)
                        }
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(
                        if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
                    ),
                    "StatusSaver"
                )
                if (!publicDir.exists()) publicDir.mkdirs()
                val destFile = File(publicDir, fileName)
                FileInputStream(file).use { inStream ->
                    FileOutputStream(destFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf(if (isVideo) "video/mp4" else "image/jpeg"),
                    null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteSavedStatus(status: StatusItem): Boolean = withContext(Dispatchers.IO) {
        try {
            var deleted = false
            if (status.filePath != null) {
                val f = File(status.filePath)
                if (f.exists()) {
                    deleted = f.delete()
                }
            }
            val deterministicFile = File(savedDir, getDeterministicSavedName(status))
            if (deterministicFile.exists()) {
                deterministicFile.delete()
                deleted = true
            }
            unmarkStatusAsSaved(status)
            deleted
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAllSavedStatuses(): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            prefs.edit().remove("saved_status_keys").apply()
            if (savedDir.exists()) {
                savedDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.delete()) {
                        count++
                    }
                }
            }
            val publicDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "StatusSaver"
            )
            if (publicDir.exists()) {
                publicDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.delete()) {
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        count
    }

    fun shareStatus(callerContext: Context? = null, status: StatusItem) {
        val targetContext = callerContext ?: context
        try {
            val shareUri = getSharableUri(status)
            if (shareUri == null) {
                Toast.makeText(targetContext, "خطا در آماده‌سازی فایل جهت اشتراک‌گذاری", Toast.LENGTH_SHORT).show()
                return
            }
            val mimeType = if (status.isVideo && status.source != StatusSource.DEMO) "video/mp4" else "image/jpeg"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                clipData = ClipData.newRawUri("Status", shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری وضعیت در سایر برنامه‌ها")

            val activity = findActivity(targetContext)
            if (activity != null) {
                activity.startActivity(chooser)
            } else {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                targetContext.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                Toast.makeText(targetContext, "خطا در اشتراک‌گذاری: ${e.localizedMessage ?: "مشکل ناشناخته"}", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
        }
    }

    fun repostToWhatsApp(status: StatusItem) {
        val shareUri = getSharableUri(status) ?: return
        val preferredPkg = when (status.source) {
            StatusSource.WA_BUSINESS -> "com.whatsapp.w4b"
            StatusSource.WHATSAPP -> "com.whatsapp"
            else -> null
        }
        com.example.util.WhatsAppHelper.repostMedia(context, shareUri, status.isVideo, preferredPkg)
    }

    private fun findActivity(ctx: Context): Activity? {
        var current: Context? = ctx
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun getSharableUri(status: StatusItem): Uri? {
        return try {
            val ext = if (status.isVideo && status.source != StatusSource.DEMO) "mp4" else "jpg"
            val shareCacheDir = File(context.cacheDir, "shared_media").apply { if (!exists()) mkdirs() }
            val cacheFile = File(shareCacheDir, "share_media_${status.id.hashCode()}.$ext")

            // Copy file to cacheDir so FileProvider ALWAYS has guaranteed access
            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                var copied = false
                if (status.filePath != null) {
                    val file = File(status.filePath)
                    if (file.exists() && file.canRead()) {
                        try {
                            FileInputStream(file).use { inStream ->
                                FileOutputStream(cacheFile).use { outStream ->
                                    inStream.copyTo(outStream)
                                }
                            }
                            copied = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (!copied) {
                    context.contentResolver.openInputStream(status.uri)?.use { inStream ->
                        FileOutputStream(cacheFile).use { outStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback attempt
            if (status.filePath != null && File(status.filePath).exists()) {
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(status.filePath)
                    )
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun getWhatsAppFolderCandidates(): List<File> {
        val base = Environment.getExternalStorageDirectory()
        return listOf(
            File(base, "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File(base, "WhatsApp/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp/WhatsApp/media/.Statuses"),
            File(base, "Android/media/com.gbwhatsapp/GBWhatsApp/Media/.Statuses"),
            File(base, "GBWhatsApp/Media/.Statuses"),
            File("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File("/storage/emulated/0/WhatsApp/Media/.Statuses"),
            File("/storage/emulated/999/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File("/storage/emulated/999/WhatsApp/Media/.Statuses"),
            File("/storage/emulated/0/DualApp/WhatsApp/Media/.Statuses")
        )
    }

    private fun getWABusinessFolderCandidates(): List<File> {
        val base = Environment.getExternalStorageDirectory()
        return listOf(
            File(base, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
            File(base, "WhatsApp Business/Media/.Statuses"),
            File(base, "Android/media/com.whatsapp.w4b/WhatsApp Business/media/.Statuses"),
            File("/storage/emulated/0/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
            File("/storage/emulated/0/WhatsApp Business/Media/.Statuses")
        )
    }
}
