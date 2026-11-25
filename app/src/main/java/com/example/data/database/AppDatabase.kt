package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AssistantDao
import com.example.data.entity.AssistantSettingsEntity
import com.example.data.entity.DesktopAppEntity
import com.example.data.entity.VoiceCommandEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VoiceCommandEntity::class, DesktopAppEntity::class, AssistantSettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assistantDao(): AssistantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lumi_assistant_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Populate default desktop apps & settings
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).assistantDao()
                            dao.saveSettings(AssistantSettingsEntity())
                            dao.insertApps(getDefaultDesktopApps())
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getDefaultDesktopApps(): List<DesktopAppEntity> {
            return listOf(
                DesktopAppEntity(id = 1, appName = "Browser", packageName = "com.android.chrome", category = "Productivity", iconType = "chrome", isOpen = false, gridPosition = 0, ramUsageMb = 240),
                DesktopAppEntity(id = 2, appName = "Camera", packageName = "com.android.camera", category = "System", iconType = "camera", isOpen = false, gridPosition = 1, ramUsageMb = 180),
                DesktopAppEntity(id = 3, appName = "Settings", packageName = "com.android.settings", category = "System", iconType = "settings", isOpen = false, gridPosition = 2, ramUsageMb = 90),
                DesktopAppEntity(id = 4, appName = "Notes", packageName = "com.example.notes", category = "Productivity", iconType = "notes", isOpen = true, gridPosition = 3, ramUsageMb = 65),
                DesktopAppEntity(id = 5, appName = "Gallery", packageName = "com.android.gallery3d", category = "Media", iconType = "gallery", isOpen = false, gridPosition = 4, ramUsageMb = 210),
                DesktopAppEntity(id = 6, appName = "Music Player", packageName = "com.example.music", category = "Media", iconType = "music", isOpen = true, gridPosition = 5, ramUsageMb = 140),
                DesktopAppEntity(id = 7, appName = "Calculator", packageName = "com.android.calculator2", category = "Utility", iconType = "calculator", isOpen = false, gridPosition = 6, ramUsageMb = 45),
                DesktopAppEntity(id = 8, appName = "Files Cabinet", packageName = "com.android.documentsui", category = "Utility", iconType = "files", isOpen = false, gridPosition = 7, ramUsageMb = 110),
                DesktopAppEntity(id = 9, appName = "Task Matrix", packageName = "com.example.tasks", category = "Productivity", iconType = "tasks", isOpen = false, gridPosition = 8, ramUsageMb = 85),
                DesktopAppEntity(id = 10, appName = "Terminal", packageName = "com.example.terminal", category = "Utility", iconType = "terminal", isOpen = false, gridPosition = 9, ramUsageMb = 95),
                DesktopAppEntity(id = 11, appName = "Memory Cleaner", packageName = "com.example.cleaner", category = "System", iconType = "cleaner", isOpen = false, gridPosition = 10, ramUsageMb = 30),
                DesktopAppEntity(id = 12, appName = "Weather Live", packageName = "com.example.weather", category = "Utility", iconType = "weather", isOpen = true, gridPosition = 11, ramUsageMb = 130)
            )
        }
    }
}
