package com.example.skinscan.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.repository.SkinMapRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SessionDetailUiState(
    val session: SessionEntity? = null,
    val photos: List<PhotoEntity> = emptyList(),
    val lesionCount: Int = 0,
)

/** Opens a saved session (one date's skin map) for review. */
class SessionDetailViewModel(
    repository: SkinMapRepository,
    sessionId: Long,
) : ViewModel() {

    val uiState: StateFlow<SessionDetailUiState> = combine(
        repository.observeSession(sessionId),
        repository.observePhotos(sessionId),
        repository.observeLesionCount(sessionId),
    ) { session, photos, count ->
        SessionDetailUiState(session = session, photos = photos, lesionCount = count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionDetailUiState())
}
