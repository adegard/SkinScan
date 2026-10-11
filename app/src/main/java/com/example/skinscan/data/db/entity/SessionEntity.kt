package com.example.skinscan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One "skin map" session. The whole point of the app is that a session is a
 * unique snapshot per date: you assemble a full-body set of photos, then later
 * open that same session to compare it area-by-area with another date.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val createdAt: Long,
    val label: String,
    val note: String = "",
)
