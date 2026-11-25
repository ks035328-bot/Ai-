package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_settings")
data class AssistantSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val personaName: String = "Lumi",
    val personaTitle: String = "Desktop Organizer AI",
    val voicePitch: Float = 1.1f,
    val voiceSpeed: Float = 1.0f,
    val desktopMode: String = "WORK_GRID", // WORK_GRID, MINIMAL, CATEGORIZED, GAMING
    val autoCleanMemory: Boolean = true,
    val speechFeedbackEnabled: Boolean = true
)
