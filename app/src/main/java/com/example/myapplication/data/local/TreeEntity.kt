package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trees")
data class TreeEntity(
    @PrimaryKey var id: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var title: String = "",
    var snippet: String = "",
    var type: String = "",
    var species: String = "Unknown",
    var girth: Double = 0.0,
    var health: String = "Healthy",
    var isEmptyPit: Boolean = false
)
