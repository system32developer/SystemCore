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

    private static final String DEFAULT_MAVEN =
            "https://maven-central.storage-download.googleapis.com/maven2/";

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        String repoUrl = mavenMirrorOrDefault();

        resolver.addRepository(new RemoteRepository.Builder(
                "maven-central",
                "default",
                repoUrl
        ).build());

        {{repositories}}

        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:2.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.system32dev:SystemCore:" + systemCoreVersion()), null));

        {{dependencies}}

        classpathBuilder.addLibrary(resolver);
    }

    private static Manifest loadManifest() {
        try (InputStream input = DependencyResolver.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (input == null) {
                throw new IllegalStateException("Not found MANIFEST.MF");
            }
            return new Manifest(input);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read MANIFEST.MF", e);
        }
    }

    private static String getManifestAttribute(String key) {
        return loadManifest().getMainAttributes().getValue(key);
    }

    public static String systemCoreVersion() {
        return getManifestAttribute("SystemCore");
    }

    public static String mavenMirror() {
        return getManifestAttribute("MavenMirror");
    }

    private static String mavenMirrorOrDefault() {
        String mirror = mavenMirror();

        if (mirror == null || mirror.isBlank()) {
            return DEFAULT_MAVEN;
        }

        if (!mirror.startsWith("http://") && !mirror.startsWith("https://")) {
            return DEFAULT_MAVEN;
        }
        if (!mirror.endsWith("/")) {
            mirror += "/";
        }

        return mirror;
    }
}