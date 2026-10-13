package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.StatusSaverScreen
import com.example.ui.StatusViewModel
import com.example.ui.components.AllFilesPermissionDialog
import com.example.ui.components.AppOnboardingTourDialog
import com.example.ui.components.PermissionRationaleDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StatusViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = when (uiState.themeMode) {
                com.example.ui.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                com.example.ui.ThemeMode.DARK -> true
                com.example.ui.ThemeMode.LIGHT -> false
            }

            MyApplicationTheme(darkTheme = isDark) {
                val prefs = remember {
                    getSharedPreferences("status_saver_prefs", Context.MODE_PRIVATE)
                }

                var showRationaleDialog by remember { mutableStateOf(false) }
                var showAllFilesDialog by remember { mutableStateOf(false) }
                var showOnboardingTour by remember {
                    mutableStateOf(!prefs.getBoolean("has_seen_onboarding", false))
                }

                // Launcher for All Files Access on Android 11+
                val allFilesSettingsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.loadStatuses()
                }

                // System Media Permission request launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    viewModel.loadStatuses()
                    // Check if second-step All Files Access is needed on Android 11+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        com.example.util.NotificationHelper.showAllFilesPermissionNotification(this@MainActivity)
                        showAllFilesDialog = true
                    }
                }

                fun checkPermissionsAndProceed() {
                    val needed = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.READ_MEDIA_IMAGES
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            needed.add(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.READ_MEDIA_VIDEO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            needed.add(Manifest.permission.READ_MEDIA_VIDEO)
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            needed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    if (needed.isNotEmpty()) {
                        showRationaleDialog = true
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        com.example.util.NotificationHelper.showAllFilesPermissionNotification(this@MainActivity)
                        showAllFilesDialog = true
                    }
                }

                LaunchedEffect(Unit) {
                    checkPermissionsAndProceed()
                }

                // 1. Initial Polite Rationale Dialog
                if (showRationaleDialog) {
                    PermissionRationaleDialog(
                        onConfirm = {
                            showRationaleDialog = false
                            val needed = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                needed.add(Manifest.permission.READ_MEDIA_IMAGES)
                                needed.add(Manifest.permission.READ_MEDIA_VIDEO)
                            } else {
                                needed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            permissionLauncher.launch(needed.toTypedArray())
                        },
                        onDismiss = {
                            showRationaleDialog = false
                        }
                    )
                }

                // 2. All Files Access Dialog for Android 11+ (Second Step)
                if (showAllFilesDialog) {
                    AllFilesPermissionDialog(
                        onGoToSettings = {
                            showAllFilesDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                        data = Uri.parse("package:$packageName")
                                    }
                                    allFilesSettingsLauncher.launch(intent)
                                } catch (e: Exception) {
                                    val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    allFilesSettingsLauncher.launch(fallback)
                                }
                            }
                        },
                        onDismiss = {
                            showAllFilesDialog = false
                        }
                    )
                }

                // 3. Comprehensive Onboarding Walkthrough Tour
                if (showOnboardingTour) {
                    AppOnboardingTourDialog(
                        onDismissTour = {
                            showOnboardingTour = false
                            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                        }
                    )
                }

                StatusSaverScreen(
                    viewModel = viewModel,
                    onRequestPermission = { checkPermissionsAndProceed() },
                    onStartTour = { showOnboardingTour = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
