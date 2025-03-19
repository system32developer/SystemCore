package com.system32.systemCore.managers.language

import java.util.concurrent.ConcurrentHashMap

enum class Language(private val defaultTranslation: String) {
    TIME_UNIT_SECOND("seconds"),
    TIME_UNIT_MINUTE("minutes"),
    TIME_UNIT_HOUR("hours"),
    TIME_UNIT_DAY("days");

    companion object {
        private val customTranslations = ConcurrentHashMap<Language, String>()

        operator fun get(language: Language): String {
            return language.translation
        }
    }

    var translation: String
        get() = customTranslations[this] ?: defaultTranslation
        set(value) {
            customTranslations[this] = value
        }

    override fun toString(): String = translation

    operator fun invoke(): String = translation

}
