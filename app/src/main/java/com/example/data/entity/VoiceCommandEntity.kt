package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_commands")
data class VoiceCommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commandText: String,
    val actionType: String, // OPEN_APP, CLOSE_APP, ORGANIZE_DESKTOP, CLEAN_MEMORY, GENERAL_CHAT, JOKE
    val responseText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)
