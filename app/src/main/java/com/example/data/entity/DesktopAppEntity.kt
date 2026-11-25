package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desktop_apps")
data class DesktopAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val category: String, // Productivity, Social, Utility, System, Media, Games
    val iconType: String, // camera, chrome, settings, notes, gallery, music, calculator, files, tasks, terminal, cleaner, weather
    val isOpen: Boolean = false,
    val isPinned: Boolean = true,
    val gridPosition: Int = 0,
    val ramUsageMb: Int = 120
)
