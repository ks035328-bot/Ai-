package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AssistantSettingsEntity
import com.example.data.entity.DesktopAppEntity
import com.example.data.entity.VoiceCommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    // Voice Logs
    @Query("SELECT * FROM voice_commands ORDER BY timestamp DESC LIMIT 50")
    fun getAllVoiceCommands(): Flow<List<VoiceCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceCommand(command: VoiceCommandEntity)

    @Query("DELETE FROM voice_commands")
    suspend fun clearVoiceHistory()

    // Desktop Apps
    @Query("SELECT * FROM desktop_apps ORDER BY gridPosition ASC")
    fun getAllDesktopApps(): Flow<List<DesktopAppEntity>>

    @Query("SELECT * FROM desktop_apps WHERE isOpen = 1")
    fun getOpenDesktopApps(): Flow<List<DesktopAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<DesktopAppEntity>)

    @Update
    suspend fun updateApp(app: DesktopAppEntity)

    @Query("UPDATE desktop_apps SET isOpen = :isOpen WHERE id = :id")
    suspend fun setAppOpenState(id: Int, isOpen: Boolean)

    @Query("UPDATE desktop_apps SET isOpen = 0")
    suspend fun closeAllApps()

    @Query("UPDATE desktop_apps SET gridPosition = :position WHERE id = :id")
    suspend fun updateGridPosition(id: Int, position: Int)

    // Settings
    @Query("SELECT * FROM assistant_settings WHERE id = 1")
    fun getSettings(): Flow<AssistantSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AssistantSettingsEntity)
}
