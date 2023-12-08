package com.dicoding.spicesens.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {

    private val _image = MutableLiveData<Bitmap?>()
    val image: LiveData<Bitmap?> = _image

    init {
        _image.value = null
    }

    fun setImage(img: Bitmap?) {
        _image.value = img
    }
}