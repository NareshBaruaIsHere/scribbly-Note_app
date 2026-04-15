package com.example.scribbly.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scribbly.data.local.NoteWithLabels
import com.example.scribbly.ui.theme.LocalNeumorphicColors
import com.example.scribbly.ui.theme.neumorphicSurface
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (Long?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val neumorphicColors = LocalNeumorphicColors.current

    Scaffold(
        containerColor = neumorphicColors.background,
        topBar = {
            if (uiState.isMultiSelectMode) {
                TopAppBar(
                    title = { Text("${uiState.selectedNotes.size} Selected") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = neumorphicColors.background,
                        titleContentColor = neumorphicColors.text,
                        actionIconContentColor = neumorphicColors.text,
                        navigationIconContentColor = neumorphicColors.text
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.pinSelectedNotes() }) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin/Unpin")
                        }
                        IconButton(onClick = { viewModel.archiveSelectedNotes() }) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                        }
                        IconButton(onClick = { viewModel.deleteSelectedNotes() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Scribbly") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = neumorphicColors.background,
                        titleContentColor = neumorphicColors.text,
                        actionIconContentColor = neumorphicColors.text
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isMultiSelectMode) {
                FloatingActionButton(
                    onClick = { onNavigateToEditor(null) },
                    containerColor = neumorphicColors.background,
                    contentColor = neumorphicColors.text,
                    modifier = Modifier.neumorphicSurface(
                        shape = RoundedCornerShape(16.dp),
                        cornerRadius = 16.dp,
                        lightShadowColor = neumorphicColors.shadowLight,
                        darkShadowColor = neumorphicColors.shadowDark,
                        surfaceColor = neumorphicColors.background,
                        shadowElevation = 6.dp
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            if (!uiState.isMultiSelectMode) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .neumorphicSurface(
                            shape = RoundedCornerShape(12.dp),
                            cornerRadius = 12.dp,
                            lightShadowColor = neumorphicColors.shadowLight,
                            darkShadowColor = neumorphicColors.shadowDark,
                            surfaceColor = neumorphicColors.background,
                            shadowElevation = 2.dp,
                            isPressed = true // Make search bar look recessed
                        ),
                    placeholder = { Text("Search notes...", color = neumorphicColors.text.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = neumorphicColors.text.copy(alpha=0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedTextColor = neumorphicColors.text,
                        unfocusedTextColor = neumorphicColors.text,
                        cursorColor = neumorphicColors.text
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (uiState.notes.isEmpty() && uiState.searchQuery.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No notes yet", style = MaterialTheme.typography.titleLarge, color = neumorphicColors.text)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap + to create your first note.", style = MaterialTheme.typography.bodyMedium, color = neumorphicColors.text.copy(alpha=0.7f))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.notes, key = { it.note.id }) { noteWithLabels ->
                        val isSelected = uiState.selectedNotes.contains(noteWithLabels.note.id)
                        NoteCard(
                            noteWithLabels = noteWithLabels,
                            isSelected = isSelected,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            onClick = {
                                if (uiState.isMultiSelectMode) {
                                    viewModel.toggleNoteSelection(noteWithLabels.note.id)
                                } else {
                                    onNavigateToEditor(noteWithLabels.note.id)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleNoteSelection(noteWithLabels.note.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    noteWithLabels: NoteWithLabels,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val neumorphicColors = LocalNeumorphicColors.current
    val note = noteWithLabels.note

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicSurface(
                shape = RoundedCornerShape(16.dp),
                cornerRadius = 16.dp,
                lightShadowColor = neumorphicColors.shadowLight,
                darkShadowColor = neumorphicColors.shadowDark,
                surfaceColor = if (isSelected) neumorphicColors.shadowDark.copy(alpha = 0.5f) else neumorphicColors.background,
                shadowElevation = if (isSelected) 2.dp else 6.dp,
                isPressed = isSelected
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    color = neumorphicColors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = neumorphicColors.text.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = neumorphicColors.text.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(note.updatedAt))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = neumorphicColors.text.copy(alpha = 0.5f)
            )
        }
    }
}
