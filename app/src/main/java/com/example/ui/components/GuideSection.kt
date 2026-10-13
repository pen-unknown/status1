package com.example.ui.components

import android.os.Build
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppEmerald
import com.example.ui.theme.WhatsAppGreen

@Composable
fun GuideSection(
    onOpenWhatsApp: () -> Unit,
    onOpenWABusiness: () -> Unit,
    onToggleDemo: () -> Unit,
    isDemoEnabled: Boolean,
    onPickFolder: () -> Unit,
    onManageAllFiles: () -> Unit,
    onStartTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val hasAllFilesAccess = if (isAndroid11Plus) Environment.isExternalStorageManager() else true

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("guide_section")
    ) {
        // Hero Guide Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(WhatsAppEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = "راهنما",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "راهنمای استفاده از برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "۳ مرحله ساده تا مشاهده و ذخیره دائمی هر وضعیت واتساپ در گالری گوشی شما",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Tour Launch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تور آموزشی تعاملی برنامه",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مشاهده مجدد آموزش تصویری ۵ مرحله‌ای کار با برنامه",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStartTour,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "مشاهده تور", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1
        StepCard(
            stepNumber = "۱",
            title = "مشاهده استوری در واتساپ",
            description = "وارد برنامه واتساپ شوید و استوری یا وضعیت دوستانتان را تا انتها مشاهده کنید. واتساپ فایل استوری را موقتاً در حافظه کش دستگاه ذخیره می‌کند.",
            icon = Icons.Outlined.Visibility,
            buttonText = "باز کردن واتساپ",
            onButtonClick = onOpenWhatsApp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2
        StepCard(
            stepNumber = "۲",
            title = "باز کردن برنامه Status Saver",
            description = "به این برنامه برگردید؛ تمام عکس‌ها و ویدیوهای استوری‌های دیده شده به صورت خودکار شناسایی و تفکیک می‌شوند.",
            icon = Icons.Outlined.PlayCircle
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3
        StepCard(
            stepNumber = "۳",
            title = "دانلود و ذخیره دائم در گالری",
            description = "روی آیکون دانلود هر استوری بزنید یا آن را به صورت تمام‌صفحه تماشا کرده و ذخیره نمایید. فایل برای همیشه در گالری گوشی (پوشه StatusSaver) محفوظ می‌ماند.",
            icon = Icons.Outlined.Save
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Test Mode / Demo Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "استوری‌های تستی و نمونه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDemoEnabled) WhatsAppEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = if (isDemoEnabled) "وضعیت: فعال (نمایش نمونه‌ها)" else "وضعیت: غیرفعال (فقط استوری‌های واقعی)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDemoEnabled) WhatsAppEmerald else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onToggleDemo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDemoEnabled) MaterialTheme.colorScheme.error else WhatsAppEmerald
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isDemoEnabled) "غیرفعال‌سازی" else "فعال‌سازی",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Storage Access / Troubleshooting Card for Android 11+
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📁 حل مشکل عدم نمایش استوری‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "در صورتی که استوری‌ها را در واتساپ دیده‌اید اما در برنامه ظاهر نمی‌شوند، از یکی از گزینه‌های زیر جهت اتصال مستقیم استفاده کنید:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // SAF Folder Selection
                Button(
                    onClick = onPickFolder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "انتخاب پوشه استوری‌ها در فایل‌ها (SAF)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Android 11+ All Files Access
                if (isAndroid11Plus && !hasAllFilesAccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onManageAllFiles,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "فعال‌سازی دسترسی مدیریت فایل‌ها (اندروید ۱۱+)", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // FAQ Section
        Text(
            text = "پرسش‌های متداول (FAQ)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FaqItem(
            question = "آیا مخاطب متوجه می‌شود استوری او را دانلود کرده‌ام؟",
            answer = "خیر، فرآیند دانلود کاملاً آفلاین و محرمانه است و هیچ اعلانی برای شخص مقابل ارسال نمی‌شود."
        )

        FaqItem(
            question = "چرا برخی استوری‌ها در لیست نمایش داده نمی‌شوند؟",
            answer = "تنها استوری‌هایی نمایش داده می‌شوند که قبلاً آن‌ها را در واتساپ تماشا کرده باشید. همچنین در اندروید ۱۱ به بالا بررسی کنید که دسترسی‌های لازم تایید شده باشند."
        )

        FaqItem(
            question = "آیا از واتساپ تجاری (بیزینس) هم پشتیبانی می‌شود؟",
            answer = "بله! برنامه از واتساپ اصلی، واتساپ بیزینس و نسخه‌های رسمی به صورت کامل پشتیبانی می‌کند."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // WhatsApp Business Quick Open
        OutlinedButton(
            onClick = onOpenWABusiness,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Launch,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "باز کردن واتساپ بیزینس", fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(WhatsAppEmerald.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppEmerald,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = WhatsAppEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = answer,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
                modifier = Modifier.padding(start = 26.dp)
            )
        }
    }
}
