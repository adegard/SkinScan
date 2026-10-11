package com.example.skinscan.ui.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.skinscan.SkinScanApplication
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.db.entity.SessionEntity
import com.example.skinscan.data.model.LesionMatch
import com.example.skinscan.data.model.MatchStatus
import com.example.skinscan.data.model.RegionComparison
import com.example.skinscan.ui.sessions.formatDate
import com.example.skinscan.ui.theme.Ok
import com.example.skinscan.ui.theme.OkContainer
import com.example.skinscan.ui.theme.Warning
import com.example.skinscan.ui.theme.WarningContainer
import com.example.skinscan.ui.viewModelFactory
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    sessionA: Long,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SkinScanApplication
    val viewModel: CompareViewModel = viewModel(
        key = "compare-$sessionA",
        factory = viewModelFactory { CompareViewModel(app.container.repository, sessionA) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compare areas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (state.candidateSessions.isEmpty()) {
                Text(
                    "There is no other skin map to compare with yet. Take a new set of " +
                        "photos on a later date and open this screen again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            SessionSelectorRow(
                label = "Compare",
                sessions = state.candidateSessions,
                selected = state.sessionB,
                onSelect = { viewModel.selectSessionB(it.id) },
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "${state.sessionA?.label ?: "A"}  vs  ${state.sessionB?.label ?: "B"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            if (state.regionIds.isEmpty()) {
                Text(
                    "These two skin maps do not share any photographed area yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.regionIds.forEach { regionId ->
                    val label = com.example.skinscan.data.model.BodyRegion.fromId(regionId)?.label ?: regionId
                    FilterChip(
                        selected = state.selectedRegionId == regionId,
                        onClick = { viewModel.selectRegion(regionId) },
                        label = { Text(label) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val comparison = state.comparison
            if (comparison == null) {
                Text(
                    "Select an area to compare.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ComparisonBody(comparison)
            }
        }
    }
}

@Composable
private fun ComparisonBody(comparison: RegionComparison) {
    Text(comparison.region.label, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(4.dp))
    Text(
        text = "${comparison.matches.size} mole(s) - ${comparison.changedCount} need attention",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ComparisonPane(
            photo = comparison.photoA,
            caption = "First date",
            modifier = Modifier.weight(1f),
        )
        ComparisonPane(
            photo = comparison.photoB,
            caption = "Later date",
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(Modifier.height(16.dp))
    Text("Moles", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    if (comparison.matches.isEmpty()) {
        Text(
            "No moles were marked in this area.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        comparison.matches.forEachIndexed { index, match ->
            MatchRow(index = index + 1, match = match)
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        "Change is estimated on-device from the local photo patch. Any result with a " +
            "low appearance match, a new mole, or one that seems to have disappeared " +
            "should be checked by a dermatologist.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ComparisonPane(
    photo: PhotoEntity?,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (photo != null) {
                AsyncImage(
                    model = File(photo.filePath),
                    contentDescription = caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text("No photo", style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(caption, style = MaterialTheme.typography.labelMedium)
        photo?.let {
            Text(
                formatDate(it.capturedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MatchRow(index: Int, match: LesionMatch) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor(match.status))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = statusLabel(match.status),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusTextColor(match.status),
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Mole #$index", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = positionText(match),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (match.lesionA != null && match.lesionB != null) {
                    Text(
                        text = "Appearance match ${(match.similarity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionSelectorRow(
    label: String,
    sessions: List<SessionEntity>,
    selected: SessionEntity?,
    onSelect: (SessionEntity) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium)
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selected?.label ?: "Choose")
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                sessions.forEach { session ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(session.label) },
                        onClick = {
                            expanded = false
                            onSelect(session)
                        },
                    )
                }
            }
        }
    }
}

private fun positionText(match: LesionMatch): String {
    val a = match.lesionA
    val b = match.lesionB
    return when {
        a != null && b != null ->
            "A ${pct(a.x)}/${pct(a.y)}  ->  B ${pct(b.x)}/${pct(b.y)}"
        a != null -> "Only in first date"
        else -> "Only in later date"
    }
}

private fun pct(value: Float): String = "${(value * 100).roundToInt()}%"

private fun statusLabel(status: MatchStatus): String = when (status) {
    MatchStatus.UNCHANGED -> "SAME"
    MatchStatus.CHANGED -> "CHANGED"
    MatchStatus.NEW -> "NEW"
    MatchStatus.MISSING -> "GONE"
}

@Composable
private fun statusColor(status: MatchStatus): Color = when (status) {
    MatchStatus.UNCHANGED -> OkContainer
    MatchStatus.CHANGED -> WarningContainer
    MatchStatus.NEW -> MaterialTheme.colorScheme.primaryContainer
    MatchStatus.MISSING -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun statusTextColor(status: MatchStatus): Color = when (status) {
    MatchStatus.UNCHANGED -> Ok
    MatchStatus.CHANGED -> Warning
    MatchStatus.NEW -> MaterialTheme.colorScheme.onPrimaryContainer
    MatchStatus.MISSING -> MaterialTheme.colorScheme.onSurfaceVariant
}
