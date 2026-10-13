package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StatusItem
import com.example.ui.theme.WhatsAppEmerald
import com.example.ui.theme.WhatsAppGreen
import com.example.util.ContactHelper
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessageBottomSheet(
    status: StatusItem,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onAssignContact: (name: String, phone: String) -> Unit,
    onOpenInDirectChat: (phone: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var contactName by remember { mutableStateOf(status.posterName) }
    var phoneNumber by remember { mutableStateOf(status.posterPhone.ifBlank { "" }) }
    var messageText by remember {
        mutableStateOf(
            if (isPersian) "سلام، استوری شما را دیدم..." else "Hello, I saw your story..."
        )
    }
    var saveForThisStory by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val contact = ContactHelper.extractContactInfo(context, uri)
            if (contact != null) {
                if (contact.name.isNotBlank()) contactName = contact.name
                if (contact.phone.isNotBlank()) phoneNumber = contact.phone
                errorMessage = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(WhatsAppEmerald.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            tint = WhatsAppEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "ارسال پیام در واتساپ" else "Message Story Owner",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPersian) "ارسال پیام مستقیم به صاحب این استوری" else "Direct chat with the person who posted this story",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pick from phone contacts button
            FilledTonalButton(
                onClick = { contactPickerLauncher.launch(null) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = WhatsAppEmerald.copy(alpha = 0.12f),
                    contentColor = WhatsAppEmerald
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Contacts,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "انتخاب از مخاطبین گوشی" else "Pick from Phone Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    errorMessage = null
                },
                label = { Text(if (isPersian) "شماره موبایل (مثال: 09121234567)" else "Phone Number") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = WhatsAppEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppEmerald,
                    focusedLabelColor = WhatsAppEmerald
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Name Input (optional)
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text(if (isPersian) "نام مخاطب (اختیاری برای نمایش در برنامه)" else "Contact Name (Optional)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppEmerald,
                    focusedLabelColor = WhatsAppEmerald
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Message text
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text(if (isPersian) "متن پیام" else "Message Text") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhatsAppEmerald,
                    focusedLabelColor = WhatsAppEmerald
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Save contact for this story checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveForThisStory,
                    onCheckedChange = { saveForThisStory = it },
                    colors = CheckboxDefaults.colors(checkedColor = WhatsAppEmerald)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPersian) "ذخیره نام این مخاطب برای این استوری" else "Remember this contact name for this story",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Send via WhatsApp Button
                Button(
                    onClick = {
                        val clean = phoneNumber.filter { it.isDigit() }
                        if (clean.isBlank()) {
                            errorMessage = if (isPersian) "لطفاً شماره موبایل را وارد یا انتخاب کنید" else "Please enter or pick a phone number"
                            return@Button
                        }
                        if (saveForThisStory && contactName.isNotBlank()) {
                            onAssignContact(contactName, phoneNumber)
                        }
                        val normalized = ContactHelper.normalizePhoneNumber(phoneNumber)
                        val ok = WhatsAppHelper.startDirectChat(context, normalized, messageText, "com.whatsapp")
                        if (ok) onDismiss() else {
                            // Try generic launch
                            WhatsAppHelper.startDirectChat(context, normalized, messageText, null)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppEmerald),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "ارسال در واتساپ" else "WhatsApp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Send via Business WhatsApp Button
                Button(
                    onClick = {
                        val clean = phoneNumber.filter { it.isDigit() }
                        if (clean.isBlank()) {
                            errorMessage = if (isPersian) "لطفاً شماره موبایل را وارد یا انتخاب کنید" else "Please enter or pick a phone number"
                            return@Button
                        }
                        if (saveForThisStory && contactName.isNotBlank()) {
                            onAssignContact(contactName, phoneNumber)
                        }
                        val normalized = ContactHelper.normalizePhoneNumber(phoneNumber)
                        WhatsAppHelper.startDirectChat(context, normalized, messageText, "com.whatsapp.w4b")
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "واتساپ بیزینس" else "WA Business",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
