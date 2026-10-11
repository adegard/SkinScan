package com.example.skinscan.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A marked mole / lesion inside a [PhotoEntity]. [x] and [y] are normalised
 * (0..1) inside the photo so that a lesion keeps its position no matter the
 * display size. [radius] is a normalised crop radius around the mark.
 *
 * [signature] is an average-hash of the cropped patch around the mark; the
 * cropping + hashing happens on-device. Comparing the signature of the same
 * area across two sessions tells us whether that area changed.
 *
 * Lesions from different sessions in the same region are matched to each other
 * by nearest [x]/[y] position (see [com.example.skinscan.data.repository.SkinMapRepository]).
 */
@Entity(
    tableName = "lesions",
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("photoId"), Index("sessionId"), Index("regionId")],
)
data class LesionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val photoId: Long,
    val sessionId: Long,
    val regionId: String,
    val x: Float,
    val y: Float,
    val radius: Float = 0.05f,
    val diameterMm: Float? = null,
    val label: String = "",
    val note: String = "",
    val signature: Long = 0L,
    val createdAt: Long,
)
