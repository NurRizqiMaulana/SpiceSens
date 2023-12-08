package com.dicoding.spicesens.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.dicoding.spicesens.data.local.database.Spice
import com.dicoding.spicesens.data.local.database.SpiceDao
import com.dicoding.spicesens.data.local.database.SpiceRoomDatabase

class SpiceRepository(application: Application) {

    private val mSpiceDao: SpiceDao

    init {
        val db = SpiceRoomDatabase.getDatabase(application)
        mSpiceDao = db.spiceDao()
    }

    fun getBatikDetail(id: Int): LiveData<Spice?> = mSpiceDao.getBatikDetail(id)

    companion object {
        @Volatile
        private var INSTANCE: SpiceRepository? = null

        fun getInstance(application: Application): SpiceRepository {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = SpiceRepository(application)
                }
                return INSTANCE as SpiceRepository
            }
        }
    }

}