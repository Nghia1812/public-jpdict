package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prj.data.local.model.NotificationHistoryEntity
import com.prj.data.local.model.NotificationPreferencesEntity

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_preferences WHERE id = 0 LIMIT 1")
    suspend fun getPreferences(): NotificationPreferencesEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePreferences(preferences: NotificationPreferencesEntity)
    
    @Insert
    suspend fun insertHistory(history: NotificationHistoryEntity)
    
    @Query("SELECT * FROM notification_history ORDER BY sent_date DESC LIMIT :limit")
    suspend fun getHistory(limit: Int): List<NotificationHistoryEntity>
    
    @Query("""
        SELECT * FROM notification_history 
        WHERE word_id = :wordId 
        AND sent_date > :sinceTimestamp
        ORDER BY sent_date DESC LIMIT 1
    """)
    suspend fun getRecentNotificationForWord(wordId: String, sinceTimestamp: Long): NotificationHistoryEntity?
    
    @Query("DELETE FROM notification_history WHERE sent_date < :beforeTimestamp")
    suspend fun cleanOldHistory(beforeTimestamp: Long): Int
}
