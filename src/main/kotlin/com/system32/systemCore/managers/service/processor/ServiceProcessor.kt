package com.system32.systemCore.managers.service.processor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.system32.systemCore.managers.service.PluginService
import com.system32.systemCore.managers.service.annotation.Service
import java.io.OutputStreamWriter

class ServiceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(Service::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val serviceInterface = PluginService::class.qualifiedName!!
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "META-INF/services",
            serviceInterface,
            ""
        )

        OutputStreamWriter(file, Charsets.UTF_8).use { writer ->
            for (symbol in symbols) {
                val className = symbol.qualifiedName?.asString()
                if (className != null &&
                    symbol.superTypes.any { it.resolve().declaration.qualifiedName?.asString() == serviceInterface }
                ) {
                    writer.write("$className\n")
                } else {
                    logger.warn("The class $className is annotated with @Service but does not implement PluginService interface.")
                }
            }
        }

        generated = true
        return emptyList()
    }
}

class ServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ServiceProcessor(environment.codeGenerator, environment.logger)
    }
}
