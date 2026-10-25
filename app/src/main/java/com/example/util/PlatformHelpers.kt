package com.example.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.config.OwnerSupportConfig

object ClipboardHelper {
    fun copyText(context: Context, label: String, text: String): Boolean {
        return runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            true
        }.getOrDefault(false)
    }
}

sealed class WhatsAppSupportResult {
    data object LaunchedWhatsApp : WhatsAppSupportResult()
    data object LaunchedBrowserFallback : WhatsAppSupportResult()
    data object NumberNotConfigured : WhatsAppSupportResult()
    data class Error(val reason: String) : WhatsAppSupportResult()
}

object WhatsAppSupportHelper {

    /**
     * Opens a WhatsApp chat with the owner-supplied support number from [OwnerSupportConfig].
     * Never invents a phone number, never requests READ_CONTACTS, and provides a safe browser fallback
     * if WhatsApp is not installed on the device.
     */
    fun openOwnerSupportChat(
        context: Context,
        customMessage: String = OwnerSupportConfig.DEFAULT_SUPPORT_MESSAGE
    ): WhatsAppSupportResult {
        if (!OwnerSupportConfig.isWhatsAppConfigured()) {
            return WhatsAppSupportResult.NumberNotConfigured
        }

        val phoneDigits = OwnerSupportConfig.sanitizedWhatsAppNumber()
        val encodedMessage = Uri.encode(customMessage)
        val waUri = Uri.parse("https://wa.me/$phoneDigits?text=$encodedMessage")

        // 1. Try launching official WhatsApp or WhatsApp Business directly
        val directPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
        for (pkg in directPackages) {
            val directIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (directIntent.resolveActivity(context.packageManager) != null) {
                return try {
                    context.startActivity(directIntent)
                    WhatsAppSupportResult.LaunchedWhatsApp
                } catch (_: ActivityNotFoundException) {
                    continue
                }
            }
        }

        // 2. Fallback: open universal https://wa.me URL in system browser
        return try {
            val browserIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            WhatsAppSupportResult.LaunchedBrowserFallback
        } catch (e: Exception) {
            WhatsAppSupportResult.Error(
                e.localizedMessage ?: "No browser or messaging app available to open WhatsApp link."
            )
        }
    }
}
