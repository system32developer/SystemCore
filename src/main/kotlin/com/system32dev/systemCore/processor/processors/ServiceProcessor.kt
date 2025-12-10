package com.system32dev.systemCore.processor.processors

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.system32dev.systemCore.processor.TemplateEngine
import com.system32dev.systemCore.processor.annotations.Service
import java.io.OutputStreamWriter

class ServiceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val services = resolver
            .getSymbolsWithAnnotation(Service::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (services.isEmpty()) return emptyList()

        val invalid = services.filter { it.classKind != ClassKind.OBJECT }

        if (invalid.isNotEmpty()) {
            invalid.forEach { symbol ->
                logger.error("@Service can only be applied to object declarations. (${symbol.qualifiedName!!.asString()})", symbol)
            }
            return invalid
        }

        val servicesCode = services.joinToString(",\n") { it.qualifiedName!!.asString() }

        val template = TemplateEngine.loadTemplate("ServiceRegistry.kt")
        val code = TemplateEngine.render(
            template,
            mapOf("services" to servicesCode)
        )

        val dependencies = Dependencies(
            aggregating = true,
            *services.mapNotNull { it.containingFile }.toTypedArray()
        )

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = "com.system32dev.systemCore.generated",
            fileName = "ServiceRegistry"
        ).use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { it.write(code) }
        }

        return emptyList()
    }
}

class ServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ServiceProcessor(environment.codeGenerator, environment.logger)
    }
}