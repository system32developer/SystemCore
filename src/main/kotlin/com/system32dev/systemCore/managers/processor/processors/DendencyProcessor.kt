package com.system32dev.systemCore.managers.processor.processors

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
import com.system32dev.systemCore.managers.processor.TemplateEngine
import com.system32dev.systemCore.managers.processor.annotations.Dependency
import com.system32dev.systemCore.managers.processor.annotations.ListenerComponent
import org.apache.maven.model.Repository
import java.io.OutputStreamWriter

class DendencyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val dependencies = mutableListOf<Pair<String, String?>>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Dependency::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        for (symbol in symbols) {
            val dependencyAnnotations = symbol.annotations.filter {
                it.shortName.asString() == "Dependency"
            }

            for (annotation in dependencyAnnotations) {
                val coordinates = annotation.arguments
                    .first { it.name?.asString() == "coordinates" }
                    .value as String

                val repository = annotation.arguments
                    .first { it.name?.asString() == "repository" }
                    .value as String

                dependencies += coordinates to repository.ifBlank { null }
            }
        }

        return emptyList()
    }

    override fun finish() {
        val centralDeps = dependencies.filter { it.second == null }.map { it.first }
        val customDeps = dependencies.filter { it.second != null }

        val repositoriesCode = customDeps.joinToString("\n\n") { (coords, repo) ->
            val id = repo!!.substringAfter("//").substringBefore("/").replace(".", "-")
            """resolver.addRepository(new RemoteRepository.Builder("$id", "default", "$repo").build());""".trimIndent()
        }

        val dependenciesCode = (centralDeps + customDeps.map { it.first })
            .joinToString("\n") { coords ->
                """resolver.addDependency(new Dependency(new DefaultArtifact("$coords"), null));"""
            }

        val template = TemplateEngine.loadTemplate("DependencyResolver.java")

        val code = TemplateEngine.render(template, mapOf(
            "repositories" to repositoriesCode,
            "dependencies" to dependenciesCode
        ))

        codeGenerator.createNewFile(
            Dependencies(false),
            "com.system32dev.generated",
            "DependencyResolver",
            "java"
        ).use { out ->
            OutputStreamWriter(out, Charsets.UTF_8).use { it.write(code) }
        }

        logger.info("[DependencyProcessor] DependencyResolver generated with ${dependencies.size} deps")
    }
}

class DependencyProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DendencyProcessor(environment.codeGenerator, environment.logger)
    }
}
