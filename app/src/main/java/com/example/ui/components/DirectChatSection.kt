package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppEmerald

@Composable
fun DirectChatSection(
    countryCode: String,
    phoneNumber: String,
    message: String,
    isPersian: Boolean,
    onCountryCodeChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("direct_chat_section"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                        .size(54.dp)
                        .background(WhatsAppEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = if (isPersian) "چت مستقیم" else "Direct Chat",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isPersian) "ارسال پیام بدون ذخیره شماره" else "Message without saving number",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPersian)
                            "مستقیماً در واتساپ به هر شماره‌ای پیام دهید بدون اینکه نیاز باشد شماره را در مخاطبین ذخیره کنید."
                        else
                            "Start WhatsApp chats directly with any number without adding them to contacts first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Phone Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isPersian) "شماره مخاطب" else "Contact Phone Number",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Code
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = onCountryCodeChange,
                        label = { Text(if (isPersian) "کد کشور" else "Code") },
                        modifier = Modifier.width(96.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppEmerald,
                            focusedLabelColor = WhatsAppEmerald
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        label = { Text(if (isPersian) "شماره موبایل (مثلاً 9123456789)" else "Mobile number (e.g. 912...)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("direct_phone_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppEmerald,
                            focusedLabelColor = WhatsAppEmerald
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Message
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text(if (isPersian) "متن پیام (اختیاری)" else "Message (Optional)") },
                    placeholder = { Text(if (isPersian) "سلام، وقت بخیر..." else "Hello, your message...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("direct_message_input"),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppEmerald,
                        focusedLabelColor = WhatsAppEmerald
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Send Button
                Button(
                    onClick = onSendChat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_direct_chat_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = if (isPersian) "ارسال پیام" else "Send Message",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "شروع گفت‌وگو در واتساپ" else "Start Chat on WhatsApp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isPersian) "💡 نکات کاربردی:" else "💡 Useful Tips:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isPersian)
                        "• پیش‌شماره ایران ۹۸+ است، نیازی به درج صفر اول شماره نیست.\n" +
                        "• این قابلیت کاملاً امن بوده و از پروتکل رسمی واتساپ استفاده می‌کند.\n" +
                        "• در صورتی که واتساپ بیزینس دارید، چت در برنامه پیش‌فرض شما باز خواهد شد."
                    else
                        "• Country code prefix (e.g. +98 or +1), no leading 0 required.\n" +
                        "• Completely secure using official WhatsApp protocol.\n" +
                        "• Opens directly in standard WhatsApp or WhatsApp Business.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
