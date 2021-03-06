package com.midsto.app.samplecode.persisting

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

interface SnapshotDao {

    @Query("SELECT * FROM snapshot")
    fun loadAll(): List<Snapshot>

    @Query("SELECT * FROM snapshot WHERE app_id LIKE :appId")
    fun loadAllByAppId(appId: String): List<Snapshot>

    @Insert
    fun insertAll(vararg snapshots: Snapshot)

    @Delete
    fun delete(snapshot: Snapshot)

}