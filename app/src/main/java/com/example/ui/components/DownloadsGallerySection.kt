package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StatusItem
import com.example.ui.SavedFilter
import com.example.ui.theme.WhatsAppEmerald

@Composable
fun DownloadsGallerySection(
    savedItems: List<StatusItem>,
    savedFilter: SavedFilter,
    isPersian: Boolean,
    onFilterChange: (SavedFilter) -> Unit,
    onItemClick: (StatusItem) -> Unit,
    onDeleteSingle: (StatusItem) -> Unit,
    onDeleteAll: () -> Unit,
    onShareItem: (StatusItem) -> Unit,
    onMessageContact: ((StatusItem) -> Unit)? = null,
    onGoToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var itemPendingDelete by remember { mutableStateOf<StatusItem?>(null) }

    val filteredList = when (savedFilter) {
        SavedFilter.ALL -> savedItems
        SavedFilter.IMAGES -> savedItems.filter { !it.isVideo }
        SavedFilter.VIDEOS -> savedItems.filter { it.isVideo }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("downloads_gallery_section")
    ) {
        // Gallery Header Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = if (isPersian) "گالری استوری‌های دانلودشده" else "Downloads Gallery",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPersian)
                            "${savedItems.size} فایل برای همیشه در گالری ذخیره شده"
                        else
                            "${savedItems.size} items saved permanently in gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                if (savedItems.isNotEmpty()) {
                    Button(
                        onClick = { showDeleteAllDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = if (isPersian) "حذف همه" else "Clear All",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isPersian) "حذف همه" else "Clear All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filter Chips (All / Images / Videos)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SavedFilter.entries.forEach { filter ->
                val selected = filter == savedFilter
                val label = if (isPersian) filter.titleFa else filter.titleEn
                val count = when (filter) {
                    SavedFilter.ALL -> savedItems.size
                    SavedFilter.IMAGES -> savedItems.count { !it.isVideo }
                    SavedFilter.VIDEOS -> savedItems.count { it.isVideo }
                }

                FilterChip(
                    selected = selected,
                    onClick = { onFilterChange(filter) },
                    label = { Text("$label ($count)", fontSize = 11.5.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsAppEmerald,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Main Content: Grid or Empty State
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPersian) "هیچ فایلی در گالری ذخیره نشده" else "No saved items in gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPersian)
                            "استوری‌های دلخواه خود را ذخیره کنید تا برای همیشه در اینجا و گالری دستگاه باقی بمانند."
                        else
                            "Save your favorite statuses to keep them forever here and in your phone gallery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onGoToBrowse,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isPersian) "مشاهده استوری‌های واتساپ" else "Browse WhatsApp Stories",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    StatusCard(
                        status = item,
                        isSelected = false,
                        isMultiSelectMode = false,
                        isPersian = isPersian,
                        onItemClick = { onItemClick(item) },
                        onItemLongClick = {},
                        onSaveClick = {},
                        onShareClick = { onShareItem(item) },
                        onMessageClick = onMessageContact?.let { cb -> { cb(item) } },
                        onDeleteClick = { itemPendingDelete = item }
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Single Item Delete
    itemPendingDelete?.let { pendingItem ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isPersian) "حذف این فایل" else "Delete File", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (isPersian)
                        "آیا مطمئن هستید که می‌خواهید «${pendingItem.getDisplayName(isPersian)}» را از حافظه دانلودها حذف کنید؟"
                    else
                        "Are you sure you want to delete \"${pendingItem.getDisplayName(isPersian)}\" from downloads?",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = pendingItem
                        itemPendingDelete = null
                        onDeleteSingle(item)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isPersian) "حذف فایل" else "Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemPendingDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isPersian) "انصراف" else "Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Delete All Items
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isPersian) "حذف تمام استوری‌های دانلودشده" else "Delete All Downloads", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (isPersian)
                        "آیا مطمئن هستید که می‌خواهید تمام ${savedItems.size} مورد را از حافظه دانلودها حذف کنید؟ این عمل غیرقابل بازگشت است."
                    else
                        "Are you sure you want to delete all ${savedItems.size} items? This action cannot be undone.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllDialog = false
                        onDeleteAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isPersian) "حذف همه موارد" else "Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAllDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isPersian) "انصراف" else "Cancel")
                }
            }
        )
    }
}
