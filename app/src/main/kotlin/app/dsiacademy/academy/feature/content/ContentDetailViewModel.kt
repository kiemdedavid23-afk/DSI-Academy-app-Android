package app.dsiacademy.academy.feature.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.data.content.ContentRepository
import app.dsiacademy.academy.domain.content.ContentItem
import app.dsiacademy.academy.domain.content.FormationNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentDetailUiState(
    val item: ContentItem? = null,
    val tree: FormationNode? = null,
    val downloadable: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ContentDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contentId: String = savedStateHandle["contentId"] ?: ""
    private val type: String = savedStateHandle["type"] ?: "book"

    private val _state = MutableStateFlow(ContentDetailUiState())
    val state: StateFlow<ContentDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            if (type == "training") {
                when (val r = contentRepository.getTree(contentId)) {
                    is DsiResult.Error -> _state.update {
                        it.copy(loading = false, error = r.error.message ?: "Formation introuvable.")
                    }
                    is DsiResult.Success -> _state.update {
                        it.copy(loading = false, tree = r.data, item = ContentItem(
                            id = r.data.id, title = r.data.title, slug = null,
                            type = "training", summary = null, coverUrl = null,
                        ))
                    }
                }
            } else {
                when (val r = contentRepository.getById(contentId)) {
                    is DsiResult.Error -> _state.update {
                        it.copy(loading = false, error = r.error.message ?: "Contenu introuvable.")
                    }
                    is DsiResult.Success -> {
                        val dl = contentRepository.isDownloadable(contentId)
                        _state.update { it.copy(loading = false, item = r.data, downloadable = dl) }
                    }
                }
            }
        }
    }
}
