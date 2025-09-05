package com.system32.systemCore.managers.processor.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.system32.systemCore.managers.processor.annotations.Event
import com.system32.systemCore.managers.processor.annotations.Service
import java.io.OutputStreamWriter

class EventProcessor (
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val collected = mutableSetOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Event::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()

        if (symbols.none()) return emptyList()

        for (func in symbols) {
            val parent = func.parentDeclaration as? KSClassDeclaration ?: continue
            collected += parent
        }

        return emptyList()
    }

    override fun finish() {
        if (collected.isEmpty()) return

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32.generated",
            "EventRegistry"
        )

        val imports = listOf("")

        val classesCode = collected.joinToString(",\n") { clazz ->
            val fqName = clazz.qualifiedName!!.asString()
            val isObject = clazz.classKind == ClassKind.OBJECT
            if (isObject) {
                "        $fqName"
            } else {
                "        $fqName()"
            }
        }

        val code = buildString {
            appendLine("package com.system32.generated")
            appendLine()
            appendLine("import org.bukkit.event.Listener")
            appendLine("import com.system32.systemCore.SystemCore")
            appendLine("import com.system32.systemCore.managers.processor.annotations.Event")
            appendLine()
            appendLine("object EventRegistry {")
            appendLine("    private val listeners: List<Any> = listOf(")
            appendLine(classesCode)
            appendLine("    )")
            appendLine()
            appendLine("    fun register() {")
            appendLine("        val plugin = SystemCore.plugin")
            appendLine("        plugin.logger.info(\"[EventRegistry] Iniciando registro de eventos...\")")
            appendLine("        listeners.forEach { obj ->")
            appendLine("            val listener = object : Listener {}")
            appendLine("            val clazz = obj::class.java")
            appendLine("            plugin.logger.info(\"[EventRegistry] Procesando clase: \${clazz.name}\")")
            appendLine("            for (method in clazz.declaredMethods) {")
            appendLine("                val ann = method.getAnnotation(Event::class.java) ?: continue")
            appendLine("                val params = method.parameterTypes")
            appendLine("                if (params.size == 1 && org.bukkit.event.Event::class.java.isAssignableFrom(params[0])) {")
            appendLine("                    val eventClass = params[0] as Class<out org.bukkit.event.Event>")
            appendLine("                    plugin.logger.info(\"[EventRegistry] Registrando método \${method.name} para evento \${eventClass.name} con prioridad \${ann.priority}\")")
            appendLine("                    plugin.server.pluginManager.registerEvent(eventClass, listener, ann.priority, { _, event ->")
            appendLine("                        if (eventClass.isInstance(event)) {")
            appendLine("                            plugin.logger.info(\"[EventRegistry] Invocando \${clazz.name}#\${method.name} con evento \${event::class.java.name}\")")
            appendLine("                            method.isAccessible = true")
            appendLine("                            try {")
            appendLine("                                method.invoke(obj, eventClass.cast(event))")
            appendLine("                            } catch (ex: Exception) {")
            appendLine("                                plugin.logger.severe(\"[EventRegistry] Error ejecutando \${clazz.name}#\${method.name}: \${ex.message}\")")
            appendLine("                                ex.printStackTrace()")
            appendLine("                            }")
            appendLine("                        }")
            appendLine("                    }, plugin, ann.ignoreCancelled)")
            appendLine("                } else {")
            appendLine("                    plugin.logger.warning(\"[EventRegistry] Método \${clazz.name}#\${method.name} ignorado: firma inválida\")")
            appendLine("                }")
            appendLine("            }")
            appendLine("        }")
            appendLine("        plugin.logger.info(\"[EventRegistry] Registro de eventos finalizado.\")")
            appendLine("    }")
            appendLine("}")
        }

        OutputStreamWriter(file, Charsets.UTF_8).use { writer ->
            writer.write(code)
        }
    }
}

class EventProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EventProcessor(environment.codeGenerator, environment.logger)
    }
}
