package com.system32dev.systemCore.processor.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.system32dev.systemCore.processor.annotations.Dependency
import com.system32dev.systemCore.processor.model.BaseTemplateProcessor
import com.system32dev.systemCore.processor.model.ProcessorSpec

class DependencyProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseTemplateProcessor(codeGenerator, logger, DependencySpec)

class DependencyProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DependencyProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}

object DependencySpec : ProcessorSpec(
    annotation = Dependency::class,
    template = "DependencyResolver.java",
    generateWhenEmpty = true
) {

    override fun collect(symbols: List<KSClassDeclaration>): Map<String, String> {

        val deps = mutableListOf<Pair<String, String?>>()

        for (symbol in symbols) {
            val ann = symbol.annotations.first {
                it.shortName.asString() == "Dependency"
            }

            val coords = ann.arguments.first { it.name?.asString() == "coordinates" }.value as String
            val repo = ann.arguments.first { it.name?.asString() == "repository" }.value as String

            deps += coords to repo.ifBlank { null }
        }

        val reposCode = deps.filter { it.second != null }.joinToString("\n") { (_, repo) ->
            val id = repo!!.substringAfter("//").substringBefore("/").replace(".", "-")
            """resolver.addRepository(new RemoteRepository.Builder("$id", "default", "$repo").build());"""
        }

        val depsCode = deps.joinToString("\n") { (coords, _) ->
            """resolver.addDependency(new Dependency(new DefaultArtifact("$coords"), null));"""
        }

        return mapOf(
            "repositories" to reposCode,
            "dependencies" to depsCode
        )
    }
}


