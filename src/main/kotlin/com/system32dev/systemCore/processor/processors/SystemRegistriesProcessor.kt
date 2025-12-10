package com.system32dev.systemCore.processor.processors

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
import com.system32dev.systemCore.processor.TemplateEngine
import com.system32dev.systemCore.processor.annotations.AutoRegistry
import com.system32dev.systemCore.processor.annotations.Service
import java.io.OutputStreamWriter

class SystemRegistriesProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val registries = resolver
            .getSymbolsWithAnnotation(AutoRegistry::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (registries.isEmpty()) return emptyList()

        val invalid = registries.filter { it.classKind != ClassKind.OBJECT }

        if (invalid.isNotEmpty()) {
            invalid.forEach { symbol ->
                logger.error("@AutoRegistry can only be applied to object declarations. (${symbol.qualifiedName!!.asString()})", symbol)
            }
            return invalid
        }

        val registriesCode = registries.joinToString(",\n") { it.simpleName.asString() }

        val template = TemplateEngine.loadTemplate("SystemLoader.kt")
        val code = TemplateEngine.render(
            template,
            mapOf("registries" to registriesCode)
        )

        val dependencies = Dependencies(
            aggregating = true,
            *registries.mapNotNull { it.containingFile }.toTypedArray()
        )

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = "com.system32dev.systemCore.generated",
            fileName = "SystemLoader"
        ).use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { it.write(code) }
        }

        return emptyList()
    }
}

class SystemRegistriesProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SystemRegistriesProcessor(environment.codeGenerator, environment.logger)
    }
}