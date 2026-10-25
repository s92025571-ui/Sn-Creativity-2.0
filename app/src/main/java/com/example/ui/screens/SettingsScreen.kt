package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.config.AiConnectionMode
import com.example.config.OwnerSupportConfig
import com.example.data.preferences.UserPreferencesState
import com.example.domain.model.GenerationLanguage
import com.example.ui.components.Neon3DActionButton
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalExtendedTheme
import com.example.util.WhatsAppSupportHelper
import com.example.util.WhatsAppSupportResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferencesState,
    connectionMode: AiConnectionMode,
    historyCount: Int,
    onSelectTheme: (AppThemeMode) -> Unit,
    onSelectLanguage: (GenerationLanguage) -> Unit,
    onToggle3DMotion: (Boolean) -> Unit,
    onToggleAutoFallback: (Boolean) -> Unit,
    onRefreshAiStatus: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val extended = LocalExtendedTheme.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.nav_settings),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Themes, AI Status, Language & Support",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .testTag("settings_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. AI Connection Status & Security Card
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (connectionMode == AiConnectionMode.UNCONFIGURED_LOCAL_ONLY) {
                                            Icons.Default.WarningAmber
                                        } else {
                                            Icons.Default.CheckCircle
                                        },
                                        contentDescription = null,
                                        tint = if (connectionMode == AiConnectionMode.UNCONFIGURED_LOCAL_ONLY) {
                                            extended.statusWarning
                                        } else {
                                            extended.statusConnected
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Gemini AI Connection Status",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (connectionMode) {
                                                AiConnectionMode.SECURE_BACKEND_PROXY ->
                                                    stringResource(R.string.status_proxy_connected)
                                                AiConnectionMode.DIRECT_GEMINI_API ->
                                                    stringResource(R.string.status_direct_gemini)
                                                AiConnectionMode.UNCONFIGURED_LOCAL_ONLY ->
                                                    stringResource(R.string.status_setup_required)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        onRefreshAiStatus()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("AI connection status refreshed.")
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh AI Status")
                                }
                            }

                            Text(
                                text = "Active Model: ${OwnerSupportConfig.GEMINI_DEFAULT_MODEL} • Zero Sign-In Required",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (connectionMode == AiConnectionMode.DIRECT_GEMINI_API) {
                                Text(
                                    text = stringResource(R.string.security_prototype_notice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extended.statusWarning
                                )
                            } else if (connectionMode == AiConnectionMode.UNCONFIGURED_LOCAL_ONLY) {
                                Text(
                                    text = "How to enable Gemini AI: Set GEMINI_API_KEY in the AI Studio Secrets panel (or gitignored .env for local Android Studio builds), or set GEMINI_BACKEND_PROXY_URL for production.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Use Offline Template When Unconfigured",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "When off, Generate clearly notifies if Gemini setup is missing.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = preferences.autoFallbackToLocalOnUnconfigured,
                                    onCheckedChange = onToggleAutoFallback,
                                    modifier = Modifier.testTag("settings_auto_fallback_switch")
                                )
                            }
                        }
                    }
                }

                // 2. Theme Selection (Neon Dark & Creative Light) + 3D Effects
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Appearance & 3D Motion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AppThemeMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = preferences.themeMode == mode,
                                        onClick = { onSelectTheme(mode) },
                                        label = { Text(mode.displayName) },
                                        modifier = Modifier.testTag("theme_option_${mode.storageKey}")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Interactive 3D Tilt & Logo Rotation",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "Disable on older devices if you prefer static lightweight controls.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = preferences.enable3DMotionEffects,
                                    onCheckedChange = onToggle3DMotion,
                                    modifier = Modifier.testTag("settings_3d_motion_switch")
                                )
                            }
                        }
                    }
                }

                // 3. Default Language Preference (English & Roman Urdu)
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Default Generation Language",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GenerationLanguage.entries.forEach { lang ->
                                    FilterChip(
                                        selected = preferences.preferredLanguage == lang,
                                        onClick = { onSelectLanguage(lang) },
                                        label = { Text(lang.label) },
                                        modifier = Modifier.testTag("settings_lang_${lang.code}")
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Prompt History Quick Access
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Prompt History & Saved Content",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$historyCount items stored locally on device",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = onOpenHistory,
                                modifier = Modifier.testTag("settings_open_history_button")
                            ) {
                                Text("Open")
                            }
                        }
                    }
                }

                // 5. Contact Help (WhatsApp Support)
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Contact Help (WhatsApp Support)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isWaConfigured = OwnerSupportConfig.isWhatsAppConfigured()
                            Text(
                                text = if (isWaConfigured) {
                                    "Opens a direct WhatsApp chat with SN Creativity support without requiring login or contact permissions."
                                } else {
                                    "Owner WhatsApp number is not set yet. Set OWNER_WHATSAPP_NUMBER in OwnerSupportConfig.kt to enable direct WhatsApp chat."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Neon3DActionButton(
                                text = stringResource(R.string.contact_whatsapp_title),
                                icon = Icons.Default.SupportAgent,
                                enable3DMotion = preferences.enable3DMotionEffects,
                                onClick = {
                                    when (val res = WhatsAppSupportHelper.openOwnerSupportChat(context)) {
                                        WhatsAppSupportResult.LaunchedWhatsApp -> Unit
                                        WhatsAppSupportResult.LaunchedBrowserFallback -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("WhatsApp app not found — opened browser support link.")
                                            }
                                        }
                                        WhatsAppSupportResult.NumberNotConfigured -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    context.getString(R.string.contact_whatsapp_unconfigured)
                                                )
                                            }
                                        }
                                        is WhatsAppSupportResult.Error -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(res.reason)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_whatsapp_button")
                            )
                        }
                    }
                }

                // 6. Privacy, Cost Controls & App Information
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, extended.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Privacy, Quotas & App Info",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "• Package ID: ${BuildConfig.APPLICATION_ID} (v${BuildConfig.VERSION_NAME})\n" +
                                    "• No Google Sign-In, no Firebase Auth, and no in-app subscriptions.\n" +
                                    "• Prompt history and preferences stay strictly on your phone.\n" +
                                    "• Gemini Free-Tier Note: Google AI Studio offers a rate-limited free tier in supported regions (quotas and availability are subject to Google policy and are never guaranteed).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.app_version_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
