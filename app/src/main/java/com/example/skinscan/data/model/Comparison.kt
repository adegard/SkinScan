package com.example.skinscan.data.model

import com.example.skinscan.data.db.entity.LesionEntity
import com.example.skinscan.data.db.entity.PhotoEntity

/** What happened to one mole between two sessions. */
enum class MatchStatus { UNCHANGED, CHANGED, NEW, MISSING }

/**
 * A lesion observed in session A paired (by position) with the corresponding
 * lesion in session B, together with how similar their local patch is.
 * A null side means the lesion exists only in one of the two sessions.
 */
data class LesionMatch(
    val lesionA: LesionEntity?,
    val lesionB: LesionEntity?,
    val similarity: Float,
    val status: MatchStatus,
)

/** Full area-by-area comparison of one [region] between two sessions. */
data class RegionComparison(
    val region: BodyRegion,
    val photoA: PhotoEntity?,
    val photoB: PhotoEntity?,
    val matches: List<LesionMatch>,
) {
    val changedCount: Int
        get() = matches.count {
            it.status == MatchStatus.CHANGED ||
                it.status == MatchStatus.NEW ||
                it.status == MatchStatus.MISSING
        }
}
