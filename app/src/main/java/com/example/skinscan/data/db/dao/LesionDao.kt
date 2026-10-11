package com.example.skinscan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.skinscan.data.db.entity.LesionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LesionDao {

    @Insert
    suspend fun insert(lesion: LesionEntity): Long

    @Query("SELECT * FROM lesions WHERE photoId = :photoId ORDER BY createdAt ASC")
    fun observeForPhoto(photoId: Long): Flow<List<LesionEntity>>

    @Query("SELECT * FROM lesions WHERE sessionId = :sessionId AND regionId = :regionId ORDER BY createdAt ASC")
    fun observeForSessionRegion(sessionId: Long, regionId: String): Flow<List<LesionEntity>>

    @Query("SELECT * FROM lesions WHERE sessionId = :sessionId AND regionId = :regionId ORDER BY createdAt ASC")
    suspend fun getForSessionRegion(sessionId: Long, regionId: String): List<LesionEntity>

    @Query("SELECT COUNT(*) FROM lesions WHERE sessionId = :sessionId")
    fun observeCountForSession(sessionId: Long): Flow<Int>

    @Query("DELETE FROM lesions WHERE id = :id")
    suspend fun delete(id: Long)
}
