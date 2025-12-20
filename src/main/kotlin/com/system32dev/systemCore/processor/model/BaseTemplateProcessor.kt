package com.system32dev.systemCore.processor.model

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.system32dev.systemCore.processor.TemplateEngine
import java.io.OutputStreamWriter

abstract class BaseTemplateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val spec: ProcessorSpec
) : SymbolProcessor {

    companion object {
        private val generated = mutableSetOf<String>()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val outputFileName = spec.template.split(".").first()
        val extension = spec.template.split(".").last()

        val key = "${spec.packageName}.$outputFileName"
        if (!generated.add(key)) return emptyList()

        val symbols = resolver
            .getSymbolsWithAnnotation(spec.annotation.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        val invalid = symbols.filterNot { spec.validate(it, logger) }
        if (invalid.isNotEmpty()) return invalid

        if (symbols.isEmpty() && !spec.generateWhenEmpty) return emptyList()

        val values = spec.collect(symbols)
        val template = TemplateEngine.loadTemplate(spec.template)
        val code = TemplateEngine.render(template, values)

        val dependencies = if (symbols.isEmpty())
            Dependencies(aggregating = true)
        else
            Dependencies(
                aggregating = true,
                *symbols.mapNotNull { it.containingFile }.toTypedArray()
            )

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = spec.packageName,
            fileName = outputFileName,
            extensionName = extension
        ).use {
            OutputStreamWriter(it, Charsets.UTF_8).use { w -> w.write(code) }
        }

        return emptyList()
    }
}
