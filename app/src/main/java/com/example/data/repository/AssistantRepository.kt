package com.example.data.repository

import com.example.data.dao.AssistantDao
import com.example.data.entity.AssistantSettingsEntity
import com.example.data.entity.DesktopAppEntity
import com.example.data.entity.VoiceCommandEntity
import kotlinx.coroutines.flow.Flow

class AssistantRepository(private val dao: AssistantDao) {

    val voiceCommands: Flow<List<VoiceCommandEntity>> = dao.getAllVoiceCommands()
    val desktopApps: Flow<List<DesktopAppEntity>> = dao.getAllDesktopApps()
    val openApps: Flow<List<DesktopAppEntity>> = dao.getOpenDesktopApps()
    val settings: Flow<AssistantSettingsEntity?> = dao.getSettings()

    suspend fun logVoiceCommand(command: String, actionType: String, response: String, isSuccess: Boolean = true) {
        dao.insertVoiceCommand(
            VoiceCommandEntity(
                commandText = command,
                actionType = actionType,
                responseText = response,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearVoiceHistory()
    }

    suspend fun setAppOpenState(id: Int, isOpen: Boolean) {
        dao.setAppOpenState(id, isOpen)
    }

    suspend fun closeAllApps() {
        dao.closeAllApps()
    }

    suspend fun updateApp(app: DesktopAppEntity) {
        dao.updateApp(app)
    }

    suspend fun saveSettings(settings: AssistantSettingsEntity) {
        dao.saveSettings(settings)
    }

    suspend fun rearrangeGrid(apps: List<DesktopAppEntity>) {
        apps.forEachIndexed { index, app ->
            dao.updateGridPosition(app.id, index)
        }
    }
}
