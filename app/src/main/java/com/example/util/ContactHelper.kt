package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

data class ContactPersona(
    val name: String,
    val phone: String
)

object ContactHelper {

    private const val PREFS_NAME = "status_assigned_contacts"

    /**
     * Get user-assigned contact for a given status identifier (file name or ID).
     * Returns null if no custom contact has been assigned by the user.
     * We NEVER return fake stranger contacts for real WhatsApp status files!
     */
    fun getAssignedContact(context: Context, identifier: String): ContactPersona? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cleanKey = identifier.substringAfterLast('/').substringAfterLast('\\').trim()
        val name = prefs.getString("${cleanKey}_name", null)
        val phone = prefs.getString("${cleanKey}_phone", null)

        return if (!phone.isNullOrBlank() || !name.isNullOrBlank()) {
            ContactPersona(name = name ?: "", phone = phone ?: "")
        } else {
            null
        }
    }

    /**
     * Persistently assign a contact to a status item.
     */
    fun saveAssignedContact(context: Context, identifier: String, name: String, phone: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cleanKey = identifier.substringAfterLast('/').substringAfterLast('\\').trim()
        prefs.edit()
            .putString("${cleanKey}_name", name.trim())
            .putString("${cleanKey}_phone", phone.trim())
            .apply()
    }

    /**
     * Formats phone number into international WhatsApp standard (e.g., 989123456789).
     */
    fun normalizePhoneNumber(rawPhone: String, defaultCountryCode: String = "+98"): String {
        val digits = rawPhone.filter { it.isDigit() }
        if (digits.isEmpty()) return ""

        return when {
            digits.startsWith("0098") -> digits.removePrefix("00")
            digits.startsWith("98") -> digits
            digits.startsWith("0") -> {
                val cc = defaultCountryCode.filter { it.isDigit() }.ifEmpty { "98" }
                cc + digits.substring(1)
            }
            rawPhone.startsWith("+") -> digits
            else -> {
                val cc = defaultCountryCode.filter { it.isDigit() }.ifEmpty { "98" }
                cc + digits
            }
        }
    }

    /**
     * Extracts contact name and phone number from system contact picker Uri
     */
    fun extractContactInfo(context: Context, contactUri: Uri): ContactPersona? {
        return try {
            val resolver = context.contentResolver
            var displayName = ""
            var contactId: String? = null

            resolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: ""
                    if (idIdx >= 0) contactId = cursor.getString(idIdx)
                }
            }

            var phoneNumber = ""
            if (contactId != null) {
                resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { pCursor ->
                    if (pCursor.moveToFirst()) {
                        val numIdx = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numIdx >= 0) {
                            phoneNumber = pCursor.getString(numIdx) ?: ""
                        }
                    }
                }
            }

            if (phoneNumber.isNotBlank() || displayName.isNotBlank()) {
                ContactPersona(name = displayName, phone = phoneNumber)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
