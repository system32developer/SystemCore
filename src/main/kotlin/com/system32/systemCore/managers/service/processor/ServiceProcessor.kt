package com.system32.systemCore.managers.service.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.system32.systemCore.managers.service.annotation.Service
import java.io.OutputStreamWriter

class ServiceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val collected = mutableSetOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Service::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        collected += symbols
        return emptyList()
    }

    override fun finish() {
        if (collected.isEmpty()) return

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32.generated",
            "ServiceRegistry"
        )

        val imports = listOf("com.system32.systemCore.managers.service.PluginService")

        val servicesCode = collected.joinToString("\n") { symbol ->
            val fqName = symbol.qualifiedName!!.asString()
            val isObject = (symbol.classKind == ClassKind.OBJECT)
            if (isObject) {
                "        $fqName"
            } else {
                "        $fqName()"
            }
        }

        val code = buildString {
            appendLine("package com.system32.generated")
            appendLine()
            imports.forEach { appendLine("import $it") }
            appendLine()
            appendLine("object ServiceRegistry {")
            appendLine("    val services: List<PluginService> = listOf(")
            appendLine(servicesCode)
            appendLine("    )")
            appendLine("    fun onEnable() {")
            appendLine("        services.forEach { it.onEnable() }")
            appendLine("    }")
            appendLine("    fun onDisable() {")
            appendLine("        services.forEach { it.onDisable() }")
            appendLine("    }")
            appendLine("}")
        }

        OutputStreamWriter(file, Charsets.UTF_8).use { writer ->
            writer.write(code)
        }
    }
}

class ServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ServiceProcessor(environment.codeGenerator, environment.logger)
    }
}
