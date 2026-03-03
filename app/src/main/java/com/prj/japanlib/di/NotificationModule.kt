package com.prj.japanlib.di

import com.prj.data.notification.NotificationSender
import com.prj.japanlib.notification.AppNotificationManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for notification functionality.
 * Binds the NotificationSender interface to its concrete implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationSender(
        impl: AppNotificationManager
    ): NotificationSender
}
