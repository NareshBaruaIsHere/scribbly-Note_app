package com.example.scribbly.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scribbly.ui.archive.ArchiveScreen
import com.example.scribbly.ui.archive.ArchiveViewModel
import com.example.scribbly.ui.editor.EditorScreen
import com.example.scribbly.ui.editor.EditorViewModel
import com.example.scribbly.ui.home.HomeScreen
import com.example.scribbly.ui.home.HomeViewModel
import com.example.scribbly.ui.settings.SettingsScreen
import com.example.scribbly.ui.settings.SettingsViewModel

@Composable
fun ScribblyNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToEditor = { noteId ->
                    if (noteId != null) {
                        navController.navigate("editor/$noteId")
                    } else {
                        navController.navigate("editor/-1") // Use -1 for new note
                    }
                },
                onNavigateToArchive = {
                    navController.navigate("archive")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteIdLong = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val noteId = if (noteIdLong == -1L) null else noteIdLong
            
            val editorViewModel: EditorViewModel = viewModel(factory = AppViewModelProvider.Factory)
            editorViewModel.initNote(noteId)

            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("archive") {
            val archiveViewModel: ArchiveViewModel = viewModel(factory = ArchiveViewModel.Factory)
            ArchiveScreen(
                viewModel = archiveViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
