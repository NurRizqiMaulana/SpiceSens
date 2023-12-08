package com.dicoding.spicesens.ui.scan.detailscan

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.spicesens.data.repository.SpiceRepository

class DetailViewModelFactory private constructor(private val repository: SpiceRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(repository) as T
            }
            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }

    companion object {
        @Volatile
        private var INSTANCE: DetailViewModelFactory? = null

        fun getInstance(application: Application): DetailViewModelFactory =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DetailViewModelFactory(
                    SpiceRepository.getInstance(application)
                )
            }
    }
}