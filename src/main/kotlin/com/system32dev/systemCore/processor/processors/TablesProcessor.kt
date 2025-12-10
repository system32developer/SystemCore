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
import com.system32dev.systemCore.processor.annotations.DatabaseTable
import com.system32dev.systemCore.processor.annotations.Service
import java.io.OutputStreamWriter

class TablesProcessor (
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val tables = resolver
            .getSymbolsWithAnnotation(DatabaseTable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (tables.isEmpty()) return emptyList()

        val invalid = tables.filter { it.classKind != ClassKind.OBJECT }

        if (invalid.isNotEmpty()) {
            invalid.forEach { symbol ->
                logger.error("@DatabaseTable can only be applied to object declarations. (${symbol.qualifiedName!!.asString()})", symbol)
            }
            return invalid
        }

        val servicesCode = tables.joinToString(",\n") { it.qualifiedName!!.asString() }

        val template = TemplateEngine.loadTemplate("TablesRegistry.kt")
        val code = TemplateEngine.render(
            template,
            mapOf("tables" to servicesCode)
        )

        val dependencies = Dependencies(
            aggregating = true,
            *tables.mapNotNull { it.containingFile }.toTypedArray()
        )

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = "com.system32dev.systemCore.generated",
            fileName = "TablesRegistry"
        ).use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { it.write(code) }
        }

        return emptyList()
    }
}

class TablesProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TablesProcessor(environment.codeGenerator, environment.logger)
    }
}