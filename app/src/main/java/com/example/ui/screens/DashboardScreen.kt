package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.config.AiConnectionMode
import com.example.data.local.PromptHistoryEntity
import com.example.domain.catalog.CreatorToolCatalog
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.StudioSection
import com.example.ui.components.Neon3DCard
import com.example.ui.components.Rotating3DSNCrystalLogo
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalExtendedTheme

@Composable
fun DashboardScreen(
    connectionMode: AiConnectionMode,
    themeMode: AppThemeMode,
    preferredLanguage: GenerationLanguage,
    enable3DMotion: Boolean,
    recentHistory: List<PromptHistoryEntity>,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenSection: (StudioSection) -> Unit,
    onOpenToolDirect: (String, Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalExtendedTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("dashboard_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Branding + Quick Theme & Language Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Rotating3DSNCrystalLogo(
                            modifier = Modifier.size(46.dp),
                            enable3DMotion = enable3DMotion,
                            showFloorReflection = false
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Creator Suite 2.0 • ${preferredLanguage.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = onToggleLanguage,
                            label = {
                                Text(
                                    text = if (preferredLanguage == GenerationLanguage.ENGLISH) "EN" else "UR-Rom",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.testTag("dashboard_language_chip")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dashboard_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (themeMode == AppThemeMode.NEON_DARK) {
                                    Icons.Default.LightMode
                                } else {
                                    Icons.Default.DarkMode
                                },
                                contentDescription = "Switch Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. Hero Studio Banner with AI Connection Status Pill & Quick Enhancer CTA
            item {
                Neon3DCard(
                    onClick = { onOpenToolDirect("prompt_enhancer", -1L) },
                    enable3DMotion = enable3DMotion,
                    cornerRadius = 26.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                        .testTag("dashboard_hero_banner_card")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_studio_banner),
                        contentDescription = "SN Creativity Studio Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xE6090514),
                                        Color(0xBF140A29),
                                        Color(0x661C0E38)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            AiStatusBadgePill(
                                mode = connectionMode,
                                onClick = onOpenSettings
                            )

                            Column {
                                Text(
                                    text = "Advanced Prompt Enhancer",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Turn one simple line into copy-ready Flow, Sora, Image & Gaming Thumbnail prompts.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE3D6FF),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 3. Section Header: Exactly Four Primary Studio Sections
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Primary Creator Studios",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "4 Studios • 20 Tools",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. The Exactly Four Primary Interactive 3D Dashboard Cards
            items(StudioSection.entries, key = { it.id }) { section ->
                val meta = studioVisualMeta(section)
                val sectionTools = CreatorToolCatalog.toolsForSection(section)

                Neon3DCard(
                    onClick = { onOpenSection(section) },
                    enable3DMotion = enable3DMotion,
                    accentColor = meta.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_card_${section.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(meta.accent.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = meta.icon,
                                        contentDescription = section.title,
                                        tint = meta.accent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = section.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = meta.accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open ${section.title}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = section.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview pills of tools inside this studio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sectionTools.take(3).forEach { tool ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = if (extended.isNeonDark) 0.72f else 0.9f
                                    )
                                ) {
                                    Text(
                                        text = tool.title.substringBefore(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                            if (sectionTools.size > 3) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = meta.accent.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "+${sectionTools.size - 3} more",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = meta.accent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Recent Prompt History Quick Access Strip (when entries exist)
            if (recentHistory.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recent Creations",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        AssistChip(
                            onClick = onOpenHistory,
                            label = { Text("View All (${recentHistory.size})") },
                            modifier = Modifier.testTag("dashboard_view_history_button")
                        )
                    }
                }

                items(recentHistory.take(2), key = { "recent_${it.id}" }) { item ->
                    Neon3DCard(
                        onClick = { onOpenToolDirect(item.toolId, item.id) },
                        enable3DMotion = enable3DMotion,
                        cornerRadius = 18.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.toolTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = item.sourceBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.userInput,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

data class StudioVisualMeta(
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun studioVisualMeta(section: StudioSection): StudioVisualMeta {
    val isDark = LocalExtendedTheme.current.isNeonDark
    return when (section) {
        StudioSection.GAMING_STUDIO -> StudioVisualMeta(
            icon = Icons.Default.SportsEsports,
            accent = if (isDark) Color(0xFFB84DFF) else Color(0xFF6B2CF5)
        )
        StudioSection.AI_PROMPT_STUDIO -> StudioVisualMeta(
            icon = Icons.Default.AutoAwesome,
            accent = if (isDark) Color(0xFFFF3D9A) else Color(0xFF9C2770)
        )
        StudioSection.VOICE_SCRIPT_STUDIO -> StudioVisualMeta(
            icon = Icons.Default.Mic,
            accent = if (isDark) Color(0xFF8C52FF) else Color(0xFF4A35B8)
        )
        StudioSection.CREATOR_TOOLKIT -> StudioVisualMeta(
            icon = Icons.Default.Build,
            accent = if (isDark) Color(0xFF38C8FF) else Color(0xFF006D99)
        )
    }
}

@Composable
private fun AiStatusBadgePill(
    mode: AiConnectionMode,
    onClick: () -> Unit
) {
    val extended = LocalExtendedTheme.current
    val (label, dotColor, icon) = when (mode) {
        AiConnectionMode.SECURE_BACKEND_PROXY -> Triple(
            "AI Status: Secure Backend Connected",
            extended.statusConnected,
            Icons.Default.CheckCircle
        )
        AiConnectionMode.DIRECT_GEMINI_API -> Triple(
            "AI Status: Gemini API Ready",
            extended.statusConnected,
            Icons.Default.CheckCircle
        )
        AiConnectionMode.UNCONFIGURED_LOCAL_ONLY -> Triple(
            "AI Status: Setup Required • Tap to View",
            extended.statusWarning,
            Icons.Default.WarningAmber
        )
    }

    AssistChip(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = dotColor,
                modifier = Modifier.size(16.dp)
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color(0x99161026)
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = dotColor.copy(alpha = 0.55f)
        ),
        modifier = Modifier.testTag("dashboard_ai_status_chip")
    )
}
