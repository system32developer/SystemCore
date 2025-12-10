package com.system32dev.systemCore.generated;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

public class DependencyResolver implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("maven-central", "default", "https://maven-central.storage-download.googleapis.com/maven2").build());
        {{repositories}}

        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:2.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.system32dev:SystemCore:"+systemCoreVersion()), null));
        {{dependencies}}
        classpathBuilder.addLibrary(resolver);
    }

    public static String systemCoreVersion() {
        try (InputStream input = DependencyResolver.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (input == null) {
                throw new IllegalStateException("Not found MANIFEST.MF");
            }
            Manifest manifest = new Manifest(input);
            return manifest.getMainAttributes().getValue("SystemCore");
        } catch (IOException e) {
            throw new RuntimeException("Cannot read MANIFEST.MF", e);
        }
    }
}