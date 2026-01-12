package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AppEntity
import com.example.myapplication.domain.repository.AppRepository

class GetInstalledAppsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(): List<AppEntity> {
        return repository.getInstalledApps()
    }
}
