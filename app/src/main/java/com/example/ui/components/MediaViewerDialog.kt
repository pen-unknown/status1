package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.StatusItem
import com.example.model.StatusSource
import com.example.ui.theme.WhatsAppEmerald
import com.example.ui.theme.WhatsAppGreen
import com.example.util.VideoThumbnailHelper
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun MediaViewerDialog(
    status: StatusItem,
    isPersian: Boolean = true,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onRepost: () -> Unit,
    onMessageContact: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        val context = LocalContext.current
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 4f)
            if (scale > 1f) {
                offsetX += offsetChange.x
                offsetY += offsetChange.y
            } else {
                offsetX = 0f
                offsetY = 0f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("media_viewer_dialog")
        ) {
            // Media Content (Real Video Player with TextureView, or Zoomable Photo)
            if (status.isVideo) {
                VideoPlayerView(
                    status = status,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Image with Zoom & Pan
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(state = transformState),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = VideoThumbnailHelper.buildCoilRequest(context, status),
                        contentDescription = status.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }
            }

            // Top Overlay Bar (Back button, File name, Status time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isPersian) "بازگشت" else "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = status.getDisplayName(isPersian),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val sourceLabel = when (status.source) {
                                StatusSource.WHATSAPP -> if (isPersian) "واتساپ معمولی" else "WhatsApp"
                                StatusSource.WA_BUSINESS -> if (isPersian) "واتساپ بیزینس" else "WA Business"
                                StatusSource.SAVED -> if (isPersian) "دانلود شده" else "Saved"
                                StatusSource.DEMO -> if (isPersian) "نمونه آزمایشی" else "Demo"
                            }
                            Text(
                                text = sourceLabel,
                                color = WhatsAppGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = " • ${status.getTimeAgo(isPersian)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Top right Message Contact shortcut
                if (onMessageContact != null) {
                    FilledTonalButton(
                        onClick = onMessageContact,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WhatsAppEmerald.copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = if (isPersian) "پیام" else "Chat",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPersian) "ارسال پیام" else "Message",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Action Bar - Compact & Refined
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                color = Color.Black.copy(alpha = 0.78f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save / Download Button - prominent 44dp
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status.isSaved) WhatsAppGreen else WhatsAppEmerald
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .weight(1.2f)
                            .testTag("preview_download_button")
                    ) {
                        Icon(
                            imageVector = if (status.isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = if (isPersian) "دانلود" else "Download",
                            tint = if (status.isSaved) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (status.isSaved) (if (isPersian) "ذخیره شد" else "Saved") else (if (isPersian) "دانلود" else "Download"),
                            color = if (status.isSaved) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Share Button - prominent 44dp
                    FilledTonalButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (isPersian) "اشتراک‌گذاری" else "Share",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPersian) "اشتراک" else "Share",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Repost to WhatsApp - prominent 44dp
                    FilledTonalButton(
                        onClick = onRepost,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WhatsAppGreen.copy(alpha = 0.25f),
                            contentColor = WhatsAppGreen
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = if (isPersian) "ارسال در واتساپ" else "Repost",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPersian) "استوری" else "Repost",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Delete button if saved
                    if (onDelete != null && status.isSaved) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Red.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = if (isPersian) "حذف" else "Delete",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Robust Video Player View:
 * Uses TextureView + MediaPlayer for real video files to prevent black screen SurfaceView bugs in Dialogs.
 * Always renders the first video frame / poster thumbnail underneath to eliminate black shadows before playing!
 * Shows prominent play button when paused or waiting for playback.
 */
@Composable
fun VideoPlayerView(
    status: StatusItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDemo = status.source == StatusSource.DEMO

    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isRealPlayerActive by remember { mutableStateOf(!isDemo) }
    var hasRenderedFirstFrame by remember { mutableStateOf(false) }
    var realDurationMs by remember { mutableIntStateOf(15000) }
    var realPositionMs by remember { mutableIntStateOf(0) }

    // Simulated progress timer for demo or fallback (0 to 15000 ms)
    var simPositionMs by remember { mutableIntStateOf(0) }

    // Extract first frame / poster image for video
    val posterBitmap by produceState<Bitmap?>(initialValue = null, status) {
        value = VideoThumbnailHelper.getThumbnail(context, status)
    }

    // Coroutine ticker for real & simulated video progress
    LaunchedEffect(isPlaying, isRealPlayerActive) {
        while (isPlaying) {
            delay(100)
            if (isRealPlayerActive && mediaPlayerRef != null) {
                try {
                    val mp = mediaPlayerRef
                    if (mp != null && mp.isPlaying) {
                        realPositionMs = mp.currentPosition
                        val dur = mp.duration
                        if (dur > 0) realDurationMs = dur
                    }
                } catch (_: Exception) {}
            } else {
                simPositionMs = (simPositionMs + 100) % 15000
            }
        }
    }

    DisposableEffect(status) {
        onDispose {
            try {
                mediaPlayerRef?.stop()
                mediaPlayerRef?.release()
                mediaPlayerRef = null
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                // Tap to toggle play/pause
                isPlaying = !isPlaying
                mediaPlayerRef?.let { mp ->
                    try {
                        if (isPlaying) mp.start() else mp.pause()
                    } catch (_: Exception) {}
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Poster / First Frame Image (ALWAYS visible underneath to eliminate black screen!)
        if (posterBitmap != null) {
            Image(
                bitmap = posterBitmap!!.asImageBitmap(),
                contentDescription = status.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = VideoThumbnailHelper.buildCoilRequest(context, status),
                contentDescription = status.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Real Video Surface (TextureView)
        if (isRealPlayerActive) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                                val surface = Surface(st)
                                try {
                                    val mp = MediaPlayer().apply {
                                        setSurface(surface)
                                        isLooping = true
                                        val uri = if (status.filePath != null && File(status.filePath).exists()) {
                                            Uri.fromFile(File(status.filePath))
                                        } else {
                                            status.uri
                                        }
                                        setDataSource(ctx, uri)
                                        setOnPreparedListener { player ->
                                            realDurationMs = player.duration.coerceAtLeast(1000)
                                            if (isPlaying) player.start()
                                            mediaPlayerRef = player
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            // Fallback to simulated playback without staying black
                                            isRealPlayerActive = false
                                            true
                                        }
                                        prepareAsync()
                                    }
                                } catch (_: Exception) {
                                    isRealPlayerActive = false
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}
                            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                try {
                                    mediaPlayerRef?.stop()
                                    mediaPlayerRef?.release()
                                    mediaPlayerRef = null
                                } catch (_: Exception) {}
                                return true
                            }
                            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {
                                hasRenderedFirstFrame = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Simulated Audio Equalizer Bars (for demo videos)
        if (!isRealPlayerActive) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 48.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
                val h1 by infiniteTransition.animateFloat(
                    initialValue = 6f, targetValue = 16f,
                    animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
                )
                val h2 by infiniteTransition.animateFloat(
                    initialValue = 14f, targetValue = 5f,
                    animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
                )
                val h3 by infiniteTransition.animateFloat(
                    initialValue = 4f, targetValue = 18f,
                    animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
                )
                Box(modifier = Modifier.width(3.dp).height(h1.dp).background(WhatsAppGreen, RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.width(3.dp).height(h2.dp).background(WhatsAppGreen, RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.width(3.dp).height(h3.dp).background(WhatsAppGreen, RoundedCornerShape(1.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "پخش ویدیو", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 4. Center Play/Pause indicator overlay when paused
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "پخش",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // 5. Video Progress Bar & Sound Toggle at Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 4.dp, start = 14.dp, end = 14.dp)
        ) {
            val progressFraction = if (isRealPlayerActive) {
                (realPositionMs.toFloat() / realDurationMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            } else {
                (simPositionMs.toFloat() / 15000f).coerceIn(0f, 1f)
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = WhatsAppGreen,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRealPlayerActive) {
                        "${formatMs(realPositionMs)} / ${formatMs(realDurationMs)}"
                    } else {
                        "${formatMs(simPositionMs)} / 00:15"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                // Mute / Unmute Button
                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        mediaPlayerRef?.let { mp ->
                            try {
                                if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "بی‌صدا" else "صدا فعال",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
