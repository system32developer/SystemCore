package com.system32.generated

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class DependencyResolver implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "system32-nexus",
                        "default",
                        "https://nexus.system32dev.com/repository/maven-releases/"
                ).build()
        );

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "maven-central",
                        "default",
                        "https://repo1.maven.org/maven2/"
                ).build()
        );

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "triumph-central",
                        "default",
                        "https://repo.triumphteam.dev/snapshots/"
                ).build()
        );

        resolver.addDependency(
                new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.2.0"), null)
        );

        resolver.addDependency(
                new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:2.2.0"), null)
        );
        resolver.addDependency(
                new Dependency(
                        new DefaultArtifact("com.system32:SystemCore:2.5.8"),
                        null
                )
        );

        classpathBuilder.addLibrary(resolver);
    }

    fun systemCoreVersion(): String{
        return Manifest(DependencyResolver::class.java.getResourceAsStream("/META-INF/MANIFEST.MF")).mainAttributes.getValue("SystemCore")
    }
}