package com.example.skinscan.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinscan.data.db.entity.LesionEntity
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.model.BodyRegion
import com.example.skinscan.data.repository.SkinMapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CaptureUiState(
    val region: BodyRegion? = null,
    val photo: PhotoEntity? = null,
    val lesions: List<LesionEntity> = emptyList(),
    val busy: Boolean = false,
)

/**
 * Drives a single region: guide the user, hold the photo for that region of the
 * current session, and annotate / compare moles found in it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModel(
    private val repository: SkinMapRepository,
    private val sessionId: Long,
    regionId: String,
) : ViewModel() {

    private val region: BodyRegion? = BodyRegion.fromId(regionId)

    private val photoFlow = region?.let { repository.observePhoto(sessionId, it) } ?: flowOf(null)

    private val lesionsFlow = photoFlow.flatMapLatest { photo ->
        if (photo == null) flowOf(emptyList()) else repository.observeLesions(photo.id)
    }

    val uiState: StateFlow<CaptureUiState> = combine(photoFlow, lesionsFlow) { photo, lesions ->
        CaptureUiState(region = region, photo = photo, lesions = lesions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaptureUiState(region = region))

    fun capture() {
        val region = region ?: return
        viewModelScope.launch { repository.captureDemo(sessionId, region) }
    }

    fun saveCapturedPhoto(bitmap: android.graphics.Bitmap) {
        val region = region ?: return
        viewModelScope.launch {
            repository.savePhoto(sessionId, region, bitmap)
            bitmap.recycle()
        }
    }

    fun addLesion(x: Float, y: Float, diameterMm: Float? = null, note: String = "") {
        val photo = uiState.value.photo ?: return
        viewModelScope.launch {
            repository.addLesion(photo = photo, x = x, y = y, diameterMm = diameterMm, note = note)
        }
    }

    fun deleteLesion(id: Long) {
        viewModelScope.launch { repository.deleteLesion(id) }
    }

    fun deletePhoto() {
        val photo = uiState.value.photo ?: return
        viewModelScope.launch { repository.deletePhoto(photo) }
    }
}
