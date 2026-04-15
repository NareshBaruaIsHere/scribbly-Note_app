package com.example.scribbly.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scribbly.data.preferences.SortMode
import com.example.scribbly.data.preferences.ThemeMode
import com.example.scribbly.ui.theme.LocalNeumorphicColors
import com.example.scribbly.ui.theme.neumorphicSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val neumorphicColors = LocalNeumorphicColors.current
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let { viewModel.exportNotes(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.importNotes(context, it) }
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = neumorphicColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Preferences", style = MaterialTheme.typography.titleMedium, color = neumorphicColors.text, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem(
                title = "Theme",
                subtitle = themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showThemeDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem(
                title = "Sort Notes By",
                subtitle = sortMode.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                onClick = { showSortDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Backup & Sync", style = MaterialTheme.typography.titleMedium, color = neumorphicColors.text, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                title = "Export Notes",
                subtitle = "Save a JSON backup of your notes",
                onClick = { exportLauncher.launch("scribbly_backup.json") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                title = "Import Notes",
                subtitle = "Restore notes from a JSON backup",
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            )

            Spacer(modifier = Modifier.weight(1f))
            Text("About", style = MaterialTheme.typography.titleMedium, color = neumorphicColors.text, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Scribbly", style = MaterialTheme.typography.bodyLarge, color = neumorphicColors.text)
            Text("Developed by Naresh Barua (Ifelseghost)", style = MaterialTheme.typography.bodySmall, color = neumorphicColors.text.copy(alpha=0.7f))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { 
                viewModel.setTheme(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showSortDialog) {
        SortSelectionDialog(
            currentSort = sortMode,
            onSortSelected = {
                viewModel.setSortMode(it)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    val colors = LocalNeumorphicColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicSurface(
                shape = RoundedCornerShape(12.dp),
                cornerRadius = 12.dp,
                surfaceColor = colors.background,
                lightShadowColor = colors.shadowLight,
                darkShadowColor = colors.shadowDark,
                shadowElevation = 4.dp
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.text.copy(alpha=0.7f))
        }
    }
}

@Composable
fun ThemeSelectionDialog(currentTheme: ThemeMode, onThemeSelected: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val colors = LocalNeumorphicColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme", color = colors.text) },
        containerColor = colors.background,
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = mode == currentTheme,
                            onClick = { onThemeSelected(mode) }
                        )
                        Text(mode.name, color = colors.text)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SortSelectionDialog(currentSort: SortMode, onSortSelected: (SortMode) -> Unit, onDismiss: () -> Unit) {
    val colors = LocalNeumorphicColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Notes By", color = colors.text) },
        containerColor = colors.background,
        text = {
            Column {
                SortMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(mode) }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = mode == currentSort,
                            onClick = { onSortSelected(mode) }
                        )
                        Text(mode.name, color = colors.text)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
