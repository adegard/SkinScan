package com.example.skinscan.ui.bodymap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.model.BodyView
import com.example.skinscan.data.repository.SkinMapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BodyMapUiState(
    val sessions: List<SessionEntity> = emptyList(),
    val activeSession: SessionEntity? = null,
    val view: BodyView = BodyView.FRONT,
    val photosByRegion: Map<String, PhotoEntity> = emptyMap(),
) {
    val coveredRegions: Int get() = photosByRegion.size
}

/**
 * Backs the home screen: the "drawing of the body" used to guide capture, and
 * the assembled skin map for the currently selected session (unique per date).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BodyMapViewModel(
    private val repository: SkinMapRepository,
) : ViewModel() {

    private val activeSessionId = MutableStateFlow<Long?>(null)
    private val view = MutableStateFlow(BodyView.FRONT)

    init {
        viewModelScope.launch {
            repository.sessions.collect { sessions ->
                val current = activeSessionId.value
                if (current == null || sessions.none { it.id == current }) {
                    activeSessionId.value = sessions.firstOrNull()?.id
                }
            }
        }
    }

    val uiState: StateFlow<BodyMapUiState> = combine(
        repository.sessions,
        activeSessionId,
        view,
    ) { sessions, sessionId, bodyView ->
        Triple(sessions, sessionId, bodyView)
    }.flatMapLatest { (sessions, sessionId, bodyView) ->
        val active = sessions.firstOrNull { it.id == sessionId }
        if (active == null) {
            flowOf(BodyMapUiState(sessions = sessions, activeSession = null, view = bodyView))
        } else {
            repository.observePhotos(active.id).map { photos ->
                BodyMapUiState(
                    sessions = sessions,
                    activeSession = active,
                    view = bodyView,
                    photosByRegion = photos.associateBy { it.regionId },
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodyMapUiState())

    fun setView(bodyView: BodyView) {
        view.value = bodyView
    }

    fun selectSession(id: Long) {
        activeSessionId.value = id
    }

    fun createSession() {
        viewModelScope.launch {
            activeSessionId.value = repository.createSession()
        }
    }
}
