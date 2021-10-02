package me.davidffa.d4rkbotkt

import org.yaml.snakeyaml.Yaml

object Translator {
  private val pt: Map<String, *> = Yaml().load(javaClass.classLoader.getResourceAsStream("locales/pt.yml"))
  private val en: Map<String, *> = Yaml().load(javaClass.classLoader.getResourceAsStream("locales/en.yml"))

  fun t(path: String, locale: Locale, placeholders: List<String>?): String {
    val splitedPath = path.split(".")

    var map: Map<*, *>? = null
    var translation: String? = null

    if (splitedPath.size == 1) {
      translation = when (locale) {
        Locale.PT -> pt[splitedPath[0]] as String?
        Locale.EN -> en[splitedPath[0]] as String?
      }
    } else {
      for (i in splitedPath.indices) {
        if (map == null && i != 0) break

        if (i == splitedPath.size - 1) {
          translation = map?.get(splitedPath[i]) as String?
          break
        }

        map = if (map == null) {
          when (locale) {
            Locale.PT -> pt[splitedPath[i]] as Map<*, *>?
            Locale.EN -> en[splitedPath[i]] as Map<*, *>?
          }
        } else map[splitedPath[i]] as Map<*, *>?
      }
    }

    if (translation != null && placeholders != null) {
      val matchedPlaceholders = "\\{.}".toRegex().findAll(translation)

      val nums = matchedPlaceholders.map { it.groupValues.map { v -> v.replace("[{}]".toRegex(), "") } }.flatten()

      nums.forEach {
        translation = translation!!.replaceFirst("{$it}", placeholders[it.toInt()])
      }
    }

    return translation ?: "No locale available"
  }
}

enum class Locale {
  PT, EN
}