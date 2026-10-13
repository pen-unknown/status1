package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SaveStatusResult
import com.example.data.StatusRepository
import com.example.model.StatusItem
import com.example.model.StatusSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val titleFa: String, val titleEn: String, val index: Int) {
    STORIES("استوری‌ها", "Stories", 0),
    SAVED("دانلودها", "Downloads", 1),
    DIRECT_CHAT("پیام مستقیم", "Direct Chat", 2)
}

enum class MediaFilter(val titleFa: String, val titleEn: String) {
    ALL("همه", "All"),
    IMAGES("عکس‌ها", "Photos"),
    VIDEOS("ویدیوها", "Videos")
}

enum class SavedFilter(val titleFa: String, val titleEn: String) {
    ALL("همه موارد", "All"),
    IMAGES("تصاویر", "Photos"),
    VIDEOS("ویدیوها", "Videos")
}

enum class AppLanguage(val code: String, val title: String) {
    PERSIAN("fa", "فارسی"),
    ENGLISH("en", "English")
}

enum class ThemeMode(val titleFa: String, val titleEn: String) {
    SYSTEM("پیروی از سیستم", "System"),
    LIGHT("روشن", "Light"),
    DARK("تاریک", "Dark")
}

data class StatusUiState(
    val currentTab: AppTab = AppTab.STORIES,
    val mediaFilter: MediaFilter = MediaFilter.ALL,
    val savedFilter: SavedFilter = SavedFilter.ALL,
    val sourceFilter: StatusSource? = null,
    val whatsappStatuses: List<StatusItem> = emptyList(),
    val businessStatuses: List<StatusItem> = emptyList(),
    val savedStatuses: List<StatusItem> = emptyList(),
    val isLoading: Boolean = false,
    val isMultiSelectMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val activePreviewItem: StatusItem? = null,
    val toastMessage: String? = null,
    val includeDemo: Boolean = true,
    val showPermissionRationaleDialog: Boolean = false,
    val showDeleteAllConfirmDialog: Boolean = false,
    val showMenuDropdown: Boolean = false,
    val showMenuBottomSheet: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showGuideDialog: Boolean = false,
    val showContactMessageSheet: StatusItem? = null,
    val appLanguage: AppLanguage = AppLanguage.PERSIAN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Direct Chat
    val countryCode: String = "+98",
    val phoneNumber: String = "",
    val directMessage: String = ""
)

class StatusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatusRepository(application)
    
    private val initialLang = if (repository.getLanguage() == "en") AppLanguage.ENGLISH else AppLanguage.PERSIAN
    private val initialTheme = try {
        ThemeMode.valueOf(repository.getThemeMode())
    } catch (_: Exception) {
        ThemeMode.SYSTEM
    }

    private val _uiState = MutableStateFlow(
        StatusUiState(
            includeDemo = repository.isDemoEnabled(),
            appLanguage = initialLang,
            themeMode = initialTheme
        )
    )
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        loadStatuses()
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab, isMultiSelectMode = false, selectedIds = emptySet()) }
    }

    fun setMediaFilter(filter: MediaFilter) {
        _uiState.update { it.copy(mediaFilter = filter) }
    }

    fun setSavedFilter(filter: SavedFilter) {
        _uiState.update { it.copy(savedFilter = filter) }
    }

    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language.code)
        _uiState.update { it.copy(appLanguage = language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode.name)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleMenuDropdown(show: Boolean? = null) {
        _uiState.update { it.copy(showMenuDropdown = show ?: !it.showMenuDropdown) }
    }

    fun setShowMenuBottomSheet(show: Boolean) {
        _uiState.update { it.copy(showMenuBottomSheet = show, showMenuDropdown = false) }
    }

    fun setShowContactMessageSheet(status: StatusItem?) {
        _uiState.update { it.copy(showContactMessageSheet = status) }
    }

    fun assignContactToStatus(status: StatusItem, name: String, phone: String) {
        viewModelScope.launch {
            repository.assignContactToStatus(status.name, name, phone)
            repository.assignContactToStatus(status.id, name, phone)
            val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
            val cleanPhone = com.example.util.ContactHelper.normalizePhoneNumber(phone)
            _uiState.update { state ->
                val updateItem = { item: StatusItem ->
                    if (item.id == status.id || item.name == status.name) {
                        item.copy(posterName = name.trim(), posterPhone = cleanPhone)
                    } else item
                }
                state.copy(
                    whatsappStatuses = state.whatsappStatuses.map(updateItem),
                    businessStatuses = state.businessStatuses.map(updateItem),
                    savedStatuses = state.savedStatuses.map(updateItem),
                    activePreviewItem = state.activePreviewItem?.let(updateItem),
                    showContactMessageSheet = null,
                    toastMessage = if (isFa) "مخاطب برای این استوری ثبت شد" else "Contact assigned to story"
                )
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            val freed = repository.clearCache()
            val mb = freed / (1024.0 * 1024.0)
            val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
            val msg = if (isFa) {
                String.format("حافظه موقت پاکسازی شد (%.1f مگابایت)", mb)
            } else {
                String.format("Cache cleared (%.1f MB)", mb)
            }
            _uiState.update { it.copy(toastMessage = msg) }
        }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show, showMenuDropdown = false, showMenuBottomSheet = false) }
    }

    fun setShowGuideDialog(show: Boolean) {
        _uiState.update { it.copy(showGuideDialog = show, showMenuDropdown = false, showMenuBottomSheet = false) }
    }

    fun setPermissionRationaleDialog(show: Boolean) {
        _uiState.update { it.copy(showPermissionRationaleDialog = show) }
    }

    fun setShowDeleteAllConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteAllConfirmDialog = show) }
    }

    fun toggleDemoStatuses() {
        val next = !_uiState.value.includeDemo
        repository.setDemoEnabled(next)
        _uiState.update { it.copy(includeDemo = next) }
        loadStatuses()
    }

    fun onSafFolderSelected(uri: Uri, context: Context) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            repository.saveSafTreeUri(uri)
            val msg = if (_uiState.value.appLanguage == AppLanguage.PERSIAN)
                "پوشه استوری‌ها متصل شد. در حال بارگذاری..."
            else
                "Statuses folder connected. Loading..."
            _uiState.update { it.copy(toastMessage = msg) }
            loadStatuses()
        } catch (e: Exception) {
            val err = if (_uiState.value.appLanguage == AppLanguage.PERSIAN)
                "خطا در برقراری دسترسی به پوشه"
            else
                "Error granting folder access"
            _uiState.update { it.copy(toastMessage = err) }
        }
    }

    fun requestManageAllFiles(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    val err = if (_uiState.value.appLanguage == AppLanguage.PERSIAN)
                        "امکان باز کردن صفحه تنظیمات وجود ندارد"
                    else
                        "Unable to open settings page"
                    _uiState.update { it.copy(toastMessage = err) }
                }
            }
        }
    }

    fun loadStatuses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isDemo = repository.isDemoEnabled()
            val (wa, business, saved) = repository.loadAllStatuses(includeDemo = isDemo)
            _uiState.update {
                it.copy(
                    includeDemo = isDemo,
                    whatsappStatuses = wa,
                    businessStatuses = business,
                    savedStatuses = saved,
                    isLoading = false
                )
            }
        }
    }

    fun openPreview(status: StatusItem) {
        _uiState.update { it.copy(activePreviewItem = status) }
    }

    fun closePreview() {
        _uiState.update { it.copy(activePreviewItem = null) }
    }

    fun toggleMultiSelectMode() {
        _uiState.update {
            val newMode = !it.isMultiSelectMode
            it.copy(
                isMultiSelectMode = newMode,
                selectedIds = if (!newMode) emptySet() else it.selectedIds
            )
        }
    }

    fun toggleItemSelection(id: String) {
        _uiState.update { state ->
            val set = state.selectedIds.toMutableSet()
            if (set.contains(id)) {
                set.remove(id)
            } else {
                set.add(id)
            }
            state.copy(
                selectedIds = set,
                isMultiSelectMode = set.isNotEmpty()
            )
        }
    }

    fun selectAll(items: List<StatusItem>) {
        _uiState.update {
            it.copy(
                isMultiSelectMode = true,
                selectedIds = items.map { item -> item.id }.toSet()
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                isMultiSelectMode = false,
                selectedIds = emptySet()
            )
        }
    }

    fun saveSingleStatus(status: StatusItem) {
        val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
        if (status.isSaved) {
            _uiState.update { it.copy(toastMessage = if (isFa) "این استوری قبلاً در گالری ذخیره شده است" else "Already saved in gallery") }
            return
        }
        viewModelScope.launch {
            when (val result = repository.saveStatus(status)) {
                is SaveStatusResult.Success -> {
                    _uiState.update {
                        it.copy(
                            toastMessage = if (isFa) "استوری با موفقیت در گالری ذخیره شد" else "Saved to gallery successfully",
                            whatsappStatuses = it.whatsappStatuses.map { item ->
                                if (item.id == status.id || item.name == status.name) item.copy(isSaved = true) else item
                            },
                            businessStatuses = it.businessStatuses.map { item ->
                                if (item.id == status.id || item.name == status.name) item.copy(isSaved = true) else item
                            },
                            activePreviewItem = if (it.activePreviewItem?.id == status.id) it.activePreviewItem.copy(isSaved = true) else it.activePreviewItem
                        )
                    }
                    loadStatuses()
                }
                is SaveStatusResult.AlreadyExists -> {
                    _uiState.update {
                        it.copy(
                            toastMessage = if (isFa) "این مورد قبلاً در گالری ذخیره شده است" else "Already saved in gallery",
                            whatsappStatuses = it.whatsappStatuses.map { item ->
                                if (item.id == status.id || item.name == status.name) item.copy(isSaved = true) else item
                            },
                            businessStatuses = it.businessStatuses.map { item ->
                                if (item.id == status.id || item.name == status.name) item.copy(isSaved = true) else item
                            },
                            activePreviewItem = if (it.activePreviewItem?.id == status.id) it.activePreviewItem.copy(isSaved = true) else it.activePreviewItem
                        )
                    }
                }
                is SaveStatusResult.Error -> {
                    val err = if (isFa) "خطا در ذخیره‌سازی: ${result.message}" else "Save error: ${result.message}"
                    _uiState.update { it.copy(toastMessage = err) }
                }
            }
        }
    }

    fun saveSelectedStatuses(allItems: List<StatusItem>) {
        viewModelScope.launch {
            val selected = allItems.filter { _uiState.value.selectedIds.contains(it.id) }
            if (selected.isEmpty()) return@launch

            var newlySaved = 0
            var alreadySaved = 0

            for (item in selected) {
                when (repository.saveStatus(item)) {
                    is SaveStatusResult.Success -> newlySaved++
                    is SaveStatusResult.AlreadyExists -> alreadySaved++
                    is SaveStatusResult.Error -> {}
                }
            }

            val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
            val message = if (isFa) {
                when {
                    newlySaved > 0 && alreadySaved > 0 ->
                        "$newlySaved مورد جدید ذخیره شد ($alreadySaved مورد قبلاً ذخیره شده بود)"
                    newlySaved > 0 ->
                        "$newlySaved استوری با موفقیت در گالری ذخیره شدند"
                    alreadySaved > 0 ->
                        "تمامی موارد انتخاب‌شده قبلاً در گالری ذخیره شده بودند"
                    else -> "خطا در ذخیره‌سازی موارد انتخاب‌شده"
                }
            } else {
                when {
                    newlySaved > 0 && alreadySaved > 0 ->
                        "$newlySaved saved ($alreadySaved previously saved)"
                    newlySaved > 0 ->
                        "$newlySaved statuses saved to gallery"
                    alreadySaved > 0 ->
                        "All selected items were already saved"
                    else -> "Error saving selected items"
                }
            }

            _uiState.update {
                it.copy(
                    toastMessage = message,
                    isMultiSelectMode = false,
                    selectedIds = emptySet()
                )
            }
            loadStatuses()
        }
    }

    fun shareStatus(context: Context? = null, status: StatusItem) {
        repository.shareStatus(context, status)
    }

    fun repostStatus(status: StatusItem) {
        repository.repostToWhatsApp(status)
    }

    fun deleteSavedStatus(status: StatusItem) {
        viewModelScope.launch {
            val deleted = repository.deleteSavedStatus(status)
            val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
            if (deleted) {
                if (_uiState.value.activePreviewItem?.id == status.id) {
                    closePreview()
                }
                _uiState.update {
                    it.copy(toastMessage = if (isFa) "استوری با موفقیت از دانلودها حذف شد" else "Deleted successfully")
                }
                loadStatuses()
            }
        }
    }

    fun deleteAllSavedStatuses() {
        viewModelScope.launch {
            val count = repository.deleteAllSavedStatuses()
            val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
            val msg = if (isFa) "$count فایل از گالری دانلودها حذف شدند" else "$count items deleted"
            _uiState.update {
                it.copy(
                    toastMessage = msg,
                    showDeleteAllConfirmDialog = false
                )
            }
            loadStatuses()
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // Direct Chat
    fun onCountryCodeChanged(code: String) {
        _uiState.update { it.copy(countryCode = code) }
    }

    fun onPhoneNumberChanged(number: String) {
        _uiState.update { it.copy(phoneNumber = number) }
    }

    fun onDirectMessageChanged(msg: String) {
        _uiState.update { it.copy(directMessage = msg) }
    }

    fun prepareDirectChatForContact(phone: String, contactName: String = "") {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        val (cc, local) = if (cleanPhone.startsWith("+98")) {
            "+98" to cleanPhone.removePrefix("+98")
        } else if (cleanPhone.startsWith("0098")) {
            "+98" to cleanPhone.removePrefix("0098")
        } else if (cleanPhone.startsWith("+")) {
            val prefix = cleanPhone.take(3)
            prefix to cleanPhone.drop(3)
        } else {
            "+98" to cleanPhone
        }

        val greeting = if (_uiState.value.appLanguage == AppLanguage.PERSIAN) {
            if (contactName.isNotBlank()) "سلام $contactName عزیز، وقت بخیر..." else "سلام، وقت بخیر..."
        } else {
            if (contactName.isNotBlank()) "Hi $contactName, hope you are well..." else "Hello..."
        }

        _uiState.update {
            it.copy(
                currentTab = AppTab.DIRECT_CHAT,
                countryCode = cc,
                phoneNumber = local,
                directMessage = greeting,
                activePreviewItem = null
            )
        }
    }

    fun sendDirectChat(context: Context) {
        val state = _uiState.value
        val isFa = state.appLanguage == AppLanguage.PERSIAN
        val rawNumber = state.phoneNumber.trim().replace(" ", "").replace("-", "")
        if (rawNumber.isEmpty()) {
            _uiState.update { it.copy(toastMessage = if (isFa) "لطفاً شماره تماس را وارد کنید" else "Please enter a phone number") }
            return
        }

        val cleanPhone = if (rawNumber.startsWith("+")) {
            rawNumber.removePrefix("+")
        } else if (rawNumber.startsWith("0")) {
            state.countryCode.removePrefix("+") + rawNumber.substring(1)
        } else {
            state.countryCode.removePrefix("+") + rawNumber
        }

        val success = com.example.util.WhatsAppHelper.startDirectChat(
            context,
            cleanPhone,
            state.directMessage,
            null
        )
        if (!success) {
            val err = if (isFa) "هیچ برنامه‌ای از واتساپ برای شروع چت یافت نشد" else "No WhatsApp application found"
            _uiState.update { it.copy(toastMessage = err) }
        }
    }

    fun openWhatsApp(context: Context, isBusiness: Boolean? = null) {
        val preferredPkg = when {
            isBusiness == true -> "com.whatsapp.w4b"
            isBusiness == false -> "com.whatsapp"
            else -> null
        }
        val opened = com.example.util.WhatsAppHelper.openWhatsApp(context, preferredPkg)
        if (!opened) {
            val isFa = _uiState.value.appLanguage == AppLanguage.PERSIAN
            val msg = if (isFa)
                "هیچ برنامه‌ای از واتساپ (معمولی، بیزینس، جی‌بی و ...) بر روی دستگاه یافت نشد!"
            else
                "No WhatsApp app found on this device!"
            _uiState.update { it.copy(toastMessage = msg) }
        }
    }
}
