package com.system32dev.systemCore.managers.processor.processors

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
import com.system32dev.systemCore.managers.processor.TemplateEngine
import com.system32dev.systemCore.managers.processor.annotations.DatabaseTable
import com.system32dev.systemCore.managers.processor.annotations.Service

class DatabaseProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val collected = mutableSetOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(DatabaseTable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        collected += symbols
        return emptyList()
    }

    override fun finish() {
        if (collected.isEmpty()) return

        val tablesCode = collected.joinToString(",\n") { symbol ->
            val fqName = symbol.qualifiedName!!.asString()
            val isObject = (symbol.classKind == ClassKind.OBJECT)
            if (isObject) fqName else "$fqName()"
        }

        val engine = TemplateEngine
        val template = engine.loadTemplate("DatabaseRegistry.kt")
        val code = engine.render(template, mapOf("tables" to tablesCode))

        codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32dev.generated",
            "DatabaseRegistry"
        ).use { out ->
            out.writer(Charsets.UTF_8).use { it.write(code) }
        }
    }
}

class DatabaseProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DatabaseProcessor(environment.codeGenerator, environment.logger)
    }
}