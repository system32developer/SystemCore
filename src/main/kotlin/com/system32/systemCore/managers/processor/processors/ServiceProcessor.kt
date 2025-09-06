package com.system32.systemCore.managers.processor.processors

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.system32.systemCore.managers.processor.TemplateEngine
import com.system32.systemCore.managers.processor.annotations.Service
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

        val servicesCode = collected.joinToString(",\n") { symbol ->
            val fqName = symbol.qualifiedName!!.asString()
            val isObject = (symbol.classKind == ClassKind.OBJECT)
            if (isObject) fqName else "$fqName()"
        }

        val engine = TemplateEngine
        val template = engine.loadTemplate("ServiceRegistry.kt")
        val code = engine.render(template, mapOf("services" to servicesCode))

        codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32.generated",
            "ServiceRegistry"
        ).use { out ->
            out.writer(Charsets.UTF_8).use { it.write(code) }
        }
    }
}

class ServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ServiceProcessor(environment.codeGenerator, environment.logger)
    }
}