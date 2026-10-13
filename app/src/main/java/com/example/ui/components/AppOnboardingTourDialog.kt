package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.WhatsAppEmerald
import com.example.ui.theme.WhatsAppGreen

data class SpotlightStep(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val targetLabel: String,
    val icon: ImageVector,
    val targetType: TargetHighlightType
)

enum class TargetHighlightType {
    SOURCE_FILTER,
    APP_TABS,
    STATUS_CARD,
    MULTI_SELECT,
    OPEN_WHATSAPP
}

@Composable
fun AppOnboardingTourDialog(
    onDismissTour: () -> Unit
) {
    val steps = remember {
        listOf(
            SpotlightStep(
                stepIndex = 1,
                title = "انتخاب نسخه واتساپ",
                description = "از این بخش در بالای صفحه، به راحتی مشخص کنید استوری‌های کدام نسخه نمایش داده شوند: واتساپ معمولی، واتساپ بیزینس یا همه استوری‌ها به صورت یکپارچه.",
                targetLabel = "فیلتر نسخه‌های واتساپ",
                icon = Icons.Outlined.FilterList,
                targetType = TargetHighlightType.SOURCE_FILTER
            ),
            SpotlightStep(
                stepIndex = 2,
                title = "سربرگ‌های دسته‌بندی محتوا",
                description = "استوری‌ها به طور خودکار به تصاویر و ویدیوها تفکیک می‌شوند. همچنین سربرگ دانلودها برای فایل‌های ذخیره‌شده و پیام مستقیم برای ارسال چت بدون ذخیره شماره است.",
                targetLabel = "سربرگ‌های عکس، ویدیو، دانلود و چت",
                icon = Icons.Outlined.PhotoLibrary,
                targetType = TargetHighlightType.APP_TABS
            ),
            SpotlightStep(
                stepIndex = 3,
                title = "کارت استوری و دانلود سریع",
                description = "هر کارت پیش‌نمایش تصویر یا فریم اول ویدیو را نشان می‌دهد. با زدن دکمه سبز کوچک، استوری مستقیماً در حافظه و گالری ذخیره می‌شود و با لمس کارت، پخش تمام‌صفحه باز خواهد شد.",
                targetLabel = "کارت پیش‌نمایش و دانلود",
                icon = Icons.Default.Download,
                targetType = TargetHighlightType.STATUS_CARD
            ),
            SpotlightStep(
                stepIndex = 4,
                title = "انتخاب چندتایی و دانلود گروهی",
                description = "با لمس این دکمه در بالای صفحه، می‌توانید ده‌ها استوری را به صورت همزمان تیک بزنید و با یک کلیک همگی را با هم در گالری ذخیره کنید.",
                targetLabel = "دکمه انتخاب چندتایی (تیک)",
                icon = Icons.Outlined.Checklist,
                targetType = TargetHighlightType.MULTI_SELECT
            ),
            SpotlightStep(
                stepIndex = 5,
                title = "مشاهده استوری‌ها در واتساپ",
                description = "برای اینکه استوری‌ها در برنامه شناسایی شوند، باید ابتدا آن‌ها را در خود واتساپ تماشا کنید. با زدن این دکمه سریعاً واتساپ باز می‌شود تا استوری‌ها را ببینید.",
                targetLabel = "دکمه باز کردن سریع واتساپ",
                icon = Icons.Outlined.Visibility,
                targetType = TargetHighlightType.OPEN_WHATSAPP
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    // Pulsing border animation for highlighted target
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Dialog(
        onDismissRequest = onDismissTour,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            decorFitsSystemWindows = false
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .testTag("interactive_tour_overlay")
            ) {
                // Top Dismiss Bar (Quick Skip Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "راهنمای تصویری بخش‌های برنامه",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Skip Tour Button
                    TextButton(
                        onClick = onDismissTour,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.85f)),
                        modifier = Modifier.testTag("tour_skip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "رد کردن آموزش",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "رد کردن آموزش", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive Simulated Spotlight Highlight Box in center
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Target Preview Card with animated glowing outline
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 3.dp,
                                color = WhatsAppGreen.copy(alpha = pulseAlpha),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Spotlight Target Simulation Visual
                            SpotlightTargetMockVisual(targetType = currentStep.targetType)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Highlight badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WhatsAppEmerald.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(WhatsAppGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "محدوده مشخص شده: ${currentStep.targetLabel}",
                                        color = WhatsAppGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Explanatory Bottom Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header with step number & Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(WhatsAppEmerald.copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = currentStep.icon,
                                            contentDescription = null,
                                            tint = WhatsAppGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentStep.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "مرحله ${currentStep.stepIndex} از ${steps.size}",
                                            fontSize = 11.sp,
                                            color = WhatsAppGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Dot Progress Indicators
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    steps.forEachIndexed { idx, _ ->
                                        Box(
                                            modifier = Modifier
                                                .height(6.dp)
                                                .width(if (idx == currentStepIndex) 18.dp else 6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(
                                                    if (idx == currentStepIndex) WhatsAppGreen else Color.White.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step Description
                            Text(
                                text = currentStep.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Navigation Buttons (Previous, Next, Finish)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (currentStepIndex > 0) {
                                    OutlinedButton(
                                        onClick = { currentStepIndex-- },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "قبلی",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "قبلی", fontSize = 12.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Button(
                                    onClick = {
                                        if (currentStepIndex < steps.size - 1) {
                                            currentStepIndex++
                                        } else {
                                            onDismissTour()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("tour_next_button")
                                ) {
                                    Text(
                                        text = if (currentStepIndex == steps.size - 1) "پایان و شروع کار" else "مرحله بعد",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (currentStepIndex == steps.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders a visual representation of the specific UI target being highlighted
 */
@Composable
private fun SpotlightTargetMockVisual(targetType: TargetHighlightType) {
    when (targetType) {
        TargetHighlightType.SOURCE_FILTER -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WhatsAppEmerald,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "همه استوری‌ها",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.White.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "واتساپ معمولی",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.White.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "واتساپ بیزینس",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        TargetHighlightType.APP_TABS -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "📷 عکس‌ها", color = WhatsAppGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "🎬 ویدیوها", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(text = "💾 دانلودها", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(text = "💬 پیام مستقیم", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }

        TargetHighlightType.STATUS_CARD -> {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0D9488))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(10.dp)
            ) {
                // Play Icon in center
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bottom row with download button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ویدیو وضعیت", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(WhatsAppEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        TargetHighlightType.MULTI_SELECT -> {
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Checklist,
                    contentDescription = null,
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "انتخاب چندتایی استوری‌ها", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        TargetHighlightType.OPEN_WHATSAPP -> {
            Row(
                modifier = Modifier
                    .background(WhatsAppEmerald.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .border(1.dp, WhatsAppGreen, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "مشاهده استوری‌ها در واتساپ", color = WhatsAppGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
