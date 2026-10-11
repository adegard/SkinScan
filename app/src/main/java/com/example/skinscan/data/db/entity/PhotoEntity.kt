package com.example.skinscan.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single photo belonging to a session, taken of one explicit [regionId].
 * The region reference is what guarantees the "exact position" of every shot.
 *
 * [signature] is an on-device perceptual hash of the whole photo (average hash),
 * used as a cheap whole-area fingerprint.
 */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("regionId")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val regionId: String,
    val filePath: String,
    val capturedAt: Long,
    val signature: Long = 0L,
    val note: String = "",
)
