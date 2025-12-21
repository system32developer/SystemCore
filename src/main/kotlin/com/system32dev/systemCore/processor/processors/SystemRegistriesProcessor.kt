package com.system32dev.systemCore.processor.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.system32dev.systemCore.processor.annotations.AutoRegistry
import com.system32dev.systemCore.processor.model.BaseTemplateProcessor
import com.system32dev.systemCore.processor.model.ProcessorSpec

class SystemRegistriesProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseTemplateProcessor(codeGenerator, logger, SystemRegistriesSpec)

class SystemRegistriesProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SystemRegistriesProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}

object SystemRegistriesSpec : ProcessorSpec(
    annotation = AutoRegistry::class,
    template = "SystemLoader.kt",
    generateWhenEmpty = true
) {

    override fun validate(symbol: KSClassDeclaration, logger: KSPLogger): Boolean {
        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@AutoRegistry can only be applied to object declarations", symbol)
            return false
        }
        return true
    }

    override fun collect(symbols: List<KSClassDeclaration>): Map<String, String> {
        if (symbols.isEmpty()) return emptyMap()
        return mapOf(
            "registries" to symbols.joinToString(",\n") {
                it.qualifiedName!!.asString()
            }
        )
    }
}
