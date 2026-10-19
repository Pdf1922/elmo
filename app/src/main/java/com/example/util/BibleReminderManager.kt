package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.MainActivity
import com.example.R
import com.example.worker.DailyBibleReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object BibleReminderManager {
  const val CHANNEL_ID = "bible_reading_reminder_channel"
  private const val PREFS_NAME = "meserete_hiwot_bible_reminders"
  private const val KEY_MORNING_ENABLED = "morning_reminder_6_30_am"
  private const val KEY_EVENING_ENABLED = "evening_reminder_6_30_pm"

  const val WORK_TAG_MORNING = "work_bible_reminder_630am"
  const val WORK_TAG_EVENING = "work_bible_reminder_630pm"

  fun initChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "Meserete Hiwot Daily Bible Reminders"
      val descriptionText = "Daily 6:30 AM and 6:30 PM Bible Reading & Prayer Reminders"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        enableVibration(true)
        enableLights(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isMorningEnabled(context: Context): Boolean =
    getPrefs(context).getBoolean(KEY_MORNING_ENABLED, true)

  fun setMorningEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_MORNING_ENABLED, enabled).apply()
    scheduleDailyReminders(context)
  }

  fun isEveningEnabled(context: Context): Boolean =
    getPrefs(context).getBoolean(KEY_EVENING_ENABLED, true)

  fun setEveningEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_EVENING_ENABLED, enabled).apply()
    scheduleDailyReminders(context)
  }

  /**
   * Schedules WorkManager tasks for 6:30 AM and 6:30 PM daily.
   */
  fun scheduleDailyReminders(context: Context) {
    initChannel(context)

    val workManager = WorkManager.getInstance(context)

    // 1. Morning 6:30 AM Work Request
    if (isMorningEnabled(context)) {
      val morningDelay = calculateInitialDelay(6, 30)
      val morningWorkRequest = PeriodicWorkRequestBuilder<DailyBibleReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(morningDelay, TimeUnit.MILLISECONDS)
        .addTag(WORK_TAG_MORNING)
        .setConstraints(
          Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        )
        .build()

      workManager.enqueueUniquePeriodicWork(
        WORK_TAG_MORNING,
        ExistingPeriodicWorkPolicy.UPDATE,
        morningWorkRequest
      )
    } else {
      workManager.cancelUniqueWork(WORK_TAG_MORNING)
    }

    // 2. Evening 6:30 PM Work Request
    if (isEveningEnabled(context)) {
      val eveningDelay = calculateInitialDelay(18, 30)
      val eveningWorkRequest = PeriodicWorkRequestBuilder<DailyBibleReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(eveningDelay, TimeUnit.MILLISECONDS)
        .addTag(WORK_TAG_EVENING)
        .setConstraints(
          Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        )
        .build()

      workManager.enqueueUniquePeriodicWork(
        WORK_TAG_EVENING,
        ExistingPeriodicWorkPolicy.UPDATE,
        eveningWorkRequest
      )
    } else {
      workManager.cancelUniqueWork(WORK_TAG_EVENING)
    }
  }

  /**
   * Immediate trigger via WorkManager for testing and previewing reminders.
   */
  fun triggerWorkManagerTestReminder(context: Context, isMorning: Boolean) {
    initChannel(context)
    val workRequest = OneTimeWorkRequestBuilder<DailyBibleReminderWorker>()
      .addTag("test_bible_reminder_${if (isMorning) "morning" else "evening"}")
      .build()

    WorkManager.getInstance(context).enqueue(workRequest)

    val timeLabel = if (isMorning) "6:30 AM Morning Scripture" else "6:30 PM Evening Scripture"
    Toast.makeText(
      context,
      "Triggering $timeLabel reminder via WorkManager...",
      Toast.LENGTH_SHORT
    ).show()
  }

  /**
   * Computes the millisecond delay until the next occurrence of a target hour:minute.
   */
  private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, targetHour)
      set(Calendar.MINUTE, targetMinute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    if (target.before(now)) {
      // If time has passed today, schedule for tomorrow
      target.add(Calendar.DAY_OF_YEAR, 1)
    }

    return target.timeInMillis - now.timeInMillis
  }

  /**
   * Triggers the 6:30 AM Morning Scripture notification immediately.
   */
  fun triggerMorningNotification(context: Context) {
    triggerWorkManagerTestReminder(context, isMorning = true)
  }

  /**
   * Triggers the 6:30 PM Evening Scripture notification immediately.
   */
  fun triggerEveningNotification(context: Context) {
    triggerWorkManagerTestReminder(context, isMorning = false)
  }
}
