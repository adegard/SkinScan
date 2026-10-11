package com.example.skinscan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.skinscan.data.db.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Update
    suspend fun update(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId ORDER BY capturedAt ASC")
    fun observeForSession(sessionId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId AND regionId = :regionId LIMIT 1")
    fun observeForRegion(sessionId: Long, regionId: String): Flow<PhotoEntity?>

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId AND regionId = :regionId LIMIT 1")
    suspend fun getForRegion(sessionId: Long, regionId: String): PhotoEntity?

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun get(id: Long): PhotoEntity?

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)
}
