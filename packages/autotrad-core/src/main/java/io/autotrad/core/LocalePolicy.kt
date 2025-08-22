package io.autotrad.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

sealed class LocaleMode {
  data object FOLLOW_SYSTEM : LocaleMode()
  data class USER_SELECTED(val tag: String) : LocaleMode()
  data object AUTO_BY_LOCATION : LocaleMode()
  data object HYBRID : LocaleMode() // segue sistema até o usuário escolher
}

data class LocalePolicy(
  val mode: LocaleMode = LocaleMode.HYBRID,
  val supported: List<String> = listOf("en"),
  val fallbackChain: List<String> = listOf("en")
)

private val Context.dataStore by preferencesDataStore(name = "autotrad_prefs")

class LocaleManager(
  private val context: Context,
  policy: LocalePolicy
) {
  private val KEY_USER = stringPreferencesKey("user_lang")
  private val policyFlow: MutableStateFlow<LocalePolicy> = MutableStateFlow(policy)
  val effective: MutableStateFlow<Locale> = MutableStateFlow(Locale.getDefault())

  fun setPolicy(newPolicy: LocalePolicy) {
    policyFlow.value = newPolicy
    recompute()
  }

  fun setUserLanguage(tag: String?) = runBlocking {
    context.dataStore.edit { prefs ->
      if (tag == null) prefs.remove(KEY_USER) else prefs[KEY_USER] = tag
    }
    setPolicy(policyFlow.value.copy(mode = if (tag == null) LocaleMode.FOLLOW_SYSTEM else LocaleMode.USER_SELECTED(tag)))
  }

  fun getUserLanguage(): String? = runBlocking {
    context.dataStore.data.map { it[KEY_USER] }.first()
  }

  fun recompute() {
    val sys = Locale.getDefault()
    val user = getUserLanguage()
    val geo = suggestBySystemLocale(policyFlow.value.supported)
    val resolved = resolveEffective(policyFlow.value, sys, geo, user)
    effective.value = resolved
    AutoTrad.setLocale(resolved.toLanguageTag())
  }

  private fun resolveEffective(
    policy: LocalePolicy,
    system: Locale,
    geoSuggested: String?,
    userSelected: String?
  ): Locale {
    fun best(tag: String?): Locale? {
      if (tag == null) return null
      if (tag in policy.supported) return Locale.forLanguageTag(tag)
      val base = tag.substringBefore('-')
      if (base in policy.supported) return Locale.forLanguageTag(base)
      policy.fallbackChain.firstOrNull { it in policy.supported }?.let { return Locale.forLanguageTag(it) }
      return null
    }
    return when (val m = policy.mode) {
      is LocaleMode.USER_SELECTED -> best(m.tag) ?: best(userSelected) ?: best(system.toLanguageTag()) ?: system
      LocaleMode.FOLLOW_SYSTEM    -> best(system.toLanguageTag()) ?: system
      LocaleMode.AUTO_BY_LOCATION -> best(geoSuggested) ?: best(system.toLanguageTag()) ?: system
      LocaleMode.HYBRID           -> best(userSelected) ?: best(system.toLanguageTag()) ?: system
    }
  }

  private fun suggestBySystemLocale(supported: List<String>): String? {
    val sys = Locale.getDefault().toLanguageTag()
    if (sys in supported) return sys
    val base = sys.substringBefore('-')
    return if (base in supported) base else null
  }
}

/** Idiomas RTL comuns. */
private val RTL_LANGS = setOf("ar","fa","he","ur","ps","ckb","dv","ku","yi")
fun isRtlLocale(locale: Locale): Boolean = locale.language.lowercase() in RTL_LANGS
