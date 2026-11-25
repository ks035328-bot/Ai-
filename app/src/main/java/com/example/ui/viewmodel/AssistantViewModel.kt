package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.AssistantSettingsEntity
import com.example.data.entity.DesktopAppEntity
import com.example.data.entity.VoiceCommandEntity
import com.example.data.remote.GeminiClient
import com.example.data.repository.AssistantRepository
import com.example.service.DesktopOrganizerEngine
import com.example.service.VoiceVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AvatarMood {
    NEUTRAL,
    LISTENING,
    THINKING,
    SPEAKING,
    HAPPY
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AssistantRepository
    private val organizerEngine: DesktopOrganizerEngine
    private var voiceManager: VoiceVoiceManager? = null

    val desktopApps: StateFlow<List<DesktopAppEntity>>
    val voiceLogs: StateFlow<List<VoiceCommandEntity>>
    val settings: StateFlow<AssistantSettingsEntity>

    private val _voiceState = MutableStateFlow(VoiceVoiceManager.VoiceState.IDLE)
    val voiceState: StateFlow<VoiceVoiceManager.VoiceState> = _voiceState.asStateFlow()

    private val _avatarMood = MutableStateFlow(AvatarMood.NEUTRAL)
    val avatarMood: StateFlow<AvatarMood> = _avatarMood.asStateFlow()

    private val _speechText = MutableStateFlow("Hi master! I'm Lumi, your desktop assistant girl. What would you like to open or organize today?")
    val speechText: StateFlow<String> = _speechText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastActionSummary = MutableStateFlow<String?>(null)
    val lastActionSummary: StateFlow<String?> = _lastActionSummary.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Desktop Grid, 1: Active Apps, 2: Voice Log, 3: Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val totalRamUsedMb: StateFlow<Int>

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.assistantDao()
        repository = AssistantRepository(dao)
        organizerEngine = DesktopOrganizerEngine(application, repository)

        desktopApps = repository.desktopApps
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        voiceLogs = repository.voiceCommands
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        settings = repository.settings
            .combine(MutableStateFlow(AssistantSettingsEntity())) { saved, default ->
                saved ?: default
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssistantSettingsEntity())

        totalRamUsedMb = desktopApps.combine(MutableStateFlow(0)) { apps, _ ->
            apps.filter { it.isOpen }.sumOf { it.ramUsageMb } + 380
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 520)

        initVoiceManager()
    }

    private fun initVoiceManager() {
        voiceManager = VoiceVoiceManager(
            context = getApplication(),
            onResult = { recognizedText ->
                processUserCommand(recognizedText)
            },
            onError = { error ->
                _errorMessage.value = error
                _avatarMood.value = AvatarMood.NEUTRAL
                _voiceState.value = VoiceVoiceManager.VoiceState.IDLE
            },
            onStateChanged = { state ->
                _voiceState.value = state
                _avatarMood.value = when (state) {
                    VoiceVoiceManager.VoiceState.LISTENING -> AvatarMood.LISTENING
                    VoiceVoiceManager.VoiceState.THINKING -> AvatarMood.THINKING
                    VoiceVoiceManager.VoiceState.SPEAKING -> AvatarMood.SPEAKING
                    VoiceVoiceManager.VoiceState.IDLE -> AvatarMood.NEUTRAL
                }
            }
        )
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun toggleListening() {
        if (_voiceState.value == VoiceVoiceManager.VoiceState.LISTENING) {
            voiceManager?.stopListening()
        } else {
            _errorMessage.value = null
            voiceManager?.startListening()
        }
    }

    fun processUserCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            _voiceState.value = VoiceVoiceManager.VoiceState.THINKING
            _avatarMood.value = AvatarMood.THINKING
            _errorMessage.value = null

            val currentSettings = settings.value
            val openAppNames = desktopApps.value.filter { it.isOpen }.map { it.appName }

            val intentResult = GeminiClient.processVoiceCommand(
                userPrompt = command,
                personaName = currentSettings.personaName,
                openAppsList = openAppNames
            )

            // Execute desktop action
            val (summary, freedRam) = organizerEngine.executeAction(
                actionType = intentResult.actionType,
                targetApp = intentResult.targetApp,
                allApps = desktopApps.value
            )

            _lastActionSummary.value = summary
            _speechText.value = intentResult.responseSpeech
            _avatarMood.value = AvatarMood.HAPPY

            // Speak out loud if enabled
            if (currentSettings.speechFeedbackEnabled) {
                voiceManager?.configureVoice(currentSettings.voicePitch, currentSettings.voiceSpeed)
                voiceManager?.speak(intentResult.responseSpeech)
            } else {
                _voiceState.value = VoiceVoiceManager.VoiceState.IDLE
            }

            // Log command
            repository.logVoiceCommand(
                command = command,
                actionType = intentResult.actionType,
                response = intentResult.responseSpeech,
                isSuccess = true
            )
        }
    }

    fun toggleAppOpen(app: DesktopAppEntity) {
        viewModelScope.launch {
            val newState = !app.isOpen
            repository.setAppOpenState(app.id, newState)
            val msg = if (newState) "Opened ${app.appName}." else "Closed ${app.appName}."
            _lastActionSummary.value = msg
            _speechText.value = "$msg Let me know if you need any other app!"
            _avatarMood.value = AvatarMood.HAPPY
            if (settings.value.speechFeedbackEnabled) {
                voiceManager?.speak(_speechText.value)
            }
        }
    }

    fun closeAllApps() {
        viewModelScope.launch {
            val openApps = desktopApps.value.filter { it.isOpen }
            val freedRam = openApps.sumOf { it.ramUsageMb }
            repository.closeAllApps()
            val msg = "Closed all ${openApps.size} active windows! Freed $freedRam MB of RAM."
            _lastActionSummary.value = msg
            _speechText.value = "All windows closed! Your desktop is completely organized now."
            _avatarMood.value = AvatarMood.HAPPY
            if (settings.value.speechFeedbackEnabled) {
                voiceManager?.speak(_speechText.value)
            }
        }
    }

    fun organizeDesktopGrid() {
        viewModelScope.launch {
            val (summary, _) = organizerEngine.executeAction("ORGANIZE_DESKTOP", null, desktopApps.value)
            _lastActionSummary.value = summary
            _speechText.value = "I've organized all your desktop app shortcuts into clean categories!"
            _avatarMood.value = AvatarMood.HAPPY
            if (settings.value.speechFeedbackEnabled) {
                voiceManager?.speak(_speechText.value)
            }
        }
    }

    fun cleanMemory() {
        viewModelScope.launch {
            val (summary, freed) = organizerEngine.executeAction("CLEAN_MEMORY", null, desktopApps.value)
            _lastActionSummary.value = summary
            _speechText.value = "Memory cleaned! Freed $freed MB of RAM for maximum performance."
            _avatarMood.value = AvatarMood.HAPPY
            if (settings.value.speechFeedbackEnabled) {
                voiceManager?.speak(_speechText.value)
            }
        }
    }

    fun updateSettings(newSettings: AssistantSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            voiceManager?.configureVoice(newSettings.voicePitch, newSettings.voiceSpeed)
            _speechText.value = "Settings saved! Persona updated to ${newSettings.personaName}."
            _avatarMood.value = AvatarMood.HAPPY
        }
    }

    fun clearVoiceLogs() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
