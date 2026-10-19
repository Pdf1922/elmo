package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.language.AppLanguage
import com.example.ui.language.LanguageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * LocalizationManager manages application-wide language switching between Amharic ("am")
 * and English ("en") using the Android resources system (res/values/strings.xml and res/values-am/strings.xml).
 */
object LocalizationManager {
  private const val PREFS_NAME = "church_localization_prefs"
  private const val KEY_LANGUAGE = "selected_language_code"

  private val _currentLanguageFlow = MutableStateFlow(AppLanguage.AMHARIC)
  val currentLanguageFlow: StateFlow<AppLanguage> = _currentLanguageFlow.asStateFlow()

  var currentLanguage: AppLanguage by mutableStateOf(AppLanguage.AMHARIC)
    private set

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  /**
   * Initializes the manager on app startup, reading saved language and applying locale to resources.
   */
  fun initialize(context: Context) {
    val prefs = getPrefs(context)
    val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.AMHARIC.code) ?: AppLanguage.AMHARIC.code
    val lang = if (savedCode == "en") AppLanguage.ENGLISH else AppLanguage.AMHARIC
    applyLanguage(context, lang, savePref = false)
  }

  /**
   * Switches the active language, updates Android configuration and SharedPreferences,
   * triggering resource reload for strings.xml.
   */
  fun setLanguage(context: Context, language: AppLanguage) {
    applyLanguage(context, language, savePref = true)
  }

  /**
   * Toggles between Amharic and English.
   */
  fun toggleLanguage(context: Context) {
    val next = if (currentLanguage == AppLanguage.AMHARIC) AppLanguage.ENGLISH else AppLanguage.AMHARIC
    setLanguage(context, next)
  }

  private fun applyLanguage(context: Context, language: AppLanguage, savePref: Boolean) {
    currentLanguage = language
    _currentLanguageFlow.value = language
    LanguageState.setLanguage(language)

    if (savePref) {
      getPrefs(context).edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)

    val resources: Resources = context.resources
    val config: Configuration = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)

    @Suppress("DEPRECATION")
    resources.updateConfiguration(config, resources.displayMetrics)

    // Also update application context resources
    context.applicationContext.resources.let { appRes ->
      val appConfig = appRes.configuration
      appConfig.setLocale(locale)
      appConfig.setLayoutDirection(locale)
      @Suppress("DEPRECATION")
      appRes.updateConfiguration(appConfig, appRes.displayMetrics)
    }
  }

  /**
   * Wraps context with updated locale configuration if needed for Activity recreation.
   */
  fun wrapContext(context: Context): Context {
    val locale = Locale.forLanguageTag(currentLanguage.code)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return context.createConfigurationContext(config)
  }

  fun getString(context: Context, resId: Int): String {
    return context.getString(resId)
  }
}
