package com.system32dev.systemCore.processor

import kotlin.collections.iterator

object TemplateEngine {
    fun loadTemplate(path: String): String {
        val templatesPath = "templates/$path"
        val stream = this::class.java.classLoader.getResourceAsStream(templatesPath)
            ?: throw IllegalArgumentException("Template not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }

    fun render(template: String, variables: Map<String, Any>): String {
        var result = template

        for ((key, rawValue) in variables) {
            val placeholder = "{{${key}}}"
            val value = rawValue.toString()

            val regex = Regex("(?m)^(\\s*)\\{\\{$key\\}\\}")
            val match = regex.find(result)

            if (match != null) {
                val indent = match.groupValues[1]

                val indentedValue = value
                    .lines()
                    .joinToString("\n") { indent + it }

                result = result.replaceFirst(
                    regex,
                    indentedValue
                )
            } else {
                // fallback simple
                result = result.replace(placeholder, value)
            }
        }

        return result
    }
}
