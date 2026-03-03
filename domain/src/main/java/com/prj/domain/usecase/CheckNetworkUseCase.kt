package com.prj.domain.usecase

import com.prj.domain.repository.INetworkMonitor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckNetworkUseCase @Inject constructor(
    private val networkMonitor: INetworkMonitor
) {
    operator fun invoke() : Flow<Boolean> {
        return networkMonitor.isConnected
    }
}