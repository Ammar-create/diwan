package com.ammar.diwan.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.data.preferences.PreferencesManager
import com.ammar.diwan.data.sample.SamplePoems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val poems: List<Poem> = SamplePoems.all,
    val selectedPoemForExport: Poem? = null,
    val isExportSheetOpen: Boolean = false,
    val statusToast: String? = null
)

class DashboardViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val showRomanUrdu: StateFlow<Boolean> = preferencesManager.showRoman.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun toggleRomanUrdu() {
        viewModelScope.launch {
            preferencesManager.toggleShowRoman()
        }
    }

    fun addPoemToFolio(poem: Poem) {
        _uiState.update { current ->
            if (current.poems.any { it.id == poem.id }) {
                current
            } else {
                current.copy(
                    poems = listOf(poem) + current.poems,
                    statusToast = "Saved to Personal Folio"
                )
            }
        }
    }

    fun addScannedPoem(poem: Poem) {
        addPoemToFolio(poem)
    }

    fun deletePoem(poemId: String) {
        _uiState.update { current ->
            current.copy(
                poems = current.poems.filterNot { it.id == poemId },
                statusToast = "Poem removed from collection"
            )
        }
    }

    fun openExportSheet(poem: Poem) {
        _uiState.update { it.copy(selectedPoemForExport = poem, isExportSheetOpen = true) }
    }

    fun closeExportSheet() {
        _uiState.update { it.copy(isExportSheetOpen = false, selectedPoemForExport = null) }
    }

    fun copyPoemToClipboard(context: Context, poem: Poem) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = buildString {
            appendLine("${poem.titleUrdu} — ${poem.poetUrdu}")
            appendLine()
            poem.verses.forEach {
                appendLine(it.urdu)
                appendLine(it.roman)
                appendLine()
            }
        }
        val clip = ClipData.newPlainText("Poem", text)
        clipboard.setPrimaryClip(clip)
        _uiState.update { it.copy(statusToast = "Verses copied to clipboard") }
    }

    fun clearToast() {
        _uiState.update { it.copy(statusToast = null) }
    }
}
