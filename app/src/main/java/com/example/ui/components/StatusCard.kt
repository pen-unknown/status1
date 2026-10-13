package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.StatusItem
import com.example.model.StatusSource
import com.example.ui.theme.WhatsAppEmerald
import com.example.ui.theme.WhatsAppGreen
import com.example.util.VideoThumbnailHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusCard(
    status: StatusItem,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    isPersian: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onMessageClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Extract video thumbnail or fast bitmap
    val thumbnailBitmap by produceState<Bitmap?>(initialValue = null, status) {
        value = VideoThumbnailHelper.getThumbnail(context, status)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 3.dp else 0.5.dp,
                color = if (isSelected) WhatsAppGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            )
            .testTag("status_card_${status.id}"),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Media Image Thumbnail (Bitmap from retriever or Coil with VideoFrameDecoder)
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap!!.asImageBitmap(),
                    contentDescription = status.getDisplayName(isPersian),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = VideoThumbnailHelper.buildCoilRequest(context, status),
                    contentDescription = status.getDisplayName(isPersian),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top gradient for badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )

            // Center Play Icon Badge for Videos
            if (status.isVideo) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = if (isPersian) "پخش ویدیو" else "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom gradient for actions & title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                        )
                    )
            )

            // Top Row: Source Badge (WhatsApp / WA Business) & Media Type (Video / Photo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source Badge (WhatsApp vs WA Business vs Demo)
                val (badgeBg, badgeText) = when (status.source) {
                    StatusSource.WA_BUSINESS -> Color(0xFF00897B) to (if (isPersian) "بیزینس" else "WA Business")
                    StatusSource.DEMO -> Color(0xFF673AB7) to (if (isPersian) "نمونه" else "Demo")
                    StatusSource.SAVED -> WhatsAppGreen to (if (isPersian) "ذخیره‌شده" else "Saved")
                    StatusSource.WHATSAPP -> WhatsAppEmerald to (if (isPersian) "واتساپ" else "WhatsApp")
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg.copy(alpha = 0.9f),
                    contentColor = if (status.source == StatusSource.SAVED) Color.Black else Color.White
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }

                // Checkbox when in MultiSelect mode or IsSelected
                if (isMultiSelectMode || isSelected) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = if (isSelected) WhatsAppGreen else Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = if (isPersian) "انتخاب شده" else "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Media Type Pill (Video or Photo)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (status.isVideo) Icons.Default.PlayArrow else Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (status.isVideo) (if (isPersian) "ویدیو" else "Video") else (if (isPersian) "عکس" else "Photo"),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bottom Actions & Contact Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Contact Name & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = status.getDisplayName(isPersian),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = status.getTimeAgo(isPersian),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 8.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Action Buttons Row: Message Contact | Download | Share (Generous 38dp touch target)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Message contact button (38dp)
                    if (onMessageClick != null) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onMessageClick),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.28f),
                            contentColor = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = if (isPersian) "ارسال پیام به مخاطب" else "Message Contact",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Save / Download / Delete Button (38dp)
                    if (onDeleteClick != null && status.isSaved) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onDeleteClick),
                            shape = CircleShape,
                            color = Color(0xFFE53935),
                            contentColor = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = if (isPersian) "حذف" else "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onSaveClick),
                            shape = CircleShape,
                            color = if (status.isSaved) WhatsAppGreen else WhatsAppEmerald,
                            contentColor = if (status.isSaved) Color.Black else Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (status.isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = if (status.isSaved) (if (isPersian) "ذخیره شده" else "Saved") else (if (isPersian) "دانلود وضعیت" else "Download"),
                                    tint = if (status.isSaved) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Share button (38dp)
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onShareClick),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.28f),
                        contentColor = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = if (isPersian) "اشتراک‌گذاری" else "Share",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
