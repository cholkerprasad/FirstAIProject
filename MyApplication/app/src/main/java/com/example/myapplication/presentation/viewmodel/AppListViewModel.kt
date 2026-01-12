package com.example.myapplication.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AppRepositoryImpl
import com.example.myapplication.domain.model.AppEntity
import com.example.myapplication.domain.usecase.ExtractApkUseCase
import com.example.myapplication.domain.usecase.GetInstalledAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    // Ideally, these would be injected via Hilt or Koin
    private val repository = AppRepositoryImpl(application)
    private val getInstalledAppsUseCase = GetInstalledAppsUseCase(repository)
    private val extractApkUseCase = ExtractApkUseCase(repository)

    private val _appList = MutableStateFlow<List<AppEntity>>(emptyList())
    val appList: StateFlow<List<AppEntity>> = _appList

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting

    fun loadInstalledApps() {
        viewModelScope.launch {
            _appList.value = getInstalledAppsUseCase()
        }
    }

    fun extractApk(app: AppEntity, context: Context) {
        viewModelScope.launch {
            _isExtracting.value = true
            val path = extractApkUseCase(app)
            if (path != null) {
                shareApk(File(path), context, app.name)
            }
            _isExtracting.value = false
        }
    }

    private fun shareApk(file: File, context: Context, appName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share $appName APK")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
