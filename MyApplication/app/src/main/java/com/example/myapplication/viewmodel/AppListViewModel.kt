package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appList: StateFlow<List<AppInfo>> = _appList

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val packageManager = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            
            val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                packageManager.queryIntentActivities(intent, 0)
            }

            val appInfoList = apps.mapNotNull { resolveInfo ->
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.PackageInfoFlags.of(0L))
                } else {
                    packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, 0)
                }
                
                val sourceDir = packageInfo.applicationInfo?.sourceDir ?: return@mapNotNull null

                AppInfo(
                    name = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(packageManager),
                    sourceDir = sourceDir,
                    versionName = packageInfo.versionName ?: "N/A"
                )
            }.sortedBy { it.name }

            _appList.value = appInfoList
        }
    }

    fun extractApk(appInfo: AppInfo, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isExtracting.value = true
            try {
                val sourceFile = File(appInfo.sourceDir)
                val destinationDir = File(context.externalCacheDir, "extracted_apks")
                if (!destinationDir.exists()) destinationDir.mkdirs()
                
                val destinationFile = File(destinationDir, "${appInfo.name}_${appInfo.versionName}.apk")
                
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destinationFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(intent, "Share APK via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isExtracting.value = false
            }
        }
    }
}
