package com.midsto.app.samplecode.persisting

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Snapshot(
    @PrimaryKey val uid: Int,
    val timestamp: Long,
    @ColumnInfo(name = "app_id") val appId: String
)