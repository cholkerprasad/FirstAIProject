package com.example.myapplication.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.domain.model.AppEntity
import com.example.myapplication.presentation.viewmodel.AppListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onSettingsClick: () -> Unit,
    viewModel: AppListViewModel = viewModel()
) {
    val appList by viewModel.appList.collectAsState()
    val isExtracting by viewModel.isExtracting.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APK Extractor") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (appList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = appList,
                        key = { it.packageName },
                        contentType = { "app_item" }
                    ) { app ->
                        AppItem(app = app) {
                            viewModel.extractApk(app, context)
                        }
                    }
                }
            }

            if (isExtracting) {
                // Use a simpler overlay to prevent Surface buffer issues
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {}, // Prevent clicks through
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Extracting APK...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppEntity, onClick: () -> Unit) {
    // Crucial: Use remember with the specific drawable instance
    // to avoid re-rendering frames unnecessarily
    val iconBitmap = remember(app.packageName) {
        try {
            app.icon?.toBitmap(width = 120, height = 120)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    ListItem(
        headlineContent = { Text(app.name, maxLines = 1) },
        supportingContent = { Text(app.packageName, maxLines = 1) },
        leadingContent = {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback placeholder
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                }
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
