package com.example.skinscan.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.skinscan.data.db.dao.LesionDao
import com.example.skinscan.data.db.dao.PhotoDao
import com.example.skinscan.data.db.dao.SessionDao
import com.example.skinscan.data.db.entity.LesionEntity
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.db.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, PhotoEntity::class, LesionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SkinScanDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun photoDao(): PhotoDao
    abstract fun lesionDao(): LesionDao

    companion object {
        @Volatile
        private var INSTANCE: SkinScanDatabase? = null

        fun get(context: Context): SkinScanDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkinScanDatabase::class.java,
                    "skinscan.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
