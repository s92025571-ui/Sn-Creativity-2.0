package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.catalog.CreatorToolCatalog
import com.example.domain.model.StudioSection
import com.example.ui.components.Neon3DCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioSectionScreen(
    section: StudioSection,
    enable3DMotion: Boolean,
    onBack: () -> Unit,
    onSelectTool: (String) -> Unit
) {
    val tools = CreatorToolCatalog.toolsForSection(section)
    val meta = studioVisualMeta(section)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${tools.size} Specialized Creator Tools",
                            style = MaterialTheme.typography.labelSmall,
                            color = meta.accent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("section_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
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
                    .widthIn(max = 700.dp)
                    .testTag("section_tools_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = meta.accent.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = meta.icon,
                                contentDescription = null,
                                tint = meta.accent,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = section.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(tools, key = { it.id }) { tool ->
                    Neon3DCard(
                        onClick = { onSelectTool(tool.id) },
                        enable3DMotion = enable3DMotion,
                        accentColor = meta.accent,
                        cornerRadius = 20.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tool_card_${tool.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when {
                                            tool.isPromptEnhancer -> Icons.Default.AutoFixHigh
                                            tool.supportsWebGrounding -> Icons.Default.Language
                                            else -> Icons.Default.Bolt
                                        },
                                        contentDescription = null,
                                        tint = meta.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tool.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tool.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tool.isPromptEnhancer || tool.supportsWebGrounding) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(meta.accent.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (tool.isPromptEnhancer) {
                                                "PRO PROMPT REWRITER"
                                            } else {
                                                "SEARCH GROUNDING CAPABLE"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = meta.accent
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open ${tool.title}",
                                tint = meta.accent
                            )
                        }
                    }
                }
            }
        }
    }
}
