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
import com.system32.systemCore.managers.processor.TemplateEngine
import com.system32.systemCore.managers.processor.annotations.ListenerComponent
import com.system32.systemCore.managers.processor.annotations.Service
import java.io.OutputStreamWriter

class EventProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val collected = mutableSetOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(ListenerComponent::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        collected += symbols

        return emptyList()
    }

    override fun finish() {
        if (collected.isEmpty()) return

        val listenersCode = collected.joinToString(",\n") { clazz ->
            val fqName = clazz.qualifiedName!!.asString()
            if (clazz.classKind == ClassKind.OBJECT) fqName else "$fqName()"
        }

        val template = TemplateEngine.loadTemplate("EventRegistry.kt")
        val code = TemplateEngine.render(template, mapOf("listeners" to listenersCode))

        codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32.generated",
            "EventRegistry"
        ).use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { it.write(code) }
        }

        logger.info("[EventProcessor] EventRegistry generated with ${collected.size} listeners.")
    }
}
