package com.example.scribbly.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorViewModelTest {

    @Test
    fun clearingExistingTitle_keepsTitleBlank() {
        val state = EditorUiState(
            id = 1,
            title = "",
            content = "Old title\nBody text"
        )

        assertEquals("", resolveTitleForSave(state))
    }

    @Test
    fun newNoteStillAutogeneratesTitleFromContent() {
        val state = EditorUiState(
            id = null,
            title = "",
            content = "Shopping list\nMilk\nEggs"
        )

        assertEquals("Shopping list", resolveTitleForSave(state))
    }
}



