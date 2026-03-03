package com.prj.japanlib.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prj.data.notification.NotificationSender
import com.prj.domain.model.ProgressStats
import com.prj.domain.model.WordNotificationData
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.japanlib.MainActivity
import com.prj.japanlib.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri

/**
 * Concrete implementation of NotificationSender that handles all Android-specific
 * notification building and channel creation.
 * This class lives in the app layer to keep UI presentation logic out of the data layer.
 */
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationSender {
    
    companion object {
        const val CHANNEL_PROGRESS = "learning_progress"
        const val CHANNEL_REMINDERS = "word_reminders"
        const val CHANNEL_DAILY_WORD = "daily_word"
        
        const val NOTIFICATION_ID_PROGRESS = 1001
        const val NOTIFICATION_ID_REMINDER = 1002
        const val NOTIFICATION_ID_DAILY_WORD = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PROGRESS,
                    context.getString(R.string.notification_channel_progress),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_progress_desc)
                },
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    context.getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_reminders_desc)
                },
                NotificationChannel(
                    CHANNEL_DAILY_WORD,
                    context.getString(R.string.notification_channel_daily_word),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_daily_word_desc)
                }
            )
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }
    
    override fun sendProgressNotification(stats: ProgressStats) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = "myapp://flashcard_list".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(R.drawable.app_ic)
            .setContentTitle(context.getString(R.string.notification_progress_title))
            .setContentText(buildProgressText(stats))
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildDetailedProgressText(stats)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PROGRESS, notification)
        }
    }
    
    override fun sendWordNotification(wordData: WordNotificationData) {
        val word = wordData.word
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = "myapp://wordDetail/${word.id}".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_WORD)
            .setSmallIcon(R.drawable.app_ic)
            .setContentTitle(context.getString(R.string.notification_word_of_day))
            .setContentText("${word.kanji ?: word.reading} - ${word.meaning?.firstOrNull()}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildWordText(word)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY_WORD, notification)
        }
    }
    
    override fun sendReviewReminderNotification(dueWordsCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = "myapp://flashcard_list".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.app_ic)
            .setContentTitle(context.getString(R.string.notification_review_reminder_title))
            .setContentText(context.getString(R.string.notification_review_reminder_text, dueWordsCount))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
        }
    }
    
    private fun buildProgressText(stats: ProgressStats): String {
        return buildString {
            append(context.getString(R.string.notification_progress_summary, stats.wordsLearnedToday))
            if (stats.currentStreak > 1) {
                append(context.getString(R.string.notification_progress_streak, stats.currentStreak))
            }
        }
    }
    
    private fun buildDetailedProgressText(stats: ProgressStats): String {
        return buildString {
            appendLine(context.getString(R.string.notification_progress_detail_today, stats.wordsLearnedToday))
            appendLine(context.getString(R.string.notification_progress_detail_week, stats.wordsLearnedThisWeek))
            if (stats.currentStreak > 0) {
                appendLine(context.getString(R.string.notification_progress_detail_streak, stats.currentStreak))
            }
            if (stats.accuracyRate > 0) {
                appendLine(context.getString(R.string.notification_progress_detail_accuracy, stats.accuracyRate.toInt()))
            }
            if (stats.dueReviewCount > 0) {
                appendLine(context.getString(R.string.notification_progress_detail_due, stats.dueReviewCount))
            }
        }.trim()
    }
    
    private fun buildWordText(word: JapaneseWord): String {
        return buildString {
            appendLine("${word.kanji ?: word.reading}")
            appendLine(word.reading)
            appendLine()
            appendLine(context.getString(R.string.notification_word_meaning_label, word.meaning))
        }
    }
}
