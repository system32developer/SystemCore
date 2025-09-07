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
import com.system32.systemCore.managers.processor.TemplateEngine
import com.system32.systemCore.managers.processor.annotations.ListenerComponent
import java.io.OutputStreamWriter

class DendencyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        return emptyList()
    }

    override fun finish() {

        val template = TemplateEngine.loadTemplate("DependencyResolver.java")
        val code = TemplateEngine.render(template, mapOf())

        codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32.generated",
            "DependencyResolver",
            "java"
        ).use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { it.write(code) }
        }

        logger.info("[EventProcessor] DependencyResolver generated")
    }
}

class DependencyProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DendencyProcessor(environment.codeGenerator, environment.logger)
    }
}
