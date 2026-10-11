package com.example.skinscan.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.repository.SkinMapRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionListViewModel(
    private val repository: SkinMapRepository,
) : ViewModel() {

    val sessions: StateFlow<List<SessionEntity>> = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createSession() {
        viewModelScope.launch { repository.createSession() }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteSession(id) }
    }
}
