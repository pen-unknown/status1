package com.example.ui

import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.StatusItem
import com.example.model.StatusSource
import com.example.ui.components.DirectChatSection
import com.example.ui.components.DownloadsGallerySection
import com.example.ui.components.GuideSection
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.SendMessageBottomSheet
import com.example.ui.components.StatusCard
import com.example.ui.components.StatusMenuBottomSheet
import com.example.ui.theme.WhatsAppEmerald
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSaverScreen(
    viewModel: StatusViewModel,
    onRequestPermission: () -> Unit,
    onStartTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isPersian = uiState.appLanguage == AppLanguage.PERSIAN

    // Folder Picker Launcher (Storage Access Framework)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.onSafFolderSelected(uri, context)
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    var showAllFilesBanner by remember { mutableStateOf(true) }
    val isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val hasAllFilesAccess = if (isAndroid11Plus) Environment.isExternalStorageManager() else true

    // Layout direction: RTL for Persian, LTR for English
    CompositionLocalProvider(LocalLayoutDirection provides if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = data.visuals.message,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            topBar = {
                StatusSaverTopBar(
                    isPersian = isPersian,
                    showMenu = uiState.showMenuDropdown,
                    onToggleMenu = { viewModel.setShowMenuBottomSheet(true) },
                    onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                    onOpenGuide = { viewModel.setShowGuideDialog(true) },
                    onStartTour = onStartTour,
                    onOpenWhatsApp = { viewModel.openWhatsApp(context, false) },
                    onOpenWABusiness = { viewModel.openWhatsApp(context, true) },
                    onPickFolder = { folderPickerLauncher.launch(null) }
                )
            },
            floatingActionButton = {
                // Item 3: Refresh button placed at the bottom depending on language layout
                if (uiState.currentTab == AppTab.STORIES) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.loadStatuses() },
                        containerColor = WhatsAppEmerald,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        icon = {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isPersian) "تازه‌سازی" else "Refresh",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        text = {
                            Text(
                                text = if (isPersian) "تازه‌سازی" else "Refresh",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .testTag("bottom_refresh_fab")
                    )
                }
            },
            bottomBar = {
                // Multi-select batch actions bottom bar (if active)
                AnimatedVisibility(
                    visible = uiState.isMultiSelectMode,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val combinedList = (uiState.whatsappStatuses + uiState.businessStatuses).let { list ->
                        when (uiState.mediaFilter) {
                            MediaFilter.ALL -> list
                            MediaFilter.IMAGES -> list.filter { !it.isVideo }
                            MediaFilter.VIDEOS -> list.filter { it.isVideo }
                        }
                    }

                    MultiSelectBottomBar(
                        isPersian = isPersian,
                        selectedCount = uiState.selectedIds.size,
                        totalCount = combinedList.size,
                        onSelectAll = { viewModel.selectAll(combinedList) },
                        onSaveAll = { viewModel.saveSelectedStatuses(combinedList) },
                        onCancel = { viewModel.clearSelection() }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Sticky Notification Banner for All Files Access on Android 11+
                if (isAndroid11Plus && !hasAllFilesAccess && showAllFilesBanner) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("all_files_sticky_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = WhatsAppEmerald.copy(alpha = 0.12f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppEmerald.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(WhatsAppEmerald, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPersian) "دسترسی کامل برای خواندن استوری‌ها" else "Full storage access for statuses",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPersian)
                                        "در اندروید ۱۱+ برای شناسایی خودکار استوری‌ها نیاز به دسترسی مدیریت فایل‌هاست."
                                    else
                                        "On Android 11+, All Files Access is needed to auto-read WhatsApp statuses.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { viewModel.requestManageAllFiles(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("banner_enable_all_files_button")
                            ) {
                                Text(text = if (isPersian) "فعال‌سازی" else "Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { showAllFilesBanner = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Main Navigation Tabs: Stories / Downloads / Direct Chat
                StatusTabRow(
                    currentTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    isPersian = isPersian,
                    storiesCount = (uiState.whatsappStatuses + uiState.businessStatuses).size,
                    savedCount = uiState.savedStatuses.size
                )

                // Sub-filter for Stories tab: All / Photos / Videos (Items 8 & 9)
                if (uiState.currentTab == AppTab.STORIES) {
                    StoriesMediaFilterRow(
                        currentFilter = uiState.mediaFilter,
                        onFilterSelected = { viewModel.setMediaFilter(it) },
                        isPersian = isPersian,
                        allCount = (uiState.whatsappStatuses + uiState.businessStatuses).size,
                        photosCount = (uiState.whatsappStatuses + uiState.businessStatuses).count { !it.isVideo },
                        videosCount = (uiState.whatsappStatuses + uiState.businessStatuses).count { it.isVideo }
                    )
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (uiState.currentTab) {
                        AppTab.STORIES -> {
                            val combinedList = (uiState.whatsappStatuses + uiState.businessStatuses).let { list ->
                                when (uiState.mediaFilter) {
                                    MediaFilter.ALL -> list
                                    MediaFilter.IMAGES -> list.filter { !it.isVideo }
                                    MediaFilter.VIDEOS -> list.filter { it.isVideo }
                                }
                            }

                            StatusGrid(
                                items = combinedList,
                                selectedIds = uiState.selectedIds,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isPersian = isPersian,
                                onCardClick = { item ->
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleItemSelection(item.id)
                                    } else {
                                        viewModel.openPreview(item)
                                    }
                                },
                                onCardLongClick = { item ->
                                    viewModel.toggleItemSelection(item.id)
                                },
                                onSaveClick = { viewModel.saveSingleStatus(it) },
                                onShareClick = { viewModel.shareStatus(context, it) },
                                onMessageClick = { item ->
                                    viewModel.setShowContactMessageSheet(item)
                                },
                                emptyTitle = if (isPersian) "هیچ وضعیتی یافت نشد" else "No statuses found",
                                emptyDesc = if (isPersian)
                                    "برای مشاهده استوری‌ها، ابتدا آن‌ها را در برنامه واتساپ تماشا کنید، سپس به این برنامه بازگردید."
                                else
                                    "View statuses in WhatsApp first, then return here.",
                                onOpenWhatsApp = { viewModel.openWhatsApp(context) },
                                onPickFolder = { folderPickerLauncher.launch(null) },
                                onManageAllFiles = { viewModel.requestManageAllFiles(context) }
                            )
                        }

                        AppTab.SAVED -> {
                            DownloadsGallerySection(
                                savedItems = uiState.savedStatuses,
                                savedFilter = uiState.savedFilter,
                                isPersian = isPersian,
                                onFilterChange = { viewModel.setSavedFilter(it) },
                                onItemClick = { item -> viewModel.openPreview(item) },
                                onDeleteSingle = { item -> viewModel.deleteSavedStatus(item) },
                                onDeleteAll = { viewModel.deleteAllSavedStatuses() },
                                onShareItem = { item -> viewModel.shareStatus(context, item) },
                                onMessageContact = { item ->
                                    viewModel.setShowContactMessageSheet(item)
                                },
                                onGoToBrowse = { viewModel.selectTab(AppTab.STORIES) }
                            )
                        }

                        AppTab.DIRECT_CHAT -> {
                            DirectChatSection(
                                countryCode = uiState.countryCode,
                                phoneNumber = uiState.phoneNumber,
                                message = uiState.directMessage,
                                isPersian = isPersian,
                                onCountryCodeChange = { viewModel.onCountryCodeChanged(it) },
                                onPhoneNumberChange = { viewModel.onPhoneNumberChanged(it) },
                                onMessageChange = { viewModel.onDirectMessageChanged(it) },
                                onSendChat = { viewModel.sendDirectChat(context) }
                            )
                        }
                    }
                }
            }

            // Fullscreen In-App Media Preview Dialog
            uiState.activePreviewItem?.let { status ->
                MediaViewerDialog(
                    status = status,
                    isPersian = isPersian,
                    onDismiss = { viewModel.closePreview() },
                    onSave = { viewModel.saveSingleStatus(status) },
                    onShare = { viewModel.shareStatus(context, status) },
                    onRepost = { viewModel.repostStatus(status) },
                    onMessageContact = {
                        viewModel.setShowContactMessageSheet(status)
                    },
                    onDelete = if (status.isSaved) {
                        { viewModel.deleteSavedStatus(status) }
                    } else null
                )
            }

            // User-friendly Send Message & Assign Contact Bottom Sheet
            uiState.showContactMessageSheet?.let { status ->
                SendMessageBottomSheet(
                    status = status,
                    isPersian = isPersian,
                    onDismiss = { viewModel.setShowContactMessageSheet(null) },
                    onAssignContact = { name, phone ->
                        viewModel.assignContactToStatus(status, name, phone)
                    },
                    onOpenInDirectChat = { phone, name ->
                        viewModel.prepareDirectChatForContact(phone, name)
                        viewModel.setShowContactMessageSheet(null)
                    }
                )
            }

            // Gorgeous Material 3 Menu Bottom Sheet
            if (uiState.showMenuBottomSheet) {
                StatusMenuBottomSheet(
                    isPersian = isPersian,
                    currentLanguage = uiState.appLanguage,
                    currentTheme = uiState.themeMode,
                    isDemoEnabled = uiState.includeDemo,
                    storiesCount = (uiState.whatsappStatuses + uiState.businessStatuses).size,
                    savedCount = uiState.savedStatuses.size,
                    onDismiss = { viewModel.setShowMenuBottomSheet(false) },
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onToggleDemo = { viewModel.toggleDemoStatuses() },
                    onOpenWhatsApp = { viewModel.openWhatsApp(context, false) },
                    onOpenWABusiness = { viewModel.openWhatsApp(context, true) },
                    onPickFolder = { folderPickerLauncher.launch(null) },
                    onClearCache = { viewModel.clearCache() },
                    onOpenGuideDialog = { viewModel.setShowGuideDialog(true) }
                )
            }

            // Settings Dialog
            if (uiState.showSettingsDialog) {
                SettingsDialog(
                    currentLanguage = uiState.appLanguage,
                    currentTheme = uiState.themeMode,
                    isDemoEnabled = uiState.includeDemo,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onToggleDemo = { viewModel.toggleDemoStatuses() },
                    onDismiss = { viewModel.setShowSettingsDialog(false) }
                )
            }

            // Guide Dialog
            if (uiState.showGuideDialog) {
                GuideDialog(
                    isPersian = isPersian,
                    onStartTour = {
                        viewModel.setShowGuideDialog(false)
                        onStartTour()
                    },
                    onDismiss = { viewModel.setShowGuideDialog(false) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSaverTopBar(
    isPersian: Boolean,
    showMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGuide: () -> Unit,
    onStartTour: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onOpenWABusiness: () -> Unit,
    onPickFolder: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(WhatsAppGreen, WhatsAppEmerald)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isPersian) "ذخیره وضعیت" else "Status Saver",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isPersian) "دانلود و ذخیره وضعیت واتساپ" else "WhatsApp Stories Downloader",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = { onToggleMenu(true) },
                modifier = Modifier.testTag("top_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = if (isPersian) "منو و تنظیمات" else "Menu & Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StoriesMediaFilterRow(
    currentFilter: MediaFilter,
    onFilterSelected: (MediaFilter) -> Unit,
    isPersian: Boolean,
    allCount: Int,
    photosCount: Int,
    videosCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaFilter.entries.forEach { filter ->
            val isSelected = filter == currentFilter
            val label = if (isPersian) filter.titleFa else filter.titleEn
            val count = when (filter) {
                MediaFilter.ALL -> allCount
                MediaFilter.IMAGES -> photosCount
                MediaFilter.VIDEOS -> videosCount
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text("$label ($count)", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WhatsAppEmerald,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusTabRow(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    isPersian: Boolean,
    storiesCount: Int,
    savedCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScrollableTabRow(
            selectedTabIndex = currentTab.index,
            containerColor = Color.Transparent,
            contentColor = WhatsAppEmerald,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (currentTab.index < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.index]),
                        height = 3.dp,
                        color = WhatsAppEmerald
                    )
                }
            },
            divider = {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            }
        ) {
            AppTab.entries.forEach { tab ->
                val count = when (tab) {
                    AppTab.STORIES -> storiesCount
                    AppTab.SAVED -> savedCount
                    else -> 0
                }
                val isSelected = currentTab == tab

                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when (tab) {
                                    AppTab.STORIES -> Icons.Outlined.PhotoLibrary
                                    AppTab.SAVED -> Icons.Default.Download
                                    AppTab.DIRECT_CHAT -> Icons.AutoMirrored.Outlined.Chat
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) WhatsAppEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersian) tab.titleFa else tab.titleEn,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) WhatsAppEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (count > 0 && tab != AppTab.DIRECT_CHAT) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) WhatsAppEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = count.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
fun StatusGrid(
    items: List<StatusItem>,
    selectedIds: Set<String>,
    isMultiSelectMode: Boolean,
    isPersian: Boolean,
    onCardClick: (StatusItem) -> Unit,
    onCardLongClick: (StatusItem) -> Unit,
    onSaveClick: (StatusItem) -> Unit,
    onShareClick: (StatusItem) -> Unit,
    onMessageClick: (StatusItem) -> Unit,
    onDeleteClick: ((StatusItem) -> Unit)? = null,
    emptyTitle: String,
    emptyDesc: String,
    onOpenWhatsApp: () -> Unit,
    onPickFolder: () -> Unit,
    onManageAllFiles: () -> Unit
) {
    if (items.isEmpty()) {
        EmptyStatusView(
            title = emptyTitle,
            desc = emptyDesc,
            isPersian = isPersian,
            onOpenWhatsApp = onOpenWhatsApp,
            onPickFolder = onPickFolder,
            onManageAllFiles = onManageAllFiles
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("status_grid")
        ) {
            items(items, key = { it.id }) { status ->
                StatusCard(
                    status = status,
                    isSelected = selectedIds.contains(status.id),
                    isMultiSelectMode = isMultiSelectMode,
                    isPersian = isPersian,
                    onItemClick = { onCardClick(status) },
                    onItemLongClick = { onCardLongClick(status) },
                    onSaveClick = { onSaveClick(status) },
                    onShareClick = { onShareClick(status) },
                    onMessageClick = { onMessageClick(status) },
                    onDeleteClick = onDeleteClick?.let { { it(status) } }
                )
            }
        }
    }
}

@Composable
fun EmptyStatusView(
    title: String,
    desc: String,
    isPersian: Boolean,
    onOpenWhatsApp: () -> Unit,
    onPickFolder: () -> Unit,
    onManageAllFiles: () -> Unit
) {
    val isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val hasAllFilesAccess = if (isAndroid11Plus) Environment.isExternalStorageManager() else true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(WhatsAppEmerald.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    tint = WhatsAppEmerald,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Main Action: Open WhatsApp
            Button(
                onClick = onOpenWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "مشاهده استوری‌ها در واتساپ" else "View Stories in WhatsApp",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Action: Select WhatsApp Status Folder directly
            OutlinedButton(
                onClick = onPickFolder,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "انتخاب پوشه استوری‌ها در فایل‌ها" else "Select Status Folder in Files",
                    fontSize = 12.sp
                )
            }

            // Android 11+ All Files Access Quick Fix
            if (isAndroid11Plus && !hasAllFilesAccess) {
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onManageAllFiles,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "اجازه دسترسی کامل فایل‌ها (اندروید ۱۱+)" else "Enable All Files Access (Android 11+)",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MultiSelectBottomBar(
    isPersian: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onSaveAll: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isPersian) "$selectedCount استوری انتخاب شده" else "$selectedCount items selected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isPersian) "از کل $totalCount مورد" else "out of $totalCount",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onSelectAll,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = if (isPersian) "انتخاب همه" else "Select All",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isPersian) "انتخاب همه" else "Select All", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSaveAll,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_selected_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = if (isPersian) "دانلود گروهی" else "Batch Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isPersian) "دانلود ($selectedCount)" else "Save ($selectedCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isPersian) "لغو" else "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    currentLanguage: AppLanguage,
    currentTheme: ThemeMode,
    isDemoEnabled: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onToggleDemo: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPersian = currentLanguage == AppLanguage.PERSIAN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = WhatsAppEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "تنظیمات برنامه" else "App Settings",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Language Selection
                Text(
                    text = if (isPersian) "🌐 زبان برنامه (Language):" else "🌐 App Language:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = currentLanguage == AppLanguage.PERSIAN,
                        onClick = { onLanguageChange(AppLanguage.PERSIAN) },
                        label = { Text("فارسی (Persian)", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsAppEmerald,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = currentLanguage == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                        label = { Text("English", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsAppEmerald,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Theme Selection
                Text(
                    text = if (isPersian) "🎨 تم برنامه (Theme):" else "🎨 App Theme:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = currentTheme == mode,
                            onClick = { onThemeChange(mode) },
                            label = { Text(if (isPersian) mode.titleFa else mode.titleEn, fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsAppEmerald,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Demo Stories Toggle
                Text(
                    text = if (isPersian) "استوری‌های نمونه و آزمایشی:" else "Demo Statuses:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                FilledTonalButton(
                    onClick = onToggleDemo,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isDemoEnabled)
                            (if (isPersian) "غیرفعال‌سازی نمونه‌ها" else "Hide Demo Statuses")
                        else
                            (if (isPersian) "نمایش استوری‌های نمونه" else "Show Demo Statuses"),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = if (isPersian) "تأیید و بستن" else "Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun GuideDialog(
    isPersian: Boolean,
    onStartTour: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = WhatsAppEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "راهنمای ذخیره استوری" else "How to Save Statuses",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = if (isPersian)
                        "۱. ابتدا وارد واتساپ شده و استوری یا وضعیت دوستان خود را کامل تماشا کنید.\n\n" +
                        "۲. سپس به برنامه ذخیره وضعیت برگردید؛ تمامی عکس‌ها و ویدیوها خودکار در صفحه استوری‌ها نمایش داده می‌شوند.\n\n" +
                        "۳. با لمس دکمه سبز دانلود، استوری برای همیشه در گالری گوشی شما ذخیره می‌شود (حتی بعد از ۲۴ ساعت حذف نخواهد شد!).\n\n" +
                        "۴. برای شروع چت مستقیم با هر شماره‌ای بدون ذخیره، از تب پیام مستقیم استفاده کنید."
                    else
                        "1. Open WhatsApp and view the status or story completely.\n\n" +
                        "2. Return to Status Saver; all photos and videos appear automatically in the Stories tab.\n\n" +
                        "3. Tap the green Download button to save permanently to your gallery (never lost after 24h!).\n\n" +
                        "4. To message anyone without saving their number, use the Direct Chat tab.",
                    lineHeight = 22.sp,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = onStartTour,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPersian) "🎯 شروع تور آموزشی تعاملی" else "🎯 Start Interactive Tour",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = if (isPersian) "متوجه شدم" else "Got it", fontWeight = FontWeight.Bold)
            }
        }
    )
}
