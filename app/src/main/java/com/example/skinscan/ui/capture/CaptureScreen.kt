package com.example.skinscan.ui.capture

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.skinscan.SkinScanApplication
import com.example.skinscan.data.db.entity.LesionEntity
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.ui.viewModelFactory
import com.example.skinscan.util.ImageSizes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    sessionId: Long,
    regionId: String,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SkinScanApplication
    val viewModel: CaptureViewModel = viewModel(
        key = "capture-$sessionId-$regionId",
        factory = viewModelFactory {
            CaptureViewModel(app.container.repository, sessionId, regionId)
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        CameraCaptureScreen(
            title = state.region?.label ?: "Area",
            onCaptured = { bitmap ->
                viewModel.saveCapturedPhoto(bitmap)
                showCamera = false
            },
            onCancel = { showCamera = false },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.region?.label ?: "Area") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state.region == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unknown area")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            val photo = state.photo
            if (photo == null) {
                CaptureGuidance(
                    areaLabel = state.region!!.label,
                    onOpenCamera = { showCamera = true },
                    onDemo = viewModel::capture,
                )
            } else {
                AnnotatedPhoto(
                    photo = photo,
                    lesions = state.lesions,
                    onAdd = { x, y -> viewModel.addLesion(x, y) },
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Tap the photo to mark a mole",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${state.lesions.size} mark(s)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { showCamera = true }) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Retake photo")
                }

                Spacer(Modifier.height(16.dp))
                Text("Marked moles", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (state.lesions.isEmpty()) {
                    Text(
                        "No marks yet. Tap the photo to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.lesions.forEachIndexed { index, lesion ->
                        LesionRow(
                            index = index + 1,
                            lesion = lesion,
                            onDelete = { viewModel.deleteLesion(lesion.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureGuidance(
    areaLabel: String,
    onOpenCamera: () -> Unit,
    onDemo: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Photograph: $areaLabel", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Hold the camera straight above this area, fill the frame, and keep the " +
                    "same distance and lighting every session so the comparison stays meaningful.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onOpenCamera, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Take photo")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                Text("Use a demo image instead")
            }
        }
    }
}

@Composable
private fun AnnotatedPhoto(
    photo: PhotoEntity,
    lesions: List<LesionEntity>,
    onAdd: (Float, Float) -> Unit,
) {
    val markerColor = MaterialTheme.colorScheme.primary
    val aspect = remember(photo.id, photo.filePath) {
        ImageSizes.aspectRatio(photo.filePath) ?: 1f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .pointerInput(photo.id) {
                detectTapGestures { offset ->
                    onAdd(
                        (offset.x / size.width.toFloat()).coerceIn(0f, 1f),
                        (offset.y / size.height.toFloat()).coerceIn(0f, 1f),
                    )
                }
            },
    ) {
        AsyncImage(
            model = File(photo.filePath),
            contentDescription = "Photo of the area",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(Modifier.fillMaxSize()) {
            val numberPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
                textSize = size.width * 0.035f
            }
            lesions.forEachIndexed { index, lesion ->
                val center = Offset(lesion.x * size.width, lesion.y * size.height)
                drawCircle(
                    color = markerColor.copy(alpha = 0.22f),
                    radius = lesion.radius * size.width,
                    center = center,
                )
                drawCircle(
                    color = markerColor,
                    radius = size.width * 0.012f,
                    center = center,
                    style = Stroke(width = size.width * 0.006f),
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "${index + 1}",
                    center.x + size.width * 0.02f,
                    center.y - size.width * 0.015f,
                    numberPaint,
                )
            }
        }
    }
}

@Composable
private fun LesionRow(
    index: Int,
    lesion: LesionEntity,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$index",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(48.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = "Position ${(lesion.x * 100).toInt()}% / ${(lesion.y * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
            )
            lesion.diameterMm?.let {
                Text(
                    text = "Approx. $it mm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete mark")
        }
    }
}
