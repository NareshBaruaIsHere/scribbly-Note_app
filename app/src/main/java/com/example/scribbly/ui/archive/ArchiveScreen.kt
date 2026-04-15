package com.example.scribbly.ui.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scribbly.ui.home.NoteCard
import com.example.scribbly.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.archiveState.collectAsStateWithLifecycle()
    val neumorphicColors = LocalNeumorphicColors.current

    Scaffold(
        containerColor = neumorphicColors.background,
        topBar = {
            if (state.isMultiSelectMode) {
                TopAppBar(
                    title = { Text("${state.selectedNotes.size} Selected") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = neumorphicColors.background,
                        titleContentColor = neumorphicColors.text,
                        actionIconContentColor = neumorphicColors.text,
                        navigationIconContentColor = neumorphicColors.text
                    ),
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::unarchiveSelected) {
                            Icon(Icons.Default.Unarchive, contentDescription = "Unarchive")
                        }
                        IconButton(onClick = viewModel::deleteSelected) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Archived Notes") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = neumorphicColors.background,
                        titleContentColor = neumorphicColors.text,
                        navigationIconContentColor = neumorphicColors.text
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No archived notes", color = neumorphicColors.text)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.notes, key = { it.note.id }) { noteWithLabels ->
                        val isSelected = state.selectedNotes.contains(noteWithLabels.note.id)
                        NoteCard(
                            noteWithLabels = noteWithLabels,
                            isSelected = isSelected,
                            isMultiSelectMode = state.isMultiSelectMode,
                            onClick = {
                                if (state.isMultiSelectMode) {
                                    viewModel.toggleSelection(noteWithLabels.note.id)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleSelection(noteWithLabels.note.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
