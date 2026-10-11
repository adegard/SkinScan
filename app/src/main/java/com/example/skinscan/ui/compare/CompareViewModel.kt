package com.example.skinscan.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.model.BodyRegion
import com.example.skinscan.data.model.RegionComparison
import com.example.skinscan.data.repository.SkinMapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompareUiState(
    val sessionA: SessionEntity? = null,
    val sessionB: SessionEntity? = null,
    val candidateSessions: List<SessionEntity> = emptyList(),
    val regionIds: List<String> = emptyList(),
    val selectedRegionId: String? = null,
    val comparison: RegionComparison? = null,
)

/**
 * Compares one region at a time between a fixed session A and a chosen session
 * B, matching moles by position and scoring local appearance change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompareViewModel(
    private val repository: SkinMapRepository,
    private val sessionA: Long,
) : ViewModel() {

    private val sessionB = MutableStateFlow<Long?>(null)
    private val selectedRegion = MutableStateFlow<String?>(null)
    private val comparison = MutableStateFlow<RegionComparison?>(null)

    private val regionIds: Flow<List<String>> = sessionB.filterNotNull().flatMapLatest { b ->
        combine(
            repository.observePhotos(sessionA),
            repository.observePhotos(b),
        ) { photosA, photosB ->
            (photosA.map { it.regionId } + photosB.map { it.regionId })
                .distinct()
                .sortedBy { BodyRegion.fromId(it)?.ordinal ?: Int.MAX_VALUE }
        }
    }

    init {
        viewModelScope.launch {
            repository.sessions.collect { sessions ->
                val others = sessions.filter { it.id != sessionA }
                if (sessionB.value == null || others.none { it.id == sessionB.value }) {
                    sessionB.value = others.firstOrNull()?.id
                }
            }
        }
        viewModelScope.launch {
            regionIds.collect { ids ->
                if (selectedRegion.value == null || selectedRegion.value !in ids) {
                    selectedRegion.value = ids.firstOrNull()
                }
            }
        }
        viewModelScope.launch {
            combine(sessionB.filterNotNull(), selectedRegion.filterNotNull()) { b, region ->
                b to region
            }.flatMapLatest { (b, regionId) ->
                val region = BodyRegion.fromId(regionId)
                if (region == null) {
                    flow { emit(null) }
                } else {
                    flow { emit(repository.compareRegion(region, sessionA, b)) }
                }
            }.collect { comparison.value = it }
        }
    }

    val uiState: StateFlow<CompareUiState> = combine(
        repository.observeSession(sessionA),
        repository.sessions,
        sessionB,
        combine(regionIds, selectedRegion, comparison) { ids, selected, cmp ->
            Triple(ids, selected, cmp)
        },
    ) { sessionAEntity, sessions, b, regionState ->
        val (ids, selected, cmp) = regionState
        CompareUiState(
            sessionA = sessionAEntity,
            sessionB = sessions.firstOrNull { it.id == b },
            candidateSessions = sessions.filter { it.id != sessionA },
            regionIds = ids,
            selectedRegionId = selected,
            comparison = cmp,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompareUiState())

    fun selectSessionB(id: Long) {
        sessionB.value = id
    }

    fun selectRegion(id: String) {
        selectedRegion.value = id
    }
}
