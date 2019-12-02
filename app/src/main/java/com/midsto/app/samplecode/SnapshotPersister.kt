package com.midsto.app.samplecode

import android.content.Context
import androidx.room.Room
import com.midsto.app.samplecode.persisting.Snapshot
import com.midsto.app.samplecode.persisting.SnapshotDatabase

class SnapshotPersister(applicationContext: Context) {

    private val db = Room.databaseBuilder(
        applicationContext,
        SnapshotDatabase::class.java, "snapshot-database"
    ).build()

    fun persist(appId: String) {
        val timeStamp = System.currentTimeMillis()
        val snapshot = Snapshot(1, timeStamp, appId)
        db.snapshotDao().insertAll(snapshot)
    }

}