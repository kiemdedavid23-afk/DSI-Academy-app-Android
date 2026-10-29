package app.dsiacademy.academy.feature.member.academy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.data.content.ContentRepository
import app.dsiacademy.academy.domain.content.ContentCategory
import app.dsiacademy.academy.domain.content.ContentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HouseCatalogUiState(
    val title: String = "",
    val type: String = "book",
    val items: List<ContentItem> = emptyList(),
    val categories: List<ContentCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class HouseCatalogViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val type: String = savedStateHandle["type"] ?: "book"
    private val title: String = savedStateHandle["title"] ?: "Catalogue"

    private val _state = MutableStateFlow(HouseCatalogUiState(title = title, type = type))
    val state: StateFlow<HouseCatalogUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            when (val r = contentRepository.getCategories(type)) {
                is DsiResult.Success -> _state.update { it.copy(categories = r.data) }
                else -> Unit
            }
        }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun search() {
        val q = _state.value.query.trim()
        if (q.isEmpty()) {
            refresh()
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = contentRepository.search(q, type)) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Recherche impossible.")
                }
                is DsiResult.Success -> _state.update {
                    it.copy(loading = false, items = r.data.items)
                }
            }
        }
    }

    fun selectCategory(id: String?) {
        _state.update { it.copy(selectedCategoryId = id) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            // category filter via catalog query if API supports — load all then client filter if needed
            when (val r = contentRepository.getCatalog(type, categoryId = _state.value.selectedCategoryId)) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Impossible de récupérer le catalogue.")
                }
                is DsiResult.Success -> {
                    var items = r.data.items
                    // if category selected and items carry category — best effort
                    _state.update { it.copy(loading = false, items = items) }
                }
            }
        }
    }
}
