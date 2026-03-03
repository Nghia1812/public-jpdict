package com.prj.domain.repository

import kotlinx.coroutines.flow.Flow

interface INetworkMonitor {
    val isConnected: Flow<Boolean>
}