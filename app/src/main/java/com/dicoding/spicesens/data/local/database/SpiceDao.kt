package com.dicoding.spicesens.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SpiceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(spice: Spice)

    @Query("SELECT * FROM spices WHERE id = :spiceId")
    fun getBatikDetail(spiceId: Int): LiveData<Spice?>
}