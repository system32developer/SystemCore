package com.system32dev.systemCore.processor.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.system32dev.systemCore.processor.annotations.PluginEnchant
import com.system32dev.systemCore.processor.model.BaseTemplateProcessor
import com.system32dev.systemCore.processor.model.ProcessorSpec

class EnchantProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseTemplateProcessor(codeGenerator, logger, EnchantSpec)

class EnchantProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EnchantProcessor(environment.codeGenerator, environment.logger)
    }
}

object EnchantSpec : ProcessorSpec(
    annotation = PluginEnchant::class,
    template = "EnchantRegistry.kt",
    generateWhenEmpty = true
) {
    override fun validate(symbol: KSClassDeclaration, logger: KSPLogger): Boolean {
        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@PluginEnchant can only be applied to object declarations", symbol)
            return false
        }
        return true
    }

    override fun collect(symbols: List<KSClassDeclaration>): Map<String, String> {
        return mapOf(
            "enchants" to symbols.joinToString(",\n") { "${it.qualifiedName!!.asString()}::class to ${it.qualifiedName!!.asString()}" }
        )
    }
}