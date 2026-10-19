package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
  entities = [
    User::class,
    Department::class,
    MemberItem::class,
    PaymentItem::class,
    PaymentSetting::class,
    AttendanceRecord::class,
    PlanItem::class,
    ReportItem::class,
    AnnouncementItem::class,
    MessageItem::class,
    FeedbackItem::class,
    GradeItem::class,
    HymnItem::class,
    DocumentItem::class,
    BibleStory::class,
    QuizResult::class,
    BibleQuizQuestion::class,
    LessonNote::class
  ],
  version = 5,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun sundaySchoolDao(): SundaySchoolDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "meserete_hiwot_church.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
