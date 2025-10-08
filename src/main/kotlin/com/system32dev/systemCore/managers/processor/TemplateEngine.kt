package com.system32dev.systemCore.managers.processor

object TemplateEngine {
    fun loadTemplate(path: String): String {
        val templatesPath = "templates/$path"
        val stream = this::class.java.classLoader.getResourceAsStream(templatesPath)
            ?: throw IllegalArgumentException("Template not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }

    fun render(template: String, variables: Map<String, Any>): String {
        var result = template
        for ((key, value) in variables) {
            result = result.replace("{{${key}}}", value.toString())
        }
        return result
    }
}
