package com.system32dev.systemCore.processor.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.system32dev.systemCore.processor.annotations.DatabaseTable
import com.system32dev.systemCore.processor.model.BaseTemplateProcessor
import com.system32dev.systemCore.processor.model.ProcessorSpec

class TablesProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseTemplateProcessor(codeGenerator, logger, TablesSpec)

class TablesProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TablesProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}

object TablesSpec : ProcessorSpec(
    annotation = DatabaseTable::class,
    template = "TablesRegistry.kt"
) {

    override fun validate(symbol: KSClassDeclaration, logger: KSPLogger): Boolean {
        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@DatabaseTable can only be applied to object declarations", symbol)
            return false
        }
        return true
    }

    override fun collect(symbols: List<KSClassDeclaration>): Map<String, String> =
        mapOf(
            "tables" to symbols.joinToString(",\n") {
                it.qualifiedName!!.asString()
            }
        )
}
