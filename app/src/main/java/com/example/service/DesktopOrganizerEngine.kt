package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.data.entity.DesktopAppEntity
import com.example.data.repository.AssistantRepository

class DesktopOrganizerEngine(
    private val context: Context,
    private val repository: AssistantRepository
) {

    suspend fun executeAction(
        actionType: String,
        targetApp: String?,
        allApps: List<DesktopAppEntity>
    ): Pair<String, Int> { // Returns Pair(StatusSummary, FreerRamMb)
        return when (actionType) {
            "OPEN_APP" -> {
                val app = findApp(targetApp, allApps)
                if (app != null) {
                    repository.setAppOpenState(app.id, true)
                    launchSystemIntentForApp(app)
                    Pair("Opened ${app.appName} on desktop grid.", 0)
                } else {
                    Pair("App '$targetApp' not found on desktop.", 0)
                }
            }
            "CLOSE_APP" -> {
                val app = findApp(targetApp, allApps)
                if (app != null) {
                    repository.setAppOpenState(app.id, false)
                    Pair("Closed ${app.appName} and freed ${app.ramUsageMb} MB RAM.", app.ramUsageMb)
                } else {
                    // Close the first open app as fallback
                    val openApp = allApps.firstOrNull { it.isOpen }
                    if (openApp != null) {
                        repository.setAppOpenState(openApp.id, false)
                        Pair("Closed ${openApp.appName} window.", openApp.ramUsageMb)
                    } else {
                        Pair("No active desktop window to close.", 0)
                    }
                }
            }
            "CLOSE_ALL" -> {
                val openApps = allApps.filter { it.isOpen }
                val freedRam = openApps.sumOf { it.ramUsageMb }
                repository.closeAllApps()
                Pair("Closed all ${openApps.size} active windows! Freed $freedRam MB RAM.", freedRam)
            }
            "ORGANIZE_DESKTOP" -> {
                // Sort apps by category then name
                val sorted = allApps.sortedWith(compareBy({ it.category }, { it.appName }))
                repository.rearrangeGrid(sorted)
                Pair("Desktop icons auto-organized into neat category folders!", 0)
            }
            "CLEAN_MEMORY" -> {
                val openApps = allApps.filter { it.isOpen && !it.isPinned }
                val freedRam = openApps.sumOf { it.ramUsageMb } + 180
                openApps.forEach { repository.setAppOpenState(it.id, false) }
                Pair("Memory optimized! Terminated background tasks, freed $freedRam MB RAM.", freedRam)
            }
            else -> Pair("Action completed successfully.", 0)
        }
    }

    private fun findApp(target: String?, apps: List<DesktopAppEntity>): DesktopAppEntity? {
        if (target.isNullOrBlank()) return apps.firstOrNull()
        val query = target.lowercase()
        return apps.firstOrNull {
            it.appName.lowercase().contains(query) ||
            query.contains(it.appName.lowercase()) ||
            (query.contains("chrome") && it.appName.lowercase().contains("browser")) ||
            (query.contains("cam") && it.appName.lowercase().contains("camera"))
        }
    }

    private fun launchSystemIntentForApp(app: DesktopAppEntity) {
        try {
            val intent = when (app.iconType) {
                "camera" -> Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                "settings" -> Intent(Settings.ACTION_SETTINGS)
                "chrome" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                "weather" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://weather.com"))
                else -> context.packageManager.getLaunchIntentForPackage(app.packageName)
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
