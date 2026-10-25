package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.PromptHistoryEntity
import com.example.ui.components.Neon3DCard
import com.example.util.ClipboardHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyEntries: List<PromptHistoryEntity>,
    enable3DMotion: Boolean,
    onReusePrompt: (String, Long) -> Unit,
    onToggleSaved: (Long, Boolean) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSavedOnly by remember { mutableStateOf(false) }
    var confirmClearDialog by remember { mutableStateOf(false) }
    var selectedDetailEntry by remember { mutableStateOf<PromptHistoryEntity?>(null) }

    val displayedEntries = remember(historyEntries, showSavedOnly) {
        if (showSavedOnly) historyEntries.filter { it.isSavedFavorite } else historyEntries
    }

    val dateFormatter = remember {
        SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prompt History & Saved",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Stored privately on this device",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (historyEntries.isNotEmpty()) {
                        IconButton(
                            onClick = { confirmClearDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("clear_all_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.action_clear_history),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = !showSavedOnly,
                        onClick = { showSavedOnly = false },
                        label = { Text("All History (${historyEntries.size})") },
                        modifier = Modifier.testTag("history_filter_all")
                    )
                    FilterChip(
                        selected = showSavedOnly,
                        onClick = { showSavedOnly = true },
                        label = {
                            Text("Saved (${historyEntries.count { it.isSavedFavorite }})")
                        },
                        modifier = Modifier.testTag("history_filter_saved")
                    )
                }

                if (displayedEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (showSavedOnly) "No Saved Creations Yet" else "Your Prompt History Is Empty",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Generate titles, prompts, or scripts in any of the 4 studios and they will automatically appear here for instant reuse.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("history_entries_list"),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(displayedEntries, key = { it.id }) { entry ->
                            Neon3DCard(
                                onClick = { selectedDetailEntry = entry },
                                enable3DMotion = enable3DMotion,
                                cornerRadius = 20.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("history_item_${entry.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = entry.toolTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${entry.parametersSummary} • ${dateFormatter.format(Date(entry.createdAtMillis))}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { onToggleSaved(entry.id, entry.isSavedFavorite) },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (entry.isSavedFavorite) {
                                                    Icons.Default.Bookmark
                                                } else {
                                                    Icons.Default.BookmarkBorder
                                                },
                                                contentDescription = "Toggle Saved",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Prompt: ${entry.userInput}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = entry.generatedOutput,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (ClipboardHelper.copyText(
                                                            context,
                                                            entry.toolTitle,
                                                            entry.generatedOutput
                                                        )
                                                    ) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Copied result!")
                                                        }
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(stringResource(R.string.action_copy))
                                            }

                                            OutlinedButton(
                                                onClick = { onReusePrompt(entry.toolId, entry.id) },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("reuse_history_${entry.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Replay,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(stringResource(R.string.action_reuse))
                                            }
                                        }

                                        IconButton(
                                            onClick = { onDeleteEntry(entry.id) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("delete_history_${entry.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = stringResource(R.string.action_delete),
                                                tint = MaterialTheme.colorScheme.error
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
    }

    // Detail View Modal Dialog
    selectedDetailEntry?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedDetailEntry = null },
            title = { Text(detail.toolTitle) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Original Input: ${detail.userInput}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SelectionContainer {
                        Text(
                            text = detail.generatedOutput,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ClipboardHelper.copyText(context, detail.toolTitle, detail.generatedOutput)
                        selectedDetailEntry = null
                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
                    }
                ) {
                    Text(stringResource(R.string.action_copy))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val tId = detail.toolId
                            val eId = detail.id
                            selectedDetailEntry = null
                            onReusePrompt(tId, eId)
                        }
                    ) {
                        Text(stringResource(R.string.action_reuse))
                    }
                    TextButton(onClick = { selectedDetailEntry = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Confirmation Dialog before clearing all history
    if (confirmClearDialog) {
        AlertDialog(
            onDismissRequest = { confirmClearDialog = false },
            title = { Text("Clear Prompt History?") },
            text = {
                Text("This will permanently remove all saved prompts and generated outputs stored on this phone. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmClearDialog = false
                        onClearAllHistory()
                        scope.launch { snackbarHostState.showSnackbar("Prompt history cleared.") }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
