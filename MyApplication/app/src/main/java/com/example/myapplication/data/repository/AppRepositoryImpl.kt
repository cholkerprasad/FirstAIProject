package com.example.myapplication.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.myapplication.domain.model.AppEntity
import com.example.myapplication.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AppRepositoryImpl(private val context: Context) : AppRepository {

    override suspend fun getInstalledApps(): List<AppEntity> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            packageManager.queryIntentActivities(intent, 0)
        }

        apps.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val packageInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
                } else {
                    packageManager.getPackageInfo(packageName, 0)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            } ?: return@mapNotNull null

            val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null

            AppEntity(
                name = resolveInfo.loadLabel(packageManager).toString(),
                packageName = packageName,
                icon = resolveInfo.loadIcon(packageManager),
                sourceDir = appInfo.sourceDir,
                splitSourceDirs = appInfo.splitSourceDirs,
                versionName = packageInfo.versionName ?: "N/A"
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.name }
    }

    override suspend fun extractApk(app: AppEntity): String? = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.externalCacheDir, "extracted_apks")
            if (!destinationDir.exists()) destinationDir.mkdirs()

            if (app.isSplit) {
                // Zip base and all splits
                val destinationFile = File(destinationDir, "${app.name}_${app.versionName}.zip")
                ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationFile))).use { zos ->
                    // Add base APK
                    addFileToZip(zos, File(app.sourceDir), "base.apk")
                    // Add all split APKs
                    app.splitSourceDirs?.forEachIndexed { index, splitPath ->
                        val splitFile = File(splitPath)
                        if (splitFile.exists()) {
                            addFileToZip(zos, splitFile, "split_$index.apk")
                        }
                    }
                }
                destinationFile.absolutePath
            } else {
                // Standard single APK
                val destinationFile = File(destinationDir, "${app.name}_${app.versionName}.apk")
                FileInputStream(File(app.sourceDir)).use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destinationFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, fileName: String) {
        BufferedInputStream(FileInputStream(file)).use { bis ->
            val entry = ZipEntry(fileName)
            zos.putNextEntry(entry)
            bis.copyTo(zos)
            zos.closeEntry()
        }
    }
}
