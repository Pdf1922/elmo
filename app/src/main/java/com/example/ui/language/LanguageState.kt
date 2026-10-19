package com.example.ui.language

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
  ENGLISH("en", "English", "English"),
  AMHARIC("am", "Amharic", "አማርኛ")
}

object LanguageState {
  var currentLanguage by mutableStateOf(AppLanguage.AMHARIC)
    private set

  fun toggleLanguage() {
    currentLanguage = if (currentLanguage == AppLanguage.AMHARIC) {
      AppLanguage.ENGLISH
    } else {
      AppLanguage.AMHARIC
    }
  }

  fun setLanguage(language: AppLanguage) {
    currentLanguage = language
  }
}
