package com.example.skinscan.ui.bodymap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skinscan.SkinScanApplication
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.model.BodyRegion
import com.example.skinscan.data.model.BodyView
import com.example.skinscan.ui.viewModelFactory

private const val DISCLAIMER =
    "Personal tracking aid only - not a medical device. Show any changing, " +
        "bleeding, itching or new mole to a dermatologist."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMapScreen(
    onOpenRegion: (Long, String) -> Unit,
    onOpenSessions: () -> Unit,
    onOpenSession: (Long) -> Unit,
) {
    val app = LocalContext.current.applicationContext as SkinScanApplication
    val viewModel: BodyMapViewModel = viewModel(
        factory = viewModelFactory { BodyMapViewModel(app.container.repository) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SkinScan") },
                actions = {
                    IconButton(onClick = onOpenSessions) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "All sessions")
                    }
                },
            )
        },
    ) { padding ->
        val session = state.activeSession
        if (session == null) {
            EmptySessions(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                onCreate = viewModel::createSession,
            )
        } else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SessionSelector(
                    state = state,
                    onSelect = viewModel::selectSession,
                    onOpen = onOpenSession,
                    onCreate = viewModel::createSession,
                )

                Spacer(Modifier.height(12.dp))

                ViewToggle(current = state.view, onSelect = viewModel::setView)

                Spacer(Modifier.height(8.dp))

                val total = BodyRegion.forView(state.view).size
                Text(
                    text = "${state.coveredRegions} of $total areas photographed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    BodyDiagram(
                        view = state.view,
                        photosByRegion = state.photosByRegion,
                        onRegionClick = { region -> onOpenRegion(session.id, region.id) },
                        modifier = Modifier.fillMaxHeight().aspectRatio(0.5f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = DISCLAIMER,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun EmptySessions(
    modifier: Modifier = Modifier,
    onCreate: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No skin maps yet", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "A skin map is a full set of photos for one date. Create one to start " +
                "mapping your moles.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCreate) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create first skin map")
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = DISCLAIMER,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SessionSelector(
    state: BodyMapUiState,
    onSelect: (Long) -> Unit,
    onOpen: (Long) -> Unit,
    onCreate: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val current = state.activeSession

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Skin map", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(current?.label ?: "-", style = MaterialTheme.typography.titleLarge)
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch skin map")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        state.sessions.forEach { session: SessionEntity ->
                            DropdownMenuItem(
                                text = { Text(session.label) },
                                onClick = {
                                    expanded = false
                                    onSelect(session.id)
                                },
                            )
                        }
                    }
                }
            }
        }
        current?.let {
            IconButton(onClick = { onOpen(it.id) }) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open skin map")
            }
        }
        IconButton(onClick = onCreate) {
            Icon(Icons.Filled.Add, contentDescription = "New skin map")
        }
    }
}

@Composable
private fun ViewToggle(
    current: BodyView,
    onSelect: (BodyView) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BodyView.entries.forEach { bodyView ->
            FilterChip(
                selected = current == bodyView,
                onClick = { onSelect(bodyView) },
                label = { Text(bodyView.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}
