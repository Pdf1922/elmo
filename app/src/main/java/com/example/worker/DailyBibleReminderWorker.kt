package com.example.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.util.BibleReminderManager
import java.util.Calendar

/**
 * WorkManager CoroutineWorker for triggering daily Bible reading reminders
 * at 6:30 AM (Dawn Scripture) and 6:30 PM (Vesper Scripture & Prayer).
 */
class DailyBibleReminderWorker(
  private val context: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    try {
      BibleReminderManager.initChannel(context)

      val calendar = Calendar.getInstance()
      val hour = calendar.get(Calendar.HOUR_OF_DAY)
      val isMorning = hour < 14 // Before 2 PM is morning reading

      // Fetch today's cached biblical story from Room Database
      val db = AppDatabase.getDatabase(context)
      val dayNumber = (calendar.get(Calendar.DAY_OF_WEEK) % 7) + 1
      val story = db.sundaySchoolDao().getBibleStoryByDay(dayNumber)

      val title = if (isMorning) {
        "🌅 6:30 AM Morning Scripture • የጧት መጽሐፍ ቅዱስ ንባብ"
      } else {
        "🌆 6:30 PM Evening Scripture & Prayer • የምሽት ጸሎትና ንባብ"
      }

      val scriptureVerse = story?.verseReference ?: "መዝሙር 143፥8 • Psalms 143:8"
      val amharicText = story?.verseTextAmharic ?: "«በጧት ቸርነትህን አሰማኝ፥ በአንተ ታምኛለሁና፤ ነፍሴን ወደ አንተ አንሥቻለሁና የምሄድበትን መንገድ አስታውቀኝ።»"
      val englishText = story?.verseTextEnglish ?: "«Cause me to hear Your lovingkindness in the morning, for in You do I trust.»"
      val storyTitle = if (isMorning) (story?.titleAmharic ?: "ቅዱስ ገብርኤልና ሠለስቱ ደቂቅ") else (story?.moralAmharic ?: "የዕለቱ መንፈሳዊ ትምህርት")

      val bigContent = """
        📖 $scriptureVerse
        $amharicText
        
        • $englishText
        
        🕊️ $storyTitle
      """.trimIndent()

      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("NAVIGATE_TO", "BIBLE_STORY")
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        if (isMorning) 630 else 1830,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
      )

      val builder = NotificationCompat.Builder(context, BibleReminderManager.CHANNEL_ID)
        .setSmallIcon(R.drawable.church_official_logo)
        .setContentTitle(title)
        .setContentText("📖 $scriptureVerse: $amharicText")
        .setStyle(NotificationCompat.BigTextStyle().bigText(bigContent))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setVibrate(longArrayOf(0, 300, 200, 300))

      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(if (isMorning) 6300 else 18300, builder.build())

      return Result.success()
    } catch (e: Exception) {
      return Result.retry()
    }
  }
}
