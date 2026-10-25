package com.example.config

import com.example.BuildConfig

/**
 * Owner-configurable support and AI backend settings for SN Creativity AI 2.0.
 *
 * HOW TO CONFIGURE WHATSAPP SUPPORT:
 * 1. Set [OWNER_WHATSAPP_NUMBER] below to your international phone number in digits only
 *    (country code + number, without '+' or spaces — e.g., "923001234567" or "14155550199").
 * 2. Do NOT commit private API secrets here; API secrets belong exclusively in `.env` (gitignored)
 *    or on your secure backend server.
 * 3. If [OWNER_WHATSAPP_NUMBER] is left empty, the Settings -> Contact Help button clearly informs
 *    the user that the owner support number has not been configured yet instead of inventing a fake number.
 */
object OwnerSupportConfig {
    /**
     * Owner WhatsApp support number supplied separately by the project owner.
     * Leave blank ("") until the owner enters their real international digits (e.g. "923XXXXXXXXX").
     */
    const val OWNER_WHATSAPP_NUMBER: String = ""

    /**
     * Default pre-filled greeting sent when a user taps Contact Help in Settings.
     */
    const val DEFAULT_SUPPORT_MESSAGE: String =
        "Assalam-o-Alaikum / Hello SN Creativity AI Support! I need help with the SN Creativity AI 2.0 Android app."

    /**
     * Supported Gemini model used for text, script, research, and prompt enhancement workflows.
     */
    const val GEMINI_DEFAULT_MODEL: String = "gemini-3.5-flash"

    fun isWhatsAppConfigured(): Boolean {
        val cleaned = sanitizedWhatsAppNumber()
        return cleaned.length in 8..16 && cleaned.all { it.isDigit() }
    }

    fun sanitizedWhatsAppNumber(): String =
        OWNER_WHATSAPP_NUMBER.filter { it.isDigit() }
}

/**
 * Evaluates current AI backend readiness without exposing or fabricating credentials.
 */
enum class AiConnectionMode {
    SECURE_BACKEND_PROXY,
    DIRECT_GEMINI_API,
    UNCONFIGURED_LOCAL_ONLY
}

object AiEnvironmentStatus {
    private val placeholderValues = setOf(
        "",
        "YOUR_GEMINI_API_KEY_HERE",
        "MY_GEMINI_API_KEY",
        "REPLACE_WITH_GEMINI_API_KEY",
        "null"
    )

    fun getBackendProxyUrl(): String {
        val raw = BuildConfig.GEMINI_BACKEND_PROXY_URL.trim()
        return if (raw == "UNCONFIGURED_BACKEND_PROXY_URL") "" else raw
    }

    fun getDirectApiKey(): String =
        BuildConfig.GEMINI_API_KEY.trim()

    fun hasSecureProxy(): Boolean {
        val url = getBackendProxyUrl()
        return url.startsWith("https://", ignoreCase = true) && url.length > 12
    }

    fun hasValidDirectApiKey(): Boolean {
        val key = getDirectApiKey()
        return key.isNotEmpty() && key !in placeholderValues && !key.startsWith("YOUR_")
    }

    fun currentMode(): AiConnectionMode = when {
        hasSecureProxy() -> AiConnectionMode.SECURE_BACKEND_PROXY
        hasValidDirectApiKey() -> AiConnectionMode.DIRECT_GEMINI_API
        else -> AiConnectionMode.UNCONFIGURED_LOCAL_ONLY
    }
}
