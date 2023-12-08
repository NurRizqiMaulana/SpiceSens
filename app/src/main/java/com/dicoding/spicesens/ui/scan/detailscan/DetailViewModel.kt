package com.dicoding.spicesens.ui.scan.detailscan

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.spicesens.data.local.database.Spice
import com.dicoding.spicesens.data.repository.SpiceRepository

class DetailViewModel(private val repository: SpiceRepository) : ViewModel() {
    fun getBatikRandom(id: Int): LiveData<Spice?> = repository.getBatikDetail(id)
}