package com.dicoding.spicesens.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dicoding.spicesens.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executors

@Database(entities = [Spice::class], version = 1)
abstract class SpiceRoomDatabase : RoomDatabase() {
    abstract fun spiceDao(): SpiceDao

    companion object {
        @Volatile
        private var INSTANCE: SpiceRoomDatabase? = null
        @JvmStatic
        fun getDatabase(context: Context): SpiceRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpiceRoomDatabase::class.java,
                    "spice.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object: Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            INSTANCE?.let {
                                Executors.newSingleThreadExecutor().execute {
                                    fillWithStartingData(context.applicationContext, it.spiceDao())
                                }
                            }
                        }
                    }).build()

                INSTANCE = instance
                instance
            }

        }
        private fun fillWithStartingData(context: Context, dao: SpiceDao) {
            val jsonArray = loadJsonArray(context)

            try {
                if (jsonArray != null) {
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        dao.insert(
                            Spice(
                                item.getInt("id"),
                                item.getString("name"),
                                item.getString("description")
                            )
                        )
                    }
                }
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
        }

        private fun loadJsonArray(context: Context): JSONArray? {
            val builder = StringBuilder()
            val `in` = context.resources.openRawResource(R.raw.spice)
            val reader = BufferedReader(InputStreamReader(`in`))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                val json = JSONObject(builder.toString())
                return json.getJSONArray("spices")
            } catch (exception: IOException) {
                exception.printStackTrace()
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
            return null
        }

    }

}