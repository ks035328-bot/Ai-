package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DesktopAppEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonPurple

@Composable
fun DesktopGrid(
    apps: List<DesktopAppEntity>,
    onToggleApp: (DesktopAppEntity) -> Unit,
    onOrganizeGrid: () -> Unit,
    onCloseAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "Productivity", "System", "Media", "Utility")

    val filteredApps = remember(apps, selectedCategory) {
        if (selectedCategory == "ALL") apps else apps.filter { it.category == selectedCategory }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Desktop Top Bar Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Desktop Apps Grid",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row {
                Button(
                    onClick = onOrganizeGrid,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("organize_grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Sort Grid",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Auto Sort", fontSize = 12.sp, color = CyberCyan)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onCloseAll,
                    colors = ButtonDefaults.buttonColors(containerColor = HotPink.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("close_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Close All",
                        tint = HotPink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Close All", fontSize = 12.sp, color = HotPink, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Category Filter Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {}
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) CyberCyan else DarkSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid View of Apps
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredApps, key = { it.id }) { app ->
                DesktopAppCard(app = app, onClick = { onToggleApp(app) })
            }
        }
    }
}

@Composable
fun DesktopAppCard(
    app: DesktopAppEntity,
    onClick: () -> Unit
) {
    val icon = getAppIconVector(app.iconType)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("app_card_${app.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isOpen) DarkSurfaceVariant else DarkCard
        ),
        border = if (app.isOpen) {
            androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan)
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with Active Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (app.isOpen) CyberCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = app.appName,
                    tint = if (app.isOpen) CyberCyan else Color.White,
                    modifier = Modifier.size(26.dp)
                )

                // Active Window Dot Indicator
                if (app.isOpen) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(HotPink)
                            .border(1.dp, Color.White, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.appName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (app.isOpen) "● Active" else "${app.ramUsageMb} MB",
                fontSize = 10.sp,
                color = if (app.isOpen) CyberCyan else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

fun getAppIconVector(iconType: String): ImageVector {
    return when (iconType) {
        "camera" -> Icons.Default.CameraAlt
        "chrome" -> Icons.Default.Language
        "settings" -> Icons.Default.Settings
        "notes" -> Icons.Default.Description
        "gallery" -> Icons.Default.Photo
        "music" -> Icons.Default.MusicNote
        "calculator" -> Icons.Default.Calculate
        "files" -> Icons.Default.Folder
        "tasks" -> Icons.Default.Task
        "terminal" -> Icons.Default.Terminal
        "cleaner" -> Icons.Default.CleaningServices
        "weather" -> Icons.Default.Cloud
        else -> Icons.Default.Apps
    }
}
