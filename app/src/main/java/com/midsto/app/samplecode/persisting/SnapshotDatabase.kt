package com.midsto.app.samplecode.persisting

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Snapshot::class), version = 1)
abstract class SnapshotDatabase : RoomDatabase() {
    abstract fun snapshotDao(): SnapshotDao
}