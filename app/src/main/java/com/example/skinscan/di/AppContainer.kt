package com.example.skinscan.di

import android.content.Context
import com.example.skinscan.data.db.SkinScanDatabase
import com.example.skinscan.data.repository.SkinMapRepository
import com.example.skinscan.data.storage.PhotoStore

/**
 * Tiny hand-rolled dependency container. Keeps the app free of a DI framework
 * while still exposing a single [repository] for the ViewModels.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: SkinScanDatabase by lazy { SkinScanDatabase.get(appContext) }

    private val photoStore: PhotoStore by lazy { PhotoStore(appContext) }

    val repository: SkinMapRepository by lazy {
        SkinMapRepository(
            sessionDao = database.sessionDao(),
            photoDao = database.photoDao(),
            lesionDao = database.lesionDao(),
            photoStore = photoStore,
        )
    }
}
