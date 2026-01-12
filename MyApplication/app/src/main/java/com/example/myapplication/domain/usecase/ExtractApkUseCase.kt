package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AppEntity
import com.example.myapplication.domain.repository.AppRepository

class ExtractApkUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(app: AppEntity): String? {
        return repository.extractApk(app)
    }
}
