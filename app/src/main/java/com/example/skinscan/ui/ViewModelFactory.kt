package com.example.skinscan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Small helper so parameterised ViewModels can be created from Compose without
 * a full DI framework:
 *
 * ```
 * val vm: CaptureViewModel = viewModel(factory = viewModelFactory {
 *     CaptureViewModel(repo, sessionId, regionId)
 * })
 * ```
 */
fun <VM : ViewModel> viewModelFactory(create: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }
