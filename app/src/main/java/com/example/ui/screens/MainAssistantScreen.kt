package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ActiveAppsScreen
import com.example.ui.components.AssistantAvatarView
import com.example.ui.components.DesktopGrid
import com.example.ui.components.SettingsScreen
import com.example.ui.components.VoiceCommandBar
import com.example.ui.components.VoiceLogsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun MainAssistantScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val desktopApps by viewModel.desktopApps.collectAsStateWithLifecycle()
    val openApps = desktopApps.filter { it.isOpen }
    val voiceLogs by viewModel.voiceLogs.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val avatarMood by viewModel.avatarMood.collectAsStateWithLifecycle()
    val speechText by viewModel.speechText.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val totalRamUsedMb by viewModel.totalRamUsedMb.collectAsStateWithLifecycle()

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = navBarPadding.calculateBottomPadding())
            ) {
                // Bottom Floating Voice Bar
                VoiceCommandBar(
                    voiceState = voiceState,
                    onToggleMic = { viewModel.toggleListening() },
                    onSendTextCommand = { command -> viewModel.processUserCommand(command) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Navigation Bar
                NavigationBar(
                    containerColor = DarkCard,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = {
                            Icon(imageVector = Icons.Default.Apps, contentDescription = "Desktop Grid")
                        },
                        label = { Text("Desktop", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_desktop_tab")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (openApps.isNotEmpty()) {
                                        Badge(containerColor = HotPink, contentColor = Color.White) {
                                            Text("${openApps.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Window, contentDescription = "Active Windows")
                            }
                        },
                        label = { Text("Windows", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = HotPink,
                            indicatorColor = HotPink,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_windows_tab")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Voice Logs")
                        },
                        label = { Text("Logs", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonPurple,
                            indicatorColor = NeonPurple,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_logs_tab")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.setSelectedTab(3) },
                        icon = {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                        },
                        label = { Text("Settings", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_settings_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = statusBarPadding.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            // Header Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${openApps.size} Active Windows",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$totalRamUsedMb MB RAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                    }
                }

                Text(
                    text = "Lumi OS v2.5",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Top Avatar View Component
            AssistantAvatarView(
                personaName = settings.personaName,
                personaTitle = settings.personaTitle,
                mood = avatarMood,
                speechBubbleText = speechText,
                onAvatarClick = { viewModel.toggleListening() },
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Optional Error Snackbar Notice
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(bottom = 8.dp),
                    containerColor = HotPink,
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Text(text = error, color = Color.White, fontSize = 12.sp)
                }
            }

            // Dynamic Main Tab View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> DesktopGrid(
                        apps = desktopApps,
                        onToggleApp = { app -> viewModel.toggleAppOpen(app) },
                        onOrganizeGrid = { viewModel.organizeDesktopGrid() },
                        onCloseAll = { viewModel.closeAllApps() }
                    )
                    1 -> ActiveAppsScreen(
                        openApps = openApps,
                        totalRamUsedMb = totalRamUsedMb,
                        onCloseApp = { app -> viewModel.toggleAppOpen(app) },
                        onCloseAll = { viewModel.closeAllApps() },
                        onCleanMemory = { viewModel.cleanMemory() }
                    )
                    2 -> VoiceLogsScreen(
                        voiceLogs = voiceLogs,
                        onClearLogs = { viewModel.clearVoiceLogs() }
                    )
                    3 -> SettingsScreen(
                        settings = settings,
                        onSaveSettings = { updated -> viewModel.updateSettings(updated) }
                    )
                }
            }
        }
    }
}
